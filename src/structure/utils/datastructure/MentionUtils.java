package structure.utils.datastructure;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.tuple.Pair;

import com.google.common.collect.Lists;

import linking.disambiguation.scorers.embedhelp.EntitySimilarityService;
import structure.config.constants.Comparators;
import structure.config.constants.Strings;
import structure.config.kg.EnumModelType;
import structure.datatypes.Mention;
import structure.datatypes.PossibleAssignment;

public class MentionUtils {

	public static Collection<Mention> mergeEntityTypes(final Collection<Mention>... mentions) {

		if (mentions.length <= 1)
			throw new IllegalArgumentException("There should be multiple entity types");

		return Stream.of(mentions).filter(Objects::nonNull).flatMap(Collection::stream).collect(
				Collectors.toMap(MentionUtils::mentionToUniqueStr, m -> m, (m1, m2) -> {
					final Mention m = new Mention(m1);
					m.setEntityTypes(
							Stream.of(m1, m2).filter(Objects::nonNull)
									.map(Mention::getEntityTypes).filter(Objects::nonNull)
									.flatMap(Collection::stream)
									.collect(Collectors.toSet()));
					return m;
				})).values();
	}

	/**
	 * 
	 * 
	 * 
	 * @param multiMentions which to copy
	 * @return deep copy of these collections of mentions
	 */
	public static Collection<Collection<Mention>> copyMultiMentions(Collection<Collection<Mention>> multiMentions) {
		if (multiMentions == null || multiMentions.size() == 0) {
			return multiMentions;
		}

		final Collection<Collection<Mention>> retMentions = Lists.newArrayList();
		for (Collection<Mention> m : multiMentions) {
			retMentions.add(copyMentions(m));
		}
		return retMentions;
	}

	/**
	 * Does a deep copy of mentions
	 * 
	 * @param mentions which to copy
	 * @return deep copy of this collection of mentions
	 */
	public static Collection<Mention> copyMentions(Collection<Mention> mentions) {
		if (mentions == null || mentions.size() == 0) {
			return mentions;
		}
		final Collection<Mention> retMentions = Lists.newArrayList();
		for (Mention m : mentions) {
			if (m != null) {
				retMentions.add(new Mention(m));
			}
		}
		return retMentions;
	}

	/**
	 * Creates a Collection<Collection<Mention>> from Collection<Mention>
	 * 
	 * @param mention
	 * @return
	 */
	public static Collection<Collection<Mention>> makeMultiMentions(Collection<Mention> mentions) {
		if (mentions == null) {
			return null;
		}
		final Collection<Collection<Mention>> retMentions = Lists.newArrayList();
		retMentions.add(mentions);
		return retMentions;
	}

	/**
	 * Displays similarities of the mentions to each other
	 * 
	 * @param similarityService
	 * @param KG
	 * @param mentions
	 */
	public static void displaySimilarities(final EntitySimilarityService similarityService, final EnumModelType KG,
			List<Mention> mentions) {
		if (similarityService == null) {
			System.err.println("No similarity service defined.");
			return;
		}
		// Get all similarities
		for (int i = 0; i < mentions.size(); ++i) {
			for (int j = i + 1; j < mentions.size(); ++j) {
				if (mentions.get(i).getMention().equals(mentions.get(j).getMention())) {
					continue;
				}
				List<String> targets = Lists.newArrayList();
				for (PossibleAssignment ass : mentions.get(j).getPossibleAssignments()) {
					targets.add(ass.getAssignment());
				}

				System.out.println("Mention:" + mentions.get(i) + "->" + mentions.get(j));
				for (PossibleAssignment ass : mentions.get(i).getPossibleAssignments()) {
					// Sorted similarities
					final List<Pair<String, Double>> similarities = similarityService.computeSortedSimilarities(
							ass.getAssignment(), targets, Comparators.pairRightComparator.reversed());
					System.out.println("Source:" + ass);
					System.out.println(similarities.subList(0, Math.min(5, similarities.size())));
				}
			}
		}
	}

