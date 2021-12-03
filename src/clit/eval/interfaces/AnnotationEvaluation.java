package clit.eval.interfaces;

import java.util.List;
import java.util.Map;

public interface AnnotationEvaluation {
	public Map<String, List<String>> getEvaluationMap();

	public String getCategory();
	
}
