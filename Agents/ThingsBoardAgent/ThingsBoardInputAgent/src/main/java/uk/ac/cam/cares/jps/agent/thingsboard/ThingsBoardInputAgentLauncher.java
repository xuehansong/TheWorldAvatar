package uk.ac.cam.cares.jps.agent.thingsboard;

import org.json.JSONObject;
import uk.ac.cam.cares.jps.base.exception.JPSRuntimeException;
import uk.ac.cam.cares.jps.base.timeseries.TimeSeriesClient;

import java.io.IOException;
import java.time.OffsetDateTime;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Class with a main method that is the entry point of the compiled jar and puts all components together to retrieve
 * data from the API and write it into the database.
 * @author 
 */
public class ThingsBoardInputAgentLauncher {

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

    // TODO: Use proper argument parsing
    /**
     * Main method that runs through all steps to update the data received from the ThingsBoard API.
     * defined in the provided properties file.
     * @param args The command line arguments. Three properties files should be passed here in order: 1) input agent
     *             2) time series client 3) API connector.
     */
    public static void main(String[] args) {

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
