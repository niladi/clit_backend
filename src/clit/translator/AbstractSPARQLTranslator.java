package clit.translator;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openrdf.model.Value;
import org.openrdf.query.BindingSet;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sparql.SPARQLRepository;

import com.google.common.collect.Lists;

import experiment.PipelineItem;
import structure.datatypes.AnnotatedDocument;
import structure.datatypes.Mention;
import structure.datatypes.PossibleAssignment;
import structure.interfaces.clit.Translator;
import structure.utils.TextUtils;

public abstract class AbstractSPARQLTranslator extends AbstractTranslator {

	private final Map<String, String> translationCache = new HashMap<>();
	private final Map<String, SPARQLRepository> repoCache = new HashMap<>();

	public AbstractSPARQLTranslator() {
	}


	/**
	 * Translate passed entity to a wanted one based on translation criteria
	 * (defined by the implementation of this abstract class)
	 * 
	 * @param entity entity which is to be translated to another KG
	 * @return translated entity (string format)
	 */
	public String translate(final String entity) {
		if (entity == null || entity.length() == 0) {
			return null;
		}

		// Check if we have had it yet!
		// If so, take it from cache rather than querying again (we don't want to get
		// banned <3)
		if (translationCache.containsKey(entity)) {
			return translationCache.get(entity);
		}

		final Set<String> sameAsEntities = new HashSet<>();
		try {
			for (String repoLink : getRepoLinks()) {
				try {

					// Optimised SPARQLRepository connections: initialize SPARQLRepository instances
					// once and caching them in a map
					SPARQLRepository sparqlRepository = null;
					// check if sparql repo is cached yet!
					if ((sparqlRepository = repoCache.get(repoLink)) == null) {
						// not cached, so create + initialise it
						sparqlRepository = new SPARQLRepository(repoLink);
						// initialise the repository
						sparqlRepository.initialize();
						// put into cache if it initialises correctly
						this.repoCache.put(repoLink, sparqlRepository);
					}

					// Execute the queries!
					final Collection<String> repoSameAsEntities = executeQueries(sparqlRepository, entity);
					if (repoSameAsEntities != null && repoSameAsEntities.size() > 0) {
						sameAsEntities.addAll(repoSameAsEntities);
					}
				} catch (RepositoryException | MalformedQueryException | QueryEvaluationException e) {
					e.printStackTrace();
				}
			}
		} catch (Exception e) {
			System.err.println("Exception while translating... Likely logic-based.");
			e.printStackTrace();
		}

		// Decide here according to what logic we want to find the correct entity...
		// e.g. if we want WD from DBp, we might want to check whether it is in the
		// wikidata namespace! (which is the idea behind the getSearchCriteria() method)
		final String chosenEntity;

		final List<String> possibleResults = Lists.newArrayList();
		for (String sameEntity : sameAsEntities) {
			// search criteria
			boolean passed = true;
			for (String criterion : getSearchCriteria()) {
				passed &= sameEntity.toLowerCase().contains(criterion.toLowerCase());
			}
			// passes all string criteria, so add it!
			if (passed) {
				possibleResults.add(sameEntity);
			}
		}

		// Depending on how many entries there are, a different chosenEntity is defined
		// (0 -> null, 1 = match, >1 = 1st one + error msg)
		final int resSize = possibleResults.size();
		if (resSize == 0) {
			chosenEntity = null;
		} else if (resSize == 1) {
			// perfect match!
			chosenEntity = possibleResults.get(0);
		} else {
			// too many... pick the first
			System.err.println("[" + getClass().getName() + "] Too many possible translations, picking the first: ["
					+ entity + "] --> " + possibleResults);
			chosenEntity = possibleResults.get(0);
		}

		// System.out.println(entity + " ---> " + chosenEntity);
		if (sameAsEntities.size() == 0 || resSize == 0) {
			// Remove the PossibleAssignment and just continue to the next one
			translationCache.putIfAbsent(entity, null);
			return null;
		} else {
			translationCache.putIfAbsent(entity, chosenEntity);
			return chosenEntity;
		}
	}

	/**
	 * Search criteria to filter out unwanted sameAs connections
	 * 
	 * @return
	 */
	protected abstract Collection<String> getSearchCriteria();

