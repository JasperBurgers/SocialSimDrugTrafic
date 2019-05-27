package supplyChainModel.agents;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;

import repast.simphony.context.Context;
import repast.simphony.random.RandomHelper;
import supplyChainModel.common.Constants;
import supplyChainModel.common.Logger;
import supplyChainModel.common.RepastParam;
import supplyChainModel.common.SU;
import supplyChainModel.enums.SCType;
import supplyChainModel.support.Order;
import supplyChainModel.support.Shipment;

/**
 * The consumer class buys from the retailers
 * and consumes the goods.
 * @author Maarten
 *
 */
public class Agent5Consumer extends BaseAgent {
	
	// State variables
	private byte quality;
	public double baseConsumption;
	public boolean satisfied;
	//public int ticksUntilRemoved;
	public int ticksWithoutSatisfaction;
	
	public Agent5Consumer(final Context<Object> context, CountryAgent country, byte quality) {
		
		super(country, SCType.CONSUMER, 0, 0);
		
		this.quality = quality;
		baseConsumption = RandomHelper.nextDoubleFromTo(RepastParam.getConsumptionMin(), RepastParam.getConsumptionMax());
		satisfied = false;
		//ticksUntilRemoved = Constants.CONSUMER_REMOVE_TICKS;
		ticksWithoutSatisfaction = 0;
		
		stock.put(quality, 0.0);
		
		setStartingStock();
	}

	/*====================================
	 * The main steps of the agents
	 *====================================*/
	
	/**
	 * Adjusted to change removal based on life and rehab instead of from bankruptcy
	 * Can't die when initializing
	 */
	@Override
	public void stepCheckRemoval() {
		
		//ticksUntilRemoved -= 1;
		
		if (!SU.getIsInitializing() && ticksWithoutSatisfaction >= Constants.CONSUMER_LIMIT_WITHOUT_SATISFACTION) { // (ticksUntilRemoved == 0 || 
			remove();
		}
	}
	
	/**
	 * Gain money
	 */
	public void stepReceiveIncome() {
		
		if (quality == Constants.QUALITY_MAXIMUM)
			money += baseConsumption * Constants.PRICE_BUY_FROM_RETAIL * Constants.QUALITY_MAX_EXTRA_COST;
		else
			money += baseConsumption * Constants.PRICE_BUY_FROM_RETAIL;
	}
	
	@Override
	public void stepProcessArrivedShipments() {
		// Intercept shipments
		for (Shipment shipment : getArrivedShipments()) {
			if (RandomHelper.nextDouble() < RepastParam.getInterceptionProbability()) {
				shipment.remove();
			}
		}
		
		updateArrivedShipments();
		
		// Handle arrived shipments
		for (Shipment shipment : getArrivedShipments()) {
			money -= shipment.getPrice();
			shipment.getSupplier().receivePayment(shipment.getPrice());
			addToStock(shipment.getGoods());
			ticksWithoutSatisfaction = 0;
			shipment.remove();
			// Add import etc for DataCollector.
		}
	}
	
	@Override
	public void stepChooseSuppliersAndClients() {
		searchSuppliers();
	}
	
	@Override
	public void stepSendShipment() {
		
		if (stock.get(quality) >= baseConsumption) {
			Logger.logInfoId(id, getNameId() + ":" + stock.get(quality) + " - " + baseConsumption + " = " + (stock.get(quality) - baseConsumption));
			stock.put(quality, stock.get(quality) - baseConsumption);
			ticksWithoutSatisfaction = 0;
			satisfied = true;
			HashMap<Byte, Double> consumedGoods = new HashMap<Byte, Double>();
			consumedGoods.put(quality, baseConsumption);
			SU.getDataCollector().addConsumedStock(consumedGoods);
		}
		else {
			HashMap<Byte, Double> consumedGoods = new HashMap<Byte, Double>();
			consumedGoods.put(quality, stock.get(quality));
			
			stock.put(quality, 0.0);
			ticksWithoutSatisfaction ++;
			satisfied = false;
			
			SU.getDataCollector().addConsumedStock(consumedGoods);
		}
	}
	
