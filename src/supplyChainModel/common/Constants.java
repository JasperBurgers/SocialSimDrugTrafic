package supplyChainModel.common;

import java.awt.Font;

/**
 * Contains all the parameters that are used in the simulation, 
 * except for the parameters that are influencable by the repast HUD.
 * This class should be used for parameters instead of hard-coding them.
 * @author Maarten Jensen
 *
 */
public class Constants {
	
	// See SupplyChainModel.rs/context.xml
	public static final String ID_CONTEXT = "supply chain";
	public static final String ID_CONTINUOUS_SPACE = "continuous space";
	public static final String ID_GRID = "grid";
	public static final String ID_SC_NETWORK = "sc network";
	public static final String ID_SC_NETWORK_REVERSED = "sc network reversed";
	
	public static final int GRID_WIDTH = 58;
	public static final int GRID_HEIGHT = 50;
	public static final int GRID_CELL_SIZE = 25;
	
	public static final Font FONT_LABEL = new Font("Tahoma", Font.PLAIN , 10);
	public static final Font FONT_BIG   = new Font("Tahoma", Font.BOLD , 14);
	public static final Font FONT_HUGE  = new Font("Tahoma", Font.BOLD , 28);
	
	public static final int PRODUCER_PRODUCE_AMOUNT = 50;
	public static final int MAX_NUMBER_OF_ACTIVE_SUPPLIERS = 10;
	public static final int MAX_NUMBER_OF_ACTIVE_CLIENTS = 10;
	public static final double TRUST_SWITCH_LEVEL = 0.1;
	
	public static final double PRICE_LIVING_COST_MULT = 0.01;
	public static final double PRICE_SAVED_STOCK_MULT = 0.01;
	public static final double PRICE_MONEY_START_MULT = 5;
	public static final double PRICE_PRODUCTION = 0.35; //800; //Leaves then, per kilogram
	public static final double PRICE_BUY_FROM_PRODUCER = 1; //2147; //Cocaine in brick form, per kilogram
	public static final double PRICE_BUY_FROM_INTERNATIONAL = 16; //34700; //Cocaine in brick form, per kilogram
	public static final double PRICE_BUY_FROM_WHOLESALER = 47; //100000; //Cocaine in brick form in Europe, per kilogram (this is not from source)
	public static final double PRICE_BUY_FROM_RETAIL = 56; //120000; //Street level cocaine, per kilogram
	public static final double PRICE_CONSUMER_INCOME = 1500; //120000;
	
	public static final double SHIPMENT_MAX_1TO2 = 100;
	public static final double SHIPMENT_MAX_2TO3 = 500;
	public static final double SHIPMENT_MAX_3TO4 = 100;
	public static final double SHIPMENT_MAX_4TO5 = 20;
	public static final double SHIPMENT_MIN_PERCENTAGE = 5;
	
	public static final double SEND_ORDER_LEARN_RATE = 0.05;
	public static final double LATE_SHIPMENT_PENALIZE_MULT = 0.5;
	
	public static final double STOCK_SECURITY_MULT_MIN = 1; //Security stock is dependent on the minimum package size
	public static final double STOCK_SECURITY_MULT_MAX = 1.5;
	public static final double STOCK_SEARCH_NEW_SUPPLIER = 0.1;
	
	public static final double PROB_POSSIBLE_NEW_MIN = 0.1;
	public static final double PROB_POSSIBLE_NEW_MULT = 0.5;
	public static final int    NEW_CONNECTION_COOLDOWN = 10;
	
	// Number of nodes per country
	public static final int N_PRODUCERS	= 6;
	public static final int N_INTERNATIONALS = 4;
	public static final int N_WHOLESALERS = 2;
	public static final int N_RETAILERS	= 1;
	public static final int N_CONSUMERS	= 1;

	//public static final byte MAX_GOOD_QUALITY = 100;
	public static final byte QUALITY_MINIMUM = 40;
	public static final byte QUALITY_MAXIMUM = 60;
	public static final double QUALITY_MAX_EXTRA_COST = 1.2;
	
	// Consumer specific
	public static final int CONSUMER_REMOVE_TICKS = 200;
	public static final int CONSUMER_LIMIT_WITHOUT_SATISFACTION = 20;
	
	public static final int COUNTRY_CONSUMERS_MAX = 2;
	
	// Visualization constants
	public static final int VSL_COUNTRY_X = 4;
	public static final int VSL_COUNTRY_WIDTH = 12;
	public static final double VSL_ORD_SHP_DIF_Y = 0.2;
	public static final double VSL_ORD_SHP_DIF_MOVE = 0.2;
}