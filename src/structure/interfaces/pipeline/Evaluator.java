package structure.interfaces.pipeline;

import java.util.Collection;
import java.util.List;

import org.aksw.gerbil.transfer.nif.Document;

import clit.eval.interfaces.AnnotationEvaluation;
import structure.datatypes.AnnotatedDocument;

public interface Evaluator extends PipelineComponent {
	public Collection<AnnotationEvaluation> evaluate(final List<Document> nifDocs,
			final List<AnnotatedDocument> toEvaluate);
}
