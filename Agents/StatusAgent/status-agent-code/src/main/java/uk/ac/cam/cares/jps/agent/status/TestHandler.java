package uk.ac.cam.cares.jps.agent.status;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import org.apache.logging.log4j.core.LoggerContext;
import uk.ac.cam.cares.jps.agent.status.define.TestDefinition;
import uk.ac.cam.cares.jps.agent.status.define.TestType;
import uk.ac.cam.cares.jps.agent.status.execute.TestExecutor;
import uk.ac.cam.cares.jps.agent.status.record.TestRecord;
import uk.ac.cam.cares.jps.agent.status.record.TestRecordStore;
import uk.ac.cam.cares.jps.agent.status.record.TestRecordStoreMarshaller;

/**
 * This class handles setting up the TestExecutor instances to run tests.
 *
 * @author Michael Hillman
 */
public class TestHandler {

    /**
     * Logger.
     */
    private static final Logger LOGGER = LogManager.getLogger(TestHandler.class);

    /**
     * Historical test results.
     */
    private final TestRecordStore recordStore;
    
    /**
     * Initialise a new TestHandler instance.
     */
    public TestHandler() {
        recordStore = TestRecordStoreMarshaller.readRecords();
    }

    /**
     * Return the current RecordStore instance.
     *
     * @return
     */
    public TestRecordStore getRecordStore() {
        return recordStore;
    }

    /**
     * Execute all registered tests in serial.
     *
     * @return list of test results.
     */
    public synchronized List<TestRecord> runAllTests() {
        List<TestRecord> records = new ArrayList<>();

        // Run all the tests
        for (TestDefinition definition : TestRegistry.getDefinedTests()) {
            records.add(runTest(definition));
        }
        return records;
    }

    /**
     * Run a single test.
     *
     * @param testName test name.
     * @param testType test type.
     * 
     * @return test result.
     */
    public synchronized TestRecord runTest(String testName, String testType) {
        TestDefinition definition = null;
        try {
            definition = TestRegistry.getDefinedTest(testName, TestType.valueOf(testType));
        } catch (Exception exception) {
            // Could happen if testType is an invalid value
            return null;
        }

        if (definition != null) {
            return runTest(definition);
        }
        return null;
    }

    /**
     * Run a single test.
     *
     * @param definition test definition.
     * 
     * @return test result.
     */
    public synchronized TestRecord runTest(TestDefinition definition) {
        try {
            // Find the executor class registered for that definition
            Class<? extends TestExecutor> executorClass = TestUtils.getExecutorForType(definition.getType());

            // Create an instance of the executor
            Constructor<? extends TestExecutor> contrusctor = executorClass.getDeclaredConstructor(TestDefinition.class);
            TestExecutor executor = contrusctor.newInstance(new Object[]{definition});

            // Run the executor
            LOGGER.info("Executing '" + definition.getName() + "' from '" + definition.getType() + "' tests");
            executor.execute();

            // Clear the logging context
            ThreadContext.clearAll();
            ((LoggerContext) LogManager.getContext(false)).reconfigure();

            // Add the record to the store
            TestRecord record = executor.getRecord();
            
            if (record != null && record.getExecutionTime() != null) {
                recordStore.addRecord(record);
                LOGGER.info("There are now " + recordStore.getRecords().size() + " test records.");
                
                // Write the updated TestRecordStore to file
                TestRecordStoreMarshaller.writeRecords(recordStore);
                return record;
            }
            return null;

        } catch (Exception exception) {
            LOGGER.error("Could not create/run TestExecutor instance for '" + definition.getName() + "' test.", exception);
            return null;
        }
    }

}
// End of class.
