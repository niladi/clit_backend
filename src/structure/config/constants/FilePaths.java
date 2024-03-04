package structure.config.constants;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import structure.config.kg.EnumModelType;

/**
 * Enumeration containing all (constant) file locations the framework utilises
 * (with the exception of properties files due to dependency cycles), handling
 * folder structure generation (if not yet existant) based on defined knowledge
 * graphs (see {@link EnumModelType} for KG locations)
 * 
 * @author Kristian Noullet
 *
 */
public enum FilePaths {
	// Contains all constant paths
	// Attempting to keep a specific order
	//
	// ##################################
	// # MAIN
	// ##################################
	// ##################################
	// DIRECTORIES for src/main
	// ##################################
	DIR_RESOURCE("resources/"), //
	DIR_DATA(DIR_RESOURCE.path + "data/"), //
	DIR_DATASETS(DIR_DATA.path + "datasets/"), //
	DIR_MENTIONS(DIR_DATA.path + "mentions/"), //

	DIR_LOGS(DIR_DATA.path + "logs/"), //
	DIR_BABELFY(DIR_DATA.path + "babelfy/", "Babelfy related files & folders within this folder"), //
	DIR_BABELFY_OUTPUT(DIR_BABELFY.path + "out/", "Babelfy outputs"), //
	DIR_BABELFY_INPUT(DIR_BABELFY.path + "in/", "Babelfy inputs (possible useful in the future)"), //
	// RDF2Vec Walks
	DIR_WALK_GENERATOR(DIR_DATA.path + "walks/", "Walk generation output directory"), //
	// SSP Embeddings output
	DIR_SSP(DIR_DATA.path + "ssp/"), //
	DIR_EMBEDDINGS_SSP_ENTITY_REPRESENTATION(DIR_SSP.path + "representations/",
			"(Potential Deprecated Logic) Directory for entities, each file being one entity's representation"), //

	// ##################################
	// FILES for src/main
	// ##################################
	FILE_API_KEYS("api_keys.properties"), //

	FILE_STOPWORDS(DIR_DATA.path + "stopwords.txt"), //

	FILE_CRUNCHBASE_ENTITIES(DIR_DATA.path + "crunchbase_entities.nt"), //
	FILE_CRUNCHBASE_ENTITIES_TYPED_LITERAL_STRING(DIR_DATA.path + "crunchbase_entities_typed_literal_string.nt"), //
	FILE_CRUNCHBASE_ENTITIES_NOUN_PHRASES(DIR_DATA.path + "crunchbase_sf_noun_phrases.nt"), //
	FILE_CRUNCHBASE_ENTITIES_NOUN_PHRASES_LINKED(DIR_DATA.path + "crunchbase_sf_noun_phrases_linked.nt"), //
	FILE_NEWS_URLS_CONTENT_UNSORTED(DIR_DATA.path + "news_urls_content_unsorted.nt"), //
	FILE_NEWS_URLS_CONTENT_SORTED(DIR_DATA.path + "news_urls_content_sorted.nt"), //
	FILE_TAGTOG_SAMPLE_OUTPUT(DIR_DATA.path + "tagtog_sample.json"), //
	FILE_DUMP_CRUNCHBASE(DIR_DATA.path + "crunchbase-dump-201510.nt"), //
	FILE_CB_NEWS_URL(DIR_DATA.path + "cb_news.txt"), //

