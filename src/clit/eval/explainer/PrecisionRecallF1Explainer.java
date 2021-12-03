package clit.eval.explainer;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.commons.text.similarity.LevenshteinDistance;

import com.google.common.collect.Lists;

import clit.eval.datatypes.EvalConstants;
import clit.eval.interfaces.AnnotationEvaluation;
import clit.eval.interfaces.Explainer;
import smile.clustering.DBScan;
import smile.math.distance.Distance;

public class PrecisionRecallF1Explainer implements Explainer {

	@Override
	public String explain(final List<AnnotationEvaluation> evaluations) {
		final Map<String, List<String>> docEvaluations = gatherDocumentEvaluations(evaluations);
		final List<String> featureKeys = getFeatureKeys(docEvaluations);

		final String precisionKey = featureKeys.get(0);
		final String recallKey = featureKeys.get(1);
		final String f1Key = featureKeys.get(2);
		final List<String> listPrecision = docEvaluations.get(precisionKey);
		final List<String> listRecall = docEvaluations.get(recallKey);
		final List<String> listF1 = docEvaluations.get(f1Key);

		// Now we have lists of precision, recall and F1 measures respectively
		return explain(listPrecision, listRecall, listF1);
	}

	private List<String> getFeatureKeys(Map<String, List<String>> docEvaluations) {
		final Set<String> featuresEvaluation = docEvaluations.keySet();
		// check with levenhstein distance if they are close together, if so --> it's
		// that, else... nope, not present
		final String[] precisionPossibleFeatureKeys = new String[] { "precision", "prec",
				EvalConstants.DATASET_PRECISION, EvalConstants.DOCUMENT_PRECISION };
		final String[] recallPossibleFeatureKeys = new String[] { "recall", "rec", EvalConstants.DATASET_RECALL,
				EvalConstants.DOCUMENT_RECALL };
		final String[] f1PossibleFeatureKeys = new String[] { "f1", "f1-measure", EvalConstants.DATASET_F1,
				EvalConstants.DOCUMENT_F1 };
		List<String> featureKeys = getFeatureKeys(featuresEvaluation, precisionPossibleFeatureKeys,
				recallPossibleFeatureKeys, f1PossibleFeatureKeys);
		if (featureKeys.size() > 3) {
			throw new RuntimeException(
					"Received more than 3 features... It's supposed to only be precision, recall and F1 measure, instead: "
							+ featureKeys.toString());
		}
		return featureKeys;
	}

	private Map<String, List<String>> gatherDocumentEvaluations(List<AnnotationEvaluation> evaluations) {
		final Map<String, List<String>> retMap = new HashMap<>();
		for (AnnotationEvaluation evaluation : evaluations) {
			if (evaluation.getCategory().toLowerCase().contains("document")) {
				final Map<String, List<String>> evalMap = evaluation.getEvaluationMap();
				for (Map.Entry<String, List<String>> e : evalMap.entrySet()) {
					List<String> val = null;
					if ((val = retMap.get(e.getKey())) == null) {
						val = Lists.newArrayList();
						retMap.put(e.getKey(), val);
					}
					val.addAll(e.getValue());
				}
			}
		}
		return retMap;
	}

