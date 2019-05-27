package frameworkTrust;

import java.util.HashMap;

import repast.simphony.engine.environment.RunEnvironment;
import supplyChainModel.common.Constants;
import supplyChainModel.common.Logger;

/**
 * This class represents the Relation in a Supplier, it can calculate the trustLevel.
 * Suppliers create an object of this class when they add a new Supplier (addClient(BaseAgent supplier))
 * 
 * @author Maarten Jensen
 *
 */
public class RelationS {

	// This is a container that should be used in a map, linked to buyers or sellers
	// This object will contain the history on which trust is calculated
	
	private int thisId;
	private int otherId;
	private int supplyTime;
	private boolean active;
	
	private HashMap<Integer, HashMap<Byte, Double>> myOrders;
	private HashMap<Integer, HashMap<Byte, Double>> otherShipments;

	// Visualization parameters
	private String label;
	
	public RelationS(int thisId, int otherId, int supplyTime, String label) {
		
		this.thisId = thisId;
		this.otherId = otherId;
		this.supplyTime = supplyTime;
		this.label = label;
		active = true;
		
		myOrders       = new HashMap<Integer, HashMap<Byte, Double>>();
		otherShipments = new HashMap<Integer, HashMap<Byte, Double>>();
	}
	
	/**
	 * Add order to relation and add it to the smoothed orders
	 * @param orderedGoods
	 */
	public void addMyOrder(HashMap<Byte, Double> orderedGoods) {
		int tick = (int) RunEnvironment.getInstance().getCurrentSchedule().getTickCount();
		myOrders.put(tick, orderedGoods);
	}
	
	/**
	 * If there is an excess of shipment send, the trust is updated but with a penalized amount
	 * this represents penalization when shipments are later
	 * @param shippedGoods
	 */
	public void addOtherShipment(HashMap<Byte, Double> shippedGoods) {
		
		int tick = (int) RunEnvironment.getInstance().getCurrentSchedule().getTickCount();
		if (myOrders.containsKey(tick - supplyTime)) {
			
			HashMap<Byte, Double> orderedGoods = myOrders.get(tick - supplyTime);
			HashMap<Byte, Double> penalizedGoods = new HashMap<Byte, Double>();
			// Penalize received goods (this happens only in the trust relation not for real)
			for (Byte quality : shippedGoods.keySet()) {
				
				double orderedGoodsQuantity = 0; //Ordered goods is zero unless it exists in the order
				if (orderedGoods.containsKey(quality))
					orderedGoodsQuantity = orderedGoods.get(quality);
				
				double penalizedQuantity = Math.min(shippedGoods.get(quality), orderedGoodsQuantity);
				if (shippedGoods.get(quality) > orderedGoodsQuantity)
					penalizedQuantity += (shippedGoods.get(quality) - orderedGoodsQuantity) * Constants.LATE_SHIPMENT_PENALIZE_MULT;
				penalizedGoods.put(quality, penalizedQuantity);		
			}
			Logger.logInfo("RelationS.addOtherShipment(): " + label + " ordered:" + orderedGoods.toString() + ", shipped:" + shippedGoods.toString() + ", penalized:" + penalizedGoods.toString());
			otherShipments.put(tick, penalizedGoods);
		}
		else {
			Logger.logError("RelationS.addOtherShipment(): " + label + " order on tick " + (tick - supplyTime) + " does not exist");
		}
	}
	
	// Getters
	public int getThisId() {
		return thisId;
	}
	
	public int getOtherId() {
		return otherId;
	}
	
	public Double getPreviousMyOrder(Byte quality) {
		int previousTick = (int) RunEnvironment.getInstance().getCurrentSchedule().getTickCount() - 1;
		if (myOrders.containsKey(previousTick)) {
			if (myOrders.get(previousTick).containsKey(quality)) 
				return myOrders.get(previousTick).get(quality);
			else
				return 0.0;
		}
		else
			return 0.0;
	}
	
	public boolean isOrderActive() {
		if (!myOrders.isEmpty())
			return true;
		return false;
	}
	
	public boolean isShipmentActive() {
		if (!otherShipments.isEmpty())
			return true;
		return false;
	}
	
	/**
	 * Returns the trust in the agent with given id
	 */
	public double getTrustLevel() {
		
		double trust = 0;
		double differentQualities = 0;

		double trustMinimum = getTrustForQuality(Constants.QUALITY_MINIMUM);
		if (trustMinimum >= 0) {
			trust += trustMinimum;
			differentQualities ++;
		}
		
		double trustMaximum = getTrustForQuality(Constants.QUALITY_MAXIMUM);
		if (trustMaximum >= 0) {
			trust += trustMaximum;
			differentQualities ++;
		}
		
		if (differentQualities > 0)
			return trust / differentQualities;
		else
			return 0;
	}
	
	/**
	 * Retrieves the trust of deliveries for the given quality,
	 * returns -1 when this quality does not have any orders on it
	 * @param quality
	 * @return
	 */
	public double getTrustForQuality(Byte quality) {
		
		double amountOrders = 0;
		double amountShipments = 0;
		int tick = (int) RunEnvironment.getInstance().getCurrentSchedule().getTickCount();
		for (Integer step : myOrders.keySet()) {

			// The order step should be smaller than the current step minus the total supply time
			if (step <= (tick - supplyTime) && myOrders.get(step).containsKey(quality)) {
				amountOrders += myOrders.get(step).get(quality);
			
				if (otherShipments.containsKey(step + supplyTime)) {
					if (otherShipments.get(step + supplyTime).containsKey(quality))
						amountShipments += otherShipments.get(step + supplyTime).get(quality);
				}
			}
		}
		
		// Return trust amount only when there are some orders
		if (amountOrders > 0) {
			return amountShipments / amountOrders;
		}
		else {
			return -1;
		}
	}
	
	public boolean isActive() {
		return active;
	}
	
	public void setInActive() {
		active = false;
	}
	
	public String getStateString() {
		return thisId + "," + otherId + ",S," + getTrustLevel();
	}
}