	/**
	 * Removes mentions from passed list which were detected based on the passed
	 * string as a Mention
	 * 
	 * @param key      non null key value corresponding to the text mention
	 * @param mentions collection of mentions from which to remove the mention with
	 *                 the appropriate key
	 */
	public static void removeStringMention(String key, Collection<Mention> mentions) {
		if (key == null) {
			throw new NullPointerException("Invalid key(null) for removal...");
		}
		Iterator<Mention> it = mentions.iterator();
		// final int initSize = mentions.size();
		while (it.hasNext()) {
			// Logic works for multiple
			if (it.next().getMention().equals(key)) {
				it.remove();
			}
		}
		// if (initSize == mentions.size()) {
		// System.out.println("COULD NOT FIND ELEMENT: " + key);
		// } else {
		// System.out.println("FOUND ELEMENT: [" + (initSize - mentions.size()) + "] x "
		// + key);
		// }
		// System.out.println("Mentions: " + mentions.toString());
		// System.out.println("-------------------------------");
	}

	/**
	 * Removes mentions from passed list which were detected based on the passed
	 * string as the original piece of text referring to it
	 * 
	 * @param key
	 * @param mentions
	 */
	public static void removeStringMentionOriginal(String key, Collection<Mention> mentions) {
		Iterator<Mention> it = mentions.iterator();
		while (it.hasNext()) {
			// Logic works for multiple
			if (it.next().getOriginalMention().equals(key)) {
				it.remove();
			}
		}
	}

	/**
	 * Removes mentions from passed list which were detected based on the passed
	 * string as the original piece of text referring to it
	 * 
	 * @param toRemove
	 * @param mentions
	 */
	public static void removeStringMentionOriginal(Collection<String> toRemove, Collection<Mention> mentions) {
		Iterator<Mention> it = mentions.iterator();
		while (it.hasNext()) {
			// Logic works for multiple
			if (toRemove.contains(it.next().getOriginalMention())) {
				it.remove();
			}
		}
	}

	/**
	 * Formats passed mentions in a specific way
	 * 
	 * @param mentions  list of scored mentions
	 * @param inputLine input line that was linked
	 * @return formatted string output
	 * @throws IOException
	 */
	public static String formatMentionsXML(List<Mention> mentions, final String inputLine) throws IOException {
		// mention\tstartoffset\tendoffset\tentityURI\tconfscore\n
		final StringWriter sw = new StringWriter();
		try (BufferedWriter bwResults = new BufferedWriter(sw)) {
			bwResults.write("<mentions>");
			// for (Map.Entry<String, Mention<Node>> e : sortedMentions.entrySet()) {
			int currIndex = -1;
			for (Mention m : mentions) {
				final String search = m.getOriginalMention();
				int foundIndex = inputLine.indexOf(search, currIndex);
				final String mention_text = "<mention><source>" + m.getMention() + "</source><original>"
						+ m.getOriginalMention() + "</original><assignment>" + m.getAssignment()
						+ "</assignment><offset>" + foundIndex + "</offset>" + "</mention>";
				currIndex = foundIndex + search.length();
				bwResults.write(mention_text);
				bwResults.newLine();
			}
			bwResults.write("</mentions>");
			bwResults.flush();
		}
		return sw.toString();
	}