	private String explain(final List<String> listPrecision, final List<String> listRecall, final List<String> listF1) {
		// Check if they are evenly distributed across the various readings or if there
		// is sth we may be able to extract

		// VARIANCE
		final BigDecimal varPrecision = variance(listPrecision);
		final BigDecimal varRecall = variance(listRecall);
		final BigDecimal varF1 = variance(listF1);

		System.out.println("[Precision] Variance: " + varPrecision);
		System.out.println("[Recall] Variance: " + varRecall);
		System.out.println("[F1] Variance: " + varF1);

		// CLUSTERING over precision/recall/F1 - to see how disparate the data actually
		// is...
		final int minPts = 2;
		final float radius = 0.05f;
		final Distance<String> distance = new Distance<String>() {

			@Override
			public double d(String x, String y) {
				return Math.abs(new BigDecimal(x).doubleValue() - new BigDecimal(y).doubleValue());
				// return Math.abs(x.doubleValue() - y.doubleValue());
			}
		};

		// CLUSTERs for the respective metrics
		final SortedMap<Integer, List<Integer>> clusterIndexMapPrecision = getClusters(listPrecision, distance, minPts,
				radius);
		final SortedMap<Integer, List<Integer>> clusterIndexMapRecall = getClusters(listRecall, distance, minPts,
				radius);
		final SortedMap<Integer, List<Integer>> clusterIndexMapF1 = getClusters(listF1, distance, minPts, radius);

		// CLUSTER AVERAGEs for each metric
		final List<BigDecimal> clusterAveragesPrecision = computeAverages(clusterIndexMapPrecision, listPrecision);
		final List<BigDecimal> clusterAveragesRecall = computeAverages(clusterIndexMapRecall, listRecall);
		final List<BigDecimal> clusterAveragesF1 = computeAverages(clusterIndexMapF1, listF1);

		System.out.println("[Precision] Cluster averages: " + clusterAveragesPrecision);
		System.out.println("[Recall] Cluster averages: " + clusterAveragesRecall);
		System.out.println("[F1] Cluster averages: " + clusterAveragesF1);

		// Get the 2 lowest "problem areas"
		final Map.Entry<Integer, List<Integer>> entryClusterIndexMapPrecision = clusterIndexMapPrecision.entrySet()
				.iterator().next();
//		final Map.Entry<Integer, List<Integer>> entryClusterIndexMapPrecision = clusterIndexMapPrecision.entrySet()
//				.iterator().next();
//		final Map.Entry<Integer, List<Integer>> entryClusterIndexMapPrecision = clusterIndexMapPrecision.entrySet()
//				.iterator().next();

		// Idea: how can I improve my system?
		// Here are your problem areas:
		// 1. low metric X
		// 2. For metric X, N clusters. To improve, work on documents {D1, D2, ...}

		// Characteristics of your system:
		// Least consistent metric X
		// Most consistent metric Y

		// Consistent + high --> system is good at this
		// Consistent + low --> system is bad at this

		return varPrecision.toString() + "; " + varRecall.toString() + "; " + varF1.toString();
	}

	/**
	 * Returns list with one entry (=average) per cluster aka. per key of passed map
	 * 
	 * @param clusterIndexMap
	 * @param listValues
	 * @return
	 */
	private List<BigDecimal> computeAverages(SortedMap<Integer, List<Integer>> clusterIndexMap,
			List<String> listValues) {
		final List<BigDecimal> averages = Lists.newArrayList();
		// What is the average for each of these clusters?
		for (SortedMap.Entry<Integer, List<Integer>> e : clusterIndexMap.entrySet()) {
			final List<Integer> indices = e.getValue();
			final List<String> values = Lists.newArrayList();
			for (int index : indices) {
				values.add(listValues.get(index));
			}
			final BigDecimal avgCluster = average(values);
			averages.add(avgCluster);
		}
		return averages;
	}

	/**
	 * Computes DBScan for the passed values and clusters them accordingly... By
	 * default ignores outliers
	 * 
	 * @param values   data to cluster
	 * @param distance hyperparameter
	 * @param minPts   hyperparameter
	 * @param radius   hyperparameter
	 * @return
	 */
	private SortedMap<Integer, List<Integer>> getClusters(List<String> values, final Distance<String> distance,
			final int minPts, final float radius) {
		return getClusters(values, distance, minPts, radius, true);
	}

