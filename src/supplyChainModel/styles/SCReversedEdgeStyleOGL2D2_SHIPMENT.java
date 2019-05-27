package supplyChainModel.styles;

import java.awt.Color;

import repast.simphony.space.graph.RepastEdge;
import repast.simphony.visualizationOGL2D.DefaultEdgeStyleOGL2D;
import supplyChainModel.agents.BaseAgent;

public class SCReversedEdgeStyleOGL2D2_SHIPMENT extends DefaultEdgeStyleOGL2D {
	
	@Override
	public Color getColor(RepastEdge<?> edge){
		
		if (edge.getSource() instanceof BaseAgent && edge.getTarget() instanceof BaseAgent) {
			BaseAgent client   = (BaseAgent) edge.getSource();
			BaseAgent supplier = (BaseAgent) edge.getTarget();
			
			if (!supplier.edgeHasSendShipment(client.getId())) {
				return Color.WHITE;
			}
			else if (client.retrieveRelationIsActive(supplier.getId())) {
				double trustLevel = client.retrieveTrustLevel(supplier.getId());
				return new Color((float) (1 - trustLevel), (float) trustLevel, 0.0f);
			}
			else 
				return Color.BLACK;
			
		}
		return Color.BLUE;
	}
	
	@Override
	public int getLineWidth(RepastEdge<?> edge) {
		
		return 1;
	}
}
