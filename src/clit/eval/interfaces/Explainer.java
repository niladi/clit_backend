package clit.eval.interfaces;

import java.util.List;

import structure.interfaces.pipeline.PipelineComponent;

public interface Explainer extends PipelineComponent {
	public String explain(final List<AnnotationEvaluation> evaluations);

}
