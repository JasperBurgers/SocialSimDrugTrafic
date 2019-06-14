package supplyChainModel;

import java.util.ArrayList;
import java.util.List;

import repast.simphony.context.Context;
import repast.simphony.context.space.continuous.ContinuousSpaceFactoryFinder;
import repast.simphony.context.space.graph.NetworkBuilder;
import repast.simphony.context.space.grid.GridFactoryFinder;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.continuous.RandomCartesianAdder;
import repast.simphony.space.graph.Network;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridBuilderParameters;
import repast.simphony.space.grid.SimpleGridAdder;
import supplyChainModel.agents.Agent3Wholesaler;
import supplyChainModel.agents.Agent4Retailer;
import supplyChainModel.agents.Agent5Consumer;
import supplyChainModel.agents.BaseAgent;
import supplyChainModel.agents.CountryAgent;
import supplyChainModel.common.Constants;
import supplyChainModel.common.Logger;
import supplyChainModel.common.RepastParam;
import supplyChainModel.common.SU;
import supplyChainModel.enums.SCType;
import supplyChainModel.styles.VisualSCType;
import supplyChainModel.support.Order;
import supplyChainModel.support.Shipment;

/**
 * The ContextBuilder is the main class of the simulation program.
 * The function build is run upon pressing the start button in the 
 * repast HUD.
 * @author Maarten Jensen
 *
 */
public class ContextBuilder implements repast.simphony.dataLoader.ContextBuilder<Object> {
	
	/**
	 * This function builds the simulation, loading text files should be done in here.
	 */
	@Override
	public Context<Object> build(Context<Object> context) {
		
		createContinuousSpace(context);
		createGrid(context);
		createNetwork(context);
		createNetworkReversed(context);
		
		context.add(this); //Used for ScheduledMethod
		SU.setContext(context);
		SU.resetId();
		
		RepastParam.setRepastParameters();
		
		DataCollector dataCollector = new DataCollector(context);

		// Create the supply chain
		ContextDataLoader countryLoader = new ContextDataLoader();
		countryLoader.readFullFile(context,"./input","contextBuildInformation");
		supplyChainCreation(context);
		
		for (SCType scType : SCType.values()) {
			new VisualSCType(scType);
		}
		
		// If running in batch mode, tell the scheduler when to end each run.
		if (RunEnvironment.getInstance().isBatch()) {
			
			double endAt = RepastParam.getRunLength();
			RunEnvironment.getInstance().endAt(endAt);
		}
		
		dataCollector.addAllCurrentStock();
		
		return context;
	}

	private void supplyChainCreation(final Context<Object> context) {
		
		if (SCType.getScLayers() <= 2)
			Logger.logError("To few supply chain layers, minimum of 3 required:" + SCType.getScLayers());
		
		
		ArrayList<CountryAgent> countryAgents = SU.getObjectsAll(CountryAgent.class);
		
		for (CountryAgent country : countryAgents) {
			
			if (country.containsSCType(SCType.PRODUCER)) {
				for (int i = 0; i < Constants.N_PRODUCERS; i++) {
					country.spawnAgent(SCType.PRODUCER);
				}
			}
			
			if (country.containsSCType(SCType.INTERNATIONAL)) {
				for (int i = 0; i < Constants.N_INTERNATIONALS; i++) {
					country.spawnAgent(SCType.INTERNATIONAL);
				}
			}
			
			if (country.containsSCType(SCType.WHOLESALER)) {
				for (int i = 0; i < Constants.N_WHOLESALERS; i++) {
					country.spawnAgent(SCType.WHOLESALER);
				}
			}
			
			if (country.containsSCType(SCType.RETAIL)) {
				for (int i = 0; i < Constants.N_RETAILERS; i++) {
					country.spawnAgent(SCType.RETAIL);
				}
			}
			
			if (country.containsSCType(SCType.CONSUMER)) {
				for (int i = 0; i < Constants.N_CONSUMERS; i++) {
					country.spawnAgent(SCType.CONSUMER);
				}
			}
		}	
		
		// Set possible new suppliers and clients
		for (BaseAgent agent : SU.getObjectsAll(BaseAgent.class)) {
			agent.setPossibleNewSuppliersAndClients();
		}
	}

	/*====================================
	 * Step method
	 *====================================*/
	
