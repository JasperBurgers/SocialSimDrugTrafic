package supplyChainModel.common;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import repast.simphony.context.Context;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.graph.Network;
import repast.simphony.space.grid.Grid;
import repast.simphony.util.SimUtilities;
import supplyChainModel.DataCollector;
import supplyChainModel.agents.BaseAgent;
import supplyChainModel.agents.CountryAgent;

/**
 * Simulation utility class. This class contains useful functions
 * such as retrieving specific objects in the simulation and getting
 * the IDs of the context, grids and networks.
 * @author Maarten Jensen
 *
 */
public class SU {

	private static Context<Object> masterContext = null; // MasterContext is saved because the SU class cannot call the context
	private static int agentId = -1;
	/**
	 * Set masterContext
	 */
	public static void setContext(Context<Object> masterContext) {
		SU.masterContext = masterContext;
	}
	
	public static void resetId() {
		agentId = 0;
	}
	
	public static int getNewId() {
		if (agentId >= 0)
			return agentId ++;
		else {
			Logger.logError("SU.getNewId(): agentId == -1, resetId() not called");
			return -1;
		}
	}
	
	/**
	 * Gets master context since I don't use the sub-contexts
	 * @return Context the master context
	 */
	public static Context<Object> getContext() {

		if (masterContext == null)  {
			Logger.logError("SimUtils.getContext(): context returned null");
		}
		return masterContext;
	}
	
	public static DataCollector getDataCollector() {
		ArrayList<DataCollector> dataCollectors = getObjectsAll(DataCollector.class);
		for (DataCollector dataCollector : dataCollectors) {
			return dataCollector;
		}
		Logger.logError("SU.getDataCollector(): no data collectors found");
		return null;
	}
	
	/**
	 * Retrieves the agent reference based on the ID.
	 * @param clazz (e.g. use as input Human.class)
	 * @return an ArrayList of objects from the given class
	 */
	public static BaseAgent getBaseAgent(int id) {
		
		//@SuppressWarnings("unchecked")
		final Iterable<Object> objects = (Iterable<Object>) getContext().getObjects(BaseAgent.class);
		for (final Object object : objects) {
			
			if (object instanceof BaseAgent) {
				if ( ((BaseAgent) object).getId() == id)
					return (BaseAgent) object;
			}
		}
		return null;
	}
	
	/**
	 * Retrieves all the objects within the master context based on the given class.
	 * @param clazz (e.g. use as input Human.class)
	 * @return an ArrayList of objects from the given class
	 */
	public static int getObjectsCount(Class<?> clazz) {
		
		final Iterable<?> objects = (Iterable<?>) getContext().getObjects(clazz);
		int count = 0;
		for (@SuppressWarnings("unused") final Object object : objects) {
			count ++;
		}
		return count;
	}
	
	/**
	 * Retrieves all the objects within the master context based on the given class.
	 * @param clazz (e.g. use as input Human.class)
	 * @return an ArrayList of objects from the given class
	 */
	public static <T> ArrayList<T> getObjectsAll(Class<T> clazz) {
		
		@SuppressWarnings("unchecked")
		final Iterable<T> objects = (Iterable<T>) getContext().getObjects(clazz);
		final ArrayList<T> objectList = new ArrayList<T>();
		for (final T object : objects) {
			objectList.add(object);
		}
		return objectList;
	}
	
	/**
	 * Same as getObjectsAll but uses SimUtilities.shuffle to randomize
	 * the ArrayList of objects
	 * @param clazz (e.g. use as input Human.class)
	 * @return an ArrayList of objects from the given class
	 */
	public static <T> ArrayList<T> getObjectsAllRandom(Class<T> clazz) {
		
		ArrayList<T> objectList = getObjectsAll(clazz);
		SimUtilities.shuffle(objectList, RandomHelper.getUniform());
		return objectList;
	}
	
	/**
	 * Retrieves all the objects within the master context based on the given class, excluding the given Object.
	 * @param clazz (e.g. use as input Human.class)
	 * @return an ArrayList of objects from the given class
	 */
	public static <T> ArrayList<T> getObjectsAllExclude(Class<T> clazz, Object exclude) {
		
		@SuppressWarnings("unchecked")
		final Iterable<T> objects = (Iterable<T>) getContext().getObjects(clazz);
		final ArrayList<T> objectList = new ArrayList<T>();
		for (final T object : objects) {
			if (object != exclude)
				objectList.add(object);
		}
		return objectList;
	}
	
