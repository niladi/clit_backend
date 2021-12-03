package clit.splitter;

import java.util.Collection;

import experiment.PipelineItem;
import structure.datatypes.AnnotatedDocument;
import structure.interfaces.clit.Splitter;

public abstract class AbstractSplitter implements Splitter {

	@Override
	public Collection<AnnotatedDocument> execute(final PipelineItem callItem, final AnnotatedDocument input) {
		final AnnotatedDocument document = callItem.getCopyOfSingleDependencyResult();
		final int copies = callItem.getTargets().size();
		return split(document, copies);
	}

}
