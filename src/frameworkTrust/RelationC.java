package frameworkTrust;

import java.util.HashMap;

import repast.simphony.engine.environment.RunEnvironment;

/**
 * This class represents the Relation in a Client, it can calculate the trustLevel.
 * Suppliers create an object of this class when they add a new Client (addClient(BaseAgent client))
 * 
 * @author Maarten Jensen
 *
 */
public class RelationC {

	// State variables
	private int thisId;
	private int otherId;
	private int supplyTime;
	private boolean active;

	private HashMap<Integer, HashMap<Byte, Double>> otherOrders;
	private HashMap<Integer, HashMap<Byte, Double>> myShipments;
	
	// Can also be derived from the otherOrders variables therefore it is not a state variable
	private int firstOrder;
	private String label;
	
	public RelationC(int thisId, int otherId, int supplyTime, String label) {
		
		this.thisId = thisId;
		this.otherId = otherId;
		this.supplyTime = supplyTime;
		this.label = label;
		active = true;
		firstOrder = -1;
		
		otherOrders = new HashMap<Integer, HashMap<Byte, Double>>();
		myShipments = new HashMap<Integer, HashMap<Byte, Double>>();
	}
	
	public void addOtherOrder(HashMap<Byte, Double> otherOrder) {
		
		int tick = (int) RunEnvironment.getInstance().getCurrentSchedule().getTickCount();
		otherOrders.put(tick, otherOrder);
		
		if (firstOrder == -1) {
			firstOrder = tick;
		}
	}
	
	public void addMyShipment(HashMap<Byte, Double> myShipment) {
		
		int tick = (int) RunEnvironment.getInstance().getCurrentSchedule().getTickCount();
		myShipments.put(tick, myShipment);
	}
	
	// Getters
	public int getThisId() {
		return thisId;
	}
	
	public int getOtherId() {
		return otherId;
	}
	
	public String getLabel() {
		return label;
	}
	
	/**
	 * Trust is defined differently with a client than with a supplier
	 * it is dependent on the time they know each other (received an order),
	 * higher firstOrder difference from tick is a higher trust
	 */
	public double getTrustLevel() {

		if (firstOrder > -1) {
			int tick = (int) RunEnvironment.getInstance().getCurrentSchedule().getTickCount();
			return (tick - (double) firstOrder) / tick;
		}
		else {
			return 0;
		}
	}
	
	/**
	 * Retrieves the previous order (with the given quality) of the previous tick (else return 0.0),
	 * this is used to estimate how much all the clients want to order
	 * @param quality
	 * @return
	 */
	public Double getPreviousOtherOrder(Byte quality) {
		
		int previousTick = (int) RunEnvironment.getInstance().getCurrentSchedule().getTickCount() - 1;
		if (otherOrders.containsKey(previousTick)) {
			if (otherOrders.get(previousTick).containsKey(quality)) 
				return otherOrders.get(previousTick).get(quality);
			else
				return 0.0;
		}
		else
			return 0.0;
	}
	
	public boolean isOrderActive() {
		if (!otherOrders.isEmpty())
			return true;
		return false;
	}
	
	public boolean isShipmentActive() {
		if (!myShipments.isEmpty())
			return true;
		return false;
	}
	
	public boolean isActive() {
		return active;
	}
	
	public void setInActive() {
		active = false;
	}
	
	public int getSupplyTime() {
		return supplyTime;
	}
	
	public String getStateString() {
		return thisId + "," + otherId + ",C," + getTrustLevel();
	}
}