	/**
	 * Displays mentions
	 * 
	 * @param mentions
	 */
	public static void displayMentions(Collection<? extends Mention> mentions) {
		System.out.println("#######################################################");
		System.out.println("Mention Details(" + mentions.size() + "):");
		final TreeMap<String, Mention> alphabeticalSortedMentions = new TreeMap<String, Mention>();
		final boolean detailed = true;
		for (Mention m : mentions) {
			alphabeticalSortedMentions.put(m.getMention() + "_" + m.getOriginalMention(), m);
		}
		// Display them
		final File outFile = new File("." + "/" + "cb_linked_output.txt");
		try {
			try (BufferedWriter bwOut = new BufferedWriter(new FileWriter(outFile))) {
				StringBuilder sb = new StringBuilder();
				for (Map.Entry<String, Mention> e : alphabeticalSortedMentions.entrySet()) {
					sb.setLength(0);
					final Mention m = e.getValue();
					if (detailed) {
						sb.append("Mention[" + m.getMention() + "; " + m.getDetectionConfidence() + "] ");
						sb.append(Strings.NEWLINE.val);
						sb.append("Original Text:" + m.getOriginalMention());
						sb.append(Strings.NEWLINE.val);
						sb.append("Possible assignments: "
								+ (m.getPossibleAssignments() != null ? m.getPossibleAssignments().size() : "None"));
						sb.append(Strings.NEWLINE.val);
						sb.append("Found assignment: " + m.getAssignment());
						sb.append(Strings.NEWLINE.val);
						sb.append("Found Assignment's Score: " + m == null ? "mention<null>"
								: m.getAssignment() == null ? "mention.ass<null>" : m.getAssignment().getScore());
						sb.append(Strings.NEWLINE.val);
						sb.append("--------------------------------------------------");
						sb.append(Strings.NEWLINE.val);
					} else {
						sb.append(m.getOriginalMention() + "(" + m.getMention() + "; " + m.getDetectionConfidence()
								+ ")\t\t-> " + m.getAssignment().getScore() + ":"
								+ m.getAssignment().getAssignment().toString());
						sb.append(Strings.NEWLINE.val);
					}
					bwOut.write(sb.toString());
					System.out.println(sb.toString());
				}
			}
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}

	/**
	 * Formats passed mentions in a specific way
	 * 
	 * @param mentions  list of scored mentions
	 * @param inputLine input line that was linked
	 * @return formatted string output
	 * @throws IOException
	 */
	public static String formatMentionsTabbedLines(List<Mention> mentions, final String inputLine) throws IOException {
		// mention\tstartoffset\tendoffset\tentityURI\tconfscore\n
		final String delim = "\t";
		final String lineSep = "\n";
		final StringWriter sw = new StringWriter();
		try (BufferedWriter bwResults = new BufferedWriter(sw)) {
			int currIndex = -1;
			for (Mention m : mentions) {
				final String search = m.getOriginalMention();
				int foundIndex = inputLine.indexOf(search, currIndex);
				final String mention_text = m.getMention() + delim + m.getOriginalMention() + delim + foundIndex + delim
						+ (foundIndex + (m.getOriginalMention().length())) + delim + m.getAssignment() + delim
						+ m.getAssignment().getScore() + lineSep;
				currIndex = foundIndex + search.length();
				bwResults.write(mention_text);
			}
			bwResults.flush();
		}
		return sw.toString();
	}

	/**
	 * Restrict a list of mentions to the mentions of another list of mentions
	 * 
	 * @param baseMentions   List of mentions that should be filtered
	 * @param filterMentions List of mentions to which baseMentions should be
	 *                       restricted
	 * @return The filtered list of mentions
	 */
	public static Collection<Mention> filterMentionsByMentions(Collection<Mention> baseMentions,
			Collection<Mention> filterMentions) {
		Collection<Mention> resultMentions = new ArrayList<>();

		if (baseMentions == null || filterMentions == null)
			return resultMentions;

		for (Mention baseMention : baseMentions) {
			String mentionText = baseMention.getMention();
			for (Mention filterMention : filterMentions) {
				if (filterMention.getOriginalMention().equals(mentionText)
						&& baseMention.getOffset() == filterMention.getOffset()) {
					// baseMention.setOffset(filterMention.getOffset());
					resultMentions.add(baseMention);
				}
			}
		}
		return resultMentions;
	}

	/**
	 * Considered as "strict" mention detection <br>
	 * Means we overwrite existing list of mentions with new mentions if no
	 * corresponding one exists and keep old ones that are found by the new
	 * system<br>
	 * We keep the references to the old ones that we have found and add a
	 * text-mention for the new ones, but we remove the old ones that are not
	 * referenced by the new list of mentions
	 * 
	 * @param oldMentions
	 * @param newMentions
	 * @return
	 */
	public static Collection<Mention> intersectAndAddNew(final Collection<Mention> oldMentions,
			final Collection<Mention> newMentions) {
		final Collection<Mention> newTextMentions = Lists.newArrayList();

		// Do an intersection between old and new mentions and then add the newly found
		// ones
		final Map<String, Mention> mapOldMentions = new HashMap<>();
		for (Mention mention : oldMentions) {
			final String key = mentionToUniqueStr(mention);
			mapOldMentions.put(key, mention);
		}

		for (Mention newMention : newMentions) {
			final String key = mentionToUniqueStr(newMention);
			final Mention val;
			if ((val = mapOldMentions.get(key)) != null) {
				// Add existing mention that was also found by MD
				newTextMentions.add(val);
			} else {
				// Add mention that was newly found by MD
				newTextMentions.add(createTextOnlyMention(newMention));
			}
		}

		return newTextMentions;
	}

	/**
	 * 
	 * @param newMentions mentions from which new possibilities for mentions may
	 *                    emerge
	 * @param oldMentions mentions we have as a base - newMentions may contain
	 *                    entries already within these
	 * @return the old mentions along with new mentions which did not exist yet
	 *         within the old mentions list
	 */
	public static Collection<Mention> unionAndKeepOldReferences(final Collection<Mention> oldMentions,
			final Collection<Mention> newMentions) {

		final Collection<Mention> retMentions = Lists.newArrayList();

		// Use set logic on text properties whether it's a new one or it exists
		final Set<String> mentionStrings = new HashSet<>();
		if (oldMentions != null)
			for (final Mention mention : oldMentions) {
				final String mentionStr = mentionToUniqueStr(mention);
				retMentions.add(mention);
				mentionStrings.add(mentionStr);
			}

		for (final Mention mention : newMentions) {
			final String mentionStr = mentionToUniqueStr(mention);
			if (!mentionStrings.contains(mentionStr)) {
				// current mention doesn't exist yet
				// so let's add it to the current list of mentions
				final Mention newTextMention = createTextOnlyMention(mention);
				retMentions.add(newTextMention);
			}
		}
		return retMentions;
	}

	/**
	 * Creates a mention only with textual content aka. no assignment nor possible
	 * assignments
	 * 
	 * @param mention
	 * @return
	 */
	private static Mention createTextOnlyMention(Mention mention) {
		return new Mention(mention.getMention(), (PossibleAssignment) null, mention.getOffset(),
				mention.getDetectionConfidence(), mention.getOriginalMention(), mention.getOriginalWithoutStopwords());
	}

	/**
	 * Main intent: for mention detection mention comparison, it should act as a
	 * kind of "hash" <br>
	 * Note: Ignores assignments. </br>
	 * 
	 * @param mention
	 * @return
	 */
	public static String mentionToUniqueStr(final Mention mention) {
		if (mention == null) {
			return "mention_null";
		}

		final String mentionText = mention.getMention();
		final String mentionOrigText = mention.getOriginalMention();
		final String mentionOrigNoStopwordsText = mention.getOriginalWithoutStopwords();
		final String mentionOffset = String.valueOf(mention.getOffset());

		final StringBuilder setText = new StringBuilder();
		setText.append(mentionText == null ? "_" : mentionText);
		setText.append("_");
		setText.append(mentionOrigText == null ? "_" : mentionText);
		setText.append("_");
		setText.append(mentionOrigNoStopwordsText == null ? "_" : mentionText);
		setText.append("_");
		setText.append(mentionOffset);
		return setText.toString();
	}

}
