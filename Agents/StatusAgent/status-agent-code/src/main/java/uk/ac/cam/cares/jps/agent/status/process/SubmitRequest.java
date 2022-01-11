package uk.ac.cam.cares.jps.agent.status.process;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.BadRequestException;
import org.json.JSONObject;
import uk.ac.cam.cares.jps.agent.status.StatusAgent;
import uk.ac.cam.cares.jps.agent.status.TestHandler;
import uk.ac.cam.cares.jps.agent.status.record.TestRecord;

/**
 * Handles generation of response for requests to view the main dashboard.
 *
 * @author Michael Hillman
 */
public class SubmitRequest extends StatusRequest {

    /**
     * Initialise a new DashboardRequest instance.
     *
     * @param request HTTP request.
     * @param response HTTP response.
     * @param handler Test handler instance.
     */
    public SubmitRequest(HttpServletRequest request, HttpServletResponse response, TestHandler handler) {
        super(request, response, handler);
    }

    /**
     * Act on the request, setting attributes and forwarding to the dashboard.jsp page.
     */
    @Override
    public void processRequest() throws ServletException, IOException {
        JSONObject parameters = super.parseRequestJSON();

        String testName = parameters.getString("NAME");
        String testType = parameters.getString("TYPE");

        List<TestRecord> records = new ArrayList<>();
        if (testName.equals("ALL") || testType == "ALL") {
            // Run all tests
            records = handler.runAllTests();
        } else {
            // Run single test case
            records.add(handler.runTest(testName, testType));
        }

        if (!StatusAgent.anyFailures(records)) {
            response.getWriter().write("<html>Test submitted successfully.</html>");
            response.setStatus(200);
        } else {
            response.getWriter().write("<html>Test could not be submitted!</html>");
            response.setStatus(503);

        }
        response.getWriter().close();
        response.setContentType("text/html;charset=UTF-8");
    }

    /**
     * Validate the request.
     *
     * @throws BadRequestException
     */
    @Override
    public void validateRequest() throws BadRequestException {
        // All requests are valid here.
    }

}
// End of class.
