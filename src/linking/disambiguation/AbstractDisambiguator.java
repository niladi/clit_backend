package linking.disambiguation;

import java.util.Collection;

import experiment.PipelineItem;
import structure.datatypes.AnnotatedDocument;
import structure.interfaces.pipeline.Disambiguator;

public abstract class AbstractDisambiguator implements Disambiguator {

	@Override
	public Collection<AnnotatedDocument> execute(final PipelineItem callItem, final AnnotatedDocument input)
			throws Exception {
		return disambiguate(input).makeMultiDocuments();
	}

}
