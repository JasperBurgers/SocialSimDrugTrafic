package supplyChainModel.styles;

import java.awt.Color;
import java.awt.Font;
import java.io.IOException;

import repast.simphony.visualizationOGL2D.DefaultStyleOGL2D;
import saf.v3d.scene.Position;
import saf.v3d.scene.VSpatial;
import supplyChainModel.common.Constants;

public class SCTypeStyle2D extends DefaultStyleOGL2D {

	@Override
	public Color getColor(Object o){
		
		if (o instanceof VisualSCType) {
			return new Color(0.9f, 0.9f, 0.9f);	
		}

		return null;
	}
	
	/**
	   * @return a circle of radius 4.
	   */
	  public VSpatial getVSpatial(Object agent, VSpatial spatial) {
	    if (spatial == null) {
	      spatial = createImageFromPath("./icons/scLayer.png");
	    }
	    return spatial;
	  }
	
	@Override
	public float getScale(Object o) {

		return 1f;
	}
	
	@Override
	public String getLabel(Object object) {

		if (object instanceof VisualSCType) {
			final VisualSCType agent = (VisualSCType) object;
			return agent.getLabel();
		}
		
		return "Warning label not found for object";
	}

	@Override
	public Font getLabelFont(Object object) {
		
	    return Constants.FONT_HUGE;
	}
	
	@Override
	public Position getLabelPosition(Object object) {
	    return Position.NORTH;
	}
	
	private VSpatial createImageFromPath(String path) {
		try {
			return shapeFactory.createImage(path);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
}