package clit.eval.launchereval;

import java.io.File;
import java.io.IOException;
import java.rmi.UnexpectedException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;

import experiment.Experiment;
import experiment.ExperimentStore;
import structure.datatypes.AnnotatedDocument;

public class LauncherAnalyseSystems {

	public static void main(String[] args) {
		final String dir = "C:\\Users\\wf7467\\Desktop\\Evaluation Datasets\\Datasets\\CLiT_executions\\MDOnly";
		final Set<String> datasets = new HashSet<>();
		final Set<String> systems = new HashSet<>();
		final Set<String> documents = new HashSet<>();

		try {

			final Map<String, JSONObject> resultMap = new LauncherAnalyseSystems().load_results(dir, datasets, systems, documents);
			final Map<String, AnnotatedDocument> mentionMap = extractMentionsFromResults(resultMap);
		} catch (IOException | ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static Map<String, AnnotatedDocument> extractMentionsFromResults(Map<String, JSONObject> resultMap) throws JsonMappingException, JsonProcessingException {
		final Map<String, AnnotatedDocument> mentionMap = new HashMap<>();
		for (Map.Entry<String, JSONObject> e : resultMap.entrySet()) {
			final JSONObject jsonDoc = e.getValue();
			// Now transform JSON into AnnotatedDocument
			final ObjectReader or = new ObjectMapper().reader();//.readerFor(Experiment.class);
			Experiment experiment = or.readValue("C:\\Users\\wf7467\\Desktop\\Evaluation Datasets\\Datasets\\CLiT_executions\\MDOnly\\AIDA-YAGO2-dataset.tsv_nif\\Babelfy\\00b2d646a8d7cb4cf8e4119616bbf6566dbd4dbe341fb3f110924628f33614c5.json");
			System.out.println("Read experiment successfully!");
			// jsonDoc.get
		}
		return mentionMap;
	}

	/**
	 * Loads all results and throws them into a map
	 * 
	 * @param dir       directory where the structure is: DIR > DATASETS > SYSTEMS >
	 *                  DOCUMENTS w/ Results & Metadata (JSON)
	 * @param documents
	 * @param systems
	 * @param datasets
	 * @return
	 * @throws IOException
	 * @throws ParseException
	 */
	public Map<String, JSONObject> load_results(final String dir, Set<String> datasets, Set<String> systems,
			Set<String> documents) throws IOException, ParseException {
		final Map<String, JSONObject> resultMap = new HashMap<>();
		final File[] dataset_folders = new File(dir).listFiles(File::isDirectory);
		for (File dataset : dataset_folders) {
			// Keep track of each dataset name for retrieval
			datasets.add(dataset.getName());
			// For each dataset...
			final Map<String, JSONObject> mapDataset = load_results_for_dataset(dataset, systems, documents);

			// Making sure as not to overwrite values within map accidentally...
			if (!checkDuplicates(mapDataset, resultMap)) {
				// There is no intersection, as such we simply merge the two maps simply.
				resultMap.putAll(mapDataset);
			}
		}
		return resultMap;

	}

	public Map<String, JSONObject> load_results_for_dataset(final File dataset, Set<String> systems,
			Set<String> documents) throws IOException, ParseException {
		final Map<String, JSONObject> resultDatasetMap = new HashMap<>();

		final File[] system_folders = dataset.listFiles(File::isDirectory);
		for (File system : system_folders) {
			// Keep track of each system/linker name for retrieval
			systems.add(system.getName());
			// Load all results (aka. all documents) for this particular system
			final Map<String, JSONObject> resultSystemMap = load_results_for_system(dataset.getName(), system, systems,
					documents);
			if (!checkDuplicates(resultSystemMap, resultDatasetMap)) {
				// There is no intersection, as such we simply merge the two maps simply.
				resultDatasetMap.putAll(resultSystemMap);
			}

		}

		return resultDatasetMap;
	}

	/**
	 * Loads all result documents within a specific system folder within a specific
	 * dataset folder folder
	 * 
	 * @param datasetName
	 * @param system
	 * @param systems
	 * @param documents
	 * @return
	 * @throws IOException
	 * @throws ParseException
	 */
	private Map<String, JSONObject> load_results_for_system(final String datasetName, final File system,
			Set<String> systems, Set<String> documents) throws IOException, ParseException {
		final Map<String, JSONObject> resultSystemMap = new HashMap<>();
		final File[] document_json_files = system.listFiles(File::isFile);
		for (File document : document_json_files) {
			// Keep track of each document name for retrieval
			documents.add(document.getName());
			// Read the JSON document
			final JSONObject result = ExperimentStore.readExperimentResultAsJson(document);
			// Generate unique key for map retrieval
			final String key = createDocumentKey(datasetName, system.getName(), document.getName());
			// Check for inconsistencies / duplicate
			if (resultSystemMap.get(key) != null) {
				throw new UnexpectedException(
						"Combination of dataset, system and document is not unique. Better look into it! (key = " + key
								+ ")");
			} else {
				// This entry does not yet exist, so let's put it in
				resultSystemMap.put(key, result);
			}
		}
		return resultSystemMap;
	}

	/**
	 * Checks for dupicate keys to prevent overwriting, logical inconsistencies and
	 * data loss
	 * 
	 * @param mapDataset
	 * @param resultMap
	 * @return
	 * @throws UnexpectedException
	 */
	private boolean checkDuplicates(Map<String, JSONObject> mapDataset, Map<String, JSONObject> resultMap)
			throws UnexpectedException {
		final Set<String> intersection = new HashSet<>(resultMap.keySet());
		intersection.retainAll(new HashSet<>(mapDataset.keySet()));
		if (intersection.size() > 0) {
			throw new UnexpectedException(
					"Combination of dataset and overall map: key for dataset & system & document is not unique. Better look into it! (Intersection: "
							+ intersection + ")");
			// return true;
		} else {
			return false;
		}
	}

	private String createDocumentKey(String name, String name2, String name3) {
		return name + " > " + name2 + " > " + name3;
	}
}
