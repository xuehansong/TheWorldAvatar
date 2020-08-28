package uk.ac.cam.cares.jps.agent.kinetics.simulation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.BadRequestException;

import org.slf4j.LoggerFactory;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import uk.ac.cam.cares.jps.agent.configuration.KineticsAgentConfiguration;
import uk.ac.cam.cares.jps.agent.configuration.KineticsAgentProperty;
import uk.ac.cam.cares.jps.agent.utils.ZipUtility;
import uk.ac.cam.cares.jps.base.agent.JPSAgent;
import uk.ac.cam.cares.jps.base.exception.JPSRuntimeException;
import uk.ac.cam.cares.jps.base.slurm.job.JobSubmission;
import uk.ac.cam.cares.jps.base.slurm.job.PostProcessing;
import uk.ac.cam.cares.jps.base.slurm.job.SlurmJobException;
import uk.ac.cam.cares.jps.base.slurm.job.Status;
import uk.ac.cam.cares.jps.base.slurm.job.Utils;
import uk.ac.cam.cares.jps.base.util.FileUtil;

/**
 * Kinetics Agent developed for setting-up and running kinetics simulation jobs.
 *
 * @author Feroz Farazi (msff2@cam.ac.uk)
 *
 */
@Controller
@WebServlet(urlPatterns = {KineticsAgent.JOB_REQUEST_PATH, KineticsAgent.JOB_STATISTICS_PATH,
	KineticsAgent.JOB_OUTPUT_REQUEST_PATH})
public class KineticsAgent extends JPSAgent {

	private static final long serialVersionUID = -8669607645910441935L;
	private Logger logger = LoggerFactory.getLogger(KineticsAgent.class);
	private File workspace;
	static JobSubmission jobSubmission;
	public static ApplicationContext applicationContextKineticsAgent;
	public static KineticsAgentProperty kineticsAgentProperty;

	public static final String BAD_REQUEST_MESSAGE_KEY = "message";
	public static final String UNKNOWN_REQUEST = "The request is unknown to Kinetics Agent";

	public static final String JOB_REQUEST_PATH = "/job/request";
	public static final String JOB_OUTPUT_REQUEST_PATH = "/job/output/request";
	public static final String JOB_STATISTICS_PATH = "/job/statistics";
	public static final String JOB_SHOW_STATISTICS_PATH = "/job/show/statistics";

	/**
	 * Shows the following statistics of quantum jobs processed by Kinetics Agent.</br>
	 * - Total number of jobs submitted - Total number of jobs currently running - Total number of jobs successfully
	 * completed - Total number of jobs terminated with an error - Total number of jobs not started yet
	 *
	 * @param input the JSON string specifying the return data format, e.g. JSON.
	 * @return the statistics in JSON format if requested.
	 */
	public JSONObject produceStatistics(String input) throws IOException, KineticsAgentException {
		System.out.println("Received a request to send statistics.\n");
		logger.info("Received a request to send statistics.\n");
		// Initialises all properties required for this agent to set-up<br>
		// and run jobs. It will also initialise the unique instance of<br>
		// Job Submission class.
		initAgentProperty();
		return jobSubmission.getStatistics(input);
	}

	/**
	 * Shows the following statistics of quantum jobs processed by Kinetics Agent.<br>
	 * This method covers the show statics URL that is not included in the<br>
	 * list of URL patterns.
	 *
	 * - Total number of jobs submitted - Total number of jobs currently running - Total number of jobs successfully
	 * completed - Total number of jobs terminated with an error - Total number of jobs not started yet
	 *
	 * @return the statistics in HTML format.
	 */
	@RequestMapping(value = KineticsAgent.JOB_SHOW_STATISTICS_PATH, method = RequestMethod.GET)
	@ResponseBody
	public String showStatistics() throws IOException, KineticsAgentException {
		System.out.println("Received a request to show statistics.\n");
		logger.info("Received a request to show statistics.\n");
		initAgentProperty();
		return jobSubmission.getStatistics();
	}

