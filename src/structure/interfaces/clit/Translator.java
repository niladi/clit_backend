package structure.interfaces.clit;

import structure.datatypes.AnnotatedDocument;

public interface Translator extends Subcomponent {
	/**
	 * Translates one entity from one KG to an entity from another KG
	 * 
	 * @param entity
	 * @return
	 */
	public String translate(final String entity);

	/**
	 * 
	 * @param input
	 * @return
	 */
	public AnnotatedDocument translate(AnnotatedDocument input);

}
