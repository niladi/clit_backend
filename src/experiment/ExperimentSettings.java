package experiment;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import com.google.common.collect.Lists;

import clit.APIComponent;
import clit.combiner.IntersectCombiner;
import clit.combiner.UnionCombiner;
import clit.eval.NIFBaseEvaluator;
import clit.eval.explainer.PrecisionRecallF1Explainer;
import clit.eval.interfaces.Explainer;
import clit.splitter.CopySplitter;
import clit.translator.TranslatorDBpediaToWikidata;
import clit.translator.TranslatorWikidataToDBpedia;
import linking.candidategeneration.DBpediaLookupCandidateGenerator;
import linking.candidategeneration.WikidataDictCandidateGenerator;
import linking.linkers.AidaLinker;
import linking.linkers.BabelfyLinker;
import linking.linkers.DBpediaSpotlightLinker;
import linking.linkers.EntityClassifierEULinker;
import linking.linkers.FOXLinker;
import linking.linkers.Falcon2Linker;
import linking.linkers.OpenTapiocaLinker;
import linking.linkers.RadboudLinker;
import linking.linkers.TagMeLinker;
import linking.linkers.TextRazorLinker;
import linking.mentiondetection.exact.SpaCyMentionDetector;
import structure.config.constants.EnumPipelineType;
import structure.datatypes.AnnotatedDocument;
import structure.interfaces.clit.Combiner;
import structure.interfaces.clit.Filter;
import structure.interfaces.clit.Splitter;
import structure.interfaces.clit.Translator;
import structure.interfaces.linker.Linker;
import structure.interfaces.pipeline.CandidateGenerator;
import structure.interfaces.pipeline.CandidateGeneratorDisambiguator;
import structure.interfaces.pipeline.Disambiguator;
import structure.interfaces.pipeline.Evaluator;
import structure.interfaces.pipeline.FullAnnotator;
import structure.interfaces.pipeline.MentionDetector;
import structure.interfaces.pipeline.PipelineComponent;

public enum ExperimentSettings {
	INSTANCE();

	// private final HashMap<Linker, Collection<ExperimentType>> mapping = new
	// HashMap<>();
	private final Map<String, Collection<EnumPipelineType>> linkerTasktypeMapping = new HashMap<>();
	private final Map<String, Class<? extends PipelineComponent>> componentClasses = new HashMap<>();
	private final Map<String, PipelineComponent> apiComponentClasses = new HashMap<>();
//	= Collections
//			.unmodifiableMap(new HashMap<String, Collection<EnumPipelineType>>() {
//				private static final long serialVersionUID = 1L;
//				{
//				}
//			});

	/**
	 * Map assigning linker names (strings) to linker classes <br>
	 * <br>
	 * Note: Primary potential attack vector as reflection is used on these.
	 * Therefore: restrict access to these maps, among others by making the
	 * unmodifiable (e.g. through Collections.unmodifiableMap(...) and losing the
	 * reference to the modifiable version)
	 */
	private final Map<String, Class<? extends PipelineComponent>> linkerClasses = new HashMap<>();
//			Collections.unmodifiableMap(new HashMap<String, String>() {
//		private static final long serialVersionUID = 1L;
//		{
//			// Linkers
//			put("AIDA", AidaLinker.class.getName());//
//			put("Agnos", AgnosLinker.class.getName());//
//			put("DBpediaSpotlight", DBpediaSpotlightLinker.class.getName());//
//			put("Babelfy", BabelfyLinker.class.getName());//
//			put("EntityClassifierEULinker", EntityClassifierEULinker.class.getName());//
//			put("FOX", FOXLinker.class.getName());//
//			put("MAG", MAGLinker.class.getName());//
//			put("OpenTapioca", OpenTapiocaLinker.class.getName());//
//			put("REL", RadboudLinker.class.getName());//
//			// Mention detection
//			put("spaCy", SpaCyMentionDetector.class.getName());//
//			// Candidate generation
//			put("WikidataDict", WikidataDictCandidateGenerator.class.getName());//
//			put("DBpediaLookup", DBpediaLookupCandidateGenerator.class.getName());//
//		}
//	});

