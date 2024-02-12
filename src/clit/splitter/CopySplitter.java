package clit.splitter;

import java.util.Collection;

import com.google.common.collect.Lists;

import structure.datatypes.AnnotatedDocument;

public class CopySplitter extends AbstractSplitter {

	public Collection<AnnotatedDocument> split(final AnnotatedDocument mentionsToSplit) {
		return split(mentionsToSplit, 2);
	}

	public Collection<AnnotatedDocument> split(final AnnotatedDocument mentionsToSplit, final int copies) {
		return split(mentionsToSplit, copies, null);
	}

	/**
	 * Splits passed mentions into copies
	 * 
	 * @param input
	 * @param copies          how many copies to manufacture
	 * @param params          parameters which may be utilised for splitting
	 * @return split documents
	 */
	public Collection<AnnotatedDocument> split(final AnnotatedDocument input, final int copies,
			final String[] params) {
		final Collection<AnnotatedDocument> result = Lists.newArrayList();
		if (params != null && params.length != 0) {
			// process parameters ...
		} else if (copies > 0) {
			for (int i = 0; i < copies; ++i) {
				// Copy the input and add to output
				System.out.println("CLONING IT...");
				final AnnotatedDocument copy = (AnnotatedDocument) input.clone();
				result.add(copy);
			}
		}
		return result;
	}
}
