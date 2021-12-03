package structure.interfaces.clit;

import java.util.Collection;

import structure.datatypes.AnnotatedDocument;

public interface Splitter extends Subcomponent {
	public abstract Collection<AnnotatedDocument> split(AnnotatedDocument mentionsToSplit);

	public abstract Collection<AnnotatedDocument> split(AnnotatedDocument mentionsToSplit, final int copies);

	/**
	 * Splits passed mentions into various copies, allowing for further processing.
	 * We recommend using a Filter or Translator subcomponent after a splitter if
	 * more complex operations for a specific component should be executed
	 * 
	 * @param documentToSplit
	 * @param copies          how many copies to manufacture
	 * @param params          parameters which may be utilised for
	 * @return split document
	 */
	public abstract Collection<AnnotatedDocument> split(AnnotatedDocument documentToSplit, final int copies,
			final String[] params);
}
