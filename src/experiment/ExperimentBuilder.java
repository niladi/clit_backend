package experiment;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.JSONParser;

import clit.recommender.LinkerRecommender;
import dataset.DatasetStore;
import structure.config.constants.EnumTaskState;
import structure.datatypes.AnnotatedDocument;
import structure.exceptions.PipelineException;
import structure.interfaces.pipeline.PipelineComponent;

/**
 * Given the experiment data from the front-end, the experiment builder builds
 * an experiment having multiple experiment tasks.
 * 
 * @author Samuel Printz
 */
public class ExperimentBuilder {

	/**
	 * Definition of an experiment provided by the front-end.
	 */
	private final String experimentData;

	/**
	 * Connector to file system for retrieving the next experiment ID.
	 */
	private final ExperimentStore experimentStore;

	public ExperimentBuilder(String experimentData) {
		this.experimentData = experimentData;
		this.experimentStore = new ExperimentStore();
	}

	/**
	 * Build an {@link Experiment} consisting of multiple {@link ExperimentTask}
	 * from given experiment data. For each pipeline configuration, one experiment
	 * task is built.
	 * 
	 * This method handles all pipeline exceptions (see {@link PipelineException})
	 * by creating experiment tasks which contain the error message instead of the
	 * actual task. The Experimenter can skip such ExperimentTasks and sends the
	 * error message in a respective {@link ExperimentTask} to the front-end.
	 */
	public Experiment buildExperiment() {
		final Experiment experiment = new Experiment();

		final int experimentId = experimentStore.getNextExperimentId();
		experiment.setExperimentId(experimentId);

		try {
			final Object obj = JSONValue.parse(experimentData);
			final JSONObject configuration = (JSONObject) obj;

			final JSONArray jsonInputTexts = (JSONArray) configuration.get("inputTexts");
			final String[] inputTexts = readInputTexts(jsonInputTexts);

			final JSONArray pipelineConfigsJson = (JSONArray) configuration.get("linkerConfigs");
			final Collection<JSONObject> pipelineConfigs;
			final Class<? extends PipelineComponent> recommendedLinker;
			if (pipelineConfigsJson == null || pipelineConfigsJson.size() == 0) {
				// we did not get any configuration... so let's just use our preferred standard
				// linker as a config!
				System.out.println("Recommending best linker for document instead");
				if (inputTexts != null && inputTexts.length > 0) {
					recommendedLinker = new LinkerRecommender().recommend(inputTexts[0]);
					System.out.println("Found recommendation: " + recommendedLinker.getName());
				} else {
					recommendedLinker = null;
				}
				// For the specified class... get the appropriate name we can feed into the JSON
				if (recommendedLinker != null) {

				}

				String recommendedLinkerName = null;
				String someLinkerName = null;
				for (Map.Entry<String, Class<? extends PipelineComponent>> e : ExperimentSettings
						.getLinkerClassesCaseInsensitive().entrySet()) {
					if (e.getValue().equals(recommendedLinker)) {
						// found the right key...
						recommendedLinkerName = e.getKey();
					} else {
						someLinkerName = e.getKey();
					}

				}

				final String linkerNameToInstantiate;
				if (recommendedLinkerName != null) {
					// use the recommendation
					linkerNameToInstantiate = recommendedLinkerName;
				} else if (someLinkerName != null) {
					// Recommendation failed - backing up by using some other...
					linkerNameToInstantiate = someLinkerName;
				} else {
					throw new RuntimeException(
							"Logical error - messed up linker recommendation and backup did not work (aka. could not find a single linker to recommend)");
				}

				final JSONParser parser = new JSONParser();
				final String jsonLinkerString = "[{\"id\":1, \"pipelineConfigType\":\"standard\", \"linker\":\""
						+ linkerNameToInstantiate + "\"}]";
				System.out.println("Using pipeline config: " + jsonLinkerString);
				final JSONArray pipelineConfigsJsonDodge = (JSONArray) parser.parse(jsonLinkerString);

				pipelineConfigs = readPipelineConfigs(pipelineConfigsJsonDodge);
			} else {
				pipelineConfigs = readPipelineConfigs(pipelineConfigsJson);
			}

			final JSONArray jsonDataset = (JSONArray) configuration.get("datasets");
			final String[] datasetNames = readDatasets(jsonDataset);

			// create documents by either reading datasets or using custom texts
			final Collection<Collection<Collection<AnnotatedDocument>>> datasets = createDocuments(datasetNames,
					inputTexts);

			// create one ExperimentTask for each dataset and each pipeline config
			for (Collection<Collection<AnnotatedDocument>> dataset : datasets) {
				for (JSONObject pipelineConfig : pipelineConfigs) {
					final int taskId = (int) (long) pipelineConfig.get("id"); // TODO
					final ExperimentTask experimentTask = new ExperimentTask(experimentId, taskId, dataset,
							pipelineConfig, EnumTaskState.BUILD);
					experiment.addExperimentTask(experimentTask);
				}
			}

		} catch (PipelineException e) {
			final ExperimentTask experimentTask = new ExperimentTask(experimentId, -1, null, e.getMessage(), "");
			experiment.addExperimentTask(experimentTask);
			e.printStackTrace();
		} catch (Exception e) {
			String errorMessage = e.getMessage();
			if (e.getMessage() == null) {
				final StringWriter sw = new StringWriter();
				e.printStackTrace(new PrintWriter(sw));
				errorMessage = sw.toString();
			}
			final ExperimentTask experimentTask = new ExperimentTask(experimentId, -1, null, errorMessage, "");
			experiment.addExperimentTask(experimentTask);
			e.printStackTrace();
		}

		return experiment;
	}

