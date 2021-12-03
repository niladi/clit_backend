package linking.mentiondetection;

import java.util.Collection;

import experiment.PipelineItem;
import structure.datatypes.AnnotatedDocument;
import structure.interfaces.pipeline.MentionDetector;

public abstract class AbstractMentionDetector implements MentionDetector {

	@Override
	public Collection<AnnotatedDocument> execute(final PipelineItem callItem, final AnnotatedDocument input)
			throws Exception {
		return detect(input).makeMultiDocuments();
	}
}