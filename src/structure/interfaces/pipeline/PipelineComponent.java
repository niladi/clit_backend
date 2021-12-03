package structure.interfaces.pipeline;

import java.util.Collection;

import experiment.PipelineItem;
import structure.datatypes.AnnotatedDocument;

public interface PipelineComponent {

	public Collection<AnnotatedDocument> execute(final PipelineItem callItem,
			final AnnotatedDocument document) throws Exception;

}
