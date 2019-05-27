package supplyChainModel.support;

import java.awt.Color;
import java.util.HashMap;

import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.continuous.NdPoint;
import supplyChainModel.agents.BaseAgent;
import supplyChainModel.common.Constants;
import supplyChainModel.common.Logger;
import supplyChainModel.common.SU;

public class Order {

	// State variables
	private BaseAgent client;
	private BaseAgent supplier;
	private HashMap<Byte, Double> goods;
	private int stepsLeft;
	private boolean savedOrder;
	
	// Visualization variables
	private double vsl_moveSpeed;
	private double vsl_moveHeading;
	private double vsl_size;
	private Byte vsl_largest_quality;
	
	public Order(BaseAgent client, BaseAgent supplier, HashMap<Byte, Double> goods, int stepsLeft) {

		SU.getContext().add(this);
		
		this.client = client;
		this.supplier = supplier;
		this.goods = goods;
		this.stepsLeft = stepsLeft;
		savedOrder = false;
		
		//Logger.logInfo("New order: " + client.getId() + " to " + supplier.getId() + " : " + goods.toString());
		setStartPosition();
		setLargestQuality();
	}
	
	/**
	 * Decreases the steps, gives an error if there are less than zero steps and moves the order
	 */
	public void stepAdvanceOrder() {
		
		if (savedOrder)
			return ;
		
		this.stepsLeft -= 1;
		if (this.stepsLeft <= -1) {
			Logger.logError("stepAdvanceOrder(): stepsLeft:" + stepsLeft + ", " + toString());
		}
		move();
	}
	
	public void remove() {

		SU.getContext().remove(this);
	}
	
	// Getters and setters
	public HashMap<Byte, Double> getGoods() {
		return goods;
	}
	
	public String getGoodsStr() {
		return goods.toString();
	}
	
	public void setSavedOrder() {
		savedOrder = true;
	}
	
	public int getSupplierId() {
		return supplier.getId();
	}
	
	public int getClientId() {
		return client.getId();
	}

	/**
	 * This function should only be allowed at initialization of
	 * the order.
	 * @param quality
	 * @param quantity
	 */
	public void addToGoods(Byte quality, Double quantity) {
		//Logger.logInfo(" Added to order: " + client.getId() + " to " + supplier.getId() + " : Q:" + quality + ", " + quantity );
		if (goods.containsKey(quality))
			goods.put(quality, goods.get(quality) + quantity);
		else
			goods.put(quality, quantity);
		setLargestQuality();
	}
	
	public boolean isArrived() {
		if (stepsLeft == 0)
			return true;
		return false;
	}
	
	public boolean isSaved() {
		return savedOrder;
	}
	
	public BaseAgent getClient() {
		return client;
	}
	
	public BaseAgent getSupplier() {
		return supplier;
	}
	
	public double getSize() {
		return vsl_size;
	}
	
	public String toString() {
		return "order";
	}
	
	public String getLabel() {
		//return String.format("%.1f", goods.get(vsl_largest_quality));
		return "";
	}
	
	public int getIdClient() {
		return client.getId();
	}
	
	public int getIdSupplier() {
		return supplier.getId();
	}
	
	public double getLocationX() {
		return SU.getContinuousSpace().getLocation(this).getX();
	}
	
	public double getLocationY() {
		return SU.getContinuousSpace().getLocation(this).getY();
	}
	
	/**
	 * Higher quality means a brighter yellow color for the shipments
	 */
	public Color getColor() {
		
		float factor = (float) vsl_largest_quality / Constants.QUALITY_MAXIMUM;
		if (factor > 1) {
			Logger.logError("Order.getColor(): factor > 1 =" + factor + ", quality:" + vsl_largest_quality);
		}
		return new Color(0, 0, factor);		
	}
	
	// Visualization
	/**
	 * 1. Set starting position of the order at the client
	 * 2. Get client and supplier location
	 * 2. Calculate direction and movement speed
	 * 3. Do one step of movement, smaller movement because it is an order
	 */
	public void setStartPosition() {
		
		final ContinuousSpace<Object> space = SU.getContinuousSpace(); 
		NdPoint clientLoc = new NdPoint(space.getLocation(client).getX(), space.getLocation(client).getY() - Constants.VSL_ORD_SHP_DIF_Y);
		NdPoint supplierLoc = new NdPoint(space.getLocation(supplier).getX(), space.getLocation(supplier).getY() - Constants.VSL_ORD_SHP_DIF_Y);
		
		space.moveTo(this, clientLoc.getX(), clientLoc.getY());
		
		vsl_moveSpeed = SU.point_distance(clientLoc.getX(), clientLoc.getY(), supplierLoc.getX(), supplierLoc.getY()) / (1 + stepsLeft);
		vsl_moveHeading = SU.point_direction(clientLoc.getX(), clientLoc.getY(), supplierLoc.getX(), supplierLoc.getY());
		
		SU.getContinuousSpace().moveByVector(this, vsl_moveSpeed * (1 - Constants.VSL_ORD_SHP_DIF_MOVE), vsl_moveHeading, 0);
	}

	public void move() {
		
		SU.getContinuousSpace().moveByVector(this, vsl_moveSpeed, vsl_moveHeading,0);
	}
	
	public void setLargestQuality() {
		
		double quantity = -1;
		for (Byte quality : goods.keySet()) {
			if (quantity == -1) {
				vsl_largest_quality = quality;
				quantity = goods.get(quality);
			}
			else if (goods.get(quality) > quantity) {
				vsl_largest_quality = quality;
				quantity = goods.get(quality);
			}	
		}
		vsl_size += goods.get(vsl_largest_quality);
	}
}