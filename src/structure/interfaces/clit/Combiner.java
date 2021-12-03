package structure.interfaces.clit;

import java.util.Collection;

import structure.datatypes.AnnotatedDocument;

public interface Combiner extends Subcomponent {
	public abstract AnnotatedDocument combine(final Collection<AnnotatedDocument> multiItems);
}