	private ExperimentSettings() {
		// Add agnos
		// addComponent("Agnos", AgnosLinker.class);

		// AIDA - unresponsive
		addComponent("AIDA", AidaLinker.class);

		// Babelfy
		addComponent("Babelfy", BabelfyLinker.class);

		// DBpediaSpotlight
		addComponent("DBpediaSpotlight", DBpediaSpotlightLinker.class);

		// EntityClassifierEULinker - unresponsive
		addComponent("EntityClassifierEULinker", EntityClassifierEULinker.class);

		// FOX - unresponsive
		addComponent("FOX", FOXLinker.class);

		// OpenTapioca
		addComponent("OpenTapioca", OpenTapiocaLinker.class);

		// TagMe
		addComponent("TagMe", TagMeLinker.class);

		// Radboud / REL
		addComponent("REL", RadboudLinker.class);

		// MAG
		// addComponent("MAG", MAGLinker.class, EnumPipelineType.CG_ED);

		// TextRazor
		addComponent("TextRazor", TextRazorLinker.class);

		// Falcon 2.0
		addComponent("Falcon 2.0", Falcon2Linker.class);//

		// spaCy - MD
		addComponent("spaCy", SpaCyMentionDetector.class, EnumPipelineType.MD);

		// Wikidata CG dictionary - CG
		addComponent("WikidataDict", WikidataDictCandidateGenerator.class, EnumPipelineType.CG);

		// DBpediaLookFinder
		addComponent("DBpediaLookup", DBpediaLookupCandidateGenerator.class, EnumPipelineType.CG);//

		// Load CLiT API components from the properties files
		// for each of them instantiate an APIComponent which will then be used by
		// APIComponentCommunicator to actually communicate with it
		final APIComponent apiComponent = new APIComponent("google.com", "Test linker...") {

			@Override
			public Collection<AnnotatedDocument> execute(PipelineItem callItem, AnnotatedDocument document)
					throws Exception {
				// TODO Auto-generated method stub
				return null;
			}
		};
		addAPIComponent(apiComponent, EnumPipelineType.MD);
	}

	/**
	 * Adds a component and tries to identify how it may be used based on
	 * implemented interfaces
	 * 
	 * @param name      key in map and display name for front-end (for now at least)
	 * @param className
	 */
	private void addComponent(final String name, final Class<? extends Linker> className) {
		if (className == null)
			throw new RuntimeException("Cannot accept a component without an associated class to instantiate it.");
		// Try to "smartly" see what it can do
		final Set<EnumPipelineType> tasks = new HashSet<>();// Lists.newArrayList();

		// Single-step components
		if (MentionDetector.class.isAssignableFrom(className)) {
			tasks.add(EnumPipelineType.MD);
		}
		if (CandidateGenerator.class.isAssignableFrom(className)) {
			tasks.add(EnumPipelineType.CG);
		}
		if (Disambiguator.class.isAssignableFrom(className)) {
			tasks.add(EnumPipelineType.ED);
		}

		// Composites...
		if (CandidateGeneratorDisambiguator.class.isAssignableFrom(className)) {
			tasks.add(EnumPipelineType.CG_ED);
		}

		// Complete Linker instances
		if (Linker.class.isAssignableFrom(className) || FullAnnotator.class.isAssignableFrom(className)) {
			// It's a linker, so add MD and CG_ED to it through our hacks
			tasks.add(EnumPipelineType.MD);
			tasks.add(EnumPipelineType.CG_ED);
			tasks.add(EnumPipelineType.FULL);
		}

		if (Disambiguator.class.isAssignableFrom(className)) {
			tasks.add(EnumPipelineType.ED);
		}

		if (tasks == null || tasks.size() < 1) {
			addComponent(name, className, (EnumPipelineType[]) null);
		} else {
			addComponent(name, className, tasks.toArray(new EnumPipelineType[0]));
		}
	}

	private void addAPIComponent(final APIComponent apiComponent, EnumPipelineType... enumPipelineTypes) {
		Collection<EnumPipelineType> pipelineTypes = Lists.newArrayList();
		if (enumPipelineTypes != null && enumPipelineTypes.length > 0) {
			for (EnumPipelineType type : enumPipelineTypes) {
				pipelineTypes.add(type);
			}
			// Allows task-specific grouping of components
			linkerTasktypeMapping.put(apiComponent.getDisplayName(), pipelineTypes);
		}
		// Add it to the "normal" components
		componentClasses.put(apiComponent.getDisplayName(), APIComponent.class);

		// Keep track of this specific API component
		apiComponentClasses.put(apiComponent.getDisplayName(), apiComponent);

	}

	private void addComponent(final String name, final Class<? extends PipelineComponent> className,
			EnumPipelineType... enumPipelineTypes) {
		if (className == null) {
			throw new RuntimeException("Tried to add a component without instantiation path...");
		}

		// NULL --> it could be anything, so add it
		// It's a linker --> add it as a linker
		if (enumPipelineTypes == null || containsLinker(enumPipelineTypes)) {
			linkerClasses.put(name, className);
		}
		if (enumPipelineTypes != null && enumPipelineTypes.length > 0) {
			Collection<EnumPipelineType> pipelineTypes = Lists.newArrayList();
			for (EnumPipelineType type : enumPipelineTypes) {
				pipelineTypes.add(type);
			}
			linkerTasktypeMapping.put(name, pipelineTypes);
		}

		componentClasses.put(name, className);
	}