	/**
	 * Same as {@link #getClusters(List, Distance, int, float)}, but allows to set
	 * whether to ignore outliers.
	 * 
	 * @param values
	 * @param distance
	 * @param minPts
	 * @param radius
	 * @param ignoreOutliers
	 * @return
	 */
	private SortedMap<Integer, List<Integer>> getClusters(List<String> values, final Distance<String> distance,
			final int minPts, final float radius, final boolean ignoreOutliers) {
		// Computes DBScan for precision...
		final DBScan<String> dbScan = new DBScan<String>(values.toArray(new String[values.size()]), distance, minPts,
				radius);

//		final int countCluster = dbScan.getNumClusters();
//
//		for (int i = 0; i < countCluster; ++i) {
//			final int clusterSize = dbScan.getClusterSize()[i];
//			//System.out.println("Cluster i[" + i + "]: " + clusterSize);
//		}

		final SortedMap<Integer, List<Integer>> labelValues = new TreeMap<>();
		for (int i = 0; i < dbScan.getClusterLabel().length; ++i) {
			final int label = dbScan.getClusterLabel()[i];
			// ignore outliers
			if (ignoreOutliers && label == DBScan.OUTLIER) {
				continue;
			}
			final int val = i;
			// final String val = values.get(i);
			List<Integer> vals = null;
			if ((vals = labelValues.get(label)) == null) {
				vals = Lists.newArrayList();
				labelValues.put(label, vals);
			}
			vals.add(val);
		}

		return labelValues;
	}

	/**
	 * Computes the average for BigDecimal values in String form and returns the
	 * result as a BigDecimal value
	 * 
	 * @param listValues
	 * @return average of passed values as a BigDecimal value
	 */
	private BigDecimal average(final List<String> listValues) {
		BigDecimal sum = new BigDecimal(0);
		for (final String value : listValues) {
			final BigDecimal val = new BigDecimal(value);
			sum = sum.add(val);
		}

		if (listValues.size() > 0) {
			BigDecimal count = new BigDecimal(listValues.size());
			return sum.divide(count, RoundingMode.HALF_UP);
		}
		return new BigDecimal(0);
	}

	/**
	 * Computes the variance for BigDecimal values in String form and returns the
	 * result as a BigDecimal value
	 * 
	 * @param listValues
	 * @return variance of passed values as a BigDecimal value
	 */
	private BigDecimal variance(List<String> listValues) {
		// Computing Variance:
		// https://en.wikipedia.org/wiki/Algorithms_for_calculating_variance

		// Let n ← 0, Sum ← 0, SumSq ← 0//
		// For each datum x://
		// n ← n + 1//
		// Sum ← Sum + x//
		// SumSq ← SumSq + x × x//
		// Var = (SumSq − (Sum × Sum) / n) / (n − 1)//
		BigDecimal counter = new BigDecimal(0);
		BigDecimal sum = new BigDecimal(0);
		BigDecimal sumSq = new BigDecimal(0);
		final BigDecimal one = new BigDecimal(1);
		final BigDecimal zero = new BigDecimal(0);
		for (final String value : listValues) {
			final BigDecimal val = new BigDecimal(value);
			// n ← n + 1//
			counter = counter.add(one);
			// Sum ← Sum + x//
			sum = sum.add(val);
			// SumSq ← SumSq + x × x//
			sumSq = sumSq.add(val.multiply(val));
		}
		// Var = (SumSq − (Sum × Sum) / n) / (n − 1)//
		if (counter.doubleValue() == 0 || sumSq.doubleValue() == sum.multiply(sum).doubleValue()) {
			return zero;
		}
		return sumSq.subtract(sum.multiply(sum).divide(counter, RoundingMode.HALF_UP)).divide(counter.subtract(one),
				RoundingMode.HALF_UP);
	}

