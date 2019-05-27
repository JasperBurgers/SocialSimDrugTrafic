package supplyChainModel.styles;

import java.awt.Color;
import java.awt.Font;

import repast.simphony.visualizationOGL2D.DefaultStyleOGL2D;
import saf.v3d.scene.Position;
import supplyChainModel.common.Constants;
import supplyChainModel.support.Shipment;

public class ShipmentStyle2D extends DefaultStyleOGL2D {

	@Override
	public Color getColor(Object o){
		
		if (o instanceof Shipment)
			return ((Shipment) o).getColor();
		
		return null;
	}

	@Override
	public float getScale(Object o) {
		if (o instanceof Shipment) {
			return (float) (0.5 + (((Shipment)o).getSize() * 0.05));
		}
		return 2f;
	}
	
	@Override
	public String getLabel(Object object) {

		if (object instanceof Shipment) {
			final Shipment shipment = (Shipment) object;
			return shipment.getLabel();
		}
		return "Warning label not found for object";
	}

	@Override
	public Font getLabelFont(Object object) {
		
	    return Constants.FONT_LABEL;
	}
	
	@Override
	public Position getLabelPosition(Object object) {
	    return Position.NORTH;
	}
}