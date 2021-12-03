package clit.eval;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.aksw.gerbil.transfer.nif.Document;
import org.aksw.gerbil.transfer.nif.Marking;
import org.aksw.gerbil.transfer.nif.Meaning;

import com.google.common.collect.HashBiMap;
import com.google.common.collect.Lists;
import com.opencsv.CSVWriter;

import clit.eval.datatypes.EvalConstants;
import clit.eval.datatypes.evaluation.BaseMetricContainer;
import clit.eval.explainer.PrecisionRecallF1Explainer;
import clit.eval.interfaces.AnnotationEvaluation;
import clit.translator.TranslatorWikidataToDBpedia;
import clit.translator.TranslatorWikipediaToDBpediaFast;
import experiment.PipelineItem;
import linking.linkers.BabelfyLinker;
import linking.linkers.DBpediaSpotlightLinker;
import linking.linkers.FOXLinker;
import linking.linkers.OpenTapiocaLinker;
import linking.linkers.RadboudLinker;
import linking.linkers.TextRazorLinker;
import structure.datatypes.AnnotatedDocument;
import structure.datatypes.Mention;
import structure.datatypes.PossibleAssignment;
import structure.interfaces.clit.Translator;
import structure.interfaces.linker.Linker;
import structure.utils.NIFUtils;

public class LauncherEvaluatorTester {
	private static final String NONE = "NONE";

