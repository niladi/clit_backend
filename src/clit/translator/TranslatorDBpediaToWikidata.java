package clit.translator;

import java.util.Arrays;
import java.util.Collection;

import api.EnumSPARQLEndpoint;

public class TranslatorDBpediaToWikidata extends AbstractSPARQLTranslator {
	private final String SEARCHED_KG_NAMESPACE = "wikidata.org";
	private final Collection<String> REPO_LINKS = Arrays.asList(new String[] {
			EnumSPARQLEndpoint.DBPEDIA.getEndpointAddress(), EnumSPARQLEndpoint.WIKIDATA.getEndpointAddress() });
	private final Collection<String> SEARCH_CRITERIA = Arrays.asList(new String[] { SEARCHED_KG_NAMESPACE });

	public TranslatorDBpediaToWikidata() {
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
