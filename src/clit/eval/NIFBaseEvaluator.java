package clit.eval;

import java.util.Collection;

import com.google.common.collect.Lists;

import clit.eval.datatypes.EvalConstants;
import clit.eval.datatypes.evaluation.DatasetEvaluation;
import clit.eval.datatypes.evaluation.DocumentEvaluation;
import clit.eval.datatypes.evaluation.MentionEvaluation;
import clit.eval.datatypes.result.DatasetResult;
import clit.eval.datatypes.result.DocumentResult;
import clit.eval.datatypes.result.MentionResult;

/**
 * Pretty basic class: let's compute TP, TN, FP, FN, Precision, Recall and F1
 * for computed results!
 * 
 * @author wf7467
 *
 */
public class NIFBaseEvaluator extends AbstractNIFEvaluator {

	@Override
	protected DatasetEvaluation evaluateDataset(DatasetResult datasetResult) {
		// TP TN FP FN on a DATASET level!
		// Do a binary "correct sentence, incorrect sentence"
		final DatasetEvaluation datasetEvaluation = new DatasetEvaluation();
		final Collection<DocumentResult> documentResults = datasetResult.getDocumentResults();
		// Compute TP, FP, FN (TN is... kinda difficult in NER(D), may want to
		// approximate it to the number of tokens minus the number of detected mentions,
		// I guess?)
		int tp, tn, fp, fn;
		tp = tn = fp = fn = 0;
		for (final DocumentResult documentResult : documentResults) {
			for (final MentionResult mr : documentResult.getMentionResults()) {
				if (mr.goldMention == null && mr.foundMention != null) {
					// We found sth that we were not supposed to...
					fp++;
				} else if (mr.goldMention != null && mr.foundMention == null) {
					// We did NOT find sth we were supposed to
					fn++;
				} else if (mr.goldMention != null && mr.foundMention != null) {
					// We found sth where we were supposed to - now let's check if we were right!
					if (mr.match) {
						// both match - awesome! True Positive!
						tp++;
					} else {
						// they don't match - sad!
						fn++;
					}
				}
			}
		}

		// Populating evaluation with our computed metrics
		datasetEvaluation.getEvaluationMap().put(EvalConstants.TP, Lists.newArrayList("" + tp));
		datasetEvaluation.getEvaluationMap().put(EvalConstants.TN, Lists.newArrayList("" + tn));
		datasetEvaluation.getEvaluationMap().put(EvalConstants.FP, Lists.newArrayList("" + fp));
		datasetEvaluation.getEvaluationMap().put(EvalConstants.FN, Lists.newArrayList("N/A(" + fn + ")"));
		final double precision = precision(tp, tn, fp, fn);// tp / (tp + fp);
		final double recall = recall(tp, tn, fp, fn);// tp / (tp + fn);
		final double f1 = f1(tp, tn, fp, fn);// 2 * precision * recall / (precision + recall);
		datasetEvaluation.getEvaluationMap().put(EvalConstants.DATASET_PRECISION, Lists.newArrayList("" + precision));
		datasetEvaluation.getEvaluationMap().put(EvalConstants.DATASET_RECALL, Lists.newArrayList("" + recall));
		datasetEvaluation.getEvaluationMap().put(EvalConstants.DATASET_F1, Lists.newArrayList("" + f1));

		return datasetEvaluation;
	}

	@Override
	protected DocumentEvaluation evaluateDocument(DocumentResult singleDocMatchResults) {
		// TP TN FP FN on a DOCUMENT level!

		// https://stackoverflow.com/questions/1783653/computing-precision-and-recall-in-named-entity-recognition
		final DocumentEvaluation documentEvaluation = new DocumentEvaluation();
		int tp, tn, fp, fn;
		tp = tn = fp = fn = 0;
		// Compute TP, FP, FN (TN is... kinda difficult in NER(D), may want to
		// approximate it to the number of tokens minus the number of detected mentions,
		// I guess?)
		for (final MentionResult mr : singleDocMatchResults.getMentionResults()) {
			if (mr.goldMention == null && mr.foundMention != null) {
				// We found sth that we were not supposed to...
				fp++;
			} else if (mr.goldMention != null && mr.foundMention == null) {
				// We did NOT find sth we were supposed to
				fn++;
			} else if (mr.goldMention != null && mr.foundMention != null) {
				// We found sth where we were supposed to - now let's check if we were right!
				if (mr.match) {
					// both match - awesome! True Positive!
					tp++;
				} else {
					// they don't match - sad!
					fn++;
				}
			}

		}

		// Populating evaluation with our computed metrics
		documentEvaluation.documentEvaluation.put(EvalConstants.TP, Lists.newArrayList("" + tp));
		documentEvaluation.documentEvaluation.put(EvalConstants.TN, Lists.newArrayList("" + tn));
		documentEvaluation.documentEvaluation.put(EvalConstants.FP, Lists.newArrayList("" + fp));
		documentEvaluation.documentEvaluation.put(EvalConstants.FN, Lists.newArrayList("" + fn));
		final double precision = precision(tp, tn, fp, fn);// tp / (tp + fp);
		final double recall = recall(tp, tn, fp, fn);// tp / (tp + fn);
		final double f1 = f1(tp, tn, fp, fn);// 2 * precision * recall / (precision + recall);
		documentEvaluation.documentEvaluation.put(EvalConstants.DOCUMENT_PRECISION, Lists.newArrayList("" + precision));
		documentEvaluation.documentEvaluation.put(EvalConstants.DOCUMENT_RECALL, Lists.newArrayList("" + recall));
		documentEvaluation.documentEvaluation.put(EvalConstants.DOCUMENT_F1, Lists.newArrayList("" + f1));

		return documentEvaluation;
	}

	@Override
	protected MentionEvaluation evaluateMention(final MentionResult mentionResult) {
		final MentionEvaluation mentionEvaluation = new MentionEvaluation();
		mentionEvaluation.mentionEvaluation.put(EvalConstants.MENTION_CORRECT,
				Lists.newArrayList(mentionResult.match ? "1" : "0"));
		mentionEvaluation.mentionEvaluation.put(EvalConstants.MENTION_SOURCE,
				Lists.newArrayList(mentionResult.goldMention == null && mentionResult.foundMention != null ? "Linker"
						: mentionResult.goldMention != null && mentionResult.foundMention == null ? "Dataset"
								: "Both"));
		mentionEvaluation.mentionEvaluation.put(EvalConstants.MENTION_TEXT,
				Lists.newArrayList((mentionResult.foundMention != null ? mentionResult.foundMention : "") + " / "
						+ (mentionResult.goldMention != null ? mentionResult.goldMention : "")));
		// mentionResults.match;
		return mentionEvaluation;
	}

}