	// NPComplete's required tagger file
	FILE_NPCOMPLETE_ENGLISH_TAGGER("./lib/english-left3words-distsim.tagger"), //
	FILE_MENTIONS_BLACKLIST(DIR_MENTIONS.path + "blacklist.txt"), //
	FILE_LSH_DOCUMENT_VECTORS_SPARSE(DIR_MENTIONS.path + "document_vectors_sparse_entries.txt"), //
	FILE_LSH_HASHES(DIR_MENTIONS.path + "hashes.txt"), //
	FILE_PAGERANK(DIR_DATA.path + "pagerank.nt"), //
	FILE_PAGERANK_ADAPTED(DIR_DATA.path + "pagerank_adapted.nt"), //
	// Query-related files
	// FILE_QUERY_INPUT_ORGANIZATION_NAME(DIR_QUERY_IN_SURFACEFORM.path +
	// "literal_organization_name.txt"), //
	// FILE_QUERY_INPUT_ORGANIZATION_ALSOKNOWNAS(DIR_QUERY_IN_SURFACEFORM.path +
	// "literal_organization_also_known_as.txt"), //
	// FILE_QUERY_INPUT_PERSON_ALSOKNOWNAS(DIR_QUERY_IN_SURFACEFORM.path +
	// "literal_person_also_known_as.txt"), //
	// FILE_QUERY_INPUT_PERSON_FIRSTNAME_LASTNAME(DIR_QUERY_IN_SURFACEFORM.path +
	// "literal_person_firstName_lastName.txt"), //
	// FILE_QUERY_INPUT_PERSON_LASTNAME(DIR_QUERY_IN_SURFACEFORM.path +
	// "literal_person_lastName.txt"), //
	// FILE_QUERY_INPUT_PRODUCT_ALSOKNOWNAS(DIR_QUERY_IN_SURFACEFORM.path +
	// "literal_product_also_known_as.txt"), //
	// FILE_QUERY_INPUT_PRODUCT_NAME(DIR_QUERY_IN_SURFACEFORM.path +
	// "literal_product_name.txt"), //
	// Extended RDF graph file
	// extended_graph.nt
	// bmw_graph.nt
	// FILE_EXTENDED_GRAPH(DIR_DATA.path + "rdf_nodocs.nt"), //
	FILE_EXTENDED_GRAPH(DIR_DATA.path + "MAGFieldsOfStudyKG.nt"), //
	// FILE_EXTENDED_GRAPH(DIR_DATA.path + "rdf.nt"), //
	FILE_OUT_HSFURL_MAPPING(DIR_DATA.path + "url_mapping_log.txt",
			"Log file keeping track where each website is stored locally so we can easily retrieve the appropriate URL's contents!"), //

	FILE_TXT_ENTITIES(DIR_DATA.path + "entities.txt", "TXT File containing all entities"), //
	FILE_NT_ENTITIES(DIR_DATA.path + "entities.nt", "NT File containing all entities"), //

	// "Corrected" RDF Graph files
	FILE_KNOWLEDGE_GRAPH(DIR_DATA.path + "kg.nt"), //
	FILE_GRAPH_RDF(DIR_DATA.path + "rdf.ttl"), //
	FILE_GRAPH_RDF_TYPES(DIR_DATA.path + "types.ttl"), //
	// Extended TXT graph file
	FILE_EXTENDED_GRAPH_TEXT(DIR_DATA.path + "extended_graph_text.txt"), //

	// ##################################
	// DATASETS
	// ##################################
	DATASET_SAMPLE(DIR_DATASETS.path + "sample.dataset", false), //
	DATASET_CRUNCHBASE(DIR_DATASETS.path + "crunchbase.dataset", false), //
	DATASET(DIR_DATASETS.path + "graph.dataset", false), //

	// ##################################
	// EVALUATION DATASETS
	// ##################################
	DIR_EVALUATION_DATASETS(DIR_DATA.path + "evaluation_datasets/"), //

	// ##################################
	// # TEST
	// ##################################
	// ##################################
	// DIRECTORIES for src/test
	// ##################################
	DIR_TEST_RDF_PAGERANK(DIR_DATA.path + "rdfpagerank_test/"), //
	DIR_TEST_RDF_PAGERANK_IN(DIR_TEST_RDF_PAGERANK.path + "in/"), //
	DIR_TEST_RDF_PAGERANK_OUT(DIR_TEST_RDF_PAGERANK.path + "out/"), //
	// DIR_TEST_BABELFY(DIR_BABELFY.path + "test/", "Babelfy Test directory"), //
	// DIR_TEST_BABELFY_OUTPUT(DIR_TEST_BABELFY.path + "out/", "Babelfy Test output
	// directory"), //
	// DIR_TEST_BABELFY_INPUT(DIR_TEST_BABELFY.path + "in/", "Babelfy Test input
	// directory"), //

