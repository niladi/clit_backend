package clit.eval;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.aksw.gerbil.datatypes.ErrorTypes;
import org.aksw.gerbil.exceptions.GerbilException;
import org.aksw.gerbil.transfer.nif.Document;
import org.aksw.gerbil.transfer.nif.Marking;
import org.aksw.gerbil.transfer.nif.Meaning;
import org.aksw.gerbil.transfer.nif.Span;

import com.google.common.collect.Lists;

import clit.eval.datatypes.evaluation.DatasetEvaluation;
import clit.eval.datatypes.evaluation.DocumentEvaluation;
import clit.eval.datatypes.evaluation.MentionEvaluation;
import clit.eval.datatypes.result.DatasetResult;
import clit.eval.datatypes.result.DocumentResult;
import clit.eval.datatypes.result.MentionResult;
import clit.eval.interfaces.AnnotationEvaluation;
import structure.datatypes.AnnotatedDocument;
import structure.datatypes.Mention;
import structure.datatypes.PossibleAssignment;

public abstract class AbstractNIFEvaluator extends AbstractEvaluator {

	@Override
	public List<AnnotationEvaluation> evaluate(final List<Document> nifDocs, final List<AnnotatedDocument> toEvaluate) {
		// Input: Dataset + (Linker | Results)
		// Output: metrics
		// Dataset + Linker -> Execute linker w/ dataset
		// Dataset + results ->
		// These results are for dataset X

		if (nifDocs.size() != toEvaluate.size()) {
			throw new IllegalArgumentException(
					"Number of gold standard documents and annotated documents does not match... (Gold standard: "
							+ nifDocs.size() + ", Annotated: " + toEvaluate.size() + ") ");
		}

		final List<AnnotationEvaluation> evaluations = Lists.newArrayList();
		final List<DatasetEvaluation> datasetEvaluations = Lists.newArrayList();
		final List<DocumentEvaluation> documentEvaluations = Lists.newArrayList();
		final List<MentionEvaluation> mentionEvaluations = Lists.newArrayList();

		final DatasetResult datasetResult = new DatasetResult();

		for (int i = 0; i < nifDocs.size(); ++i) {
			final Document goldDoc = nifDocs.get(i);
			final AnnotatedDocument found = toEvaluate.get(i);
			final DocumentResult singleDocResults;
			if (found == null || found.getMentions() == null) {
				// TODO check whether this needs to be changed to be coherent...
				singleDocResults = new DocumentResult();
			} else {
				// Gather match results for this document
				singleDocResults = computeMatches(goldDoc.getMarkings(), found.getMentions());
			}
			// Pass on our document's results to the encompassing dataset (for aggregation
			// prior to evaluation)
			datasetResult.addDocumentResult(singleDocResults);
		}

		// Now we can compute metrics for 1. a single document (local) and/or 2.
		// aggregate information for entire dataset (global)
		final DatasetEvaluation datasetEvaluation = evaluateDataset(datasetResult);
		datasetEvaluations.add(datasetEvaluation);
		for (final DocumentResult singleDocResults : datasetResult.getDocumentResults()) {
			if (singleDocResults == null) {
				continue;
			}
			// Evaluate for a document (e.g. precision for a sentence)
			final DocumentEvaluation singleDocEvaluation = evaluateDocument(singleDocResults);
			documentEvaluations.add(singleDocEvaluation);
			for (MentionResult mentionResults : singleDocResults.getMentionResults()) {
				if (mentionResults == null) {
					continue;
				}
				// Evaluate for a mention (e.g. degree of ambiguity of a mention)
				final MentionEvaluation singleMentionEvaluation = evaluateMention(mentionResults);
				mentionEvaluations.add(singleMentionEvaluation);
			}
		}

		evaluations.addAll(datasetEvaluations);
		evaluations.addAll(documentEvaluations);
		evaluations.addAll(mentionEvaluations);

		// If it's NIF, we unpack it, parse it accordingly and then evaluate it
		return evaluations;
	}

