package clit.eval.interfaces;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public interface AnnotationEvaluation extends Serializable {
	public Map<String, List<String>> getEvaluationMap();

	public String getCategory();

}
