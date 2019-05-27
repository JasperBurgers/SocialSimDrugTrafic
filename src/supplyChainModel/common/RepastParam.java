package supplyChainModel.common;

import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.parameter.Parameters;

/**
 * Class that upon running setRepastParameters() saves the
 * parameters that are set in the Repast HUD. The code can 
 * retrieve these parameters through this class.
 * @author Maarten Jensen
 *
 */
public final class RepastParam {

	// Default settings
	//private static int runLength = 1000;
	private static double consumptionMin = 0.5;
	private static double consumptionMax = 5;
	private static double productionMax = 10;
	private static int shipmentStep = 3;
	private static double spawnRate = 0.01;
	private static int ticksInitPopulation = 100;
	private static double sendShipmentProbability = 1;
	private static double interceptionProbability = 0.2;
	private static int producerNumberCap = 8;
	private static boolean limitedSuppliersClients = false;
	
	public static void setRepastParameters() {
		
		Parameters p = RunEnvironment.getInstance().getParameters();
		//runLength = p.getInteger("pRunLength");
		consumptionMin = p.getDouble("pConMin");
		consumptionMax = p.getDouble("pConMax");
		productionMax = p.getDouble("pProdMax");
		shipmentStep = p.getInteger("pShipmentStep");
		spawnRate = p.getDouble("pSpawnRate");
		ticksInitPopulation = p.getInteger("pTicksInitPopulation");
		sendShipmentProbability = p.getDouble("pSendShipmentProbability");
		producerNumberCap = p.getInteger("pProducerNumberCap");
		limitedSuppliersClients = p.getBoolean("pLimitedSuppliersClients");
	}

	/**
	 * Run length is just taken from the parameters each time,
	 * so it can be changed during a run by the user
	 * @return
	 */
	public static double getRunLength() {
		Parameters p = RunEnvironment.getInstance().getParameters();
		return p.getInteger("pRunLength");
	}
	
	public static double getConsumptionMin() {
		return consumptionMin;
	}
	
	public static double getConsumptionMax() {
		return consumptionMax;
	}
	
	public static double getProductionMax() {
		return productionMax;
	}	
	
	public static int getShipmentStep() {
		return shipmentStep;
	}
	
	public static double getSpawnRate() {
		return spawnRate;
	}
	
	public static int getTicksInitPopulation() {
		return ticksInitPopulation;
	}
	
	public static double getSendShipmentProbability() {
		return sendShipmentProbability;
	}
	
	public static int getProducerNumberCap() {
		return producerNumberCap;
	}
	
	public static boolean getLimitedSuppliersClients() {
		return limitedSuppliersClients;
	}
	
	public static double getInterceptionProbability() {
		return interceptionProbability;
	}
}