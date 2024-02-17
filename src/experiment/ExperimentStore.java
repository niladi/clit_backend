package experiment;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.commons.io.FilenameUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import structure.config.constants.FilePaths;
import structure.config.kg.EnumModelType;

public class ExperimentStore {

	private EnumModelType KG;
	private String path;

	// private static final String KEY_EXPERIMENT_ID = "experimentId";
	// private static final String KEY_TASK_ID = "taskId";
	// private static final String KEY_PIPELINE_TYPE = "pipelineType";
	// private static final String KEY_PIPELINE_CONFIG = "pipelineConfig";
	// private static final String KEY_DATASET = "dataset";
	// private static final String KEY_KNOWLEDGE_BASE = "knowledgeBase";
	// private static final String KEY_DOCUMENT = "document";
	// private static final String KEY_SUCCEEDED = "succeeded";
	// private static final String KEY_ERROR_MESSAGE = "errorMessage";
	// private static final String KEY_TEXT = "text";
	// private static final String KEY_MENTIONS = "mentions";
	// private static final String KEY_MENTION_TEXT = "mention";
	// private static final String KEY_MENTION_ASSIGNMENT = "assignment";
	// private static final String KEY_MENTION_OFFSET = "offset";
	// private static final String KEY_MENTION_DETECTION_CONFIDENCE =
	// "detectionConfidence";
	// private static final String KEY_MENTION_POSSIBLE_ASSIGNMENTS =
	// "possibleAssignments";
	// private static final String KEY_MENTION_ORIGINAL_MENTION = "originalMention";
	// private static final String KEY_MENTION_ORIGINAL_WITHOUT_STOPWORDS =
	// "originalWithoutStopwords";

	public ExperimentStore() {
		KG = EnumModelType.DEFAULT;
		path = FilePaths.DIR_EXPERIMENT_RESULTS.getPath(KG);
	}

	/**
	 * Write the results of an experiments to a JSON file.
	 * 
	 * @param path
	 * @param results
	 */
	public void writeExperimentResultToJsonFile(Experiment experimentResult, String[] path) {
		ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
		try {
			String json = ow.writeValueAsString(experimentResult);
			FileWriter file = new FileWriter(getFile(path));
			System.out.println(getFile(path).getAbsolutePath());
			file.write(json.toString());
			file.flush();
			file.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Write the results of an experiments to a JSON file.
	 * 
	 * @param path
	 * @param results
	 */
	public void writeExperimentResultToJsonFile(Experiment experimentResult) {
		int experimentId = experimentResult.getExperimentId();
		String[] path = { String.valueOf(experimentId) };
		this.writeExperimentResultToJsonFile(experimentResult, path);
	}

	/**
	 * Read experiment results from JSON file and return the JSON.
	 * 
	 * @param id ID of the experiment
	 */
	public JSONObject readExperimentResultAsJson(final int id) throws IOException, ParseException {
		String[] path = { String.valueOf(id) };
		return readExperimentResultAsJson(getFile(path));
	}

	/**
	 * Read experiment results from JSON file and return the JSON.
	 * 
	 * @param filepath path of the experiment file
	 */
	public JSONObject readExperimentResultAsJson(final String filepath) throws IOException, ParseException {
		return readExperimentResultAsJson(new File(filepath));
	}

	/**
	 * Read experiment results from JSON file and return the JSON.
	 * 
	 * @param file Experiment file
	 */
	public static JSONObject readExperimentResultAsJson(final File file) throws IOException, ParseException {
		// TODO Jackson
		JSONParser jsonParser = new JSONParser();
		FileReader reader = new FileReader(file);
		Object obj = jsonParser.parse(reader);
		JSONObject experimentResultsJson = (JSONObject) obj;
		return experimentResultsJson;
	}

	public static JSONObject createErrorResult(int experimentId, String errorMessage) {
		Experiment result = new Experiment(experimentId);
		ExperimentTask experimentTask = new ExperimentTask(experimentId, errorMessage);
		result.addExperimentTask(experimentTask);

		// TODO Jackson
		JSONObject resultJson = new JSONObject();
		ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
		try {
			String jsonStr = ow.writeValueAsString(result);
			JSONParser parser = new JSONParser();
			resultJson = (JSONObject) parser.parse(jsonStr);
		} catch (IOException | ParseException e) {
			e.printStackTrace();
			// TODO Add results with other error message
		}
		return resultJson;
	}

	/**
	 * Return the path of the JSON file.
	 * 
	 * @param path path of the results
	 */
	public File getFile(String[] outputPath) {
		if (outputPath.length == 1)
			return new File(path + outputPath[0] + ".json");
		String dirs = "";
		for (int i = 0; i < outputPath.length - 1; ++i) {
			dirs += outputPath[i] + "/";
		}
		File directory = new File(path + dirs);
		directory.mkdirs();
		File file = new File(directory, outputPath[outputPath.length - 1] + ".json");
		return file;
	}

	/**
	 * Searches for existing experiment result files and returns the next free ID.
	 * 
	 * @return the next free experiment ID
	 */
	public int getLastExperimentId() {
		int lastId = 0;

		// read existing files
		File folder = new File(path);
		File[] listOfFiles = folder.listFiles();

		// find highest id
		if (listOfFiles != null) {
			for (int i = 0; i < listOfFiles.length; i++) {
				if (listOfFiles[i].isFile()) {
					String filename = listOfFiles[i].getName();
					String filenameWithoutExt = FilenameUtils.removeExtension(filename);
					try {

						int id = Integer.parseInt(filenameWithoutExt);
						if (id > lastId) {
							lastId = id;
						}
					} catch (NumberFormatException e) {
						continue;
					}
				}
			}
		}

		return lastId;
	}

	/**
	 * Get the next unused experiment ID, i.e. the last ID + 1.
	 * 
	 * @return a free experiment ID
	 */
	public int getNextExperimentId() {
		int lastExperimentId = getLastExperimentId();
		int nextExperimentId = lastExperimentId + 1;
		return nextExperimentId;
	}

}
