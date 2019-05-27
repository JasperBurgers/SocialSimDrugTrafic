package supplyChainModel.agents;

import java.util.ArrayList;
import java.util.HashMap;

import repast.simphony.context.Context;
import repast.simphony.random.RandomHelper;
import supplyChainModel.common.Constants;
import supplyChainModel.common.RepastParam;
import supplyChainModel.common.SU;
import supplyChainModel.enums.SCType;
import supplyChainModel.support.Order;
import supplyChainModel.support.Shipment;

/**
 * Producer agent, the first in the supply chain.
 * @author Maarten Jensen
 *
 */
public class Agent1Producer extends BaseAgent {

	// State variables
	private byte quality;
	private Double previousOrder;
	
	public Agent1Producer(final Context<Object> context, CountryAgent country, byte quality) {
		super(country, SCType.PRODUCER, Constants.PRICE_BUY_FROM_PRODUCER, Constants.SHIPMENT_MAX_1TO2);
		
		this.quality = quality;
		this.previousOrder = 0.0;
		
		setStartingStock();
	}
	
	/**
	 * The producer receives shipments it has made itself,
	 * the money is the trade in for raw materials and production costs
	 */
	@Override
	public void stepProcessArrivedShipments() {
		
		for (Shipment shipment : getArrivedShipments()) {
			money -= shipment.getPrice();
			addToStock(shipment.getGoods());
			shipment.remove();
		}
	}
	
	@Override
	public void stepChooseSuppliersAndClients() {
		searchClients();
	}
	
	/**
	 * Only send shipments when the probability check is done and you have received a new order
	 * The new order is combined with the old orders to know how much to send
	 */
	@Override
	public void stepSendShipment() {
		
		updateArrivedOrders();
		
		ArrayList<TrustCompare> sortedClients = retrieveSortedClients();
		for (TrustCompare client : sortedClients) {
			boolean gotANewOrder = false;
			//Logger.logSCAgent(scType, "stepSendShipment(): " + id + " other id: " + client.getAgent().getId() + ", " + client.getTrust());
			ArrayList<Order> clientOrders = new ArrayList<Order>();
			for (Order order : getArrivedOrders()) {
				if (order.getClient().getId() == client.getAgent().getId()) {
					clientOrders.add(order);
					if (!order.isSaved())
						gotANewOrder = true;
				}
			}
			
			//Look for all the orders that are arrived and then combine them
			if (!clientOrders.isEmpty()) {
				if (RandomHelper.nextDouble() <= RepastParam.getSendShipmentProbability() && gotANewOrder) {
				
					HashMap<Byte, Double> orderedGoodsCombined = combineOrderedGoods(clientOrders);
					HashMap<Byte, Double> goodsToSend = findGoodsInStock(orderedGoodsCombined);
					if (!goodsToSend.isEmpty()) {
						
						new Shipment(clientOrders.get(0).getClient(), this, goodsToSend, calculateCostOfGoods(goodsToSend, sellPrice), RepastParam.getShipmentStep()); 
						relationsC.get(clientOrders.get(0).getClient().getId()).addMyShipment(goodsToSend);
					}
					for (Order order : clientOrders) 
						order.remove();
				}
				else {
					for (Order order : clientOrders) 
						order.setSavedOrder();
				}
			}
		}
	}

	/**
	 * The producer directly creates shipments
	 * and can freely decide the quantity,
	 * the max creation size is defined by the RepastParam
	 */
	@Override
	public void stepSendOrder() {
		
		double requiredQuantity = securityStockMultiplier * minPackageSize;
		for (Integer id : relationsC.keySet()) {
			requiredQuantity += relationsC.get(id).getPreviousOtherOrder(quality);
		}
		
		if (stock.containsKey(quality))
			requiredQuantity -= stock.get(quality);
		
		double chosenQuantity = Constants.SEND_ORDER_LEARN_RATE * requiredQuantity +
								(1 - Constants.SEND_ORDER_LEARN_RATE) * previousOrder;
		chosenQuantity = Math.max(0, Math.min(RepastParam.getProductionMax(), chosenQuantity));
		
		if (chosenQuantity == 0.0) {
			previousOrder = 0.0;
			return ;
		}
		
		HashMap<Byte, Double> producedGoods = new HashMap<Byte, Double>();
		producedGoods.put(quality, chosenQuantity);
		double productionCost = calculateCostOfGoods(producedGoods, Constants.PRICE_PRODUCTION);
		new Shipment(this, null, producedGoods, productionCost, RepastParam.getShipmentStep());
		
		SU.getDataCollector().addProducedStock(producedGoods);
		
		previousOrder = chosenQuantity;
	}
	
	/**
	 * Require a client when all of the stocks are above security level
	 * @return
	 */
	public boolean getRequireNewClient() {
		
		if (newClientCooldown > 0)
			return false;
			
		double securityStock = securityStockMultiplier * minPackageSize;
		if (stock.containsKey(quality)) {
			if (stock.get(quality) < securityStock)
				return false;
		}
		return true;
	}
	
	protected void setStartingStock() {
		stock.put(quality, securityStockMultiplier * minPackageSize);
	}
	
	/*================================
	 * Getters and setters
	 *===============================*/	
	
	public boolean isConnected() {
		if (!relationsC.isEmpty())
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
		return id + String.format(" $%.0f", money) + stockStr + String.format(",[%.1f", (securityStockMultiplier * minPackageSize));
	}
	
	@Override
	public double getSecurityStock() {
		return securityStockMultiplier * minPackageSize;
	}
	
	public byte getQuality() {
		return quality;
	}
}