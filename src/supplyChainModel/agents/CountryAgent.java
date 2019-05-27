package supplyChainModel.agents;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;

import repast.simphony.context.Context;
import repast.simphony.random.RandomHelper;
import supplyChainModel.common.Constants;
import supplyChainModel.common.Logger;
import supplyChainModel.common.RepastParam;
import supplyChainModel.common.SU;
import supplyChainModel.enums.SCType;

/**
 * Class is used to spawn nodes at the correct country,
 * country objects contain their position, possible spawn locations (countryPoints)
 * and quality. This class can be outfitted with police interventions.
 * @author Maarten Jensen
 *
 */
public class CountryAgent {

	private String name = "none";
	private ArrayList<SCType> scTypes = new ArrayList<SCType>();

	private final int x;
	private int y;
	private int height;
	private double lowQualityProb;
	
	private ArrayList<Integer> countryPoints;
	
	public CountryAgent(final Context<Object> context, String countryName, ArrayList<SCType> scTypes, int x, int y, int height, double lowQualityProb) {
		
		context.add(this);
		name = countryName;
		this.scTypes = scTypes;
		this.x = x;
		this.y = y;
		this.height = height;
		this.lowQualityProb = lowQualityProb;
		countryPoints = createCountryPoints();
		move(this.x, this.y);
	}

	public void spawnAgent(SCType scType) {
		
		if (containsSCType(SCType.PRODUCER) && scType == SCType.PRODUCER && SU.getObjectsCount(Agent1Producer.class) < RepastParam.getProducerNumberCap()) {
			if (RandomHelper.nextDouble() <= 0.5)
				new Agent1Producer(SU.getContext(), this, Constants.QUALITY_MINIMUM);
			else
				new Agent1Producer(SU.getContext(), this, Constants.QUALITY_MAXIMUM);
		}
		
		if (containsSCType(SCType.INTERNATIONAL) && scType == SCType.INTERNATIONAL) {
			new Agent2International(SU.getContext(), this);
		}
		
		if (containsSCType(SCType.WHOLESALER) && scType == SCType.WHOLESALER) {
			new Agent3Wholesaler(SU.getContext(), this);
		}
		
		if (containsSCType(SCType.RETAIL) && scType == SCType.RETAIL) {
			new Agent4Retailer(SU.getContext(), this);
		}
		
		if (containsSCType(SCType.CONSUMER) && scType == SCType.CONSUMER) {
			if (RandomHelper.nextDouble() <= lowQualityProb)
				new Agent5Consumer(SU.getContext(), this, Constants.QUALITY_MINIMUM);
			else
				new Agent5Consumer(SU.getContext(), this, Constants.QUALITY_MAXIMUM);
		}
	}

	/**
	 * Moves the supply chain agent to the correct location, dependent on the base country
	 */
	public void move(int x, int y) {

		Point newPos = new Point(x, y);
		Logger.logInfo(name + " new pos:[" + newPos.x + ", " + newPos.y + "]");
		
		SU.getContinuousSpace().moveTo(this, newPos.getX(), newPos.getY());	
		SU.getGrid().moveTo(this, newPos.x, (int) newPos.y);
	}
	
	public String getLabel()  {
		return name;
	}
	
	private ArrayList<Integer> createCountryPoints() {

		ArrayList<Integer> emptyCountryPoints = new ArrayList<Integer>();
		for (int y = this.y; y > this.y - height; y --) {
				emptyCountryPoints.add(y);
		}
		return emptyCountryPoints;
	}
	
	public Point getFreePosition(Object toExclude, SCType scType) {
		
		boolean free = false;
		Collections.shuffle(countryPoints);
		for (Integer y : countryPoints) {
		
				free = true;
				for (Object o: SU.getGrid().getObjectsAt(scType.getX(), y)) {
					if (o != toExclude)
						free = false;
				}
				
				if (free) {
					return new Point(scType.getX(),y);
				}
		}
		Logger.logInfo("No free point in " + name);
		return new Point(scType.getX(), this.y - height/2);
	}
	
	public boolean containsSCType(SCType scType) {

		if (scTypes.contains(scType))
			return true;
		else
			return false;
	}
	
	public String getName() {
		return name;
	}
	
	public double getQuality() {
		return lowQualityProb;
	}
	
	public int getHeight() {
		return height;
	}
}