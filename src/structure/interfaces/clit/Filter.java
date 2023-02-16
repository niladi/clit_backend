package structure.interfaces.clit;

import structure.datatypes.AnnotatedDocument;

public interface Filter extends Subcomponent {
	public AnnotatedDocument filter(final AnnotatedDocument document);

}
