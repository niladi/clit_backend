package api;

import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;

import com.google.common.collect.Lists;

public enum EnumSPARQLEndpoint {
	// https://www.w3.org/wiki/SparqlEndpoints
	WIKIDATA("https://query.wikidata.org/sparql", ".*wikidata\\.org.*"), //
	DBPEDIA("https://dbpedia.org/sparql/", "^https://dbpedia\\.org.*"), //
	EUROPA("https://data.europa.eu/sparql"),//
	;// "https://query.wikidata.org/sparql", "https://dbpedia.org/sparql"

	private final String endpointAddress;
	private final String[] stringPatterns;

	private EnumSPARQLEndpoint(final String endpointAddress, final String... stringPatterns) {
		this.endpointAddress = endpointAddress;
		if (stringPatterns == null || stringPatterns.length < 1) {
			this.stringPatterns = null;
		} else {
//			final List<String> patterns = Lists.newArrayList();
//			for (String s : stringPatterns) {
//				patterns.add(s);
//			}
			this.stringPatterns = stringPatterns;
//			this.stringPatterns = patterns;
		}
	}

	/**
	 * Helper function to find which endpoint may be used to find information on a
	 * specified entity
	 * 
	 * @param entity
	 * @return
	 */
	public static Collection<String> findEndpointByEntity(final CharSequence entity) {
		if (entity == null) {
			return null;
		}
		final List<String> endpoints = Lists.newArrayList();

		for (EnumSPARQLEndpoint endpoint : values()) {
			boolean addEndpoint = true;
			for (final String pattern : endpoint.stringPatterns) {
				if (!(addEndpoint &= Pattern.matches(pattern, entity))) {
					// doesn't match, so try next one!
					//
					// nope, not these patterns, so move to next possible endpoint
					break;
				}
			}
			if (addEndpoint) {
				endpoints.add(endpoint.endpointAddress);
			}
		}

		return endpoints;
	}

	/**
	 * Returns associated SPARQL address for wanted endpoint
	 * 
	 * @return
	 */
	public final String getEndpointAddress() {
		return this.endpointAddress;
	}
}