	private List<String> getFeatureKeys(Set<String> featuresEvaluation, String[] precisionPossibleFeatureKeys,
			String[] recallPossibleFeatureKeys, String[] f1PossibleFeatureKeys) {
		final Integer max_distance = 100;
		final LevenshteinDistance distComputer = new LevenshteinDistance(max_distance);
		final List<DistanceEntry<String>> precisionPossibleEntries = Lists.newArrayList();
		final List<DistanceEntry<String>> recallPossibleEntries = Lists.newArrayList();
		final List<DistanceEntry<String>> f1PossibleEntries = Lists.newArrayList();
		boolean hasPrecision = false, hasRecall = false, hasF1 = false;
		String precisionFeatureKey = null, recallFeatureKey = null, f1FeatureKey = null;

		// Makes fuzzy checking b/c we're not sure what it may go by depending on
		// evaluator...
		for (String featureKey : featuresEvaluation) {
			final String featureKeyLowerCase = featureKey.toLowerCase();
			Integer precisionDistance;
			Integer recallDistance;
			Integer f1Distance;
			precisionDistance = recallDistance = f1Distance = Integer.MAX_VALUE;
			for (String precisionKey : precisionPossibleFeatureKeys) {
				final Integer distance = distComputer.apply(featureKeyLowerCase, precisionKey);
				// only update distance if not every single letter of the shorter word is being
				// replaced...
				if (featureKeyLowerCase.contains(precisionKey.toLowerCase())
						&& distance < Math.max(featureKeyLowerCase.length(), precisionKey.length())) {
					precisionDistance = distance;
				}
			}

			for (String recallKey : recallPossibleFeatureKeys) {
				final Integer distance = distComputer.apply(featureKeyLowerCase, recallKey);
				// only update distance if not every single letter of the shorter word is being
				// replaced...
				if (featureKeyLowerCase.contains(recallKey.toLowerCase())
						&& distance < Math.max(featureKeyLowerCase.length(), recallKey.length())) {
					recallDistance = distance;
				}
			}

			for (String f1Key : f1PossibleFeatureKeys) {
				final Integer distance = distComputer.apply(featureKeyLowerCase, f1Key);
				// only update distance if not every single letter of the shorter word is being
				// replaced...
				if (featureKeyLowerCase.contains(f1Key.toLowerCase())
						&& distance < Math.max(featureKeyLowerCase.length(), f1Key.length())) {
					f1Distance = distance;
				}
			}

			if (precisionDistance < recallDistance && precisionDistance < f1Distance) {
				// found it for precision
				precisionPossibleEntries.add(new DistanceEntry<String>(precisionDistance, featureKey));
			} else if (recallDistance < precisionDistance && recallDistance < f1Distance) {
				// found it for recall
				recallPossibleEntries.add(new DistanceEntry<String>(recallDistance, featureKey));
			} else if (f1Distance < precisionDistance && f1Distance < recallDistance) {
				// found it for f1
				f1PossibleEntries.add(new DistanceEntry<String>(f1Distance, featureKey));
			} else {
				// it is equal for all -> ignore
				System.out.println("Ignoring feature key: " + featureKey);
			}

		}

		// Get best entry (based on minimal distance) first
		Collections.sort(precisionPossibleEntries);
		Collections.sort(recallPossibleEntries);
		Collections.sort(f1PossibleEntries);

		// Making sure sth. was found
//		System.out.println("Prec entries: " + precisionPossibleEntries);
//		System.out.println("Recall entries: " + recallPossibleEntries);
//		System.out.println("F1 entries: " + f1PossibleEntries);
		if (precisionPossibleEntries.size() > 0) {
			hasPrecision = true;
			precisionFeatureKey = precisionPossibleEntries.get(0).getEntry();
		}

		// Get best recall entry
		if (recallPossibleEntries.size() > 0) {
			hasRecall = true;
			recallFeatureKey = recallPossibleEntries.get(0).getEntry();
		}

		// Get best f1 entry
		if (f1PossibleEntries.size() > 0) {
			hasF1 = true;
			f1FeatureKey = f1PossibleEntries.get(0).getEntry();
		}

		if (!(hasPrecision && hasRecall && hasF1)) {
			// One of them is missing...
			throw new RuntimeException("ERROR - Could not find Precision(" + hasPrecision + "), Recall(" + hasRecall
					+ ") or F1-Measure(" + hasF1 + ")");
		}

		return Arrays.asList(new String[] { precisionFeatureKey, recallFeatureKey, f1FeatureKey });
	}

}
