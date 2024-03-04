package clit.eval.launchereval;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import org.aksw.gerbil.transfer.nif.Document;
import org.aksw.gerbil.transfer.nif.Marking;
import org.aksw.gerbil.transfer.nif.Meaning;

import com.google.common.collect.HashBiMap;
import com.google.common.collect.Lists;
import com.opencsv.CSVWriter;

import clit.combiner.UnionCombiner;
import clit.eval.NIFBaseEvaluator;
import clit.eval.datatypes.EvalConstants;
import clit.eval.datatypes.evaluation.BaseMetricContainer;
import clit.eval.datatypes.evaluation.MentionEvaluation;
import clit.eval.interfaces.AnnotationEvaluation;
import clit.translator.TranslatorWikidataToDBpedia;
import clit.translator.TranslatorWikipediaToDBpediaFast;
import experiment.PipelineItem;
import linking.linkers.BabelfyLinker;
import linking.linkers.DBpediaSpotlightLinker;
import linking.linkers.FOXLinker;
import linking.linkers.OpenTapiocaLinker;
import linking.linkers.TextRazorLinker;
import structure.datatypes.AnnotatedDocument;
import structure.datatypes.Mention;
import structure.datatypes.PossibleAssignment;
import structure.interfaces.clit.Combiner;
import structure.interfaces.clit.Translator;
import structure.interfaces.linker.Linker;
import structure.utils.NIFUtils;

public class LauncherEvaluatorTesterMentions {
	private static final String NONE = "NONE";

