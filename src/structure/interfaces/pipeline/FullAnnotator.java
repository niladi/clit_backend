package structure.interfaces.pipeline;

import structure.datatypes.AnnotatedDocument;

public interface FullAnnotator extends PipelineComponent {

	public AnnotatedDocument annotate(AnnotatedDocument document) throws Exception;

}