	boolean containsLinker(EnumPipelineType[] enumPipelineTypes) {
		for (EnumPipelineType e : enumPipelineTypes) {
			if (EnumPipelineType.FULL == e) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Return the linkers that can perform a specific experiment type
	 * 
	 * @param experimentType
	 * @return
	 */
	public static List<String> getLinkerForExperimentType(EnumPipelineType experimentType) {
		List<String> linkers = new ArrayList<>();
		for (String key : INSTANCE.linkerTasktypeMapping.keySet()) {
			if (INSTANCE.linkerTasktypeMapping.get(key).contains(experimentType)) {
				linkers.add(key);
			}
		}
		return linkers;
	}

	/**
	 * Map assigning linker names (strings) to linker classes <br>
	 * <br>
	 * Note: Primary potential attack vector as reflection is used on these.
	 * Therefore: restrict access to these maps, among others by making the
	 * unmodifiable (e.g. through Collections.unmodifiableMap(...) and losing the
	 * reference to the modifiable version)
	 */
	private static final Map<String, Class<? extends Translator>> translatorClasses = Collections
			.unmodifiableMap(new HashMap<String, Class<? extends Translator>>() {
				private static final long serialVersionUID = 1L;
				{
					// Translators

					// DBpedia to Wikidata
					put("DBP2WD", TranslatorDBpediaToWikidata.class);
					// Wikidata to DBpedia
					put("WD2DBP", TranslatorWikidataToDBpedia.class);
				}
			});

	/**
	 * Map assigning evaluator names (strings) to evaluator classes <br>
	 * <br>
	 * Note: Primary potential attack vector as reflection is used on these.
	 * Therefore: restrict access to these maps, among others by making the
	 * unmodifiable (e.g. through Collections.unmodifiableMap(...) and losing the
	 * reference to the modifiable version)
	 */
	private static final Map<String, Class<? extends Evaluator>> evaluatorClasses = Collections
			.unmodifiableMap(new HashMap<String, Class<? extends Evaluator>>() {
				private static final long serialVersionUID = 1L;
				{
					// Translators

					// Base evaluator for Precision, Recall, F1
					put("Base Evaluator (NIF, Precision, Recall, F1)", NIFBaseEvaluator.class);
				}
			});

	/**
	 * Map assigning evaluator names (strings) to evaluator classes <br>
	 * <br>
	 * Note: Primary potential attack vector as reflection is used on these.
	 * Therefore: restrict access to these maps, among others by making the
	 * unmodifiable (e.g. through Collections.unmodifiableMap(...) and losing the
	 * reference to the modifiable version)
	 */
	private static final Map<String, Class<? extends Explainer>> explainerClasses = Collections
			.unmodifiableMap(new HashMap<String, Class<? extends Explainer>>() {
				private static final long serialVersionUID = 1L;
				{
					// Translators

					// Base evaluator for Precision, Recall, F1
					put("Explainer based on Precision, Recall and F1 measure.", PrecisionRecallF1Explainer.class);
				}
			});

	/**
	 * Map assigning filter names (strings) to filter classes <br>
	 * <br>
	 * Note: Primary potential attack vector as reflection is used on these.
	 * Therefore: restrict access to these maps, among others by making the
	 * unmodifiable (e.g. through Collections.unmodifiableMap(...) and losing the
	 * reference to the modifiable version)
	 */
	private static final Map<String, Class<? extends Filter>> filterClasses = Collections
			.unmodifiableMap(new HashMap<String, Class<? extends Filter>>() {
				private static final long serialVersionUID = 1L;
				{
					// Filters
					// no filter classes yet defined <3
				}
			});

	/**
	 * Map assigning linker names (strings) to linker classes <br>
	 * <br>
	 * Note: Primary potential attack vector as reflection is used on these.
	 * Therefore: restrict access to these maps, among others by making the
	 * unmodifiable (e.g. through Collections.unmodifiableMap(...) and losing the
	 * reference to the modifiable version)
	 */
	private static final Map<String, Class<? extends Combiner>> combinerClasses = Collections
			.unmodifiableMap(new HashMap<String, Class<? extends Combiner>>() {
				private static final long serialVersionUID = 1L;
				{
					// combiners: union and intersection
					put("union", UnionCombiner.class);
					put("intersection", IntersectCombiner.class);
				}
			});

	/**
	 * Map assigning linker names (strings) to linker classes <br>
	 * <br>
	 * Note: Primary potential attack vector as reflection is used on these.
	 * Therefore: restrict access to these maps, among others by making the
	 * unmodifiable (e.g. through Collections.unmodifiableMap(...) and losing the
	 * reference to the modifiable version)
	 */
	private static final Map<String, Class<? extends Splitter>> splitterClasses = Collections
			.unmodifiableMap(new HashMap<String, Class<? extends Splitter>>() {
				private static final long serialVersionUID = 1L;
				{
					// splitters: copy
					put("copy", CopySplitter.class);
				}
			});

	/**
	 * Returns a case insensitive copy of an unmodifiable SPLITTER map
	 * 
	 * @return case insensitive map <3
	 */
	public static Map<String, Class<? extends Splitter>> getSplitterClassesCaseInsensitive() {
		final Map<String, Class<? extends Splitter>> retMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
		retMap.putAll(splitterClasses);
		return retMap;
	}

	/**
	 * Returns a case insensitive copy of an unmodifiable LINKER map
	 * 
	 * @return case insensitive map <3
	 */
	public static Map<String, Class<? extends PipelineComponent>> getLinkerClassesCaseInsensitive() {
		final Map<String, Class<? extends PipelineComponent>> retMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
		retMap.putAll(INSTANCE.linkerClasses);
		retMap.putAll(INSTANCE.translatorClasses);
		retMap.putAll(INSTANCE.combinerClasses);
		retMap.putAll(INSTANCE.splitterClasses);
		retMap.putAll(INSTANCE.filterClasses);
		retMap.putAll(INSTANCE.evaluatorClasses);
		return retMap;
	}

	/**
	 * Returns a case insensitive copy of an unmodifiable LINKER map
	 * 
	 * @return case insensitive map <3
	 */
	public static Set<String> getComponentNamesCaseInsensitive() {
		final Set<String> retSet = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
		return INSTANCE.linkerTasktypeMapping.keySet();
	}

	/**
	 * Returns a case insensitive copy of an unmodifiable COMBINER map
	 * 
	 * @return case insensitive map <3
	 */
	public static Map<String, Class<? extends Combiner>> getCombinerClassesCaseInsensitive() {
		final Map<String, Class<? extends Combiner>> retMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
		retMap.putAll(combinerClasses);
		return retMap;
	}

	/**
	 * Returns a case insensitive copy of an unmodifiable TRANSLATOR map
	 * 
	 * @return case insensitive map <3
	 */
	public static Map<String, Class<? extends Translator>> getTranslatorClassesCaseInsensitive() {
		final Map<String, Class<? extends Translator>> retMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
		retMap.putAll(translatorClasses);
		return retMap;
	}

	/**
	 * Returns a case insensitive copy of an unmodifiable FILTER map
	 * 
	 * @return case insensitive map <3
	 */
	public static Map<String, Class<? extends Filter>> getFilterClassesCaseInsensitive() {
		final Map<String, Class<? extends Filter>> retMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
		retMap.putAll(filterClasses);
		return retMap;
	}

	/**
	 * Return a collection of possible linkers
	 * 
	 * @return a copy of possible linkers
	 */
	public static final Set<String> getLinkerNames() {
		return new HashSet<>(INSTANCE.linkerClasses.keySet());
	}

	/**
	 * Return a collection of possible translators
	 * 
	 * @return a copy of possible translators
	 */
	public static final Set<String> getTranslatorNames() {
		return new HashSet<>(translatorClasses.keySet());
	}

	/**
	 * Return a collection of possible combiners
	 * 
	 * @return a copy of possible combiners
	 */
	public static Collection<? extends String> getCombinerNames() {
		return new HashSet<>(combinerClasses.keySet());
	}

	/**
	 * Return a collection of possible splitters
	 * 
	 * @return a copy of possible splitters
	 */
	public static Collection<? extends String> getSplitterNames() {
		return new HashSet<>(splitterClasses.keySet());
	}

	/**
	 * Return a collection of possible filters
	 * 
	 * @return a copy of possible filters
	 */
	public static Collection<? extends String> getFilterNames() {
		return new HashSet<>(filterClasses.keySet());
	}

	public static Collection<? extends String> getEvaluatorNames() {
		return new HashSet<>(evaluatorClasses.keySet());
	}

	public static Collection<? extends String> getExplainerNames() {
		return new HashSet<>(explainerClasses.keySet());
	}

	public static Map<String, Class<? extends PipelineComponent>> getComponentClassesCaseInsensitive() {
		final Map<String, Class<? extends PipelineComponent>> retMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
		retMap.putAll(INSTANCE.componentClasses);
		return retMap;
	}

	public static Map<String, PipelineComponent> getAPIComponentClassesCaseInsensitive() {
		final Map<String, PipelineComponent> retMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
		retMap.putAll(INSTANCE.apiComponentClasses);
		return retMap;
	}

}
