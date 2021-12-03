package clit.eval.datatypes.evaluation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import clit.eval.interfaces.AnnotationEvaluation;

public class DocumentEvaluation implements AnnotationEvaluation {
	public final Map<String, List<String>> documentEvaluation = new HashMap<>();

	@Override
	public Map<String, List<String>> getEvaluationMap() {
		return this.documentEvaluation;
	}

	@Override
	public String getCategory() {
		return getClass().getName();
	}

}