	/**
	 * Computes the DocumentResult for a given gold document and an annotated
	 * document.</br>
	 * <b>IMPORTANT NOTE</b>: The algorithm goes through mentions and markings aka.
	 * it goes from BOTH sides - gold standard and experiment results, but checks
	 * for UNIQUEness, making it exactly 1. Then it generates the DocumentResult
	 * based on the iteration.
	 * 
	 * @param goldDoc
	 * @param toEvaluate
	 * @return
	 */
	public DocumentResult computeMatches(final List<Marking> goldMarkings, final Collection<Mention> foundMentions) {
		if (goldMarkings == null || foundMentions == null) {
			return null;
		}

		try {
			final DocumentResult documentResult = new DocumentResult();
			// Found
			final Collection<Mention> mentions = foundMentions;
			// What we want found
			final List<Marking> markings = goldMarkings;

			// Grouped... mentions
			final Map<String, List<Mention>> offsetMentions = mapMentionsByOffsetLength(mentions);
			// Grouped... markings
			Map<String, List<Marking>> offsetMarkings = mapMarkingsByOffsetLength(markings);

			// Keep track what was already evaluated
			final Set<String> checked = new HashSet<>();

			// ----------------------------------------------
			// From gold standard side
			for (final Marking marking : markings) {
				try {
					final String k = key(marking);
					List<Mention> listMentionsOffset = offsetMentions.get(k);
					final Mention mention;
					if (listMentionsOffset == null || listMentionsOffset.size() == 0) {
						mention = null;
					} else if (listMentionsOffset.size() == 1) {
						mention = listMentionsOffset.get(0);
					} else {
						// What to do when there's more than one...
						// Take the first
						mention = listMentionsOffset.get(0);
						// TODO: Decide what to do when multiple possibilities are found...
					}

					// If these two have already been compared --> skip to next
					if (checked.contains(key(marking, mention))) {
						continue;
					}
					final MentionResult matchResult = computeMatch(marking, mention);
					documentResult.addMentionResult(matchResult);
					checked.add(key(marking, mention));
				} catch (GerbilException ge) {
					ge.printStackTrace();
				}
			}

			// ----------------------------------------------
			// From linker side (aka. what was found)
			for (final Mention mention : mentions) {
				final String k = key(mention);
				List<Marking> listMarkingsOffset = offsetMarkings.get(k);
				final Marking marking;
				if (listMarkingsOffset == null || listMarkingsOffset.size() == 0) {
					marking = null;
				} else if (listMarkingsOffset.size() == 1) {
					marking = listMarkingsOffset.get(0);
				} else {
					// What to do when there's more than one...
					// Take the first
					marking = listMarkingsOffset.get(0);
					// TODO: Decide what to do when multiple possibilities are found...
				}

				// If these two have already been compared --> skip to next
				if (checked.contains(key(marking, mention))) {
					continue;
				}
				final MentionResult matchResult = computeMatch(marking, mention);
				documentResult.addMentionResult(matchResult);
				checked.add(key(marking, mention));
			}

			return documentResult;
		} catch (GerbilException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Computes the match between a marking (NIF) and mention (internal)
	 * 
	 * @param mark
	 * @param m
	 * @return
	 */
	public MentionResult computeMatch(final Marking mark, final Mention m) {
		// Marking info:
		// https://github.com/dice-group/gerbil/wiki/Document-Markings-in-gerbil.nif.transfer
		final MentionResult result = new MentionResult();
		result.unpackFrom(mark, m);

		if (mark instanceof Meaning) {
			final Meaning meaning = (Meaning) mark;
			if (m != null && m.getAssignment() != null && meaning.containsUri(m.getAssignment().getAssignment())) {
				result.match = true;
			} else {
				result.match = false;
			}

			// Could do some more efficient checking e.g. set intersection and see how much
			// is left...
			// But containsUri may be doing sth. interesting itself, so let's roll with it
			// Set intersect in Java:
			// https://stackoverflow.com/questions/8882097/how-to-calculate-the-intersection-of-two-sets
			if (m != null) {
				for (PossibleAssignment possAss : m.getPossibleAssignments()) {
					if (possAss != null && possAss.getAssignment() != null) {
						result.containsUriMatches += meaning.containsUri(possAss.getAssignment()) ? 1 : 0;
					}
				}
			}

			if (result.containsUriMatches > 0) {
				result.inCandidates = true;
			} else {
				result.inCandidates = false;
			}
		}

		return result;
	}

	/**
	 * Generates a key from a marking (similar function called for Mention)
	 * 
	 * @param m
	 * @return
	 * @throws GerbilException
	 */
	protected String key(final Marking m) throws GerbilException {
		if (m instanceof Span) {
			final Span s = (Span) m;
			return super.key(s.getStartPosition(), s.getLength());
		} else {
			throw new GerbilException("Cannot create key for map due to not knowing offset/length of mention...",
					ErrorTypes.UNEXPECTED_EXCEPTION);
		}
	}

	/**
	 * Helper function to more easily match mentions and markings against each other
	 * 
	 * @param mentions
	 * @return
	 * @throws GerbilException
	 */
	protected Map<String, List<Marking>> mapMarkingsByOffsetLength(Collection<Marking> markings)
			throws GerbilException {
		final Map<String, List<Marking>> offsetMarkings = new HashMap<>();
		for (final Marking m : markings) {
			final String k = key(m);
			List<Marking> toPopulate;
			if ((toPopulate = offsetMarkings.get(k)) == null) {
				toPopulate = Lists.newArrayList();
				offsetMarkings.put(k, toPopulate);
			}
			toPopulate.add(m);
		}
		return offsetMarkings;
	}

	private String key(final Marking marking, final Mention mention) {
		final String markingText;
		if (marking instanceof Span) {
			final Span s = (Span) marking;
			markingText = s.getStartPosition() + "_" + s.getLength();
		} else {
			if (marking != null) {
				markingText = marking.toString();
			} else {
				markingText = "null_marking";
			}
		}
		final String mentionText;
		if (mention == null) {
			mentionText = "null_mention";
		} else {
			mentionText = "_" + mention.getOffset() + "_" + mention.getMention();
		}
		return markingText + mentionText;
	}

}
