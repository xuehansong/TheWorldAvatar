package uk.ac.cam.cares.jps.agent.status.execute;

import java.time.Instant;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.ThreadContext;
import org.apache.logging.log4j.core.LoggerContext;
import uk.ac.cam.cares.jps.agent.status.define.TestDefinition;
import uk.ac.cam.cares.jps.agent.status.record.TestRecord;
import uk.ac.cam.cares.jps.base.timeseries.TimeSeriesClient;

/**
 * Simple test class that uses the TimeSeries client to test how many measurement entries existing
 * across an entire namespace?
 *
 * @author Michael Hillman
 */
public class TimeSeriesTestExecutor extends TestExecutor {

    /**
     * Initialise a new AvailabilityTestDefinition instance with the input test definition.
     *
     * @param definition Definition of test to run.
     */
    public TimeSeriesTestExecutor(TestDefinition definition) {
        super(definition);
    }

    /**
     * Attempts to contact the KG endpoint and count the number of triples present via the JPS Base
     * Library.
     */
    @Override
    public void execute() {
        // Generate the test record
        record = new TestRecord();
        record.setDefinition(definition);

        // Mark the test time
        record.markTime();

        // Set logging context
        ThreadContext.put("groupName", definition.getType().toString());
        ThreadContext.put("testName", definition.getName());
        ThreadContext.put("testTime", record.getExecutionTime());
        ((LoggerContext) LogManager.getContext(false)).reconfigure();

        // Get the endpoint
        String endpoint = definition.getInput("endpoint");
        if (endpoint == null || endpoint.isEmpty()) {
            LOGGER.error("Could not find required 'endpoint' input.");
            record.setResult(false);
            return;
        }

        try {
            TimeSeriesClient<Instant> client = null;
            
            // Get IRIs with type
            
            
            
//            // Initialise the client
//            RemoteStoreClient kgClient = new RemoteStoreClient(endpoint);
//
//            // Pass credentials (if present)
//            if (definition.getUsername() != null && definition.getPassword() != null) {
//                kgClient.setUser(definition.getUsername());
//                kgClient.setPassword(definition.getPassword());
//                LOGGER.info("Using credentials for KG access.");
//            } else {
//                LOGGER.info("No credentials set for KG access, skipping.");
//            }
//
//            // Run the query
//            LOGGER.info("Running query.");
//            JSONArray results = kgClient.executeQuery(QUERY);
//            LOGGER.info("Query finished.");
//
//            // Parse the result
//            LOGGER.info("Full result: " + results.toString());
//            JSONObject firstResult = results.getJSONObject(0);
//
//            // Count the triples
//            int triples = firstResult.optInt("NO_OF_TRIPLES");
//            LOGGER.info("Triple count determined as: " + triples);
//
//            // Store in record
//            record.setResult(triples > 0);
//
//            if (record.getResult()) {
//                LOGGER.info("Test passes successfully.");
//            } else {
//                LOGGER.error("Test has failed.");
//            }

        } catch (Exception exception) {
            // Catch everything as the base library throws some strange stuff
            LOGGER.error("Exception occurred during test execution.", exception);
            record.setResult(false);
        }

        ThreadContext.clearAll();
    }

}
// End of class.
