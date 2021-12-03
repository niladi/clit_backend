package clit.translator;

import java.util.Arrays;
import java.util.Collection;

import api.EnumSPARQLEndpoint;

/**
 * Translator instance making use of <dbpedia_entity> foaf:isPrimaryTopicOf
 * <wikipedia_page>
 * 
 * @author wf7467
 *
 */
public class TranslatorWikipediaToDBpedia extends AbstractSPARQLTranslator {
	private final String SEARCHED_KG_NAMESPACE = "dbpedia.org";
	private final Collection<String> REPO_LINKS = Arrays
			.asList(new String[] { EnumSPARQLEndpoint.DBPEDIA.getEndpointAddress() });
	private final Collection<String> SEARCH_CRITERIA = Arrays.asList(new String[] { SEARCHED_KG_NAMESPACE });

	@Override
	protected Collection<String> getSearchCriteria() {
		return SEARCH_CRITERIA;
	}

	@Override
	protected Collection<String> getRepoLinks() {
		return REPO_LINKS;
	}

	@Override
	protected String getSameAsPredicate() {
		// return super.getSameAsPredicate();
		return "foaf:isPrimaryTopicOf";
	}

	@Override
	public String translate(String entity) {
		final String translation = super.translate(entity);
		if (translation == null) {
			// Could not find translation via SPARQL, so attempt doing it manually
			if (entity == null) {
				return null;
			}
			return entity.replace("en.wikipedia.org/wiki", "dbpedia.org/resource");
		} else {
			return translation;
		}
	}

}
