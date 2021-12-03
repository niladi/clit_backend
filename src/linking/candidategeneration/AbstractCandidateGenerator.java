package linking.candidategeneration;

import java.io.IOException;
import java.util.Collection;

import experiment.PipelineItem;
import structure.datatypes.AnnotatedDocument;
import structure.interfaces.pipeline.CandidateGenerator;

public abstract class AbstractCandidateGenerator implements CandidateGenerator {

	@Override
	public Collection<AnnotatedDocument> execute(final PipelineItem callItem, final AnnotatedDocument input)
			throws IOException {
		return generate(input).makeMultiDocuments();
	}

}
