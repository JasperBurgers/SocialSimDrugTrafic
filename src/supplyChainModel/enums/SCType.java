package supplyChainModel.enums;

import supplyChainModel.common.Constants;
import supplyChainModel.common.Logger;

/**
 * Enum for the supply chain type.
 * @author Maarten
 *
 */
public enum SCType {

	PRODUCER(0), INTERNATIONAL(1), WHOLESALER(2), RETAIL(3), CONSUMER(4);
	
	static int scLayers = 5;
	
	private int scLayer;
	
	private SCType(int scLayer) {
		
		this.scLayer = scLayer;
	}

	public int getX() {

		int x = Constants.VSL_COUNTRY_X + scLayer * Constants.VSL_COUNTRY_WIDTH + 1;
		if (x > Constants.GRID_WIDTH)
			Logger.logError("SCType.getScLayerX(): x > Constants.GRID_WIDTH . " + x + " > " + Constants.GRID_WIDTH);
		return x;
	}
	
	public int getScLayer() {
		return scLayer;
	}
	
	public String getScCharacter() {
		if (name().length() > 0) {
			return name().substring(0, 1);
		}
		else {
			Logger.logError("SCType.getScCharacter() ");
			return "X";
		}
	}
	
	// Static functions
	public static int getScLayers() {
		return scLayers;
	}
}