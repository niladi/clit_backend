package structure.interfaces.pipeline;

import structure.datatypes.AnnotatedDocument;

public interface Disambiguator extends PipelineComponent {

	public AnnotatedDocument disambiguate(final AnnotatedDocument document) throws Exception;

}