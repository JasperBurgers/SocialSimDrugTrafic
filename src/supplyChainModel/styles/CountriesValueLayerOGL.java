package supplyChainModel.styles;

import java.awt.Color;

import repast.simphony.valueLayer.ValueLayer;
import repast.simphony.visualizationOGL2D.ValueLayerStyleOGL;
import supplyChainModel.common.Constants;

public class CountriesValueLayerOGL implements ValueLayerStyleOGL {

	private ValueLayer layer = null;
	
	@Override
	public Color getColor(double... coordinates) {

		if (layer.get(coordinates) > 0)
			return Color.LIGHT_GRAY;
		else
			return Color.WHITE;
	}

	@Override
	public float getCellSize() {
		return Constants.GRID_CELL_SIZE;
	}

	@Override
	public void init(ValueLayer layer) {
		this.layer = layer;
	}
}
