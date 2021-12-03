package structure.utils;

import java.util.Collection;

import com.google.common.collect.Lists;

import structure.datatypes.AnnotatedDocument;

public class DocumentUtils {

	/**
	 * Creates a Collection<AnnotatedDocument> from AnnotatedDocument.
	 * Better use this.makeMultiDocument() in {@link AnnotatedDocument}.
	 */
	@Deprecated
	public static Collection<AnnotatedDocument> makeMultiDocuments(final AnnotatedDocument document) {
		if (document == null)
			return null;

		final Collection<AnnotatedDocument> retDocuments = Lists.newArrayList();
		retDocuments.add(document);
		return retDocuments;
	}

}
