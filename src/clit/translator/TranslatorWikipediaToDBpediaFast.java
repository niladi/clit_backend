package clit.translator;

/**
 * Translator instance making use of <dbpedia_entity> foaf:isPrimaryTopicOf
 * <wikipedia_page>
 * 
 * @author wf7467
 *
 */
public class TranslatorWikipediaToDBpediaFast extends AbstractTranslator {

	@Override
	public String translate(String entity) {
		if (entity == null) {
			return null;
		}
		return entity.replace("en.wikipedia.org/wiki", "dbpedia.org/resource").replace("wikipedia.org/wiki", "dbpedia.org/resource");
	}
}
