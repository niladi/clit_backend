package clit.eval.datatypes.evaluation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import clit.eval.interfaces.AnnotationEvaluation;

public class DatasetEvaluation implements AnnotationEvaluation {
	private final Map<String, List<String>> datasetEvaluation = new HashMap<>();

	@Override
	public Map<String, List<String>> getEvaluationMap() {
		return this.datasetEvaluation;
	}

	@Override
	public String getCategory() {
		return getClass().getName();
	}
}
