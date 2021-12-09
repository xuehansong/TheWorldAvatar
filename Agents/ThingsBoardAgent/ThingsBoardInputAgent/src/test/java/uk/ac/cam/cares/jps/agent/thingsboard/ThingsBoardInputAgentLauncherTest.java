package uk.ac.cam.cares.jps.agent.thingsboard;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;
import uk.ac.cam.cares.jps.base.exception.JPSRuntimeException;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;

public class ThingsBoardInputAgentLauncherTest {

    // Temporary folder to place a properties file
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();
    // Name of the properties files
    private final String agentPropertiesFilename = "agent.properties";
    private final String clientPropertiesFilename = "client.properties";
    private final String apiPropertiesFilename = "api.properties";
    // Argument array used with the main function containing all the paths to the property files as string
    private String[] args;

    @Before
    public void initializePropertyFile() throws IOException {
        File agentPropertyFile= folder.newFile(agentPropertiesFilename);
        File clientPropertyFile= folder.newFile(clientPropertiesFilename);
        File apiPropertyFile= folder.newFile(apiPropertiesFilename);
        // Paths to the three different property files
        String agentPropertiesFile = agentPropertyFile.getCanonicalPath();
        String clientPropertiesFile = clientPropertyFile.getCanonicalPath();
        String apiPropertiesFile = apiPropertyFile.getCanonicalPath();

        args = new String[] {agentPropertiesFile, clientPropertiesFile, apiPropertiesFile};
    }

    @Test
    public void testMainNoArgs() {
        String[] args = {};
        try {
            ThingsBoardInputAgentLauncher.main(args);
            Assert.fail();
        }
        catch (JPSRuntimeException e) {
            Assert.assertEquals("Need three properties files in the following order: 1) input agent 2) time series client 3) API connector.",
                    e.getMessage());
        }
    }

    @Test
    public void testMainInvalidAgentPropertyFile() {
        // Empty agent properties file should result in an error
        try {
            ThingsBoardInputAgentLauncher.main(args);
            Assert.fail();
        }
        catch (JPSRuntimeException e) {
            Assert.assertEquals("The ThingsBoard input agent could not be constructed!", e.getMessage());
        }
    }

    @Test
    public void testMainErrorWhenCreatingTSClient() throws IOException {
        createProperAgentPropertiesFile();
        // Empty properties file for time series client should result in exception
        try {
            ThingsBoardInputAgentLauncher.main(args);
            Assert.fail();
        }
        catch (JPSRuntimeException e) {
            Assert.assertEquals("Could not construct the time series client needed by the input agent!", e.getMessage());
        }

    }

    @Test
    public void testMainErrorWhenCreatingAPIConnector() throws IOException {
        createProperClientPropertiesFile();
        // Use a mock for the input agent
        try(MockedConstruction<ThingsBoardInputAgent> mockAgent = Mockito.mockConstruction(ThingsBoardInputAgent.class)) {
            // Empty API properties file should result in an exception
            try {
                ThingsBoardInputAgentLauncher.main(args);
                Assert.fail();
            }
            catch (JPSRuntimeException e) {
                // Ensure that the method to set the time series client was invoked once
                Mockito.verify(mockAgent.constructed().get(0), Mockito.times(1)).setTsClient(Mockito.any());
                // Ensure that the initialization was invoked once
                Mockito.verify(mockAgent.constructed().get(0), Mockito.times(1)).initializeTimeSeriesIfNotExist();
                Assert.assertEquals("Could not construct the ThingsBoard API connector needed to interact with the API!", e.getMessage());
            }
        }

    }