	/**
	 * Same as getObjectsAll but uses SimUtilities.shuffle to randomize
	 * the ArrayList of objects
	 * @param clazz (e.g. use as input Human.class)
	 * @return an ArrayList of objects from the given class
	 */
	public static <T> ArrayList<T> getObjectsAllExcludeRandom(Class<T> clazz, Object exclude) {
		
		ArrayList<T> objectList = getObjectsAllExclude(clazz, exclude);
		SimUtilities.shuffle(objectList, RandomHelper.getUniform());
		return objectList;
	}
	
	/**
	 * Returns the angle in degrees
	 * @param x1
	 * @param y1
	 * @param x2
	 * @param y2
	 * @return
	 */
	public static double point_direction(double x1, double y1, double x2, double y2) {
		return Math.atan2(y2 - y1, x2 - x1);
	}
	
	public static double point_distance(double x1, double y1, double x2, double y2) {
		return Math.sqrt( Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2) );
	}
	
	public static boolean getIsInitializing() {
		if (getTick() <= RepastParam.getTicksInitPopulation()) 
			return true;
		else
			return false;
	}
	
	public static int getTick() {
		return (int) RunEnvironment.getInstance().getCurrentSchedule().getTickCount();
	}
	
	public static boolean isInitializing() {
		if (getTick() <= RepastParam.getTicksInitPopulation()) {
			return true;
		}
		else {
			return false;
		}
	}
	
	public static Grid<Object> getGrid() {
		@SuppressWarnings("unchecked")
		Grid<Object> grid = (Grid<Object>) getContext().getProjection(Constants.ID_GRID);
		if (grid == null)  {
			Logger.logError("SU.getGrid(): grid returned null (ID:" + Constants.ID_GRID + ")");
		}
		return grid;
	}
	
	public static ContinuousSpace<Object> getContinuousSpace() {
		@SuppressWarnings("unchecked")
		ContinuousSpace<Object> continuousSpace = (ContinuousSpace<Object>) getContext().getProjection(Constants.ID_CONTINUOUS_SPACE);
		if (continuousSpace == null)  {
			Logger.logError("SU.getContinuousSpace(): continouse returned null (ID:" + Constants.ID_CONTINUOUS_SPACE + ")");
		}
		return continuousSpace;
	}
	
	public static Network<Object> getNetworkSC() {
		@SuppressWarnings("unchecked")
		Network<Object> network = (Network<Object>) SU.getContext().getProjection(Constants.ID_SC_NETWORK);
		if (network == null)  {
			Logger.logError("SU.getNetworkSC(): network returned null (ID:" + Constants.ID_SC_NETWORK + ")");
		}
		return network;
	}
	
	public static Network<Object> getNetworkSCReversed() {
		@SuppressWarnings("unchecked")
		Network<Object> network = (Network<Object>) SU.getContext().getProjection(Constants.ID_SC_NETWORK_REVERSED);
		if (network == null)  {
			Logger.logError("SU.getNetworkSCReversed(): network returned null (ID:" + Constants.ID_SC_NETWORK_REVERSED + ")");
		}
		return network;
	}
	
	public static String getCurrentDateTime() {
		SimpleDateFormat formatter= new SimpleDateFormat(".yyyy.MMM.dd.HH_mm_ss");  
		Date date = new Date(System.currentTimeMillis());
		return formatter.format(date);
	}
	
	public static void writeToFile(String filePathAndName, List<String> data) {
		PrintWriter writer;
		try {
			writer = new PrintWriter(filePathAndName, "UTF-8");
			for (String datum : data) {
				writer.println(datum);
			}
			writer.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Returns a risk factor based on the years of imprisonment for a drug trafficking offense.
	 * 
	 * @param country
	 * @return the risk factor for a given country
	 */
	public static double riskFactor(CountryAgent country) {
		String countryName = country.getName();
		
		switch(countryName){
			case "The Netherlands":
				return 0.82;
			case "Germany":
				return 1.003; 
			case "France":
				return 0.85; 
			case "United Kingdom":
				return 1.38; 
			case "Italy":
				return 1.08; 
			case "Spain":
				return 0.87; 
			default:
				Logger.logError(country + " is not a valid country.");
				return 1;
		}
	}
	public static double travelcosts(CountryAgent clientcountry, CountryAgent suppliercountry) {
	String clientName = clientcountry.getName();
	String supplierName = suppliercountry.getName();
	if (clientName == supplierName)
		return 1;
	else
		return 2;
}
}
