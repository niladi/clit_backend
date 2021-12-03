package clit.combiner;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;

import experiment.PipelineItem;
import structure.datatypes.AnnotatedDocument;
import structure.datatypes.Mention;
import structure.datatypes.PossibleAssignment;
import structure.interfaces.clit.Combiner;

public abstract class AbstractCombiner implements Combiner {
	final int MAX_PARAMS = 0;
	final int MIN_PARAMS = 0;

	@Override
	public Collection<AnnotatedDocument> execute(final PipelineItem callItem, final AnnotatedDocument input) {
		// Ignore "input" and get it from the previous step
		final Collection<AnnotatedDocument> documents = callItem.getCopyOfAllDependencyResults();

		if (documents.size() < 2)
			throw new RuntimeException("Combiner requires two or more sources.");

		return combine(documents).makeMultiDocuments();
	}

	protected Map<String, List<Mention>> collectMentions(Collection<AnnotatedDocument> documents) {
		final Map<String, List<Mention>> mapMentions = new HashMap<>();

		// Fill mentionMap, grouping mentions belonging together in a list...
		for (final AnnotatedDocument document : documents) {
			// TODO NullPointer exception when items is null, e.g. when one of the linkers fails (added 19.06.2021
			// because of AIDA)
			for (final Mention mention : document.getMentions()) {
				final String key = mentionKey(mention);
				List<Mention> mentions = null;
				if ((mentions = mapMentions.get(key)) == null) {
					mentions = Lists.newArrayList();
					mapMentions.put(key, mentions);
				}
				mentions.add(mention);
			}
		}
		return mapMentions;
	}

	/**
	 * Collects all assignments for the given list of mentions.<br>
	 * Note: it is assumed that all passed Mention objects refer to the same mention
	 * / piece of text.
	 * 
	 * @param toMerge assignments to merge for the same mentions
	 * @return
	 */
	protected Map<String, List<PossibleAssignment>> collectAssignments(final Collection<Mention> toMerge) {
		final Iterator<Mention> it = toMerge.iterator();
		if (!it.hasNext()) {
			return null;
		}
		final Map<String, List<PossibleAssignment>> mapAssignments = new HashMap<>();

		// Populate assignment map
		while (it.hasNext()) {
			final Mention m = it.next();
			// Same structure for possible assignments
			for (final PossibleAssignment ass : m.getPossibleAssignments()) {
				if (ass == null) {
					// Ignore null assignments
					continue;
				}
				final String assKey = assignmentKey(ass);

				List<PossibleAssignment> listAssignment;
				if ((listAssignment = mapAssignments.get(assKey)) == null) {
					listAssignment = Lists.newArrayList();
					mapAssignments.put(assKey, listAssignment);
				}
				listAssignment.add(new PossibleAssignment(ass));
			}
		}
		return mapAssignments;
	}

	protected String mentionKey(final Mention mention) {
		return mention.getOffset() + "%%%" + mention.getMention() + mention.getOriginalMention();
	}

	protected String assignmentKey(final PossibleAssignment ass) {
		return ass.getAssignment();
	}

}
