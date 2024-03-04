package launcher.debug;

/**
 * This was for testing combinations of different linkers. The interfaces are deprecated since 29.10.2021 and were not
 * updated.
 */
public class LauncherTestLinker {

	public static void main(String[] args) {
//		final String text = "The Steve Jobs and Joan Baez are famous people, among others there is also Mr. Hernandes there additionally to Joan Baez and Joan herself.";
		final String text = "Napoleon was the emperor of the First French Empire .";

		try {
//			consolidateTest();

//			singleDBpedia();
//			singleOpenTapioca(text);

//			Collection<Mention> mentions = singleDBpediaMD(text);

//			Collection<Mention> entities = singleAidaCGED(text, mentions);
//			Collection<Mention> entities = singleBabelfyCGED(text, mentions);
//			Collection<Mention> entities = singleDBpediaCGED(text, mentions);
//			Collection<Mention> entities = singleFoxCGED(text, mentions);
//			Collection<Mention> entities = singleOpenTapiocaCGED(text, mentions);

//			Collection<Mention> mentions = singleAgnosMD(text);
//			Collection<Mention> candidates = singleAgnosCG(mentions);
//			Collection<Mention> entities = singleAgnosED(candidates);

//			Collection<Mention> mentions = singleAidaMD(text);
//			Collection<Mention> entities = singleAidaCGED(text, mentions);

//			Collection<Mention> mentions = singleDBpediaMD(text);
//			Collection<Mention> candidates = singleDBpediaCG(text, mentions);
//			Collection<Mention> entities = singleDBpediaED(text, candidates);

			// Collection<Mention> mentions = singleBabelfy(text);
//			Collection<Mention> candidates = singleBabelfyCG(text, mentions);
//			Collection<Mention> entities = singleBabelfyED(text, candidates);

//			Collection<Mention> mentions = singleOpenTapiocaMD(text);
//			Collection<Mention> candidates = singleOpenTapiocaCG(text, mentions);
//			Collection<Mention> entities = singleOpenTapiocaED(text, candidates);

//			Collection<Mention> mentions = singleDBpediaMD(text);
//			Collection<Mention> mentions = singleDBpediaMD(text);
//			testHIEA();
			// Collection<Mention> entities = singleMAG(text, mentions);

			// Geht nicht so gut, weil Spotlight nur Disambiguated Entities für 2
			// der 4 von Babelfy gefundenen Mentions zurückgibt
//			Collection<Mention> mentions = singleBabelfyMD(text);
//			Collection<Mention> candidates = singleDBpediaCG(text, mentions);
//			Collection<Mention> entities = singleDBpediaED(text, candidates);

//			final Collection<Mention> ret = new AidaLinker(EnumModelType.DBPEDIA_FULL).annotateMentions(text);
//			final Collection<Mention> ret = new DexterLinker(EnumModelType.DBPEDIA_FULL).annotateMentions(text);
//			final Collection<Mention> ret = new OpenTapiocaLinker().annotateMentions(text);
//			final Collection<Mention> ret = new EntityClassifierEULinker(null).annotateMentions(text);
//			final Collection<Mention> ret = new DBpediaSpotlightLinker().annotateMentions(text);
			
			
//			System.out.println(ret);
//			for (Mention m : ret)
//			{
//				System.out.println( m.getAssignment().getAssignment() +" -> "+ m.getAssignment().getScore());
//			}

			// TODO Implement for another annotator (e.g. MAG, or NIF based annotator from
			// GERBILs annotator.properties
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

//	private static Collection<Mention> singleAidaMD(String inputText) {
//		AidaLinker linker = new AidaLinker(EnumModelType.DEFAULT);
//		linker.init();
//		Collection<Mention> mentionsWithEntities = linker.detect(inputText);
//		MentionUtils.displayMentions(mentionsWithEntities);
//		return mentionsWithEntities;
//	}
//
//	private static Collection<Mention> singleAidaCGED(String inputText, Collection<Mention> mentions) {
//		AidaLinker linker = new AidaLinker(EnumModelType.DEFAULT);
//		linker.init();
//		Collection<Mention> mentionsWithEntities = linker.generateDisambiguate(inputText, mentions);
//		MentionUtils.displayMentions(mentionsWithEntities);
//		return mentionsWithEntities;
//	}
//
//	private static Collection<Mention> singleMAG(String text, Collection<Mention> mentions) throws IOException {
//		final MAGLinker linker = new MAGLinker(EnumModelType.DEFAULT);
//		Document document = makeDocument(text, mentions);
//		final Collection<Mention> entities = linker.performD2KB(document);
//		MentionUtils.displayMentions(entities);
//		return entities;
//	}
//
//	private static Collection<Mention> singleBabelfy(String text) throws GerbilException, IOException {
//		final BabelfyLinker linker = new BabelfyLinker();
//		final Collection<Mention> entities = linker.annotateMentions(text);
//		MentionUtils.displayMentions(entities);
//		return entities;
//	}
//
//	private static Collection<Mention> singleAgnosMD(String text) throws IOException {
//		AgnosLinker linker = new AgnosLinker(EnumModelType.DEFAULT);
//		linker.init();
//		MentionDetector mentionDetector = linker.getMentionDetector();
//		Collection<Mention> mentions = mentionDetector.detect(text);
//		return mentions;
//	}
//
//	private static Collection<Mention> singleAgnosCG(Collection<Mention> mentions) throws IOException {
//		AgnosLinker linker = new AgnosLinker(EnumModelType.DEFAULT);
//		linker.init();
//		CandidateGenerator candidateGenerator = linker.getCandidateGenerator();
//		candidateGenerator.generate(mentions);
//		return mentions;
//	}
//
//	private static Collection<Mention> singleAgnosED(String input, Collection<Mention> candidates) throws InterruptedException {
//		AgnosLinker linker = new AgnosLinker(EnumModelType.DEFAULT);
//		linker.init();
//		Disambiguator entityDisambiguator = linker.getEntityDisambiguator();
//		entityDisambiguator.disambiguate(input, candidates);
//		return candidates;
//	}
//
//	private static void singleOpenTapioca(final String text) throws Exception {
//		final Linker linker = new OpenTapiocaLinker();
//		final Collection<Mention> ret = linker.annotateMentions(text);
//		MentionUtils.displayMentions(ret);
//		System.out.println("Res: " + ret);
//	}
//
//	// ERROR: createNIF fails with Exception in thread "main"
//	// java.lang.NoSuchMethodError: 'org.apache.jena.rdf.model.Model
//	// org.apache.jena.rdf.model.Model.setNsPrefixes(org.apache.jena.shared.PrefixMapping)'
//	// TODO Try other NIF based annotator! Cf. GERBIL annotator.properties file
//	private static Collection<Mention> singleOpenTapiocaMD(String text) throws GerbilException, IOException {
//		// TODO Create OpenTapiocaMentionDetector
//		String url = "https://opentapioca.org/api/nif";
//		final NIFBasedAnnotatorWebservice linker = new NIFBasedAnnotatorWebservice(url);
//		// TODO Create method linker.detectMentions()
//		Document document = convertTextToDocument(text);
//		final Collection<MeaningSpan> ret = linker.performA2KBTask(document);
//		Collection<Mention> mentions = LinkerUtils.convertMeaningSpanToMention(text, ret);
//		linker.close();
//		return mentions;
//	}
//
//	private static Collection<Mention> singleOpenTapiocaCG(String text, Collection<Mention> mentions) {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	private static Collection<Mention> singleOpenTapiocaCGED(String inputText, Collection<Mention> mentions) {
//		OpenTapiocaLinker linker = new OpenTapiocaLinker(EnumModelType.DEFAULT);
//		linker.init();
//		Collection<Mention> mentionsWithEntities = linker.generateDisambiguate(inputText, mentions);
//		MentionUtils.displayMentions(mentionsWithEntities);
//		return mentionsWithEntities;
//	}
//
//	private static Collection<Mention> singleBabelfyCGED(String inputText, Collection<Mention> mentions) {
//		BabelfyLinker linker = new BabelfyLinker(EnumModelType.DEFAULT);
//		linker.init();
//		Collection<Mention> mentionsWithEntities = linker.generateDisambiguate(inputText, mentions);
//		MentionUtils.displayMentions(mentionsWithEntities);
//		return mentionsWithEntities;
//	}
//
//	private static Collection<Mention> singleBabelfyMD_with_GERBIL(String text) throws GerbilException, IOException {
//		// TODO Create BabelfyMentionDetector
//		final BabelfyAnnotator linker = new BabelfyAnnotator();
//		// TODO Create method linker.detectMentions()
//		Document document = convertTextToDocument(text);
//		final Collection<MeaningSpan> ret = linker.performA2KBTask(document);
//		Collection<Mention> mentions = LinkerUtils.convertMeaningSpanToMention(text, ret);
//		MentionUtils.displayMentions(mentions);
//		linker.close();
//		return mentions;
//	}
//
//	private static Collection<Mention> singleBabelfyCG_with_GERBIL(String text, Collection<Mention> mentions)
//			throws GerbilException, IOException {
//		// TODO Create BabelfyCandidateGenerator
//		final BabelfyAnnotator linker = new BabelfyAnnotator();
//		// TODO Create method linker.detectMentions()
//		Document document = makeDocument(text, mentions);
//		// TODO Can babelfy return a set of candidate entities? For now: GERBIL requests
//		// only the disambiguated
//		final Collection<MeaningSpan> ret = linker.performD2KBTask(document);
//		// Convert GERBIL MeaningSpan to Agnos Mention
//		Collection<Mention> mentionsWithEntityCandidates = new ArrayList<>();
//		for (MeaningSpan m : ret) {
//			String word = text.substring(m.getStartPosition(), m.getStartPosition() + m.getLength());
//			// Note: URIs returned by getUris point to the same object (its not multiple
//			// candidates!
//			// They are owl:sameAs, cf. org.aksw.gerbil.transfer.nif.Meaning.java)
//			// For now: simply take first one
//			String entityCandidateUri = m.getUris().iterator().next();
//			PossibleAssignment entityCandidate = new PossibleAssignment(entityCandidateUri);
//			Mention mention = new Mention(word, entityCandidate, m.getStartPosition());
//			mention.toCandidateGenerationResult();
//			mentionsWithEntityCandidates.add(mention);
//		}
//		MentionUtils.displayMentions(mentionsWithEntityCandidates);
//		linker.close();
//		return mentionsWithEntityCandidates;
//	}
//
//	private static void singleDBpedia() throws IOException {
//		final DBpediaSpotlightLinker linker = new DBpediaSpotlightLinker();
//		final Collection<Mention> ret = linker.annotateMentions("Steve Jobs and Joan Baez are famous people");
//		MentionUtils.displayMentions(ret);
//		System.out.println("Res: " + ret);
//	}
//
//	private static Collection<Mention> singleDBpediaMD(String text) throws IOException {
//		// TODO Create DBpediaSpotlightMentionDetector
//		final DBpediaSpotlightLinker linker = new DBpediaSpotlightLinker();
//		// TODO Create method linker.detectMentions()
//		final Collection<Mention> mentions = linker.annotateMentions(text);
//		// TODO Remove this workaround (of removing the entity candidates and the
//		// disambiguated entity from the mentions)
//		for (Mention mention : mentions) {
//			mention.toMentionDetectionResult();
//		}
//
//		MentionUtils.displayMentions(mentions);
////		System.out.println("Res: " + mentions);
//		return mentions;
//	}
//
//	// API: https://www.dbpedia-spotlight.org/api
//	// NOT provided by API, use workaround
//	private static Collection<Mention> singleDBpediaCG(String text, Collection<Mention> mentions) throws IOException {
//		// TODO Create DBpediaSpotlightCandidateGenerator
//		final DBpediaSpotlightLinker linker = new DBpediaSpotlightLinker();
//		// TODO Create method linker.generateCandidates() and remove this workaround (of
//		// asking DBpediaSpotlight
//		// to disambiguate texts that contain only mentions that were already detected
//		// in the MD
//		Collection<Mention> mentionsWithEntityCandidates = new ArrayList<>();
//		for (Mention mention : mentions) {
//			String mentionText = mention.getOriginalMention();
//			Collection<Mention> res = linker.annotateMentions(mentionText);
//			// TODO Improve this workaround (of removing the entity candidates and the
//			// disambiguated entity from the mentions)
//			for (Mention mentionWithEntityCandidate : res) {
//				// Use only the original mention from Spotlight (not possibly others, esp.
//				// substrings)
//				if (mentionWithEntityCandidate.getMention().equals(mentionText)) {
//					mentionWithEntityCandidate.toCandidateGenerationResult();
//					mentionsWithEntityCandidates.add(mentionWithEntityCandidate);
//				}
//			}
//		}
//		MentionUtils.displayMentions(mentionsWithEntityCandidates);
//		return mentionsWithEntityCandidates;
//	}
//
//	private static Collection<Mention> singleDBpediaED(String text, Collection<Mention> mentions) throws IOException {
//		// TODO Create DBpediaSpotlightEntityDisambiguator
//		final DBpediaSpotlightLinker linker = new DBpediaSpotlightLinker();
//		// TODO Create method linker.disambiguateEntities() and remove this workaround
//		// (of letting Spotlight
//		// disambiguate the text and match the results with the existing entity
//		// candidates)
//		final Collection<Mention> res = linker.annotateMentions(text);
//		// Compare if the disambiguated entity of Spotlight was in the set of entity
//		// candidates
//		for (Mention mention : mentions) {
//			for (Mention mentionWithDisambiguatedEntities : res) {
//				if (mention.getOriginalMention().equals(mentionWithDisambiguatedEntities.getOriginalMention())) {
//					// Add disambiguated entity to the mention
//					PossibleAssignment disambiguatedEntity = mentionWithDisambiguatedEntities.getAssignment();
//					for (PossibleAssignment entityCandidate : mention.getPossibleAssignments()) {
//						if (entityCandidate.getAssignment().equals(disambiguatedEntity.getAssignment())) {
//							mention.assignTo(disambiguatedEntity);
//						}
//					}
//				}
//			}
//		}
//		MentionUtils.displayMentions(mentions);
//		return mentions;
//	}
//
//	private static Collection<Mention> singleDBpediaCGED(String inputText, Collection<Mention> mentions) {
//		DBpediaSpotlightLinker linker = new DBpediaSpotlightLinker(EnumModelType.DEFAULT);
//		linker.init();
//		Collection<Mention> mentionsWithEntities = linker.generateDisambiguate(inputText, mentions);
//		MentionUtils.displayMentions(mentionsWithEntities);
//		return mentionsWithEntities;
//	}
//
//	// ERROR: HTTP 404
//	private static Collection<Mention> singleDBpediaCGOld(String text, Collection<Mention> mentions)
//			throws GerbilException, IOException {
//		// TODO Use own DBpediaSpotlightCandidateGenerator instead of GERBIL
//		// final DBpediaSpotlightCandidateGenerator cg = new
//		// DBpediaSpotlightCandidateGenerator();
//		final SpotlightAnnotator spotlightAnntotator = new SpotlightAnnotator(
//				"https://model.dbpedia-spotlight.org/en/");
//		ArrayList<Marking> markings = new ArrayList<Marking>();
//		for (Mention m : mentions) {
//			String mentionText = m.getOriginalMention();
//			int offset = m.getOffset();
//			markings.add(new SpanImpl(offset, mentionText.length()));
//		}
//		Document document = new DocumentImpl(text, "http://a.de", markings);
//		List<MeaningSpan> ret = spotlightAnntotator.performD2KBTask(document);
//		System.out.println(ret.toString());
//		spotlightAnntotator.close();
//
//		return null;
//	}
//
//	private static Collection<Mention> singleFOXCGED(String inputText, Collection<Mention> mentions) {
//		FOXLinker linker = new FOXLinker(EnumModelType.DEFAULT);
//		linker.init();
//		Collection<Mention> mentionsWithEntities = linker.generateDisambiguate(inputText, mentions);
//		MentionUtils.displayMentions(mentionsWithEntities);
//		return mentionsWithEntities;
//	}
//
//
//	private static void consolidateTest() {
//		final List<Linker> linkers = new ArrayList<>();
//		final DBpediaSpotlightLinker linker1 = new DBpediaSpotlightLinker();
//		linker1.confidence(0.0f);
//		// final Linker linker1 = new OpenTapiocaLinker();
//		final Linker linker2 = new OpenTapiocaLinker();
//		final Linker linker3 = new BabelfyLinker(EnumModelType.DBPEDIA_FULL);
//		linkers.add(linker1);
//		linkers.add(linker2);
//		linkers.add(linker3);
//		// final String ret = linker.annotate("Steve Jobs and Joan Baez are famous
//		// people");
//
//		final String input = "Steve Jobs and Joan Baez are famous people";
//		// final Collection<Mention> ret = linker.annotateMentions(input);
//
//		final SumConsolidator consolidator = new SumConsolidator(linkers.toArray(new Linker[] {}));
//		Map<Linker, Collection<Mention>> linkerResults;
//		try {
//			linkerResults = consolidator.executeLinkers(input);
//			// Output annotations for each linker
//			for (Entry<Linker, Collection<Mention>> e : linkerResults.entrySet()) {
//				System.out.print("Linker[" + e.getKey().getClass() + "]:");
//				System.out.println(e.getValue());
//			}
//			System.out.println("Linker Count:" + linkerResults.size());
//			System.out.println("Linker results:" + linkerResults);
//
//			// Merge annotations by KG
//			final Map<String, Collection<Mention>> results = consolidator.mergeByKG(linkerResults);
//
//			// Display consolidated results
//			for (Entry<String, Collection<Mention>> e : results.entrySet()) {
//				final Collection<Mention> ret = e.getValue();
//				MentionUtils.displayMentions(ret);
//
//			}
//		} catch (InterruptedException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		} finally {
//		}
//	}
//
//	private static void testHIEA() throws IOException {
//		String textWithMentions = "The <entity>University of Leipzig</entity> in <entity>Leipzig</entity>.";
//		String urlParameters = "type=agdistis&text=" + URLEncoder.encode(textWithMentions, "UTF-8");
//		byte[] postData = urlParameters.getBytes(StandardCharsets.UTF_8);
//		int postDataLength = postData.length;
//		String request = "http://akswnc9.informatik.uni-leipzig.de:8113/AGDISTIS";
//		URL url = new URL(request);
//		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
//		conn.setDoOutput(true);
//		conn.setInstanceFollowRedirects(false);
//		conn.setRequestMethod("POST");
//		conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
//		conn.setRequestProperty("charset", "utf-8");
//		conn.setRequestProperty("Content-Length", Integer.toString(postDataLength));
//		conn.setUseCaches(false);
//		conn.getOutputStream().write(postData);
//		Reader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
//
//		StringBuilder sb = new StringBuilder();
//		for (int c; (c = in.read()) >= 0;)
//			sb.append((char) c);
//		String response = sb.toString();
//
//		System.out.println(response);
//	}
//
//	private static Document convertTextToDocument(String text) {
//		return new DocumentImpl(text);
//	}
//
//	private static Document makeDocument(String text, Collection<Mention> mentions) {
//		Document document = new DocumentImpl(text);
//		for (Mention m : mentions) {
//			String mentionText = m.getOriginalMention();
//			int offset = m.getOffset();
//			Marking marking = new SpanImpl(offset, mentionText.length());
//			document.addMarking(marking);
//		}
//		return document;
//	}
}