	public static void main(String[] args) {
		// Load gold standard
		// NIF Docs --> load from KORE50
		final String[] inPathNIFDatasets = new String[] {
				"C:\\Users\\wf7467\\Desktop\\Evaluation Datasets\\Datasets\\entity_linking\\conll_aida-yago2-dataset\\AIDA-YAGO2-dataset.tsv_nif",
				"C:\\Users\\wf7467\\Desktop\\Evaluation Datasets\\Datasets\\entity_linking\\KORE_50_NIF/KORE_50_DBpedia.ttl",
				"C:\\Users\\wf7467\\Desktop\\Evaluation Datasets\\Datasets\\entity_linking\\" + "News-100.ttl",
				"C:\\Users\\wf7467\\Desktop\\Evaluation Datasets\\Datasets\\entity_linking\\" + "RSS-500.ttl",
				"C:\\Users\\wf7467\\Desktop\\Evaluation Datasets\\Datasets\\entity_linking\\" + "Reuters-128.ttl" };
		final String inPathNIFDataset = inPathNIFDatasets[2];

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
					return entity;// super.translate(entity);
				}
				return entity.replace("http://en.wikipedia.org/wiki", "http://dbpedia.org/resource");
			}

			@Override
			public AnnotatedDocument translate(AnnotatedDocument input) {
				return null;
			}
		}));

		// linkers.add(new RadboudLinker());

		new LauncherEvaluatorTesterMentions().evaluate(inPathNIFDataset, linkers);
		// executeLinkerOnDocuments(linker, nifDocs);
		/*
		 * What do I want? 1. execute linkers on specific documents 2. easily evaluate
		 * their results
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
			final String pathSerializationCombinedEvaluations;
			final String pathOutputEvals = baseDir + "evaluations.txt";
			// TODO: Now change annotated document returned by linkers (by union combiner)
			// and add mentions from multiple systems together to see how that would look
			// Display all in a table
			final String linkerStringDelim = " & ";
			final Combiner combiner = new UnionCombiner();// IntersectCombiner();//

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

			// Read in documents and translate them (e.g. from DBpedia to Wikidata) if
			// needed
			final List<Document> nifDocs = loadDocumentsAndTranslate(inFile, documentTranslator);

			int markingsSum = 0;
			for (int i = 0; i < nifDocs.size(); ++i) {
				final Document goldDoc = nifDocs.get(i);
				markingsSum += goldDoc.getMarkings().size();
			}

			// Keep track of everyone's scores, so we can aggregate them...
			final Map<String, BaseMetricContainer> mapLinkerMetricContainers = new HashMap<>();

			final Map<String, List<AnnotatedDocument>> mapLinkerResults = new HashMap<>();
			final Map<String, Integer> mapMentionTextTP = new HashMap<>();
			final Map<String, Integer> mapMentionTextFP = new HashMap<>();
			final Map<String, Integer> mapMentionTextFN = new HashMap<>();

			for (Linker linker : linkers) {
				// Execute linker on documents
				List<AnnotatedDocument> annotatedDocs = executionAndSerializationHandling(linker, nifDocs,
						mapLinkerSerialization);
				// Store so we can have access to the results outside and do fun stuff with them
				mapLinkerResults.put(linkerToKey(linker), annotatedDocs);

				// Evaluate annotated documents
				final List<AnnotationEvaluation> evaluations = new NIFBaseEvaluator().evaluate(nifDocs, annotatedDocs);

				// printEvaluations(evaluations);

				// Add metrics to a container and keep track of it via map
				final BaseMetricContainer linkerMetricContainer = addMetricsToContainer(evaluations);
				mapLinkerMetricContainers.put(linker.toString(), linkerMetricContainer);

				final List<Integer> mentionTruthMetricValues = computeMentionTP_FP_FN(evaluations);
				final int mentionTextTP = mentionTruthMetricValues.get(0);
				final int mentionTextFP = mentionTruthMetricValues.get(1);
				final int mentionTextFN = mentionTruthMetricValues.get(2);

				mapMentionTextTP.put(linkerToKey(linker), mentionTextTP);
				mapMentionTextFP.put(linkerToKey(linker), mentionTextFP);
				mapMentionTextFN.put(linkerToKey(linker), mentionTextFN);
				System.out.println("THIS MENTION");
				System.out.println("Total annotations / mentions in document: (TP + FN =) " + markingsSum);
				System.out.println("TP:" + mentionTextTP + " FN:" + mentionTextFN + " FP:" + mentionTextFP);
				System.out.println("NEXT MENTION");
				System.out.println(
						"--------------------------------------- NEXT LINKER'S EVAL ------------------------------------------");
				// Explain evaluations
				// final String explanation = new
				// PrecisionRecallF1Explainer().explain(evaluations);
				// System.out.println(explanation);
			}

			//
			final int depthToDo = 3
			// mapLinkerResults.keySet().size()
			// 2
			;

			pathSerializationCombinedEvaluations = generateMapEvalSerializationPath(baseDir, depthToDo, combiner);

			// Used simply for output formatting to create figures...
			final String[] linkerPossibilitiesForOutput = new String[] { "Babelfy", "DBpediaSpotlight", "FOX",
					"OpenTapioca", "TextRazor" };

			final boolean pairwiseOrAllPermutations = false;

			final Map<String, List<AnnotationEvaluation>> mapEvaluations;
			if (pairwiseOrAllPermutations) {

				mapEvaluations = combineSystemsPairwise(linkerStringDelim, nifDocs, mapLinkerResults, combiner);
			} else {
				final Map<String, List<AnnotationEvaluation>> newMapEvaluations = new TreeMap<>();
				// final Map<String, List<AnnotationEvaluation>> recursivePermutationEvaluations
				// = combineSystemResultPermutationsRecursive(
				final Map<String, List<AnnotationEvaluation>> tmpMapEvaluations = deserializeMapAnnotationEvaluations(
						pathSerializationCombinedEvaluations);
				if (tmpMapEvaluations == null || tmpMapEvaluations.size() < 1) {
					mapEvaluations = combineSystemResultPermutationsRecursive(depthToDo, new ArrayList<String>(),
							newMapEvaluations, linkerStringDelim, nifDocs, mapLinkerResults, combiner);
					serialize(pathSerializationCombinedEvaluations, mapEvaluations);
				} else {
					mapEvaluations = tmpMapEvaluations;
				}
			}

			// See "how is it when they are ALL combined into one document?"
			final List<AnnotationEvaluation> allCombinedEvaluation = combineSystemsAll(linkerStringDelim, nifDocs,
					mapLinkerResults, combiner);

			// TODO: now we have the evaluations, so let's output interesting stuff
			int mentionTruthMetricIndex = 0;
			// final int mentionTextTP = mentionTruthMetricValues.get(0);
			// final int mentionTextFP = mentionTruthMetricValues.get(1);
			// final int mentionTextFN = mentionTruthMetricValues.get(2);

			final List<Integer> mentionAllCombinedTruthMetricValues = computeMentionTP_FP_FN(allCombinedEvaluation);
			System.out.println("If we just combine them all together: " + mentionAllCombinedTruthMetricValues);
			final Float numPrecision = Float.valueOf(mentionAllCombinedTruthMetricValues.get(0));
			final Float denomPrecision = Float.valueOf(mentionAllCombinedTruthMetricValues.get(0))
					+ Float.valueOf(mentionAllCombinedTruthMetricValues.get(1));
			final Float precision = numPrecision / denomPrecision;
			System.out.println("combined precision: " + precision);
			final Float numRecall = Float.valueOf(mentionAllCombinedTruthMetricValues.get(0));
			final Float denomRecall = Float.valueOf(mentionAllCombinedTruthMetricValues.get(0))
					+ Float.valueOf(mentionAllCombinedTruthMetricValues.get(2));
			final Float recall = numRecall / denomRecall;
			System.out.println("combined recall: " + recall);

			final Float numF1 = 2 * Float.valueOf(mentionAllCombinedTruthMetricValues.get(0));
			final Float denomF1 = 2 * Float.valueOf(mentionAllCombinedTruthMetricValues.get(0))
					+ Float.valueOf(mentionAllCombinedTruthMetricValues.get(1))
					+ Float.valueOf(mentionAllCombinedTruthMetricValues.get(2));
			final Float f1 = numF1 / denomF1;
			System.out.println("combined F1: " + f1);
			// Outputting all metrics, first TP, then FP then FN
			for (mentionTruthMetricIndex = 0; mentionTruthMetricIndex <= 2; ++mentionTruthMetricIndex) {
				int outputCounter = 0;
				for (Entry<String, List<AnnotationEvaluation>> e : mapEvaluations.entrySet()) {
					// final String[] linkerNames = e.getKey().split(linkerStringDelim);
					//
					// final String firstLinker =
					// linkerNames[0].substring(linkerNames[0].lastIndexOf(".") + 1);
					//
					// final String secondLinker =
					// linkerNames[1].substring(linkerNames[1].lastIndexOf(".") + 1);
					final List<Integer> mentionTruthMetricValues = computeMentionTP_FP_FN(e.getValue());
					final int mentionTextMetric = mentionTruthMetricValues.get(mentionTruthMetricIndex);

					// System.out.println(firstLinker + " x " + secondLinker + ": TP[" +
					// mentionTextTP + "] FN ["
					// + mentionTextFN + "] FP[" + mentionTextFP + "]");
					if (outputCounter % linkerPossibilitiesForOutput.length == 0) {
						System.out.println("],");// and output the linker name
						System.out.println(
								"\"" + linkerPossibilitiesForOutput[outputCounter / linkerPossibilitiesForOutput.length]
										+ "\": [");
					}
					outputCounter++;

					System.out.print(mentionTextMetric + ", ");// + " & % " + firstLinker + " x " + secondLinker);
				}
				System.out.println();
				System.out.println();

				// System.out.println("--------------------TABLE END-------------------");
			}
			System.out.println();

			// MD Precision
			System.out.println("Outputting MD precision");
			int outputCounter = 0;
			for (Entry<String, List<AnnotationEvaluation>> e : mapEvaluations.entrySet()) {
				// final String[] linkerNames = e.getKey().split(linkerStringDelim);
				// final String firstLinker =
				// linkerNames[0].substring(linkerNames[0].lastIndexOf(".") + 1);
				// final String secondLinker =
				// linkerNames[1].substring(linkerNames[1].lastIndexOf(".") + 1);
				final List<Integer> mentionTruthMetricValues = computeMentionTP_FP_FN(e.getValue());
				final int mentionTextTP = mentionTruthMetricValues.get(0);
				final int mentionTextFP = mentionTruthMetricValues.get(1);
				final int mentionTextFN = mentionTruthMetricValues.get(2);

				final Float num = Float.valueOf(mentionTextTP);
				final Float denom = Float.valueOf(mentionTextTP) + Float.valueOf(mentionTextFP);

				if (outputCounter % linkerPossibilitiesForOutput.length == 0) {
					// new line
					System.out.println();// and output the linker name
					System.out.println("],");// and output the linker name
					System.out.println(
							"\"" + linkerPossibilitiesForOutput[outputCounter / linkerPossibilitiesForOutput.length]
									+ "\": [");
				}
				outputCounter++;

				// System.out.println(firstLinker + " x " + secondLinker + ": TP[" +
				// mentionTextTP + "] FN ["
				// + mentionTextFN + "] FP[" + mentionTextFP + "]");
				System.out.print(num / denom + ", ");// + " & % " + firstLinker + " x " + secondLinker);
			}
			System.out.println();
			System.out.println();

			// Doing Recall
			System.out.println("Outputting MD recall");
			outputCounter = 0;
			for (Entry<String, List<AnnotationEvaluation>> e : mapEvaluations.entrySet()) {
				// final String[] linkerNames = e.getKey().split(linkerStringDelim);
				// final String firstLinker =
				// linkerNames[0].substring(linkerNames[0].lastIndexOf(".") + 1);
				// final String secondLinker =
				// linkerNames[1].substring(linkerNames[1].lastIndexOf(".") + 1);
				final List<Integer> mentionTruthMetricValues = computeMentionTP_FP_FN(e.getValue());
				final int mentionTextTP = mentionTruthMetricValues.get(0);
				final int mentionTextFP = mentionTruthMetricValues.get(1);
				final int mentionTextFN = mentionTruthMetricValues.get(2);

				if (outputCounter % linkerPossibilitiesForOutput.length == 0) {
					System.out.println();// and output the linker name
					System.out.println("],");// and output the linker name
					System.out.println(
							"\"" + linkerPossibilitiesForOutput[outputCounter / linkerPossibilitiesForOutput.length]
									+ "\": [");
				}
				outputCounter++;

				final Float num = Float.valueOf(mentionTextTP);
				final Float denom = Float.valueOf(mentionTextTP) + Float.valueOf(mentionTextFN);

				// System.out.println(firstLinker + " x " + secondLinker + ": TP[" +
				// mentionTextTP + "] FN ["
				// + mentionTextFN + "] FP[" + mentionTextFP + "]");
				System.out.print(num / denom + ", ");// + " & % " + firstLinker + " x " + secondLinker);
			}
			System.out.println();
			System.out.println();

			// Doing F1
			outputCounter = 0;
			System.out.println("Outputting MD F1");
			for (Entry<String, List<AnnotationEvaluation>> e : mapEvaluations.entrySet()) {
				// final String[] linkerNames = e.getKey().split(linkerStringDelim);
				// final String firstLinker =
				// linkerNames[0].substring(linkerNames[0].lastIndexOf(".") + 1);
				// final String secondLinker =
				// linkerNames[1].substring(linkerNames[1].lastIndexOf(".") + 1);
				final List<Integer> mentionTruthMetricValues = computeMentionTP_FP_FN(e.getValue());
				final int mentionTextTP = mentionTruthMetricValues.get(0);
				final int mentionTextFP = mentionTruthMetricValues.get(1);
				final int mentionTextFN = mentionTruthMetricValues.get(2);

				if (outputCounter % linkerPossibilitiesForOutput.length == 0) {
					System.out.println();// and output the linker name
					System.out.println("],");// and output the linker name
					System.out.println(
							"\"" + linkerPossibilitiesForOutput[outputCounter / linkerPossibilitiesForOutput.length]
									+ "\": [");
				}
				outputCounter++;

				final Float num = 2 * Float.valueOf(mentionTextTP);
				final Float denom = 2 * Float.valueOf(mentionTextTP) + Float.valueOf(mentionTextFP)
						+ Float.valueOf(mentionTextFN);

				// System.out.println(firstLinker + " x " + secondLinker + ": TP[" +
				// mentionTextTP + "] FN ["
				// + mentionTextFN + "] FP[" + mentionTextFP + "]");
				System.out.print(num / denom + ", ");// + " & % " + firstLinker + " x " + secondLinker);
			}
			System.out.println();

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
				final List<AnnotatedDocument> annotatedDocs = deserializeDocuments(mapLinkerSerialization.get(linker));
				System.out.println("Deserialised documents: " + annotatedDocs.size());
			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private String generateMapEvalSerializationPath(String baseDir, int depthToDo, Combiner combiner) {
		return baseDir + "serializations/" + depthToDo + "_" + combiner.getClass().toString() + "_map_evaluations.raw";
	}

	/**
	 * Combines systems' results in a pairwise fashion. <br>
	 * It follows the idea: if we used two systems (out of all the listed systems in
	 * mapEvaluations), what would the results be like?
	 * 
	 * @param linkerStringDelim
	 * @param nifDocs
	 * @param mapLinkerResults
	 * @param combiner
	 * @return
	 */
	public static Map<String, List<AnnotationEvaluation>> combineSystemsPairwise(final String linkerStringDelim,
			final List<Document> nifDocs, Map<String, List<AnnotatedDocument>> mapLinkerResults,
			final Combiner combiner) {
		final Map<String, List<AnnotationEvaluation>> mapEvaluations = new TreeMap<>();
		for (Entry<String, List<AnnotatedDocument>> eMain : mapLinkerResults.entrySet()) {
			final List<AnnotatedDocument> docsMain = eMain.getValue();
			for (Entry<String, List<AnnotatedDocument>> eSub : mapLinkerResults.entrySet()) {
				final List<AnnotatedDocument> docsSub = eSub.getValue();
				final List<AnnotatedDocument> annotatedDocs = Lists.newArrayList();

				// since doing it in a single way is enough, generate a key and check whether
				// results have been added yet
				final boolean sort_key = false;
				final String key;
				if (sort_key) {
					if (eMain.getKey().compareTo(eSub.getKey()) < 0) {
						key = eMain.getKey() + linkerStringDelim + eSub.getKey();
					} else {
						key = eSub.getKey() + linkerStringDelim + eMain.getKey();
					}
				} else {
					key = eMain.getKey() + linkerStringDelim + eSub.getKey();
				}
				// Move on to next if this key already exists
				if (mapEvaluations.get(key) != null) {
					// continue;
				}

				for (int i = 0; i < docsSub.size(); ++i) {
					final List<AnnotatedDocument> toCombine = Lists.newArrayList();
					// docsMain and docsSub have the same size since it's the same annotated
					// documents we are working with...
					// And they are in the same order
					toCombine.add(docsMain.get(i));
					toCombine.add(docsSub.get(i));
					final AnnotatedDocument combinedDoc = combiner.combine(toCombine);
					annotatedDocs.add(combinedDoc);
				}
				final List<AnnotationEvaluation> evaluations = new NIFBaseEvaluator().evaluate(nifDocs, annotatedDocs);
				mapEvaluations.put(key, evaluations);
			}
		}
		return mapEvaluations;
	}

	/**
	 * Recursively generates permutations and combines them, yielding evaluation
	 * results
	 * 
	 * @param depthToDo         how deep to go (aka. how long should each
	 *                          permutation be)
	 * @param keysToDo
	 * @param mapEvaluations
	 * @param linkerStringDelim
	 * @param nifDocs
	 * @param mapLinkerResults
	 * @param combiner
	 * @return
	 */
	public static Map<String, List<AnnotationEvaluation>> combineSystemResultPermutationsRecursive(final int depthToDo,
			final List<String> keysToDo, final Map<String, List<AnnotationEvaluation>> mapEvaluations,
			final String linkerStringDelim, final List<Document> nifDocs,
			Map<String, List<AnnotatedDocument>> mapLinkerResults, final Combiner combiner) {

		if (depthToDo > 0) {
			// loop over all keys to be added
			for (Entry<String, List<AnnotatedDocument>> eMain : mapLinkerResults.entrySet()) {
				// final List<AnnotatedDocument> docsMain = eMain.getValue();
				final String currentKey = eMain.getKey();
				final List<String> newKeys = Lists.newArrayList(keysToDo);
				newKeys.add(currentKey);
				combineSystemResultPermutationsRecursive(depthToDo - 1, newKeys, mapEvaluations, linkerStringDelim,
						nifDocs, mapLinkerResults, combiner);
			}
		}

		// Now do the actual processing of the keys we have been given
		if (keysToDo.size() > 0) {
			final int numberOfDocuments = mapLinkerResults.get(keysToDo.get(0)).size();
			// For each document, we take all the given keys
			final List<AnnotatedDocument> annotatedDocs = Lists.newArrayList();
			for (int i = 0; i < numberOfDocuments; ++i) {
				final List<AnnotatedDocument> toCombine = Lists.newArrayList();
				for (String key : keysToDo) {
					final List<AnnotatedDocument> keyDocuments = mapLinkerResults.get(key);
					final AnnotatedDocument annDoc = keyDocuments.get(i);
					toCombine.add(annDoc);
				}
				final AnnotatedDocument combinedDoc = combiner.combine(toCombine);
				annotatedDocs.add(combinedDoc);
			}

			// Generate key
			String key = keysToDo.get(0);
			for (int i = 1; i < keysToDo.size(); ++i) {
				// String doneKey : keysToDo)
				key += linkerStringDelim + keysToDo.get(i);
			}
			System.out.println("Computed for Key [" + key + "]");
			final List<AnnotationEvaluation> evaluations = new NIFBaseEvaluator().evaluate(nifDocs, annotatedDocs);
			mapEvaluations.put(key, evaluations);
		}

		return mapEvaluations;
	}

	public static List<AnnotationEvaluation> combineSystemsAll(final String linkerStringDelim, final List<Document> nifDocs,
			Map<String, List<AnnotatedDocument>> mapLinkerResults, final Combiner combiner) {
		List<List<AnnotatedDocument>> toCombineAllDocs = Lists.newArrayList();
		List<AnnotatedDocument> annotatedDocs = Lists.newArrayList();
		for (Entry<String, List<AnnotatedDocument>> eMain : mapLinkerResults.entrySet()) {
			final List<AnnotatedDocument> docsMain = eMain.getValue();
			final List<AnnotatedDocument> toCombine = Lists.newArrayList();
			for (int i = 0; i < docsMain.size(); ++i) {
				toCombine.add(docsMain.get(i));
			}
			toCombineAllDocs.add(toCombine);
		}

		for (int i = 0; i < toCombineAllDocs.get(0).size(); ++i) {
			final List<AnnotatedDocument> toCombine = Lists.newArrayList();
			for (int j = 0; j < toCombineAllDocs.size(); ++j) {
				toCombine.add(toCombineAllDocs.get(j).get(i));
			}
			final AnnotatedDocument combinedDoc = combiner.combine(toCombine);
			annotatedDocs.add(combinedDoc);
		}
		final List<AnnotationEvaluation> evaluations = new NIFBaseEvaluator().evaluate(nifDocs, annotatedDocs);
		return evaluations;

	}

	public static List<Integer> computeMentionTP_FP_FN(List<AnnotationEvaluation> evaluations) {
		int mentionTextTP = 0;
		int mentionTextFP = 0;
		int mentionTextFN = 0;

		for (AnnotationEvaluation eval : evaluations) {
			if (eval instanceof MentionEvaluation) {
				final Map<String, List<String>> mentionEvalMap = ((MentionEvaluation) eval).mentionEvaluation;
				final String key1 = "Mention from";
				final String key2 = "Text";
				final String key3 = "Correct";
				// System.out.println(mentionEvalMap.get(key1) + " -> " +
				// mentionEvalMap.get(key2) + " -> " + mentionEvalMap.get(key3));
				if (mentionEvalMap.get(key1).size() > 1) {
					throw new RuntimeException("I fucked up the logic");
				} else {
					final String mentionSource = mentionEvalMap.get(key1).get(0);
					if (mentionSource.equalsIgnoreCase("Both")) {
						// It counts for both!
						mentionTextTP++;
					} else if (mentionSource.equalsIgnoreCase("Linker")) {
						// False positive
						mentionTextFP++;
					} else if (mentionSource.equalsIgnoreCase("Dataset")) {
						// False negative
						mentionTextFN++;
					}
				}
			}
		}
		final List<Integer> ret = Lists.newArrayList();
		ret.add(mentionTextTP);
		ret.add(mentionTextFP);
		ret.add(mentionTextFN);
		return ret;
	}

	public static String linkerToKey(Linker linker) {
		return linker.getClass().toString();
	}

	public static List<AnnotatedDocument> executionAndSerializationHandling(final Linker linker, final List<Document> nifDocs,
			HashMap<Linker, String> mapLinkerSerialization) throws Exception {
		List<AnnotatedDocument> annotatedDocs = null;
		final String annotatedDocsRawPath = mapLinkerSerialization.get(linker);
		// Whether there exists a serialization of these results we can rely on...
		boolean foundAnnotatedDocuments = false;
		if (annotatedDocsRawPath != null && (foundAnnotatedDocuments = new File(annotatedDocsRawPath).exists())) {
			// We have the results --> take results from that (aka. deserialise it)
			System.out.println("[" + linker.toString() + "] Reusing existing results.");
			annotatedDocs = deserializeDocuments(annotatedDocsRawPath);
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
			System.out.println("Serializing[" + linker.toString() + "] to: " + mapLinkerSerialization.get(linker));
			serialize(mapLinkerSerialization.get(linker), annotatedDocs);
		}
		return annotatedDocs;
	}

	public static BaseMetricContainer addMetricsToContainer(List<AnnotationEvaluation> evaluations) {
		final BaseMetricContainer linkerMetricContainer = new BaseMetricContainer();
		for (AnnotationEvaluation eval : evaluations) {
			if (eval.getCategory().toLowerCase().contains("document")) {
				// save result for this linker...
				final List<String> precDocVal = eval.getEvaluationMap().getOrDefault(EvalConstants.DOCUMENT_PRECISION,
						Lists.newArrayList());
				final List<String> recDocVal = eval.getEvaluationMap().getOrDefault(EvalConstants.DOCUMENT_RECALL,
						Lists.newArrayList());
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
	 * Loads documents from NIF file and translates all markings appropriately w/
	 * passed Translator instance
	 * 
	 * @param inFile
	 * @param documentTranslator
	 * @return
	 * @throws FileNotFoundException
	 */
	public static List<Document> loadDocumentsAndTranslate(final File inFile, final Translator documentTranslator)
			throws FileNotFoundException {
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
	public static void outputLabels(String outCSVPath, List<Document> nifDocs, List<List<String>> allPrecisions,
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

	public static void serialize(final String outPath, final Map<String, List<AnnotationEvaluation>> obj) {
		try (final ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(new File(outPath)))) {
			System.out.println("Serializing annotation evaluations");
			oos.writeObject(obj);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static void outputEvaluationsToFile(final Map<String, Serializable> mapEvals,
			final String pathOutputEvals) {
		// try (final BufferedWriter bwOut = new BufferedWriter(new FileOutputStream(new
		// File(pathOutputEvals)))) {
		// for (Entry<String, Serializable> e : mapEvals.entrySet()) {

		// }
		// }
	}

	public static Map<String, List<AnnotationEvaluation>> deserializeMapAnnotationEvaluations(final String inPath) {
		try (final ObjectInputStream ois = new ObjectInputStream(new FileInputStream(new File(inPath)))) {
			System.out.println("Deserialising annotation evaluations");
			return (Map<String, List<AnnotationEvaluation>>) ois.readObject();
		} catch (IOException | ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return new HashMap<String, List<AnnotationEvaluation>>();
	}

	public static void serialize(final String outPath, final List<AnnotatedDocument> annotatedDocs) {
		try (final ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(new File(outPath)))) {
			oos.writeObject(annotatedDocs);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static List<AnnotatedDocument> deserializeDocuments(final String inPath) {
		try (final ObjectInputStream ois = new ObjectInputStream(new FileInputStream(new File(inPath)))) {
			return (List<AnnotatedDocument>) ois.readObject();
		} catch (IOException | ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return Lists.newArrayList();
	}

	public static void printEvaluations(List<AnnotationEvaluation> evaluations) {
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
	public static List<AnnotatedDocument> executeLinkerOnDocuments(Linker linker, List<Document> nifDocs)
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

	public static List<Integer> argmin(Collection<? extends Comparable> vals) {
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

	public static List<Integer> argmax(Collection<? extends Comparable> vals) {
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