	public static void main(String[] args) {
		// Load gold standard
		// NIF Docs --> load from KORE50
		final String[] inPathNIFDatasets = new String[] {
				"C:\\Users\\wf7467\\Desktop\\Evaluation Datasets\\Datasets\\entity_linking\\conll_aida-yago2-dataset\\AIDA-YAGO2-dataset.tsv_nif",
				"C:\\Users\\wf7467\\Desktop\\Evaluation Datasets\\KORE_50_NIF/KORE_50_DBpedia.ttl" };
		final String inPathNIFDataset = inPathNIFDatasets[0];

		final List<Linker> linkers = Lists.newArrayList();
//		linkers.add((new AidaLinker(EnumModelType.DEFAULT));
		linkers.add(new BabelfyLinker());
		linkers.add(new DBpediaSpotlightLinker());
		// linkers.add(new DexterLinker());
//		linkers.add(new EntityClassifierEULinker(EnumModelType.DEFAULT));
		linkers.add(new FOXLinker());
//		linkers.add(new MAGLinker());
		linkers.add(new OpenTapiocaLinker().translator(new TranslatorWikidataToDBpedia()));
		linkers.add(new TextRazorLinker().translator(new Translator() {

			@Override
			public Collection<AnnotatedDocument> execute(PipelineItem callItem, AnnotatedDocument document)
					throws Exception {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public String translate(String entity) {
				// return super.translate(entity);
				if (entity == null) {
					return entity;//super.translate(entity);
				}
				return entity.replace("http://en.wikipedia.org/wiki", "http://dbpedia.org/resource");
			}

			@Override
			public AnnotatedDocument translate(AnnotatedDocument input) {
				return null;
			}}));

		linkers.add(new RadboudLinker());

		new LauncherEvaluatorTester().evaluate(inPathNIFDataset, linkers);
		//executeLinkerOnDocuments(linker, nifDocs);
		/*
		 * What do I want?
		 * 1. execute linkers on specific documents
		 * 2. easily evaluate their results
		 */
	}

	
	public void evaluate(final String inPathNIFDataset, List<Linker> linkers) {

		try {
			//
			final File datasetFile = new File(inPathNIFDataset);
			final String datasetFileNameUnderscores = datasetFile.getName().replace(".", "_");
			final String baseDir = "./";
			final String baseDirLabels = baseDir + "labels/";
			final String labelsDirPath = baseDirLabels + datasetFileNameUnderscores + "/";
			final String outCSVPathPrecision = labelsDirPath + "label_features_precision.csv";
			final String outCSVPathRecall = labelsDirPath + "label_features_recall.csv";
			final String outCSVPathF1 = labelsDirPath + "label_features_f1.csv";
			final String serializationDirectoryPath = baseDir + "serializations/" + datasetFileNameUnderscores + "/";
			final File serializationDirectory = new File(serializationDirectoryPath);
			final File labelsDirectory = new File(labelsDirPath);

			if (!serializationDirectory.exists()) {
				serializationDirectory.mkdirs();
			}
			if (!labelsDirectory.exists()) {
				labelsDirectory.mkdirs();
			}

			final File inFile = new File(inPathNIFDataset);
			final HashMap<Linker, String> mapLinkerSerialization = new HashMap<>();
			final Translator documentTranslator = new TranslatorWikipediaToDBpediaFast();

			// Add all linkers and keep track of potential serialization
			for (Linker linker : linkers) {
				mapLinkerSerialization.put(linker, makePath(linker.getClass(), serializationDirectoryPath));
			}

			if (!inFile.exists()) {
				throw new FileNotFoundException(
						"Could not find the evaluation input file at: " + inFile.getAbsolutePath());
			}

			
			// Read in documents and translate them (e.g. from DBpedia to Wikidata) if needed
			final List<Document> nifDocs = loadDocumentsAndTranslate(inFile, documentTranslator);

			// Keep track of everyone's scores, so we can aggregate them...
			final Map<String, BaseMetricContainer> mapLinkerMetricContainers = new HashMap<>();
			
			for (Linker linker : linkers) {
				// Execute linker on documents
				List<AnnotatedDocument> annotatedDocs = executionAndSerializationHandling(linker, nifDocs, mapLinkerSerialization);

				// Evaluate annotated documents
				final List<AnnotationEvaluation> evaluations = new NIFBaseEvaluator().evaluate(nifDocs, annotatedDocs);

				// printEvaluations(evaluations);

				// Add metrics to a container and keep track of it via map
				final BaseMetricContainer linkerMetricContainer = addMetricsToContainer(evaluations);
				mapLinkerMetricContainers.put(linker.toString(), linkerMetricContainer);

				// Explain evaluations
				final String explanation = new PrecisionRecallF1Explainer().explain(evaluations);
				//System.out.println(explanation);
			}

			// --------------------------------------------------------------------------
			// Now we have gathered all containers... so we can aggregate and output them as
			// we like
			// --------------------------------------------------------------------------
			System.out.println("Evaluations for each system have been gathered (in containers) - time to output them.");
			final List<List<String>> allPrecisions = Lists.newArrayList();
			final List<List<String>> allRecalls = Lists.newArrayList();
			final List<List<String>> allF1 = Lists.newArrayList();
			final HashBiMap<String, Integer> mapLabelIndex = HashBiMap.create(mapLinkerMetricContainers.size());

			int counterCluster = 0;
			for (Map.Entry<String, BaseMetricContainer> e : mapLinkerMetricContainers.entrySet()) {
				mapLabelIndex.put(e.getKey(), counterCluster);
				counterCluster++;

				allPrecisions.add(e.getValue().getPrecisions());
				allRecalls.add(e.getValue().getRecalls());
				allF1.add(e.getValue().getF1());
			}

			final List<Integer> labels = Lists.newArrayList();

			System.out.println("Outputting evaluation info, e.g. Precision, Recall, F1...");
			outputLabels(outCSVPathPrecision, nifDocs, allPrecisions, mapLabelIndex);
			outputLabels(outCSVPathRecall, nifDocs, allRecalls, mapLabelIndex);
			outputLabels(outCSVPathF1, nifDocs, allF1, mapLabelIndex);

			// --------------------------------------------------------------------------
			// Checking whether serialization worked properly and we can deserialize
			// --------------------------------------------------------------------------
			for (Linker linker : linkers) {
				final List<AnnotatedDocument> annotatedDocs = deserialize(mapLinkerSerialization.get(linker));
				System.out.println("Deserialised documents: " + annotatedDocs.size());
			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private List<AnnotatedDocument> executionAndSerializationHandling(final Linker linker, final List<Document> nifDocs, HashMap<Linker, String> mapLinkerSerialization) throws Exception {
		List<AnnotatedDocument> annotatedDocs = null;
		final String annotatedDocsRawPath = mapLinkerSerialization.get(linker);
		// Whether there exists a serialization of these results we can rely on...
		boolean foundAnnotatedDocuments = false;
		if (annotatedDocsRawPath != null
				&& (foundAnnotatedDocuments = new File(annotatedDocsRawPath).exists())) {
			// We have the results --> take results from that (aka. deserialise it)
			System.out.println("[" + linker.toString() + "] Reusing existing results.");
			annotatedDocs = deserialize(annotatedDocsRawPath);
			if (annotatedDocs == null || annotatedDocs.size() == 0) {
				// There is likely sth. wrong... so let's annotate anew and re-serialise!
				foundAnnotatedDocuments = false;
			}
		}
		// What to do when we don't yet have results for this linker instance...
		if (!foundAnnotatedDocuments) {
			System.out.println("[" + linker.toString() + "] Executing linker...");
			// Execute linkers
			annotatedDocs = executeLinkerOnDocuments(linker, nifDocs);
			System.out.println(
					"Serializing[" + linker.toString() + "] to: " + mapLinkerSerialization.get(linker));
			serialize(mapLinkerSerialization.get(linker), annotatedDocs);
		}
		return annotatedDocs;
	}


	private BaseMetricContainer addMetricsToContainer(List<AnnotationEvaluation> evaluations) {
		final BaseMetricContainer linkerMetricContainer = new BaseMetricContainer();
		for (AnnotationEvaluation eval : evaluations) {
			if (eval.getCategory().toLowerCase().contains("document")) {
				// save result for this linker...
				final List<String> precDocVal = eval.getEvaluationMap()
						.getOrDefault(EvalConstants.DOCUMENT_PRECISION, Lists.newArrayList());
				final List<String> recDocVal = eval.getEvaluationMap()
						.getOrDefault(EvalConstants.DOCUMENT_RECALL, Lists.newArrayList());
				final List<String> f1DocVal = eval.getEvaluationMap().getOrDefault(EvalConstants.DOCUMENT_F1,
						Lists.newArrayList());
				linkerMetricContainer.addPrecision(precDocVal);
				linkerMetricContainer.addRecall(recDocVal);
				linkerMetricContainer.addF1(f1DocVal);
			}
		}

		return linkerMetricContainer;
	}


	/**
	 * Loads documents from NIF file and translates all markings appropriately w/ passed Translator instance
	 * @param inFile
	 * @param documentTranslator
	 * @return
	 * @throws FileNotFoundException
	 */
	private List<Document> loadDocumentsAndTranslate(final File inFile, final Translator documentTranslator) throws FileNotFoundException {
		final List<Document> nifDocs = Lists.newArrayList();
		for (Document document : NIFUtils.parseDocuments(inFile)) {
			final AnnotatedDocument translatedDocument = documentTranslator.translate(
					new AnnotatedDocument(document.getText(), NIFUtils.transformMarkings(document.getMarkings())));
			final Collection<Mention> translatedMentions = translatedDocument.getMentions();
			final List<Marking> markings = document.getMarkings();

			// Replace old URIs w/ new URIs in the document instance
			int i = 0;
			for (Mention m : translatedMentions) {
				final Set<String> assignments = new HashSet<>();
				for (final PossibleAssignment assignment : m.getPossibleAssignments()) {
					assignments.add(assignment.getAssignment());
				}
				final Marking marking = markings.get(i);
				((Meaning) marking).setUris(assignments);
				i++;
			}
			nifDocs.add(document);
			// System.out.println(document.getMarkings());
		}
		return nifDocs;
	}


	/**
	 * Outputs labels to a CSV file.
	 * 
	 * @param outCSVPath
	 * @param nifDocs
	 * @param allPrecisions
	 * @param labelCluster
	 * @param labels
	 * @throws IOException
	 */
	private static void outputLabels(String outCSVPath, List<Document> nifDocs, List<List<String>> allPrecisions,
			HashBiMap<String, Integer> labelCluster) throws IOException {
		final File fileCSV = new File(outCSVPath);
		boolean addHeader = true;
		try (final CSVWriter allResultsWriter = new CSVWriter(new FileWriter(fileCSV))) {

			// For each document...
			for (int docCounter = 0; docCounter < nifDocs.size(); ++docCounter) {
				final List<BigDecimal> listPrecision = Lists.newArrayList();
				// For each linker we have... get the precision at a specific index so we can
				// output them together
				for (final List<String> precisionAtIndex : allPrecisions) {
					listPrecision.add(new BigDecimal(precisionAtIndex.get(docCounter)));
				}

				final List<Integer> maxIndices = argmax(listPrecision);
				final int maxIndex = maxIndices.iterator().next();
				final String[] nextLine = new String[listPrecision.size() + 4];
				int index = 0;
				for (int i = 0; index < listPrecision.size(); i++) {
					final BigDecimal prec = listPrecision.get(i);
					nextLine[index++] = prec.toString();
				}
				// best index
				nextLine[index++] = "" + maxIndex;
				// top indices
				nextLine[index++] = maxIndices.toString();
				final String maxIndexName;
				if (maxIndices.size() != 1 && Collections.max(listPrecision).doubleValue() == 0) {
					// low values... so put NONE
					maxIndexName = "NONE";
				} else {
					maxIndexName = labelCluster.inverse().get(maxIndex);
				}

				// entire document as output for consistency...
				final String docText = nifDocs.get(docCounter).getText();
				nextLine[index++] = docText;
				// name of the best index
				nextLine[index++] = maxIndexName;

				// Header - Output information
				if (addHeader) {
					final List<String> header = Lists.newArrayList();
					// Add the metric for the specific linker
					for (int i = 0; i < listPrecision.size(); i++) {
						// final BigDecimal prec = listPrecision.get(i);
						final String linkerName = labelCluster.inverse().get(i);
						header.add(linkerName);
					}
					// best index
					header.add("best index");
					// top indices
					header.add("max. indices");
					// document content...
					header.add("Doc. Content");
					// name of the best index
					header.add("max. index name");

					final String[] headerLine = header.toArray(new String[0]);
					allResultsWriter.writeNext(headerLine);
					addHeader = false;
				}

				allResultsWriter.writeNext(nextLine);
			}
		}
	}

	private static String makePath(Class class1, String baseDir) {
		final String path = baseDir + class1.getName().replace(".", "_") + ".raw";
		System.out.println("Path: " + path);
		return path;
	}

	private static void serialize(final String outPath, final List<AnnotatedDocument> annotatedDocs) {
		try (final ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(new File(outPath)))) {
			oos.writeObject(annotatedDocs);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static List<AnnotatedDocument> deserialize(final String inPath) {
		try (final ObjectInputStream ois = new ObjectInputStream(new FileInputStream(new File(inPath)))) {
			return (List<AnnotatedDocument>) ois.readObject();
		} catch (IOException | ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return Lists.newArrayList();
	}

	private static void printEvaluations(List<AnnotationEvaluation> evaluations) {
		System.out.println("EVALUATIONS");
		for (AnnotationEvaluation eval : evaluations) {
			if (eval.getCategory().toLowerCase().contains("dataset")
					|| eval.getCategory().toLowerCase().contains("document")) {
				System.out.println("------------------------------------[" + eval.getCategory()
						+ "] EVAL ------------------------------------");
				for (Map.Entry<String, List<String>> e : eval.getEvaluationMap().entrySet()) {
					System.out.println(e.getKey() + " -> " + e.getValue());
				}
			}
		}
	}

	/**
	 * Executes linker on passed documents (once per document) and returns the list
	 * of received annotated document instances
	 * 
	 * @param linker  to be executed
	 * @param nifDocs to be run upon
	 * @return list of annotated documents returned from linker
	 * @throws Exception
	 */
	private static List<AnnotatedDocument> executeLinkerOnDocuments(Linker linker, List<Document> nifDocs)
			throws Exception {
		final List<AnnotatedDocument> toEvaluate = Lists.newArrayList();
		for (final Document nifDoc : nifDocs) {
			final String text = nifDoc.getText();
			// Execute annotator
			AnnotatedDocument annotatedDocument = null;
			try {
				System.out.println("[#" + toEvaluate.size() + "] Annotating: " + text);
				annotatedDocument = linker.annotate(new AnnotatedDocument(text));
			} catch (IOException ioe) {
				ioe.printStackTrace();
				annotatedDocument = null;
			}
			// System.out.println("Found entities: " + (annotatedDocument == null ? "null" :
			// annotatedDocument.getMentions() == null ? "null mentions" :
			// annotatedDocument.getMentions()));
			System.out.println("[" + linker.toString() + "] Annotated " + toEvaluate.size() + " documents.");
//		final AnnotatedDocument annotatedDocument = new AnnotatedDocumentBuilder().text(text).mentions(entities)
//				.taskType(EnumPipelineType.FULL).build();
			toEvaluate.add(annotatedDocument);
		}
		return toEvaluate;
	}

	private static List<Integer> argmin(Collection<? extends Comparable> vals) {
		final List<Integer> indices = Lists.newArrayList();
		final Comparable<Object> minVal = Collections.min(vals);
		int i = 0;
		for (Comparable<Object> val : vals) {
			if (val.compareTo(minVal) == 0) {
				indices.add(i);
			}
			i++;
		}
		return indices;
	}

	private static List<Integer> argmax(Collection<? extends Comparable> vals) {
		final List<Integer> indices = Lists.newArrayList();
		final Comparable<Object> maxVal = Collections.max(vals);
		int i = 0;
		for (Comparable<Object> val : vals) {
			if (val.compareTo(maxVal) == 0) {
				indices.add(i);
			}
			i++;
		}
		return indices;
	}

}
