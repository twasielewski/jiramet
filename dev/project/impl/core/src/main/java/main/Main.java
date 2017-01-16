package main;

import database.exception.IssueNotFoundException;
import utils.InsufficientArgumentsException;
import utils.UsageApplication;
import utils.properties.PropertiesReader;

/**
 * Contains main method that invokes all other methods.
 *
 */
public class Main
{

	/**
	 * Invokes all methods needed to determine times needed to resolve new issue by concrete developers.
	 * 
	 * @param args - you should pass the path to the file with the properties used in prediction calculations
	 */
	public static void main(String args[])
	{
		UsageApplication application = new UsageApplication(args);
		try
		{
			PropertiesReader propertiesReader = new PropertiesReader(application.getArg(1));
			PredictionModelViewer predictionModelViewer = new PredictionModelViewer(propertiesReader);
			predictionModelViewer.init();
			predictionModelViewer.showPrediction();
		} catch (InsufficientArgumentsException e)
		{
			application.showUsage();
		} catch (IssueNotFoundException e)
		{
			application.showIssueNotFoundText(e.getMessage());
		}

	}

}
