package supplyChainModel;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import repast.simphony.context.Context;
import supplyChainModel.agents.CountryAgent;
import supplyChainModel.common.Constants;
import supplyChainModel.common.Logger;
import supplyChainModel.enums.SCType;

/**
 * This class can load data to generate a simulation state with
 * appropriate classes.
 * @author Maarten Jensen
 *
 */
public class ContextDataLoader {

	/**
	 * Function to read the csv data file
	 * @param context
	 * @param filePath
	 * @param fileName
	 */
	public void readFullFile(final Context<Object> context, String filePath, String fileName) {
		
		Logger.logInfo("ContextDataLoader.readFullFile() from " + filePath + "/" + fileName + ".txt");
		List<String> dataAll = readFile(filePath + "/" + fileName + ".txt");
		List<String> dataC = new ArrayList<String>();
		
		for (String datum : dataAll) {
			if (!datum.startsWith("%")) {
				dataC.add(datum);
			}
		}
		
		Logger.logInfo("ContextDataLoader.readFullFile() generate country");
		generateCountries(context, dataC);
	}

	/**
	 * Function to generate the plain countries based on the data file.
	 * It creates the countries on locations dependent on the type of country
	 * (with countries at the beginning of the supply chain more to the left)
	 * and ordered on height.
	 * @param context
	 * @param dataSC
	 */
	public void generateCountries(final Context<Object> context, List<String> dataC) {
		
		int vslTotalConsumers = Constants.COUNTRY_CONSUMERS_MAX;
		// Count countries
		for (String nodeString : dataC) {
			
			List<String> vars = Arrays.asList(nodeString.split(","));			
			if (Integer.parseInt(vars.get(1)) == 3)
				vslTotalConsumers ++;
		}
		
		int vslStepSize = (Constants.GRID_HEIGHT - 2) / vslTotalConsumers;
		Logger.logInfo("Country distance: (" + Constants.GRID_HEIGHT + " - 2) / Consumers = " + vslStepSize);
		int vslTransitCountry = 0; //Assuming there are two transit countries
		int vslConsumerCountry = 1; //The current count of consumer countries
	
		double qualMin = getCountryQualityMin(dataC);
		double qualDif = getCountryQualityMax(dataC) - qualMin;
		
		// Create countries
		for (String nodeString : dataC) {
			
			List<String> vars = Arrays.asList(nodeString.split(","));
			String name = vars.get(0);
			int layer = Integer.parseInt(vars.get(1));
			
			//double retailPrice = Double.parseDouble(vars.get(2));
			double avgQuality = Double.parseDouble(vars.get(3));
			double countryQuality = qualDif - (avgQuality - qualMin);
			
			int vslCountryX = Constants.VSL_COUNTRY_X + layer * Constants.VSL_COUNTRY_WIDTH;
			
			ArrayList<SCType> scTypes = new ArrayList<SCType>(); 
			switch (layer) {
			case 0: // Producer country
				scTypes.add(SCType.PRODUCER);
				new CountryAgent(context, name, scTypes, vslCountryX, Constants.GRID_HEIGHT - 4, Constants.GRID_HEIGHT - 8, countryQuality); 
				break;
			case 1: // International country
				scTypes.add(SCType.INTERNATIONAL);
				new CountryAgent(context, name, scTypes, vslCountryX, Constants.GRID_HEIGHT - 6, Constants.GRID_HEIGHT - 12, countryQuality);
				break;
			case 2: // Transit country
				scTypes.add(SCType.WHOLESALER);
				scTypes.add(SCType.RETAIL);
				scTypes.add(SCType.CONSUMER);
				if (vslTransitCountry == 0)
					new CountryAgent(context, name, scTypes, vslCountryX, Constants.GRID_HEIGHT - 3, vslStepSize - 1, countryQuality);
				else
					new CountryAgent(context, name, scTypes, vslCountryX, (Constants.GRID_HEIGHT - 3) - vslStepSize * (vslTotalConsumers - 1), vslStepSize - 1, countryQuality);
				vslTransitCountry ++;
				break;
			case 3: // Consumer country
				scTypes.add(SCType.RETAIL);
				scTypes.add(SCType.CONSUMER);
				new CountryAgent(context, name, scTypes, vslCountryX, (Constants.GRID_HEIGHT - 3) - vslStepSize * vslConsumerCountry, vslStepSize - 1, countryQuality);
				vslConsumerCountry ++;
				break;
			}
		}
	}
	
	/**
	 * Return the minimum quality value of all countries.
	 * This variable is found at position 3 in the string.
	 * @param dataC
	 * @return
	 */
	public double getCountryQualityMin(List<String> dataC) {
		
		double minQuality = Double.MAX_VALUE;
		for (String nodeString : dataC) {
			
			List<String> vars = Arrays.asList(nodeString.split(","));
			double countryQuality = Double.parseDouble(vars.get(3));
			if (countryQuality < minQuality)
				minQuality = countryQuality;
		}
		return minQuality;
	}
	
	/**
	 * Return the maximum quality value of all countries.
	 * This variable is found at position 3 in the string.
	 * @param dataC
	 * @return
	 */
	public double getCountryQualityMax(List<String> dataC) {
		
		double maxQuality = Double.MIN_VALUE;
		for (String nodeString : dataC) {
			
			List<String> vars = Arrays.asList(nodeString.split(","));
			double countryQuality = Double.parseDouble(vars.get(3));
			if (countryQuality > maxQuality)
				maxQuality = countryQuality;
		}
		return maxQuality;
	}
	
	/**
	 * Technical function that converts a text file (given with the filePathAndName) to 
	 * a list of strings where each element in the list is a line in the text file
	 * @param filePathAndName
	 * @return
	 */
	public List<String> readFile(String filePathAndName) {
		BufferedReader reader;
		List<String> data = new ArrayList<String>();
		try {
			reader = new BufferedReader(new FileReader(filePathAndName));
			String line = reader.readLine();
			while (line != null) {
				data.add(line);
				line = reader.readLine();
			}
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return data;
	}
}