package supplyChainModel.styles;

import java.awt.Point;

import supplyChainModel.common.Constants;
import supplyChainModel.common.Logger;
import supplyChainModel.common.SU;
import supplyChainModel.enums.SCType;

/**
 * This class is only used for visualization purposes
 * @author Maarten Jensen
 *
 */
public class VisualSCType {

	private SCType scType;
	
	public VisualSCType(SCType scType) {
		
		SU.getContext().add(this);
		
		this.scType = scType;
		
		move();
	}
	
	public String getLabel() {
		return scType.getScCharacter();
	}
	
	/**
	 * Moves the supply chain agent to the correct location, dependent on the base country
	 */
	private void move() {

		Point newPos = new Point(scType.getX(), (int) (Constants.GRID_HEIGHT * 0.5) - 1);
		Logger.logInfo("scType " + scType.name() + " spawned at pos:[" + newPos.x + ", " + newPos.y + "]");
		
		SU.getContinuousSpace().moveTo(this, newPos.getX(), newPos.getY());	
		SU.getGrid().moveTo(this, newPos.x, newPos.y);
	}
	
}
