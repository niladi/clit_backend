package structure.interfaces.pipeline;

import java.io.IOException;

import structure.datatypes.AnnotatedDocument;

/**
 * Simple interface for candidate generators with one method -
 * 'generate(Mention), taking an input mention and returning a list of
 * candidates for it
 * 
 * @author Kristian Noullet
 *
 */
public interface CandidateGenerator extends PipelineComponent {
	/**
	 * Takes a mention and returns a list of possible assignments (=candidates) for
	 * it <br>
	 * Note: Enforced list rather than collection due to the different assumptions
	 * that may be made in regards to the annotation procedure, whether the used
	 * object is a set or a list
	 * 
	 * @param mention Mention for which to find candidates
	 * @return List of candidates
	 * @throws IOException 
	 */
	public AnnotatedDocument generate(final AnnotatedDocument document) throws IOException;
}
