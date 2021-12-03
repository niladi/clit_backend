package clit.translator;

import java.util.Arrays;
import java.util.Collection;

import api.EnumSPARQLEndpoint;

public class TranslatorWikidataToDBpedia extends AbstractSPARQLTranslator {
	private final String SEARCHED_KG_NAMESPACE = "://dbpedia.org";
	private final Collection<String> REPO_LINKS = Arrays.asList(new String[] { //
			EnumSPARQLEndpoint.WIKIDATA.getEndpointAddress(), //
			EnumSPARQLEndpoint.DBPEDIA.getEndpointAddress() //
	});
	private final Collection<String> SEARCH_CRITERIA = Arrays.asList(new String[] { SEARCHED_KG_NAMESPACE });

	public TranslatorWikidataToDBpedia() {
	}

	@Override
	protected Collection<String> getRepoLinks() {
		return this.REPO_LINKS;
	}

	@Override
	protected Collection<String> getSearchCriteria() {
		return this.SEARCH_CRITERIA;
	}

}
