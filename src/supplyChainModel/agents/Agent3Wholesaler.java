package supplyChainModel.agents;

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
 * Imports goods bought from the internationals,
 * the goods are then sold to retailers.
 * @author Maarten Jensen
 *
 */
public class Agent3Wholesaler extends BaseAgent {
	
	public Agent3Wholesaler(final Context<Object> context, CountryAgent country) {
		super(country, SCType.WHOLESALER, Constants.PRICE_BUY_FROM_WHOLESALER, Constants.SHIPMENT_MAX_3TO4);
		
		setStartingStock();
	}

	@Override
	public void stepProcessArrivedShipments() {
		if (!SU.isInitializing()) {
			// Chance to be arrested per tick
			if (RandomHelper.nextDouble() < RepastParam.getArrestProbabilityW()) {
				remove();
			}
			
			// Intercept shipments
			for (Shipment shipment : getArrivedShipments()) {
				if (RandomHelper.nextDouble() < RepastParam.getInterceptionProbabilityW()) {
					shipment.intercept();
				}
			}
		}

		updateArrivedShipments();
		
		for (Shipment shipment : getArrivedShipments()) {
			money -= shipment.getPrice();
			shipment.getSupplier().receivePayment(shipment.getPrice());
			addToStock(shipment.getGoods());
			shipment.remove();
			// Add import etc for DataCollector.
		}
	}
	
	@Override
	public void stepChooseSuppliersAndClients() {
		searchClients();
		searchSuppliers();
	}
	
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
					HashMap<Byte, Double> goodsToSend = findGoodsInStockWholesaler(orderedGoodsCombined);
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
	 * Find the given quality and amount in the stock,
	 * removes it from the stock and returns the amount
	 * when the stock is lower than the craved amount it
	 * is put at zero. When the requested quality is not 
	 * in the stock it is added as a zero to the stock
	 * There is a check on minimum package size
	 * This function is adapted for the wholesaler, as the wholesaler
	 * can convert high quality to low quality. //TODO change package size to total package
	 */
	public HashMap<Byte, Double> findGoodsInStockWholesaler(HashMap<Byte, Double> cravedGoods) {

		double extraLowQualityNeeded = 0.0;
		HashMap<Byte, Double> choosenGoods = new HashMap<Byte, Double>();
		for (Byte quality : cravedGoods.keySet()) {
			if (stock.containsKey(quality)) {
				if (stock.get(quality) <= cravedGoods.get(quality)) {
					if (stock.get(quality) >= minPackageSize) {
						choosenGoods.put(quality, stock.get(quality));
						stock.put(quality, 0.0);
						if (quality == Constants.QUALITY_MINIMUM)
							extraLowQualityNeeded = cravedGoods.get(quality) - choosenGoods.get(quality);						
					}
					else { // Stock is not sufficient for minPackageSize, maybe with convertion from high quality it is
						if (quality == Constants.QUALITY_MINIMUM)
							extraLowQualityNeeded = cravedGoods.get(quality) - stock.get(quality);
					}
				}
				else { // Stock is sufficient
					choosenGoods.put(quality, cravedGoods.get(quality));
					stock.put(quality, stock.get(quality) - cravedGoods.get(quality));
				}
			}
			else { // This quality is added to the stock to make the agent send orders for this quality
				stock.put(quality, 0.0);
				if (quality == Constants.QUALITY_MINIMUM)
					extraLowQualityNeeded = cravedGoods.get(quality);
			}
		}
		
		//Convert high quality to low quality
		if (extraLowQualityNeeded > 0 && stock.containsKey(Constants.QUALITY_MAXIMUM)) {
			Logger.logSCAgent(scType, id + " Step 1: extraLowQualityNeeded:" + extraLowQualityNeeded + ", stock:" + stock.toString() + ", choosenGoods:" + choosenGoods.toString());
			double availableLowQuality = Math.min(extraLowQualityNeeded, 1.5 * stock.get(Constants.QUALITY_MAXIMUM));
			if (choosenGoods.containsKey(Constants.QUALITY_MINIMUM)) {
				
				choosenGoods.put(Constants.QUALITY_MINIMUM, availableLowQuality + choosenGoods.get(Constants.QUALITY_MINIMUM) );
				stock.put(Constants.QUALITY_MAXIMUM, Math.max(0.0, stock.get(Constants.QUALITY_MAXIMUM) - availableLowQuality * (2.0/3.0))); //Used Math.min because of rounding errors to keep it at least 0.0
			}
			else {
				if (availableLowQuality + stock.get(Constants.QUALITY_MAXIMUM) >= minPackageSize) {
					
					choosenGoods.put(Constants.QUALITY_MINIMUM, availableLowQuality + stock.get(Constants.QUALITY_MINIMUM) );
					stock.put(Constants.QUALITY_MAXIMUM, Math.max(0.0, stock.get(Constants.QUALITY_MAXIMUM) - availableLowQuality * (2.0/3.0))); //Used Math.min because of rounding errors to keep it at least 0.0
					stock.put(Constants.QUALITY_MINIMUM, 0.0);
				}
			}
			Logger.logSCAgent(scType, id + " Step 2: availableLowQuality:" + availableLowQuality + ", stock:" + stock.toString() + ", choosenGoods:" + choosenGoods.toString());
		}
		
		return choosenGoods;
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
		
		for (Byte quality : stock.keySet()) {
			
			double requiredQuantity = securityStockMultiplier * getMinPackageSizeBoth();
			for (Integer id : relationsC.keySet()) {
				requiredQuantity += relationsC.get(id).getPreviousOtherOrder(quality);
			}
			
			requiredQuantity -= stock.get(quality);
			
			requiredGoods.put(quality, requiredQuantity);
		}
		
		return requiredGoods;
	}
	
	protected void setStartingStock() {
		
		if (RandomHelper.nextDouble() <= 0.5)
			stock.put(Constants.QUALITY_MINIMUM, securityStockMultiplier * minPackageSize);
		else
			stock.put(Constants.QUALITY_MAXIMUM, securityStockMultiplier * minPackageSize);
	}
}