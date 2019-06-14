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
	private static double spawnRate = 0.015;
	private static int ticksInitPopulation = 100;
	private static double sendShipmentProbability = 1;
	
	// The probability for any shipment to W, R, or C to be intercepted upon arrival
	private static double interceptionProbabilityW = 0.00;
	private static double interceptionProbabilityR = 0.00;
	private static double interceptionProbabilityC = 0.00;
	
	// The probability for any wholesaler or retailer to be arrested per tick
	private static double arrestProbabilityW = 0.00;
	private static double arrestProbabilityR = 0.00;
	
	private static boolean dynamicSpawnRate = true;
	
	private static int producerNumberCap = 8;
	private static boolean limitedSuppliersClients = false;
	
	public static void setRepastParameters() {
		
		Parameters p = RunEnvironment.getInstance().getParameters();
		//runLength = p.getInteger("pRunLength");
		consumptionMin = p.getDouble("pConMin");
		consumptionMax = p.getDouble("pConMax");
		productionMax = p.getDouble("pProdMax");
		shipmentStep = p.getInteger("pShipmentStep");
		//spawnRate = p.getDouble("pSpawnRate");
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
	
	public static double getInterceptionProbabilityW() {
		return interceptionProbabilityW;
	}
	
	public static double getInterceptionProbabilityR() {
		return interceptionProbabilityR;
	}
	
	public static double getInterceptionProbabilityC() {
		return interceptionProbabilityC;
	}
	
	public static double getArrestProbabilityW() {
		return arrestProbabilityW;
	}
	
	public static double getArrestProbabilityR() {
		return arrestProbabilityR;
	}
	
	public static boolean getDynamicSpawnRate() {
		return dynamicSpawnRate;
	}
}