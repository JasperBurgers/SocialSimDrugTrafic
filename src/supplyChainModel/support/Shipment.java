package supplyChainModel.support;

import java.awt.Color;
import java.util.HashMap;

import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.continuous.NdPoint;
import supplyChainModel.agents.BaseAgent;
import supplyChainModel.common.Constants;
import supplyChainModel.common.Logger;
import supplyChainModel.common.SU;

public class Shipment {

	// State variables
	private BaseAgent client;
	private BaseAgent supplier;
	private HashMap<Byte, Double> goods;
	private int stepsLeft;
	private double price;
	//private double seizureChance;
	
	// Visualization variables
	private double vsl_moveSpeed;
	private double vsl_moveHeading;
	private double vsl_size;
	private Byte vsl_largest_quality;

	public Shipment(BaseAgent client, BaseAgent supplier, HashMap<Byte, Double> goods, double price, int stepsLeft) {

		SU.getContext().add(this);
		
		this.client = client;
		this.supplier = supplier;
		this.goods = goods;
		this.price = price;
		this.stepsLeft = stepsLeft;
		//this.seizureChance = seizureChance;
		
		setStartPosition();
		setLargestQuality();
	}
	
	/**
	 * Decreases the steps, gives an error if there are less than zero steps and moves the order
	 */
	public void stepAdvanceShipment() {
		
		stepsLeft -= 1;
		if (stepsLeft <= -1) {
			Logger.logError("stepAdvanceShipment(): stepsLeft:" + stepsLeft + ", " + toString());
		}
		move();
	}
	
	public void remove() {
		SU.getContext().remove(this);
	}
	
	// Remove method for police interceptions. Goods are removed from the system.
	public void intercept() {
		SU.getContext().remove(this);
		SU.getDataCollector().addDeletedStock(goods);
	}
	
	// Getters and setters
	public HashMap<Byte, Double> getGoods() {
		return goods;
	}
	
	public String getGoodsStr() {
		return goods.toString();
	}
	
	public double getPrice() {
		return price;
	}
	
	public boolean isArrived() {
		if (stepsLeft == 0)
			return true;
		return false;
	}
	
	public BaseAgent getSupplier() {
		return supplier;
	}
	
	public BaseAgent getClient() {
		return client;
	}
	
	/**
	 * This function returns the 
	 * @return
	 */
	public int getIdSupplier() {
		if (supplier == null && client != null)
			return client.getId();
		else
			return supplier.getId();
	}
	
	public int getIdClient() {
		return client.getId();
	}
	
	public double getSize() {
		return vsl_size;
	}
	
	public String getLabel() {
		return String.format("%.1f", goods.get(vsl_largest_quality));
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
			Logger.logError("Shipment.getColor(): factor > 1 =" + factor + ", quality:" + vsl_largest_quality);
		}
		return new Color(factor, factor, 0);		
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
		NdPoint clientLoc   = new NdPoint(space.getLocation(client).getX(), space.getLocation(client).getY() + Constants.VSL_ORD_SHP_DIF_Y);
		NdPoint supplierLoc = new NdPoint(clientLoc.getX() - Constants.VSL_COUNTRY_X, clientLoc.getY());
		if (supplier != null)
			supplierLoc = new NdPoint(space.getLocation(supplier).getX(), space.getLocation(supplier).getY() + Constants.VSL_ORD_SHP_DIF_Y);
		
		space.moveTo(this, supplierLoc.getX(), supplierLoc.getY());
		
		vsl_moveSpeed = SU.point_distance(supplierLoc.getX(), supplierLoc.getY(), clientLoc.getX(), clientLoc.getY()) / (1 + stepsLeft);
		vsl_moveHeading = SU.point_direction(supplierLoc.getX(), supplierLoc.getY(), clientLoc.getX(), clientLoc.getY());
		
		SU.getContinuousSpace().moveByVector(this, vsl_moveSpeed * (1 + Constants.VSL_ORD_SHP_DIF_MOVE), vsl_moveHeading,0);
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