	/**
	 * Starts the asynchronous scheduler to monitor quantum jobs.
	 *
	 * @throws KineticsAgentException
	 */
	public void init() throws ServletException {
		logger.info("---------- Kinetics Simulation Agent has started ----------");
		System.out.println("---------- Kinetics Simulation Agent has started ----------");
		ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
		KineticsAgent kineticsAgent = new KineticsAgent();
		// initialising classes to read properties from the kinetics-agent.properites file
		initAgentProperty();
		// In the following method call, the parameter getAgentInitialDelay-<br>
		// ToStartJobMonitoring refers to the delay (in seconds) before<br>
		// the job scheduler starts and getAgentPeriodicActionInterval<br>
		// refers to the interval between two consecutive executions of<br>
		// the scheduler.
		executorService.scheduleAtFixedRate(() -> {
			try {
				kineticsAgent.monitorJobs();
			} catch (SlurmJobException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}, kineticsAgentProperty.getAgentInitialDelayToStartJobMonitoring(),
			kineticsAgentProperty.getAgentPeriodicActionInterval(), TimeUnit.SECONDS);
		logger.info("---------- Kinetics Simulation jobs are being monitored  ----------");
		System.out.println("---------- Kinetics Simulation jobs are being monitored  ----------");

	}

	/**
	 * Initialises the unique instance of the KineticsAgentProperty class that<br>
	 * reads all properties of KineticsAgent from the kinetics-agent property file.<br>
	 *
	 * Initialises the unique instance of the SlurmJobProperty class and<br>
	 * sets all properties by reading them from the kinetics-agent property file<br>
	 * through the KineticsAgent class.
	 */
	private void initAgentProperty() {
		// initialising classes to read properties from the kinetics-agent.properites
		// file
		if (applicationContextKineticsAgent == null) {
			applicationContextKineticsAgent = new AnnotationConfigApplicationContext(KineticsAgentConfiguration.class);
		}
		if (kineticsAgentProperty == null) {
			kineticsAgentProperty = applicationContextKineticsAgent.getBean(KineticsAgentProperty.class);
		}
		if (jobSubmission == null) {
			jobSubmission = new JobSubmission(kineticsAgentProperty.getAgentClass(), kineticsAgentProperty.getHpcAddress());
			jobSubmission.slurmJobProperty.setHpcServerLoginUserName(kineticsAgentProperty.getHpcServerLoginUserName());
			jobSubmission.slurmJobProperty
				.setHpcServerLoginUserPassword(kineticsAgentProperty.getHpcServerLoginUserPassword());
			jobSubmission.slurmJobProperty.setAgentClass(kineticsAgentProperty.getAgentClass());
			jobSubmission.slurmJobProperty
				.setAgentCompletedJobsSpacePrefix(kineticsAgentProperty.getAgentCompletedJobsSpacePrefix());
			jobSubmission.slurmJobProperty
				.setAgentFailedJobsSpacePrefix(kineticsAgentProperty.getAgentFailedJobsSpacePrefix());
			jobSubmission.slurmJobProperty.setHpcAddress(kineticsAgentProperty.getHpcAddress());
			jobSubmission.slurmJobProperty.setInputFileName(kineticsAgentProperty.getInputFileName());
			jobSubmission.slurmJobProperty.setInputFileExtension(kineticsAgentProperty.getInputFileExtension());
			jobSubmission.slurmJobProperty.setOutputFileName(kineticsAgentProperty.getOutputFileName());
			jobSubmission.slurmJobProperty.setOutputFileExtension(kineticsAgentProperty.getOutputFileExtension());
			jobSubmission.slurmJobProperty.setJsonInputFileName(kineticsAgentProperty.getJsonInputFileName());
			jobSubmission.slurmJobProperty.setJsonFileExtension(kineticsAgentProperty.getJsonFileExtension());
			jobSubmission.slurmJobProperty.setJsonFileExtension(kineticsAgentProperty.getJsonFileExtension());
			jobSubmission.slurmJobProperty.setSlurmScriptFileName(kineticsAgentProperty.getSlurmScriptFileName());
			jobSubmission.slurmJobProperty.setMaxNumberOfHPCJobs(kineticsAgentProperty.getMaxNumberOfHPCJobs());
			jobSubmission.slurmJobProperty.setAgentInitialDelayToStartJobMonitoring(
				kineticsAgentProperty.getAgentInitialDelayToStartJobMonitoring());
			jobSubmission.slurmJobProperty
				.setAgentPeriodicActionInterval(kineticsAgentProperty.getAgentPeriodicActionInterval());
		}
	}

	/**
	 * Receives and processes HTTP requests that match with the URL patterns<br>
	 * listed in the annotations of this class.
	 *
	 */
	@Override
	public JSONObject processRequestParameters(JSONObject requestParams, HttpServletRequest request) {
		String path = request.getServletPath();
		System.out.println("A request has been received..............................");
		if (path.equals(KineticsAgent.JOB_REQUEST_PATH)) {
			try {
				return setUpJob(requestParams.toString());
			} catch (SlurmJobException | IOException | KineticsAgentException e) {
				throw new JPSRuntimeException(e.getMessage());
			}
		} else if (path.equals(KineticsAgent.JOB_OUTPUT_REQUEST_PATH)) {
			JSONObject result = getSimulationResults(requestParams);
			return result;
		} else if (path.equals(KineticsAgent.JOB_STATISTICS_PATH)) {
			try {
				return produceStatistics(requestParams.toString());
			} catch (IOException | KineticsAgentException e) {
				throw new JPSRuntimeException(e.getMessage());
			}
		} else {
			System.out.println("Unknown request");
			throw new JPSRuntimeException(UNKNOWN_REQUEST);
		}
	}

	/**
	 * Validates input parameters specific to Kinetics Agent to decide whether<br>
	 * the job set up request can be served.
	 */
	@Override
	public boolean validateInput(JSONObject requestParams) throws BadRequestException {
		if (requestParams.isEmpty()) {
			throw new BadRequestException();
		}
		return true;
	}

	/**
	 * Checks the status of a job and returns results if it is finished and<br>
	 * post-processing is successfully completed. If the job has terminated<br>
	 * with an error or failed, then error termination message is sent.
	 *
	 * The JSON input for this request has the following format: {"jobId":
	 * "login-skylake.hpc.cam.ac.uk_117804308649998"}
	 *
	 * @param requestParams
	 * @return
	 */
	private JSONObject getSimulationResults(JSONObject requestParams) {
		JSONObject json = new JSONObject();
		String jobId = getJobId(requestParams);
		if (jobId == null) {
			return json.put("message", "jobId is not present in the request parameters.");
		}
		initAgentProperty();
		JSONObject message = checkJobInWorkspace(jobId);
		if (message != null) {
			return message;
		}
		JSONObject result = checkJobInCompletedJobs(jobId);
		if (result != null) {
			return result;
		}
		message = checkJobInFailedJobs(jobId);
		if (message != null) {
			return message;
		}
		return json.put("message", "The job no longer exists in the system.");
	}

	/**
	 * Checks the presence of the requested job in the workspace.<br>
	 * If the job is available, it returns that the job is currently running.
	 *
	 * @param json
	 * @return
	 */
	private JSONObject checkJobInWorkspace(String jobId) {
		JSONObject json = new JSONObject();
		// The path to the set-up and running jobs folder.
		workspace = jobSubmission.getWorkspaceDirectory();
		if (workspace.isDirectory()) {
			File[] jobFolders = workspace.listFiles();
			for (File jobFolder : jobFolders) {
				if (jobFolder.getName().equals(jobId)) {
					return json.put("message", "The job is being executed.");
				}
			}
		}
		return null;
	}

	/**
	 * Checks the presence of the requested job in the completed jobs.<br>
	 * If the job is available, it returns the result.
	 *
	 * @param json
	 * @return
	 */
	private JSONObject checkJobInCompletedJobs(String jobId) {
		JSONObject json = new JSONObject();
		// The path to the completed jobs folder.
		String completedJobsPath = workspace.getParent().concat(File.separator)
			.concat(kineticsAgentProperty.getAgentCompletedJobsSpacePrefix()).concat(workspace.getName());
		File completedJobsFolder = new File(completedJobsPath);
		if (completedJobsFolder.isDirectory()) {
			File[] jobFolders = completedJobsFolder.listFiles();
			for (File jobFolder : jobFolders) {
				if (jobFolder.getName().equals(jobId)) {
					try {
						String inputJsonPath = completedJobsPath.concat(File.separator).concat(kineticsAgentProperty.getJsonInputFileName()).concat(kineticsAgentProperty.getJsonFileExtension());
						InputStream inputStream = new FileInputStream(inputJsonPath);
						return new JSONObject(FileUtil.inputStreamToString(inputStream));
					} catch (FileNotFoundException e) {
						return json.put("message", "The job has been completed, but the file that contains results is not found.");
					}
				}
			}
		}
		return null;
	}

	/**
	 * Checks the presence of the requested job in the failed jobs.<br>
	 * If the job is available, it returns a message saying that<br>
	 * job has failed.
	 *
	 * @param json
	 * @param jobId
	 * @return
	 */
	private JSONObject checkJobInFailedJobs(String jobId) {
		JSONObject json = new JSONObject();
		// The path to the failed jobs folder.
		String failedJobsPath = workspace.getParent().concat(File.separator)
			.concat(kineticsAgentProperty.getAgentFailedJobsSpacePrefix()).concat(workspace.getName());
		File failedJobsFolder = new File(failedJobsPath);
		if (failedJobsFolder.isDirectory()) {
			File[] jobFolders = failedJobsFolder.listFiles();
			for (File jobFolder : jobFolders) {
				if (jobFolder.getName().equals(jobId)) {
					return json.put("message",
						"The job terminated with an error. Please check the failed jobs folder.");
				}
			}
		}
		return null;
	}

	/**
	 * Monitors already set up jobs.
	 *
	 * @throws SlurmJobException
	 */
	private void monitorJobs() throws SlurmJobException {
		//Configures all properties required for setting-up and running a Slurm job. 
		jobSubmission.monitorJobs();
		processOutputs();
	}

	/**
	 * Monitors the currently running quantum jobs to allow new jobs to start.</br>
	 * In doing so, it checks if the number of running jobs is less than the</br>
	 * maximum number of jobs allowed to run at a time.
	 *
	 */
	public void processOutputs() {
		workspace = jobSubmission.getWorkspaceDirectory();
		try {
			if (workspace.isDirectory()) {
				File[] jobFolders = workspace.listFiles();
				for (File jobFolder : jobFolders) {

					if (Utils.isJobCompleted(jobFolder) && !Utils.isJobOutputProcessed(jobFolder)) {

						// Name of directory within job folder containing CSV outputs
						Path outputsDir = Paths.get(jobFolder.getAbsolutePath(), "outputs");

						if (!Files.exists(outputsDir)) {
							// Failure
							Utils.modifyStatus(
								Utils.getStatusFile(jobFolder).getAbsolutePath(),
								Status.JOB_LOG_MSG_ERROR_TERMINATION.getName()
							);
							continue;
						}

						// Get the location of the python scripts directory
						Path scriptsDir = Paths.get(kineticsAgentProperty.getAgentScriptsLocation());
						if (!Files.exists(scriptsDir)) throw new IOException("Cannot find python scripts directory at: " + scriptsDir);

						// Build commands for Daniel's postproc script
						List<String> commands = new ArrayList<>();
						commands.add(Paths.get(scriptsDir.toString(), "venv", "Scripts", "agkin_post.exe").toString());

						// Location of job directory
						commands.add("-d");
						commands.add(jobFolder.getAbsolutePath());

						// Run script 
						ProcessBuilder builder = new ProcessBuilder();
						builder.directory(Paths.get(scriptsDir.toString(), "venv", "Scripts").toFile());
						builder.redirectErrorStream(true);
						builder.command(commands);

						// Could redirect the script's output here, looks like a logging system is required first
						Process process = builder.start();

						// Wait until the process is finished (should add a timeout here, expected duration?)
						while (process.isAlive()) {
							try {
								Thread.sleep(500);
							} catch (InterruptedException iException) {
								// Failure
								Utils.modifyStatus(
									Utils.getStatusFile(jobFolder).getAbsolutePath(),
									Status.JOB_LOG_MSG_ERROR_TERMINATION.getName()
								);
							}
						}

						// Check the outputs JSON file
						Path outputsJSON = Paths.get(outputsDir.toString(), "output.json");

						if (!Files.exists(outputsJSON) || Files.readAllBytes(outputsJSON).length <= 0) {
							// Try looking in the job directory directly
							outputsJSON = Paths.get(jobFolder.getAbsolutePath(), "output.json");

							if (!Files.exists(outputsJSON) || Files.readAllBytes(outputsJSON).length <= 0) {
								// No valid output.json, failure
								Utils.modifyStatus(
									Utils.getStatusFile(jobFolder).getAbsolutePath(),
									Status.JOB_LOG_MSG_ERROR_TERMINATION.getName()
								);
								continue;
							}
						}

						// Success
						PostProcessing.updateJobOutputStatus(jobFolder);
					}
				}
			}
		} catch (IOException e) {
			logger.error("KineticsAgent: IOException.".concat(e.getMessage()));
			e.printStackTrace();
		}
	}

	/**
	 * Sets up a quantum job by creating the job folder and the following files</br>
	 * under this folder:</br>
	 * - the input file.</br>
	 * - the Slurm script file.</br. - the Status file.</br> - the JSON input file, which comes from the user
	 * request.</br>
	 *
	 * @param jsonString
	 * @return
	 * @throws IOException
	 * @throws KineticsAgentException
	 */
	public JSONObject setUpJob(String jsonString) throws IOException, KineticsAgentException, SlurmJobException {
		String message = setUpJobOnAgentMachine(jsonString);
		JSONObject obj = new JSONObject();
		obj.put("jobId", message);
		return obj;
	}

	/**
	 * Sets up the quantum job for the current input.
	 *
	 * @param jsonInput
	 * @return
	 * @throws IOException
	 * @throws KineticsAgentException
	 */
	private String setUpJobOnAgentMachine(String jsonInput) throws IOException, KineticsAgentException, SlurmJobException {
		initAgentProperty();
		long timeStamp = Utils.getTimeStamp();
		String jobFolderName = getNewJobFolderName(kineticsAgentProperty.getHpcAddress(), timeStamp);
		return jobSubmission.setUpJob(
			jsonInput, new File(getClass().getClassLoader()
				.getResource(kineticsAgentProperty.getSlurmScriptFileName()).getPath()),
			getInputFile(jsonInput, jobFolderName), timeStamp);
	}

	/**
	 * Prepares input files, bundle them in a zip file and return the zip file to the calling method.
	 *
	 * @param jsonInput
	 * @param jobFolderName
	 * @return
	 * @throws IOException
	 * @throws KineticsAgentException
	 */
	private File getInputFile(String jsonInput, String jobFolderName) throws IOException, KineticsAgentException {

		// Get the location of the python scripts directory
		Path scriptsDir = Paths.get(kineticsAgentProperty.getAgentScriptsLocation());
		if (!Files.exists(scriptsDir)) throw new IOException("Cannot find python scripts directory at: " + scriptsDir);

		// Contains directories for each provided SRM simulation template
		Path templatesDir = Paths.get(scriptsDir.toString(), "simulation_templates");
		if (!Files.exists(templatesDir)) throw new IOException("Cannot find SRM templates directory at: " + templatesDir);

		// Create a temporary folder in the user's home location
		Path destination = Paths.get(System.getProperty("user.home"), "." + jobFolderName);
		try {
			Files.createDirectory(destination);

			// Save JSON raw input to file
			Path dstJSON = Paths.get(destination.toString(), "input.json");
			Files.writeString(dstJSON, jsonInput);

		} catch (IOException ioException) {
			throw new IOException("Could not create temporary directory with JSON file at: " + destination);
		}

		// Build commands for Daniel's preproc script
		List<String> commands = new ArrayList<>();
		commands.add(Paths.get(scriptsDir.toString(), "venv", "Scripts", "agkin_pre.exe").toString());

		// Location of SRM simulation templates folder
		commands.add("-s");
		commands.add("../../simulation_templates");

		// Location of temporary output folder
		commands.add("-d");
		commands.add(destination.toString());

		// Run script 
		ProcessBuilder builder = new ProcessBuilder();
		builder.directory(Paths.get(scriptsDir.toString(), "venv", "Scripts").toFile());
		builder.redirectErrorStream(true);
		builder.command(commands);

		// Could redirect the script's output here, looks like a logging system is required first
		Process process = builder.start();

		// Wait until the process is finished (should add a timeout here, expected duration?)
		while (process.isAlive()) {
			try {
				Thread.sleep(500);
			} catch (InterruptedException iException) {
				throw new KineticsAgentException("Python script process was interrupted!");
			}
		}

		// Compress all files in the temporary directory into a ZIP
		Path zipFile = Paths.get(System.getProperty("user.home"), destination.getFileName().toString() + ".zip");
		List<File> zipContents = new ArrayList<>();

		Files.walk(destination)
			.map(Path::toFile)
			.forEach((File f) -> zipContents.add(f));
		zipContents.remove(destination.toFile());

		// Will throw an IOException if something goes wrong
		new ZipUtility().zip(zipContents, zipFile.toString());

		// Delete the temporary directory
		try {
			// TODO - Consider using the Apache Commons IO library to help here
			Files.walk(destination)
				.sorted(Comparator.reverseOrder())
				.map(Path::toFile)
				.forEach(File::delete);
		} catch (IOException ioException) {
			throw new IOException("Could not delete temporary directory at: " + destination);
		}

		// Return the final ZIP file
		return new File(zipFile.toString());
	}

	/**
	 * Produces a job folder name by following the schema hpcAddress_timestamp.
	 *
	 * @param hpcAddress
	 * @param timeStamp
	 * @return
	 */
	public String getNewJobFolderName(String hpcAddress, long timeStamp) {
		return hpcAddress.concat("_").concat("" + timeStamp);
	}

	/**
	 * Returns the job id.
	 *
	 * @param jsonObject
	 * @return
	 */
	public String getJobId(JSONObject jsonObject) {
		if (jsonObject.has("jobId")) {
			return jsonObject.get("jobId").toString();
		} else {
			return null;
		}
	}
}