	/**
	 * Step method, called from ContextBuilder to have more control
	 */
	@ScheduledMethod(start = 1, interval = 1, priority = 0, shuffle=false)
	public void step() {
		
		Logger.logMain("----------------------------------");
		Logger.logMain("Step " + SU.getTick());
		Logger.logMain("----------------------------------");
		
		/*Logger.logMain("Step-Shipment: police intervention on shipments");
		for (Shipment shipment : SU.getObjectsAll(Shipment.class)) {
			shipment.stepRemoval();
		}*/
		
		Logger.logMain("Step-BaseAgent: remove bankrupt agents");
		for (BaseAgent baseAgent : SU.getObjectsAll(BaseAgent.class)) {
			baseAgent.stepCheckRemoval();
		}

		Logger.logMain("Step-BaseAgent: reset output parameters");
		for (BaseAgent baseAgent : SU.getObjectsAll(BaseAgent.class)) {
			baseAgent.stepResetParameters();
		}
		
		Logger.logMain("Step-ContextBuilder: spawning of new agents");
		stepSpawning();
		
		Logger.logMain("Step-Shipment: shipments advancements");
		for (Shipment shipment : SU.getObjectsAll(Shipment.class)) {
			shipment.stepAdvanceShipment();
		}
		
		Logger.logMain("Step-Shipment: orders advancements");
		for (Order order : SU.getObjectsAll(Order.class)) {
			order.stepAdvanceOrder();
		}
		
		//Temporary
		Logger.logMain("Step-Agent5Consumer: temporary receive income");
		for (Agent5Consumer producer : SU.getObjectsAll(Agent5Consumer.class)) {
			producer.stepReceiveIncome();
		}
		
		Logger.logMain("Step-BaseAgent: receive shipment from suppliers");
		for (BaseAgent baseAgent : SU.getObjectsAll(BaseAgent.class)) {
			baseAgent.stepProcessArrivedShipments();
		}
		
		Logger.logMain("Step-BaseAgent: choose suppliers and buyers");
		for (BaseAgent baseAgent : SU.getObjectsAll(BaseAgent.class)) {
			baseAgent.stepChooseSuppliersAndClients();
		}
		
		Logger.logMain("Step-BaseAgent: send shipment to clients");
		for (BaseAgent baseAgent : SU.getObjectsAll(BaseAgent.class)) {
			baseAgent.stepSendShipment();
		}
		
		Logger.logMain("Step-BaseAgent: send order");
		for (BaseAgent baseAgent : SU.getObjectsAll(BaseAgent.class)) {
			baseAgent.stepSendOrder();
		}
		
		Logger.logMain("Step-BaseAgent: add to data");
		for (BaseAgent baseAgent : SU.getObjectsAll(BaseAgent.class)) {
			baseAgent.stepAddToData();
		}
		
		if (SU.getTick() >= RepastParam.getRunLength()) {
			
			Logger.logMain("------------------------------------------------------------------------------");
			saveRelations();
			RunEnvironment.getInstance().pauseRun();
			Logger.logMain("Simulation ended at : " + SU.getTick());
			Logger.logMain("------------------------------------------------------------------------------");
		}
	}
	
	public void saveRelations() {
		
		String filePathAndName = "C:/Work/Output/SimulationOutput22-03-2019/RelationsData" + SU.getCurrentDateTime() + ".txt";
		Logger.logMain("Relations information saved in: " + filePathAndName);
		List<String> data = new ArrayList<String>();
		data = SU.getDataCollector().getRelationsData();
		SU.writeToFile(filePathAndName, data);
	}
	
	public void stepSpawning() {
		
		if (SU.isInitializing())
			return ;
		
		for (SCType scType : SCType.values()) {
			double spawnRate = RepastParam.getSpawnRate();
			
			if (RepastParam.getDynamicSpawnRate()) {
				if (scType == SCType.WHOLESALER) {
					spawnRate = (0.5 * RepastParam.getSpawnRate() * SU.getObjectsCount(Agent4Retailer.class)) / SU.getObjectsCount(Agent3Wholesaler.class);
				} else if (scType == SCType.RETAIL) {
					spawnRate = (0.5 * RepastParam.getSpawnRate() * SU.getObjectsCount(Agent5Consumer.class)) / SU.getObjectsCount(Agent4Retailer.class);
				}
			}
			
			if (RandomHelper.nextDouble() < spawnRate) {
				
				for (CountryAgent country : SU.getObjectsAllRandom(CountryAgent.class)) {
					if (country.containsSCType(scType)) {
						country.spawnAgent(scType);
						break;
					}
				}
			}
			
		}
	}
	
	/**
	 * Create continuous space space for the given context
	 * @param context
	 * @return
	 */
	private ContinuousSpace<Object> createContinuousSpace(final Context<Object> context) {
		
		final ContinuousSpace<Object> space = ContinuousSpaceFactoryFinder.createContinuousSpaceFactory(null).
				createContinuousSpace( 	Constants.ID_CONTINUOUS_SPACE, context,
										new RandomCartesianAdder<Object>(),
										new repast.simphony.space.continuous.BouncyBorders(),
										Constants.GRID_WIDTH, Constants.GRID_HEIGHT);
		return space;
	}

	/**
	 * Create grid factory for the given context
	 * @param context
	 * @return
	 */
	private Grid<Object> createGrid(final Context<Object> context) {
		
		final Grid<Object> grid = GridFactoryFinder.createGridFactory(null).createGrid(
										Constants.ID_GRID, context,
										new GridBuilderParameters<Object>(
										new repast.simphony.space.grid.BouncyBorders(),
										new SimpleGridAdder<Object>(), true,
										Constants.GRID_WIDTH, Constants.GRID_HEIGHT));
		return grid;
	}

	/**
	 * Create network for the given context, this networks shows the trust relation from supplier to buyer
	 * @param context
	 * @return
	 */
	private Network<Object> createNetwork(final Context<Object> context) {
		
		NetworkBuilder <Object> netBuilder = new NetworkBuilder <Object> (Constants.ID_SC_NETWORK, context , true );
		netBuilder.buildNetwork();
		@SuppressWarnings("unchecked")
		Network<Object> net = (Network <Object>) context.getProjection(Constants.ID_SC_NETWORK);
		return net;
	}

	/**
	 * Create reversed network for the given context, this network shows the trust relation from buyer to supplier
	 * @param context
	 * @return
	 */
	private Network<Object> createNetworkReversed(final Context<Object> context) {
		
		NetworkBuilder <Object> netBuilder = new NetworkBuilder <Object> (Constants.ID_SC_NETWORK_REVERSED, context , true );
		netBuilder.buildNetwork();
		@SuppressWarnings("unchecked")
		Network<Object> net = (Network <Object>) context.getProjection(Constants.ID_SC_NETWORK_REVERSED);
		return net;
	}
}