    @Test
    public void testMainErrorWhenRetrievingReadings() throws IOException {
        createProperClientPropertiesFile();
        createProperAPIPropertiesFile();
        // Use a mock for the input agent
        try(MockedConstruction<ThingsBoardInputAgent> ignored = Mockito.mockConstruction(ThingsBoardInputAgent.class)) {
            // Use a mock for the connector that throws an exception when readings are requested
            try(MockedConstruction<ThingsBoardAPIConnector> mockConnector = Mockito.mockConstruction(ThingsBoardAPIConnector.class,
                    (mock, context) -> Mockito.when(mock.getAllReadings()).thenThrow(new JPSRuntimeException("exception")))) {
                try {
                    ThingsBoardInputAgentLauncher.main(args);
                    Assert.fail();
                }
                catch (JPSRuntimeException e) {
                    // Ensure that the connect method was invoked once
                    Mockito.verify(mockConnector.constructed().get(0), Mockito.times(1)).connect();
                    Assert.assertEquals("Some readings could not be retrieved.", e.getMessage());
                    Assert.assertEquals(JPSRuntimeException.class, e.getCause().getClass());
                    Assert.assertEquals("exception", e.getCause().getMessage());
                }
            }
        }
    }


    @Test
    public void testReadingsNotEmpty() throws IOException {
        createProperClientPropertiesFile();
        createProperAPIPropertiesFile();
        // Create dummy readings to return
        JSONObject readings = new JSONObject();
        JSONObject values_01 = new JSONObject();
        values_01.put("ts","7891011" );
        values_01.put("value", "0.123");
        JSONObject values_02 = new JSONObject();
        values_02.put("ts","123456" );
        values_02.put("value", "0.456");
        JSONArray values = new JSONArray();
        values.put(values_01);
        values.put(values_02);
        readings.put("Current", values);
        // Use a mock for the input agent
        try(MockedConstruction<ThingsBoardInputAgent> mockAgent = Mockito.mockConstruction(ThingsBoardInputAgent.class)) {
            // Use a mock for the connector that returns the dummy readings
            try(MockedConstruction<ThingsBoardAPIConnector> ignored = Mockito.mockConstruction(ThingsBoardAPIConnector.class,
                    (mock, context) -> {
                        Mockito.when(mock.getAllReadings()).thenReturn(readings);
                    })) {
                ThingsBoardInputAgentLauncher.main(args);
                // Ensure that the update of the agent was invoked
                Mockito.verify(mockAgent.constructed().get(0), Mockito.times(1)).updateData(readings);
            }
        }
    }

    private void createProperAgentPropertiesFile() throws IOException {
        // Create a properties file that points to the example/test mapping folder in the resources //
        // Create mappings folder
        String folderName = "mappings";
        File mappingFolder = folder.newFolder(folderName);
        // Create empty file in mappings folder
        File mappingFile = new File(Paths.get(mappingFolder.getCanonicalPath(), "allTypes.properties").toString());
        Assert.assertTrue(mappingFile.createNewFile());
        // Filepath for the properties file
        String propertiesFile = Paths.get(folder.getRoot().toString(), agentPropertiesFilename).toString();
        try (FileWriter writer = new FileWriter(propertiesFile, false)) {
            writer.write("thingsboard.mappingfolder=" + mappingFolder.getCanonicalPath().
                    replace("\\","/") + "\n");
        }
    }

    private void createProperClientPropertiesFile() throws IOException {
        // Filepath for the properties file
        String propertiesFile = Paths.get(folder.getRoot().toString(), clientPropertiesFilename).toString();
        try (FileWriter writer = new FileWriter(propertiesFile, false)) {
            writer.write("db.url=jdbc:postgresql:timeseries\n");
            writer.write("db.user=postgres\n");
            writer.write("db.password=cares1010\n");
            writer.write("sparql.query.endpoint=http://localhost:9999/blazegraph/namespace/kb/sparql\n");
            writer.write("sparql.update.endpoint=http://localhost:9999/blazegraph/namespace/kb/sparql\n");
        }
    }

    private void createProperAPIPropertiesFile() throws IOException {
        // Filepath for the properties file
        String propertiesFile = Paths.get(folder.getRoot().toString(), apiPropertiesFilename).toString();
        try (FileWriter writer = new FileWriter(propertiesFile, false)) {
            writer.write("thingsboard.username=username\n");
            writer.write("thingsboard.password=password\n");
            writer.write("path.url=http://localhost:9090\n");
            writer.write("device.token=f6e2fac0-4b9a-11ec-bc75-d14ed3d0d946");
        }
    }
}