	// ##################################
	// FILES for src/test
	// ##################################
	TEST_FILE_OUT_NEWS_TXT(DIR_DATA.path + "out_#news.txt"), //
	TEST_FILE_NEWS_URLS_IN(DIR_DATA.path + "news_urls_test.nt"), //

	// ##################################
	// DIRECTORY for TRAINING documents
	// ##################################
	TRAINING_FILES(DIR_DATA.path + "TESTING/"), //

	// ##################################
	// DIRECTORY for experiment results (JSON)
	// ##################################
	DIR_EXPERIMENT_RESULTS(DIR_DATA.path + "experiment_results/"), //

	;

	protected final String path;
	private final String val;

	FilePaths(final String path) {
		this(path, true, EnumModelType.DEFAULT);
	}

	FilePaths(final String path, final EnumModelType KG) {
		this(path, true, KG);
	}

	FilePaths(final String path, final boolean initFile) {
		this(path, "", initFile, EnumModelType.DEFAULT);
	}

	FilePaths(final String path, final boolean initFile, final EnumModelType KG) {
		this(path, "", initFile, KG);
	}

	FilePaths(final String path, final String desc) {
		this(path, desc, EnumModelType.DEFAULT);
	}

	FilePaths(final String path, final String desc, final EnumModelType KG) {
		this(path, desc, true, KG);
	}

	FilePaths(final String path, final String desc, final boolean initFile, final EnumModelType KG) {
		if (KG != null) {
			switch (KG) {
			case DEFAULT:
				// Initialise for every KG if it's default one
				for (EnumModelType initKG : EnumModelType.values()) {
					init((initKG.root.endsWith("/") ? initKG.root : initKG.root + "/") + path, initFile);
				}
				break;
			default:
				init((KG.root.endsWith("/") ? KG.root : KG.root + "/") + path, initFile);
				break;
			}
		} else {
			init(path, initFile);
		}
		this.path = path;
		this.val = path;
	}

	public String getPath(EnumModelType KG) {
		return KG.root + this.path;
	}

	private void init(final String path, final boolean initFile) {
		if (initFile) {
			final File file = new File(path);
			try {
				if (!file.exists()) {
					final File parentFile = file.getParentFile();
					boolean parentCreatedSuccessfully = true;
					if (parentFile != null && !parentFile.exists()) {
						parentCreatedSuccessfully = parentFile.mkdirs();
					}

					if (parentCreatedSuccessfully) {
						if (path.endsWith("/")) {
							final boolean created = file.mkdir();
							// System.out.println("Created(" + created + "): " + file.getAbsolutePath());
						} else {
							final boolean created = file.createNewFile();
							// System.out.println("Created(" + created + "): " + file.getAbsolutePath());
						}
					}
				}
			} catch (IOException ioe) {
				// Ignore, just don't do anything
			}
		}
	}

	/**
	 * Loads value from given file in which data is stored line-wise as:<br>
	 * key=value
	 * 
	 * @param key key that a value is stored under
	 * @return
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public String load(final String key) throws FileNotFoundException, IOException {
		final String compositeKey = key + "=";
		try (BufferedReader brIn = new BufferedReader(new FileReader(this.path))) {
			boolean found = false;
			String line = null;
			while ((line = brIn.readLine()) != null) {
				if (line.startsWith(compositeKey)) {
					return line.substring(compositeKey.length());
				}
			}
		}
		return null;
	}

	@Override
	public String toString() {
		return path;
	}
}
