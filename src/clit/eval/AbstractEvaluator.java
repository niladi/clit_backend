package clit.eval;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;

import clit.eval.datatypes.evaluation.DatasetEvaluation;
import clit.eval.datatypes.evaluation.DocumentEvaluation;
import clit.eval.datatypes.evaluation.MentionEvaluation;
import clit.eval.datatypes.result.DatasetResult;
import clit.eval.datatypes.result.DocumentResult;
import clit.eval.datatypes.result.MentionResult;
import experiment.PipelineItem;
import structure.datatypes.AnnotatedDocument;
import structure.datatypes.Mention;
import structure.interfaces.pipeline.Evaluator;

public abstract class AbstractEvaluator implements Evaluator {

	public Collection<AnnotatedDocument> execute(final PipelineItem callItem, final AnnotatedDocument document)
			throws Exception {
		return null;
	}

	protected abstract DatasetEvaluation evaluateDataset(final DatasetResult datasetResult);

	protected abstract DocumentEvaluation evaluateDocument(final DocumentResult singleDocMatchResults);

	protected abstract MentionEvaluation evaluateMention(final MentionResult mentionResults);

	/**
	 * Helper function to more easily match mentions and markings against each other
	 * 
	 * @param mentions
	 * @return
	 */
	protected Map<String, List<Mention>> mapMentionsByOffsetLength(Collection<Mention> mentions) {
		final Map<String, List<Mention>> offsetMentions = new HashMap<>();
		if (mentions != null) {
			for (final Mention m : mentions) {
				final String k = key(m);
				List<Mention> toPopulate;
				if ((toPopulate = offsetMentions.get(k)) == null) {
					toPopulate = Lists.newArrayList();
					offsetMentions.put(k, toPopulate);
				}
				toPopulate.add(m);
			}
		}
		return offsetMentions;
	}

	protected String key(int offset, int length) {
		return offset + "_" + (offset + length);
	}

	protected String key(final Mention m) {
		if (m == null)
			return "null";
		return key(m.getOffset(), m.getMention().length());
	}

	protected double precision(Number tp, Number tn, Number fp, Number fn) {
		final double den = tp.doubleValue() + fp.doubleValue();
		if (den == 0)
			return 0;
		final double precision = tp.doubleValue() / den;
		return precision;
	}

	protected double recall(Number tp, Number tn, Number fp, Number fn) {
		final double den = tp.doubleValue() + fn.doubleValue();
		if (den == 0)
			return 0;
		final double recall = tp.doubleValue() / (den);
		return recall;
	}

	protected double f1(Number tp, Number tn, Number fp, Number fn) {
		final double precision = precision(tp, tn, fp, fn);
		final double recall = recall(tp, tn, fp, fn);
		final double numerator = (2 * precision * recall);
		final double denominator = (precision + recall);
		if (numerator == 0 || denominator == 0) {
			return 0d;
		}
		final double f1 = numerator / denominator;
		return f1;
	}

}