	/**
	 * Send order based on what is required. For each quality the suppliers are checked.
	 * The suppliers with the highest trust for that quality will be asked first to provide
	 * the maximum amount. Orders are created but if an order already exists the required goods
	 * are added to the order
	 */
	@Override
	public void stepSendOrder() {
		
		HashMap<Integer, Order> placedOrders = new HashMap<Integer, Order>();
		HashMap<Byte, Double> requiredGoods = getRequiredGoods();
		
		for (Byte quality : requiredGoods.keySet()) {
				
			double requiredQuantity = requiredGoods.get(quality);
			ArrayList<TrustCompare> sortedSuppliers = retrieveSortedSuppliers(quality);
			for (TrustCompare sortedSupplier : sortedSuppliers) {
				
				BaseAgent supplier = sortedSupplier.getAgent();
				//Logger.logInfo("Required:" + requiredQuantity + ", min package size:" + supplier.getMinPackageSize());
				if (requiredQuantity >= supplier.getMinPackageSize()) {
					
					double oldQuantity = relationsS.get(supplier.getId()).getPreviousMyOrder(quality);
					double chosenQuantity = Constants.SEND_ORDER_LEARN_RATE * requiredQuantity +
											(1 - Constants.SEND_ORDER_LEARN_RATE) * oldQuantity;

					chosenQuantity = Math.min(Math.max(chosenQuantity, supplier.getMinPackageSize()), supplier.getMaxPackageSize());
					requiredQuantity -= chosenQuantity;
					
					//Decide whether the order should be added or create anew
					if (placedOrders.containsKey(supplier.getId())) {
						placedOrders.get(supplier.getId()).addToGoods(quality, chosenQuantity);
					}
					else {
						HashMap<Byte, Double> chosenGoods = new HashMap<Byte, Double>();
						chosenGoods.put(quality, chosenQuantity);
						placedOrders.put(supplier.getId(), new Order(this, supplier, chosenGoods, RepastParam.getShipmentStep()));
					}
				}
			}
		}
		
		addOrdersToRelation(placedOrders);
	}
	
	public HashMap<Byte, Double> getRequiredGoods() {
		
		HashMap<Byte, Double> requiredGoods = new HashMap<Byte, Double>();

		double requiredQuantity = securityStockMultiplier * getMinPackageSizeBoth() + baseConsumption;
		requiredQuantity -= stock.get(quality);
		requiredGoods.put(quality, requiredQuantity);
		
		return requiredGoods;
	}

	protected void setStartingStock() {
		
		stock.put(quality, securityStockMultiplier * minPackageSize);
	}
	
	/**
	 * Require a new supplier when stock is zero for any of the qualities
	 * @return
	 */
	@Override
	public boolean getRequireNewSupplier() {
		
		if (newSupplierCooldown > 0)
			return false;
		
		for (Byte quality : stock.keySet()) {
			if (stock.get(quality) == 0)
				return true;
		}
		return false;
	}
	
	/*================================
	 * Getters and setters
	 *===============================*/	
	
	/*public String getLabel() {
		return id + String.format("  $:%.0f", money);
	}*/
	
	public boolean isConnected() {
		if (!relationsS.isEmpty())
			return true;
		return false;
	}
	
	@Override
	public String getLabel() {
		String stockStr = ",s:";
		for (Byte quality : stock.keySet()) {
			if (!stockStr.equals(",s:"))
				stockStr += ",";
			if (quality == Constants.QUALITY_MINIMUM)
				stockStr += String.format("L%.1f", stock.get(quality));
			else if (quality == Constants.QUALITY_MAXIMUM)
				stockStr += String.format("H%.1f", stock.get(quality));
		}
		return id + String.format(" $%.0f", money) + stockStr + String.format(",[%.1f", (securityStockMultiplier * getMinPackageSizeBoth() + baseConsumption));
	}
	
	@Override
	public double getSecurityStock() {
		return securityStockMultiplier * getMinPackageSizeBoth() + baseConsumption;
	}
	
	public Color getColor() {
		if (satisfied)
			return Color.GREEN;
		else
			return Color.RED;
	}
	
	public boolean getSatisfied() {
		return satisfied;
	}
	
	public byte getQuality() {
		return quality;
	}
}