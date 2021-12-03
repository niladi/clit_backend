package clit.translator;

import java.util.Collection;
import java.util.Iterator;

import com.google.common.collect.Lists;

import experiment.PipelineItem;
import structure.datatypes.AnnotatedDocument;
import structure.datatypes.Mention;
import structure.datatypes.PossibleAssignment;
import structure.interfaces.clit.Translator;

public abstract class AbstractTranslator implements Translator {
	protected final boolean REMOVE_NOT_FOUND_ENTITIES = true;
	protected final boolean TRANSLATE_CANDIDATE_ENTITIES = true;

	/**
	 * Does the heavy lifting for the translation...</br>
	 * Translates a collection of mentions from one KG's namespace to another's
	 * 
	 * @param mentions mentions to be translated
	 * @return translated mentions
	 */
	@Override
	public AnnotatedDocument translate(AnnotatedDocument input) {
		// move mentions into this list...
		final Collection<Mention> resMentions = Lists.newArrayList();
		final Iterator<Mention> itMentions = input.getMentions().iterator();
		// go through each mention, go through each entity, translate
		while (itMentions.hasNext()) {
			// for every mention
			final PossibleAssignment translatedAssignment;
			final Mention m = itMentions.next();

			//
			// START - translating assigned entity
			//
			final PossibleAssignment oldAssignment = m.getAssignment();
			// Translates old assignment into a new assignment
			final String translatedEntity = translate(oldAssignment.getAssignment());
			// And all other mentions associated
			// new PossibleAssignment(newAssignment, score)
			if (translatedEntity == null) {
				// no translation was found...
				if (REMOVE_NOT_FOUND_ENTITIES) {
					// aka. don't transfer to the wanted list
					translatedAssignment = null;
				} else {
					// Keep same entity aka. transfer as-such to the list
					translatedAssignment = oldAssignment;
				}
			} else {
				translatedAssignment = new PossibleAssignment(translatedEntity, oldAssignment.getScore());
			}
			//
			// END - translating assigned entity
			//

			//
			// START - Take care of possible candidates...!
			//
			final Collection<PossibleAssignment> translatedCandidates;
			if (TRANSLATE_CANDIDATE_ENTITIES) {
				// Means we should also translate possible candidate entities
				final Collection<PossibleAssignment> oldCandidates = m.getPossibleAssignments();
				translatedCandidates = Lists.newArrayList();
				for (PossibleAssignment candidate : oldCandidates) {
					final String translatedCandidate = translate(candidate.getAssignment());
					if (translatedCandidate == null) {
						if (REMOVE_NOT_FOUND_ENTITIES) {
							// "remove un-found candidate" aka. ignore current candidate and move on to next
							continue;
						} else {
							// no new one found, so add old one
							translatedCandidates.add(candidate);
						}
					} else {
						// new translation found, so add it!
						translatedCandidates.add(new PossibleAssignment(translatedCandidate, candidate.getScore()));
					}
				}
			} else {
				translatedCandidates = null;
			}
			//
			// END - Take care of possible candidates...!
			//

			final Mention translatedMention = new Mention(m);
			translatedMention.assignTo(translatedAssignment);
			if (TRANSLATE_CANDIDATE_ENTITIES) {
				translatedMention.updatePossibleAssignments(translatedCandidates);
			}
			resMentions.add(translatedMention);

			// itMentions.remove();
		}

		// create new document and set mentions
		final AnnotatedDocument result = (AnnotatedDocument) input.clone();
		result.setMentions(resMentions);
		return result;//
	}

	@Override
	public Collection<AnnotatedDocument> execute(final PipelineItem callItem, final AnnotatedDocument input) {
		final AnnotatedDocument document = callItem.getCopyOfSingleDependencyResult();
		return translate(document).makeMultiDocuments();
	}
}
