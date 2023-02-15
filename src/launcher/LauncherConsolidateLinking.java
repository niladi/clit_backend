package launcher;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import linking.disambiguation.consolidation.SumConsolidator;
import linking.linkers.BabelfyLinker;
import linking.linkers.DBpediaSpotlightLinker;
import linking.linkers.OpenTapiocaLinker;
import structure.config.kg.EnumModelType;
import structure.datatypes.AnnotatedDocument;
import structure.interfaces.linker.Linker;
import structure.utils.datastructure.MentionUtils;

public class LauncherConsolidateLinking {

	public static void main(String[] args) {
		final String input = "Steve Jobs and Joan Baez are famous people";
		try {
			consolidateTest();
			// singleOpenTapioca(input);
			// singleDBpedia();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void singleOpenTapioca(final AnnotatedDocument document) throws Exception {
		final Linker linker = new OpenTapiocaLinker();
		// final String ret = linker.annotate("Steve Jobs and Joan Baez are famous
		// people");
		// System.out.println("Result:" + linker.annotate(input));
		linker.annotate(document);

		MentionUtils.displayMentions(document.getMentions());
		System.out.println("Res: " + document.getMentions());
	}

	private static void singleDBpedia() throws IOException {
		final DBpediaSpotlightLinker linker = new DBpediaSpotlightLinker();
		// final String ret = linker.annotate("Steve Jobs and Joan Baez are famous
		// people");
		final AnnotatedDocument document = new AnnotatedDocument("Steve Jobs and Joan Baez are famous people");
		linker.annotate(document);

		MentionUtils.displayMentions(document.getMentions());
		System.out.println("Res: " + document.getMentions());
	}

	private static void consolidateTest() throws Exception {
		final AnnotatedDocument input = new AnnotatedDocument("Steve Jobs and Joan Baez are famous people");

		final List<Linker> linkers = new ArrayList<>();
		final DBpediaSpotlightLinker linker1 = new DBpediaSpotlightLinker();
		linker1.confidence(0.0f);
//		final Linker linker1 = new OpenTapiocaLinker();
		final Linker linker2 = new OpenTapiocaLinker();
		final Linker linker3 = new BabelfyLinker(EnumModelType.DBPEDIA_FULL);
		
		//linkers.add(linker1);
		linkers.add(linker2);
//		linkers.add(linker3);
		// final String ret = linker.annotate("Steve Jobs and Joan Baez are famous
		// people");

		final boolean ALL_OR_KG = true;
		// final Collection<Mention> ret = linker.annotateMentions(input);

		final SumConsolidator consolidator = new SumConsolidator(linkers.toArray(new Linker[] {}));
		Map<Linker, AnnotatedDocument> linkerResults;
		try {
			linkerResults = consolidator.executeLinkers(input);

			final boolean output = true;

			if (output) {
				// Output annotations for each linker
				for (Entry<Linker, AnnotatedDocument> e : linkerResults.entrySet()) {
					System.out.print("Linker[" + e.getKey().getClass() + "]:");
					System.out.println(e.getValue().getMentions());
				}
				// results one by one
				System.out.println("Linker Count:" + linkerResults.size());
			}
			// results in map
			// System.out.println("Linker results:" + linkerResults);

			// Merge annotations by KG
			final Map<String, AnnotatedDocument> results;

			if (ALL_OR_KG) {
				final Map<String, AnnotatedDocument> tmpResults = new HashMap<>();
				final AnnotatedDocument tmp = consolidator.mergeAll(linkerResults);
				tmpResults.put("", tmp);
				results = tmpResults;
			} else {
				results = consolidator.mergeByKG(linkerResults);
			}

			// Merged results
			System.out.println("Results: " + results);

			// Display consolidated results
			for (Entry<String, AnnotatedDocument> e : results.entrySet()) {
				final AnnotatedDocument ret = e.getValue();
				MentionUtils.displayMentions(ret.getMentions());
			}
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		} finally {
		}
	}
}
