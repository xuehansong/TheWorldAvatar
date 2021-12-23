package uk.ac.cam.cares.jps.agent.thingsboard;

import org.json.JSONException;
import org.json.JSONObject;
import uk.ac.cam.cares.jps.base.exception.JPSRuntimeException;
import uk.ac.cam.cares.jps.base.timeseries.TimeSeriesClient;
import uk.ac.cam.cares.jps.base.agent.JPSAgent;

import java.io.IOException;
import java.time.OffsetDateTime;
import javax.servlet.annotation.WebServlet;
import javax.ws.rs.BadRequestException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Class with a main method that is the entry point of the compiled war and puts all components together to retrieve
 * data from the API and write it into the database.
 * @author 
 */
@WebServlet(urlPatterns = {"/retrieve"})
public class ThingsBoardInputAgentLauncher extends JPSAgent {
	
	public static final String KEY_AGENTPROPERTIES = "agentProperties";
	public static final String KEY_APIPROPERTIES = "apiProperties";
	public static final String KEY_CLIENTPROPERTIES = "clientProperties";
	
	
	 String agentProperties;
	 String apiProperties;
	 String clientProperties;
    /**
     * Logger for reporting info/errors.
     */
    private static final Logger LOGGER = LogManager.getLogger(ThingsBoardInputAgentLauncher.class);
    /**
     * Logging / error messages
     */
    private static final String ARGUMENT_MISMATCH_MSG = "Need three properties files in the following order: 1) input agent 2) time series client 3) API connector.";
    private static final String AGENT_ERROR_MSG = "The ThingsBoard input agent could not be constructed!";
    private static final String TSCLIENT_ERROR_MSG = "Could not construct the time series client needed by the input agent!";
    private static final String INITIALIZE_ERROR_MSG = "Could not initialize time series.";
    private static final String CONNECTOR_ERROR_MSG = "Could not construct the ThingsBoard API connector needed to interact with the API!";
    private static final String GET_READINGS_ERROR_MSG = "Some readings could not be retrieved.";

    
    @Override
    public JSONObject processRequestParameters(JSONObject requestParams) {
      if (validateInput(requestParams)) {
        	LOGGER.info("Passing request to ThingsBoard Input Agent..");
            String agentProperties = System.getenv(requestParams.getString(KEY_AGENTPROPERTIES));
            String clientProperties = System.getenv(requestParams.getString(KEY_CLIENTPROPERTIES));
            String apiProperties = System.getenv(requestParams.getString(KEY_APIPROPERTIES));
            String[] args = new String[] {agentProperties,clientProperties,apiProperties};
            initializeAgent(args);
            }
        JSONObject jsonMessage = new JSONObject();
        jsonMessage.put("Result", "Timeseries Data has been updated.");
        requestParams = jsonMessage;
	return requestParams;
}
    
    @Override
    public boolean validateInput(JSONObject requestParams) throws BadRequestException {
      boolean validate = true;
      String agentProperties;
      String apiProperties;
      String clientProperties;
      if (!requestParams.isEmpty()) {
    	 try {
    		 agentProperties = (requestParams.getString(KEY_AGENTPROPERTIES));
    		 clientProperties =  (requestParams.getString(KEY_CLIENTPROPERTIES));
    		 apiProperties = (requestParams.getString(KEY_APIPROPERTIES));
      }
    	 catch (JSONException e) {
    		 validate = false;
    		 throw new BadRequestException ("Invalid keys in the JSON Object.", e);
    	 }
    	 if (System.getenv(agentProperties) == null){
    		 validate = false;
    		 LOGGER.error("The environment variable does not point to a valid agent properties file.");
    		 }
    	 if (System.getenv(apiProperties) == null){
    		 validate = false;
    		 LOGGER.error("The environment variable does not point to a valid api properties file.");
    		 }
    	 if (System.getenv(clientProperties) == null){
    		 validate = false;
    		 LOGGER.error("The environment variable does not point to a valid client properties file.");
    		 }
    	 }
	return validate;
    }
    
 // TODO: Use proper argument parsing
    /**
     * Main method that runs through all steps to update the data received from the ThingsBoard API.
     * defined in the provided properties file.
     * @param args The command line arguments. Three properties files should be passed here in order: 1) input agent
     *             2) time series client 3) API connector.
     */
    
    public static void initializeAgent(String[] args) {

        // Ensure that there are three properties files
        if (args.length != 3) {
            LOGGER.error(ARGUMENT_MISMATCH_MSG);
            throw new JPSRuntimeException(ARGUMENT_MISMATCH_MSG);
        }
        LOGGER.debug("Launcher called with the following files: " + String.join(" ", args));

        // Create the agent
        ThingsBoardInputAgent agent;
        try {
            agent = new ThingsBoardInputAgent(args[0]);
        } catch (IOException e) {
            LOGGER.error(AGENT_ERROR_MSG, e);
            throw new JPSRuntimeException(AGENT_ERROR_MSG, e);
        }
        LOGGER.info("Input agent object initialized.");

        // Create and set the time series client
        try {
            TimeSeriesClient<OffsetDateTime> tsClient = new TimeSeriesClient<>(OffsetDateTime.class, args[1]);
            agent.setTsClient(tsClient);
        } catch (IOException | JPSRuntimeException e) {
            LOGGER.error(TSCLIENT_ERROR_MSG, e);
            throw new JPSRuntimeException(TSCLIENT_ERROR_MSG, e);
        }
        LOGGER.info("Time series client object initialized.");

        // Initialize time series'
        try {
            agent.initializeTimeSeriesIfNotExist();
        }
        catch (JPSRuntimeException e) {
            LOGGER.error(INITIALIZE_ERROR_MSG,e);
            throw new JPSRuntimeException(INITIALIZE_ERROR_MSG, e);
        }

        // Create the connector to interact with the ThingsBoard API
        ThingsBoardAPIConnector connector;
        try {
            connector = new ThingsBoardAPIConnector(args[2]);
        } catch (IOException e) {
            LOGGER.error(CONNECTOR_ERROR_MSG, e);
            throw new JPSRuntimeException(CONNECTOR_ERROR_MSG, e);
        }
        LOGGER.info("API connector object initialized.");
        connector.connect();

        // Retrieve readings
        JSONObject ElectricalTemperatureHumidityReadings;
        
        try {
            ElectricalTemperatureHumidityReadings = connector.getAllReadings();
        }
        catch (Exception e) {
            LOGGER.error(GET_READINGS_ERROR_MSG, e);
            throw new JPSRuntimeException(GET_READINGS_ERROR_MSG, e);
        }
        LOGGER.info(String.format("Retrieved %d electrical, temperature and humdity readings.",
                ElectricalTemperatureHumidityReadings.length()));

        // If readings are not empty there is new data
        if(!ElectricalTemperatureHumidityReadings.isEmpty()) {
            // Update the data
            agent.updateData(ElectricalTemperatureHumidityReadings);
            LOGGER.info("Data updated with new readings from API.");
        }
        // If all are empty no new readings are available
        else if(ElectricalTemperatureHumidityReadings.isEmpty()) {
            LOGGER.info("No new readings are available.");
        }
    }

}
