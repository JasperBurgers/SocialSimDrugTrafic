package supplyChainModel.styles;

import java.awt.Color;

import repast.simphony.space.graph.RepastEdge;
import repast.simphony.visualizationOGL2D.DefaultEdgeStyleOGL2D;
import supplyChainModel.agents.BaseAgent;

public class SCEdgeStyleOGL2D extends DefaultEdgeStyleOGL2D {

	@Override
	public Color getColor(RepastEdge<?> edge){
		
		if (edge.getSource() instanceof BaseAgent && edge.getTarget() instanceof BaseAgent) {
			BaseAgent supplier = (BaseAgent) edge.getSource();
			BaseAgent client   = (BaseAgent) edge.getTarget();
			
			if (supplier.retrieveRelationIsActive(client.getId())) {
				double trustLevel = supplier.retrieveTrustLevel(client.getId());
				return new Color((float) (1 - trustLevel), (float) trustLevel, 0.0f);
			}
			else 
				return Color.BLACK;
			
		}
		return Color.BLUE;
	}
	
	@Override
	public int getLineWidth(RepastEdge<?> edge) {
		
		if (edge.getSource() instanceof BaseAgent && edge.getTarget() instanceof BaseAgent) {
			BaseAgent supplier = (BaseAgent) edge.getSource();
			BaseAgent client   = (BaseAgent) edge.getTarget();
			
			if (supplier.edgeHasSendOrder(client.getId()))
				return 1;
			else 
				return 0;
		}
		return 2;
	}
}