	/**
	 * Repositories where it would make sense to execute the sameAs search queries
	 * 
	 * @return
	 */
	protected abstract Collection<String> getRepoLinks();

	/**
	 * Executes queries for translation on the defined SPARQLRepository instance for
	 * a defined entity
	 * 
	 * @param sparqlRepository where to execute query/queries
	 * @param entity           what entity to execute query for
	 * @return collection of possible translations for passed entity
	 * @throws RepositoryException
	 * @throws MalformedQueryException
	 * @throws QueryEvaluationException
	 */
	private Collection<String> executeQueries(final SPARQLRepository sparqlRepository, final String entity)
			throws RepositoryException, MalformedQueryException, QueryEvaluationException {
		final RepositoryConnection repoConnection = sparqlRepository.getConnection();
		final Set<String> retEntities = new HashSet<>();

		// In case inference rules are not properly applied, let's test sameAs links in
		// both ways
		// First with our known entity as the OBJECT, so we're interested in the subject
		final String queryS = getSameAsQuerySubject(entity);
		final Set<String> entitiesS = executeAndGrabResults(repoConnection, queryS, "s");
		if (entitiesS != null && entitiesS.size() > 0) {
			retEntities.addAll(entitiesS);
		}

		// Secondly with our known entity as the SUBJECT, so we're interested in the
		// object
		final String queryO = getSameAsQueryObject(entity);
		final Set<String> entitiesO = executeAndGrabResults(repoConnection, queryO, "o");

		if (entitiesO != null && entitiesO.size() > 0) {
			retEntities.addAll(entitiesO);
		}

		return retEntities;
		// System.out.println("Result for tupleQuery" + tupleQuery.evaluate());
	}

	protected String getSameAsQueryObject(String entity) {
		return "SELECT ?o WHERE { <" + TextUtils.stripArrowSigns(entity) + "> " + getSameAsPredicate() + " ?o }";// LIMIT
		// 100";
	}

	protected String getSameAsQuerySubject(String entity) {
		return "SELECT ?s WHERE { ?s " + getSameAsPredicate() + " <" + TextUtils.stripArrowSigns(entity) + "> }";// LIMIT
		// 100";
		// return null;
	}

	protected String getSameAsPredicate() {
		return "owl:sameAs";
	}

	/**
	 * Returns a set of entities found for the given bindname (=name of variable
	 * without '?') and query
	 * 
	 * @param repoConnection connection to the repository, e.g. DBpedia or Wikidata
	 *                       SPARQL endpoint
	 * @param query          query querying the appropriate entities
	 * @param bindName       what variable to look out for to grab the results
	 * @return set containing said possible translations
	 * @throws RepositoryException
	 * @throws MalformedQueryException
	 * @throws QueryEvaluationException
	 */
	private Set<String> executeAndGrabResults(final RepositoryConnection repoConnection, final String query,
			final String bindName) throws RepositoryException, MalformedQueryException, QueryEvaluationException {
		final Set<String> resultAggregator = new HashSet<>();
		final TupleQueryResult tqrS = query(repoConnection, query);

		for (BindingSet bs : tqrS.asList()) {
			final Value val = bs.getValue(bindName);
			if (val != null) {
				resultAggregator.add(val.toString());
			}
//			for (String bindName : bs.getBindingNames()) {
//				System.out.println("bName: " + bindName + " -> " + bs.getValue(bindName));
//			}
		}

		if (tqrS != null) {
			// System.out.println(tqrS.asList());
			tqrS.close();
		}

		return resultAggregator;
	}

	/**
	 * Queries the SPARQLConnection
	 * 
	 * @param sparqlConnection
	 * @param query
	 * @return
	 * @throws RepositoryException
	 * @throws MalformedQueryException
	 * @throws QueryEvaluationException
	 */
	private TupleQueryResult query(RepositoryConnection sparqlConnection, String query)
			throws RepositoryException, MalformedQueryException, QueryEvaluationException {
		final TupleQuery tupleQuery = sparqlConnection.prepareTupleQuery(QueryLanguage.SPARQL, query);
		return tupleQuery.evaluate();
	}

}
