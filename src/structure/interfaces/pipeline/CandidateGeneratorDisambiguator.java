package structure.interfaces.pipeline;

import structure.datatypes.AnnotatedDocument;

public interface CandidateGeneratorDisambiguator extends PipelineComponent {

	public AnnotatedDocument generateDisambiguate(AnnotatedDocument document) throws Exception;
}
