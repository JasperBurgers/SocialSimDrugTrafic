package supplyChainModel.styles;

import java.awt.Color;
import java.awt.Font;

import repast.simphony.visualizationOGL2D.DefaultStyleOGL2D;
import saf.v3d.scene.Position;
import supplyChainModel.DataCollector;
import supplyChainModel.common.Constants;
import supplyChainModel.common.SU;

public class DataCollector2D extends DefaultStyleOGL2D {

	@Override
	public Color getColor(Object o){
		
		return Color.WHITE;
	}
	
	@Override
	public float getScale(Object o) {
		
		return 1f;
	}
	
	@Override
	public String getLabel(Object object) {
		
		if (object instanceof DataCollector)
			return "";((DataCollector) object).getLabel();
		return "no label";
	}

	@Override
	public Font getLabelFont(Object object) {
		
	    return Constants.FONT_BIG;
	}
	
	@Override
	public Position getLabelPosition(Object object) {
	    return Position.SOUTH_EAST;
	}
	
	@Override
	public Color getLabelColor(Object object) {
		if (SU.isInitializing()) 
			return Color.RED;
		else
			return Color.BLACK;
	}
}