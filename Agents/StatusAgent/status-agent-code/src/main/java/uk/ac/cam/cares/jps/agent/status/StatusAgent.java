package uk.ac.cam.cares.jps.agent.status;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import uk.ac.cam.cares.jps.agent.status.process.StatusRequest;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.core.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Controller;
import uk.ac.cam.cares.jps.agent.status.process.DashboardRequest;
import uk.ac.cam.cares.jps.agent.status.process.SubmitRequest;
import uk.ac.cam.cares.jps.agent.status.record.TestRecord;
import uk.ac.cam.cares.jps.base.agent.JPSAgent;
import uk.ac.cam.cares.jps.base.email.EmailSender;

/**
 * The StatusAgent provides a number of availability tests to check that KG endpoints (and other
 * agents) are available at their expected URLs, and contain the expected data. These tests are run
 * on a regular schedule, and the results viewed via a provided webpage. Manual execution of the
 * tests can also be triggered via the webpage and HTTP requests.
 *
 * @author Michael Hillman
 */
@Controller
@WebServlet(urlPatterns = {
    StatusAgent.DASHBOARD_URL,
    StatusAgent.SUBMISSION_URL
})
public class StatusAgent extends JPSAgent {

    /**
     * URL path to view dashboard.
     */
    public static final String DASHBOARD_URL = "/dashboard";

    /**
     * URL path to trigger/submit a test.
     */
    public static final String SUBMISSION_URL = "/submit";

    /**
     * Logger for error output.
     */
    private static final Logger LOGGER = LogManager.getLogger(StatusAgent.class);

    /**
     * Handles scheduled execution of tests.
     */
    private static ScheduledExecutorService SCHEDULER;

    /**
     * TestHandler instance.
     */
    private final TestHandler handler = new TestHandler();

    /**
     * Perform required setup.
     *
     * @throws ServletException if in invalid state
     */
    @Override
    public void init() throws ServletException {
        super.init();

        if (SCHEDULER == null) {
            LOGGER.info("StatusAgent object has been created for the first time.");
            
            // Add shutdown hook for debugging
            Thread shutdownHook = new Thread(() -> LOGGER.info("JVM is being shutdown."));
            Runtime.getRuntime().addShutdownHook(shutdownHook);

            // Read the test registry
            Path registryFile = Paths.get(System.getProperty("user.home"), ".jps", "test-registry.json");
            TestRegistry.readRegistryFile(registryFile);

            // Run all tests on boot, then run once per day
            Runnable runnable = () -> {
                try {
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSS");
                    String datetime = dateFormat.format(new Date());
                    LOGGER.info("Running scheduled tests at " + datetime);

                    List<TestRecord> records = handler.runAllTests();
                    if (anyFailures(records)) {
                        LOGGER.info("Not all tests have passed, sending error report...");
                        sendErrorReport(records);
                        LOGGER.info("Error report should now have been sent.");
                    } else {
                        LOGGER.info("All scheduled tests have passed.");
                    }
                } catch (Exception exception) {
                    LOGGER.error("Encountered exception!", exception);
                }
            };
            SCHEDULER = Executors.newScheduledThreadPool(1);
            SCHEDULER.scheduleAtFixedRate(runnable, 0, 1, TimeUnit.DAYS);
        }
    }

    /**
     * Triggers if the scheduled tests result in any failures, send a notification email via the JPS
     * Base Library's EmailSender class.
     *
     * @param records test records
     */
    private void sendErrorReport(List<TestRecord> records) {
        EmailSender sender = new EmailSender();

        String subject = "StatusAgent - Failures Detected";
        String body = "This is an automated notification from the StatusAgent servlet. One (or more) of the regularly "
                + "scheduled tests has reported a failed result.A list of the tests run in the most recent batch, and their results, is shown below."
                + "\n\nPlease view the online dashboard (or the log) of the StatusAgent for more details.\n\n\n";

        for (TestRecord record : records) {
            body += record.getDefinition().getName() + ": ";
            body += (record.getResult()) ? "Success" : "Failed";
            body += "\n";
        }

        try {
            LOGGER.info("Attemping to send notification...");
            sender.sendEmail(subject, body);
            LOGGER.info("...email notification sent?");
        } catch (Exception exception) {
            LOGGER.error("Could not send notification via EmailSender class!", exception);
        }
    }

    /**
     * Main servlet method, overrided from JPSAgent as that framework always expects JSON to be
     * returned.
     *
     * @param request
     * @param response
     * @throws ServletException
     * @throws IOException
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSS");
        String datetime = dateFormat.format(new Date());
        LOGGER.info("New request received at: " + datetime);

        // Get the URL path
        String url = request.getRequestURI();
        url = url.substring(url.lastIndexOf("/"), url.length());
        if (url.contains("?")) url = url.split("?")[0];

        // Run actions depending on the URL path
        StatusRequest requestHandler = null;

        switch (url) {
            // URL pattern to view dashboard
            case DASHBOARD_URL:
                LOGGER.info("Passing request to DashboardRequest instance...");
                requestHandler = new DashboardRequest(request, response, handler);
                break;

            // URL to submit test(s) for execution
            case SUBMISSION_URL:
                LOGGER.info("Passing request to submitRequest instance...");
                requestHandler = new SubmitRequest(request, response, handler);
                break;
        }

        // Process and write result
        if (requestHandler != null) {
            try {
                requestHandler.validateRequest();
                requestHandler.processRequest();

            } catch (BadRequestException brException) {
                response.setStatus(Response.Status.BAD_REQUEST.getStatusCode());
            } catch (Exception exception) {
                LOGGER.error("Unexpected exception thrown!", exception);
                response.setStatus(Response.Status.SERVICE_UNAVAILABLE.getStatusCode());
            }
        } else {
            LOGGER.error("Could not find and run the correct StatusRequest handler");
            response.setStatus(Response.Status.SERVICE_UNAVAILABLE.getStatusCode());
        }
    }

    /**
     * Returns true if any of the input TestRecord instances encountered failures.
     *
     * @param records test records
     * @return true if any failures
     */
    public static boolean anyFailures(List<TestRecord> records) {
        for (TestRecord record : records) {
            if (!record.getResult()) return true;
        }
        return false;
    }

}
// End of class.