	/**
	 * Read the pipeline configurations from the JSON.
	 */
	private Collection<JSONObject> readPipelineConfigs(JSONArray pipelineConfigsJson) throws PipelineException {
		if (pipelineConfigsJson == null || pipelineConfigsJson.size() == 0)
			throw new PipelineException("No pipeline/linker specified. Please configure at least one.");

		Collection<JSONObject> pipelineConfigs = new ArrayList<>();
		for (Object pipelineConfigObj : pipelineConfigsJson) {
			if (!(pipelineConfigObj instanceof JSONObject))
				throw new PipelineException("Could not read pipeline configuration: " + pipelineConfigObj.toString());
			final JSONObject pipelineConfig = (JSONObject) pipelineConfigObj;
			pipelineConfigs.add(pipelineConfig);
		}
		return pipelineConfigs;
	}

	/**
	 * Read dataset names from JSON
	 */
	private String[] readDatasets(JSONArray jsonDataset) {
		if (jsonDataset == null || jsonDataset.size() == 0)
			return null;

		String[] datasets = new String[jsonDataset.size()];
		for (int i = 0; i < jsonDataset.size(); i++) {
			datasets[i] = (String) jsonDataset.get(i);
		}

		System.out.println("Datasets: " + Arrays.toString(datasets));
		return datasets;
	}

	/**
	 * Read input texts from JSON
	 */
	private String[] readInputTexts(JSONArray jsonInputTexts) throws PipelineException {
		if (jsonInputTexts == null || jsonInputTexts.size() == 0)
			return null;

		String[] inputTexts = new String[jsonInputTexts.size()];
		for (int i = 0; i < jsonInputTexts.size(); i++) {
			inputTexts[i] = (String) jsonInputTexts.get(i);
		}

		System.out.println(inputTexts.length + " input texts");
		return inputTexts;
	}

	/**
	 * Create one list for each dataset. A dataset consists of a list of documents.
	 * A document consists of another list for its intermediate results. If no
	 * datasets but only input texts are specified, one dataset containing all input
	 * texts is created (with the same nesting as above).
	 */
	private Collection<Collection<Collection<AnnotatedDocument>>> createDocuments(String[] datasetNames,
			String[] inputTexts) throws PipelineException {
		Collection<Collection<Collection<AnnotatedDocument>>> datasets = new ArrayList<>();

		if (datasetNames == null && inputTexts == null) {
			throw new PipelineException("No input was specified");

		} else if (datasetNames != null && inputTexts != null) {
			throw new PipelineException("Both a dataset and an input text were specified");

		} else if (inputTexts != null && datasetNames == null) {
			// input is a text
			Collection<Collection<AnnotatedDocument>> dataset = new ArrayList<>();
			for (String inputText : inputTexts) {
				AnnotatedDocument document = new AnnotatedDocument(inputText);
				ArrayList<AnnotatedDocument> resultDocuments = new ArrayList<>(); // for intermediate results
				resultDocuments.add(document);
				dataset.add(resultDocuments);
			}
			datasets.add(dataset);

		} else if (datasetNames != null && inputTexts == null) {
			// input is a dataset
			for (String datasetName : datasetNames) {
				Collection<Collection<AnnotatedDocument>> dataset = new ArrayList<>();
				ArrayList<AnnotatedDocument> dataset2 = new DatasetStore().readDatasetAsAnnotatedDocument(datasetName);
				for (AnnotatedDocument document : dataset2) {
					ArrayList<AnnotatedDocument> resultDocuments = new ArrayList<>(); // for intermediate results
					resultDocuments.add(document);
					dataset.add(resultDocuments);
				}
				datasets.add(dataset);
			}
		}

		return datasets;
	}
}
