package experiment;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import com.google.common.collect.Lists;

import clit.APIComponent;
import clit.APIPropertyLoader;
import clit.combiner.IntersectCombiner;
import clit.combiner.UnionCombiner;
import clit.splitter.CopySplitter;
import clit.translator.TranslatorDBpediaToWikidata;
import clit.translator.TranslatorWikidataToDBpedia;
import linking.candidategeneration.DBpediaLookupCandidateGenerator;
import linking.candidategeneration.Falcon2CandidateGenerator;
import linking.candidategeneration.WikidataDictCandidateGenerator;
import linking.linkers.AidaLinker;
import linking.linkers.BabelfyLinker;
import linking.linkers.DBpediaSpotlightLinker;
import linking.linkers.EntityClassifierEULinker;
import linking.linkers.FOXLinker;
import linking.linkers.Falcon2Linker;
import linking.linkers.OpenTapiocaLinkerWordlift;
import linking.linkers.RadboudLinker;
import linking.linkers.TagMeLinker;
import linking.linkers.TextRazorLinker;
import linking.mentiondetection.exact.SpaCyMentionDetector;
import structure.interfaces.linker.Linker;
import structure.interfaces.pipeline.FullAnnotator;
import structure.interfaces.pipeline.PipelineComponent;

public enum ExperimentSettings {
	INSTANCE();

	/**
	 * Map assigning linker names (strings) to linker classes <br>
	 * <br>
	 * Note: Primary potential attack vector as reflection is used on these.
	 * Therefore: restrict access to these maps, among others by making the
	 * unmodifiable (e.g. through Collections.unmodifiableMap(...) and losing the
	 * reference to the modifiable version)
	 */

	// private final HashMap<Linker, Collection<ExperimentType>> mapping = new
	// HashMap<>();
	/**
	 * Answers the question: for this component name I specified, what tasks can it
	 * do? (Opposite of mapTypeComponentNames)
	 */
	private final Map<String, Collection<EnumComponentType>> linkerTasktypeMapping = new HashMap<>();
	/**
	 * Allows for easy task-specific grouping, answers the question: "for this task,
	 * what components are available?"
	 */
	private final Map<EnumComponentType, List<String>> mapTypeComponentNames = new HashMap<>();
	/**
	 * Answers the question: for this component name I specified, what's the class?
	 * (for execution)
	 */
	private final Map<String, Class<? extends PipelineComponent>> componentClasses = new HashMap<>();

	/**
	 * Answers the question: for this API component name I specified, what's the
	 * class? (for execution)
	 */
	private final Map<String, PipelineComponent> apiComponentClasses = new HashMap<>();
//	= Collections
//			.unmodifiableMap(new HashMap<String, Collection<EnumComponentType>>() {
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
		// addComponent("OpenTapioca", OpenTapiocaLinker.class);
		addComponent("OpenTapioca", OpenTapiocaLinkerWordlift.class);

		// TagMe
		addComponent("TagMe", TagMeLinker.class);

		// Radboud / REL
		addComponent("REL", RadboudLinker.class);

		// MAG
		// addComponent("MAG", MAGLinker.class, EnumComponentType.CG_ED);

		// TextRazor
		addComponent("TextRazor", TextRazorLinker.class);

		// Falcon 2.0
		addComponent("Falcon 2.0", Falcon2Linker.class);//

		// spaCy - MD
		addComponent("spaCy", SpaCyMentionDetector.class, EnumComponentType.MD);

		// Wikidata CG dictionary - CG
		addComponent("WikidataDict", WikidataDictCandidateGenerator.class, EnumComponentType.CG);

		// DBpediaLookFinder
		addComponent("DBpediaLookup", DBpediaLookupCandidateGenerator.class, EnumComponentType.CG);//

		// Falcon 2.0 "topK"
		addComponent("Falcon TopK", Falcon2CandidateGenerator.class, EnumComponentType.CG);//

		// Translators
		// DBpedia to Wikidata
		addComponent("DBP2WD", TranslatorDBpediaToWikidata.class, EnumComponentType.TRANSLATOR);
		// Wikidata to DBpedia
		addComponent("WD2DBP", TranslatorWikidataToDBpedia.class, EnumComponentType.TRANSLATOR);

		// Combiners
		addComponent("Union", UnionCombiner.class, EnumComponentType.COMBINER);
		addComponent("Intersection", IntersectCombiner.class, EnumComponentType.COMBINER);

		// Splitters
		// splitters: copy
		addComponent("Copy", CopySplitter.class, EnumComponentType.SPLITTER);

		// Load CLiT API components from the properties files
		// for each of them instantiate an APIComponent which will then be used by
		// APIComponentCommunicator to actually communicate with it
		System.out.println("starting properties loading process");
		final APIPropertyLoader propertyLoader = new APIPropertyLoader();
		final Collection<APIComponent> apiComponents = propertyLoader.load();
		System.out.println("Adding components: " + apiComponents.size());
		for (APIComponent apiComponent : apiComponents) {
			System.out.println("Adding API Component: " + apiComponent.getDisplayName());
			addAPIComponent(apiComponent, apiComponent.getTasks().toArray(new EnumComponentType[] {}));
		}

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
		final Set<EnumComponentType> tasks = new HashSet<>();// Lists.newArrayList();

		// Covers checking for all tasks defined in EnumComponentType
		for (EnumComponentType type : EnumComponentType.values()) {
			if (type.type != null) {
				if (type.type.isAssignableFrom(className)) {
					tasks.add(type);
				}
			}
		}

		// Complete Linker instances
		if (Linker.class.isAssignableFrom(className) || FullAnnotator.class.isAssignableFrom(className)) {
			// It's a linker, so add MD and CG_ED to it through our hacks
			tasks.add(EnumComponentType.MD);
			tasks.add(EnumComponentType.CG_ED);
			tasks.add(EnumComponentType.MD_CG_ED);
		}

		if (tasks == null || tasks.size() < 1) {
			addComponent(name, className, (EnumComponentType[]) null);
		} else {
			addComponent(name, className, tasks.toArray(new EnumComponentType[] {}));
		}
	}

	/**
	 * Add a component that is powered by the default protocols defined by
	 * APIComponentCommunicator
	 * 
	 * @param apiComponent       the API component with information on URL, display
	 *                           name and such
	 * @param EnumComponentTypes what this component can do
	 */
	private void addAPIComponent(final APIComponent apiComponent, EnumComponentType... EnumComponentTypes) {
		// Keep track of this specific API component
		apiComponentClasses.put(apiComponent.getDisplayName(), apiComponent);
		addComponent(apiComponent.getDisplayName(), APIComponent.class, EnumComponentTypes);
	}

	private void addComponent(final String name, final Class<? extends PipelineComponent> clazz,
			EnumComponentType... EnumComponentTypes) {
		if (clazz == null) {
			throw new RuntimeException("Tried to add a component without instantiation path...");
		}

		if (EnumComponentTypes != null && EnumComponentTypes.length > 0) {
			// Easily find out what each component can do
			final Collection<EnumComponentType> pipelineTypes = Lists.newArrayList(EnumComponentTypes);
			linkerTasktypeMapping.put(name, pipelineTypes);

			// Sort components by type for simplified access...
			for (EnumComponentType type : EnumComponentTypes) {
				List<String> names;
				if ((names = mapTypeComponentNames.get(type)) == null) {
					names = Lists.newArrayList();
					mapTypeComponentNames.put(type, names);
				}
				names.add(name);
			}
		}

		// NULL --> it could be anything, so add it
		// It's a linker --> add it as a linker
		if (EnumComponentTypes == null || containsLinker(EnumComponentTypes)) {
			linkerClasses.put(name, clazz);
		}

		componentClasses.put(name, clazz);
	}

	boolean containsLinker(EnumComponentType[] EnumComponentTypes) {
		for (EnumComponentType e : EnumComponentTypes) {
			if (EnumComponentType.MD_CG_ED == e) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Return the linkers
	 * 
	 * @param experimentType
	 * @return
	 */
	public static List<String> getLinkers() {
		final List<String> linkers = INSTANCE.mapTypeComponentNames.get(EnumComponentType.MD_CG_ED);
		if (linkers == null || linkers.size() < 1) {
			return Lists.newArrayList();
		}
		return linkers;
	}

	/**
	 * Return the component names that can perform a specific type of action
	 * 
	 * @param experimentType
	 * @return
	 */
	public static List<String> getComponentsForType(EnumComponentType experimentType) {
		final List<String> linkers = INSTANCE.mapTypeComponentNames.get(experimentType);
		if (linkers == null) {
			return Lists.newArrayList();
		}
		return linkers;
	}

	/**
	 * Returns a case insensitive copy of an unmodifiable SPLITTER map
	 * 
	 * @return case insensitive map <3
	 */
	public static Map<String, Class<? extends PipelineComponent>> getSplitterClassesCaseInsensitive() {
		final Map<String, Class<? extends PipelineComponent>> retMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
		// Get all SPLITTER names and then extract all the classes from Components
		final Map<String, Class<? extends PipelineComponent>> namesAndClasses = getNamesAndClassesForType(
				EnumComponentType.SPLITTER);
		retMap.putAll(namesAndClasses);
		return retMap;
	}

	private final static Map<String, Class<? extends PipelineComponent>> getNamesAndClassesForType(
			EnumComponentType type) {
		final Map<String, Class<? extends PipelineComponent>> retMap = new HashMap<>();
		if (type == null) {
			throw new RuntimeException("No type passed...");
		}
		List<String> names = INSTANCE.mapTypeComponentNames.get(type);
		if (names == null) {
			// Avoid NPEs & unexpected behaviour
			names = Lists.newArrayList();
		}
		for (String name : names) {
			final Class<? extends PipelineComponent> clazz = INSTANCE.componentClasses.get(name);
			if (clazz != null) {
				retMap.put(name, clazz);
			}
		}

		return retMap;
	}

	/**
	 * Returns a case insensitive copy of an unmodifiable LINKER map
	 * 
	 * @return case insensitive map <3
	 */
	public static Map<String, Class<? extends PipelineComponent>> getLinkerClassesCaseInsensitive() {
		final Map<String, Class<? extends PipelineComponent>> retMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
		// retMap.putAll(INSTANCE.linkerClasses);
		retMap.putAll(INSTANCE.componentClasses);
		// retMap.putAll(INSTANCE.translatorClasses);
		// retMap.putAll(INSTANCE.combinerClasses);
		// retMap.putAll(INSTANCE.splitterClasses);
		// retMap.putAll(INSTANCE.filterClasses);
		// retMap.putAll(INSTANCE.evaluatorClasses);
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
	public static Map<String, Class<? extends PipelineComponent>> getCombinerClassesCaseInsensitive() {
		final Map<String, Class<? extends PipelineComponent>> retMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
		// Get all SPLITTER names and then extract all the classes from Components
		final Map<String, Class<? extends PipelineComponent>> namesAndClasses = getNamesAndClassesForType(
				EnumComponentType.COMBINER);
		retMap.putAll(namesAndClasses);
		return retMap;
	}

	/**
	 * Returns a case insensitive copy of an unmodifiable TRANSLATOR map
	 * 
	 * @return case insensitive map <3
	 */
	public static Map<String, Class<? extends PipelineComponent>> getTranslatorClassesCaseInsensitive() {
		final Map<String, Class<? extends PipelineComponent>> retMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
		// Get all SPLITTER names and then extract all the classes from Components
		final Map<String, Class<? extends PipelineComponent>> namesAndClasses = getNamesAndClassesForType(
				EnumComponentType.TRANSLATOR);
		retMap.putAll(namesAndClasses);
		return retMap;
	}

	/**
	 * Returns a case insensitive copy of an unmodifiable FILTER map
	 * 
	 * @return case insensitive map <3
	 */
	public static Map<String, Class<? extends PipelineComponent>> getFilterClassesCaseInsensitive() {
		final Map<String, Class<? extends PipelineComponent>> retMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
		// Get all SPLITTER names and then extract all the classes from Components
		final Map<String, Class<? extends PipelineComponent>> namesAndClasses = getNamesAndClassesForType(
				EnumComponentType.FILTER);
		retMap.putAll(namesAndClasses);
		return retMap;
	}

	/**
	 * Returns a case insensitive copy of an unmodifiable MD map
	 * 
	 * @return case insensitive map <3
	 */
	public static Map<String, Class<? extends PipelineComponent>> getMDClassesCaseInsensitive() {
		final Map<String, Class<? extends PipelineComponent>> retMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
		// Get all SPLITTER names and then extract all the classes from Components
		final Map<String, Class<? extends PipelineComponent>> namesAndClasses = getNamesAndClassesForType(
				EnumComponentType.MD);
		retMap.putAll(namesAndClasses);
		return retMap;
	}

	/**
	 * Returns a case insensitive copy of an unmodifiable CG map
	 * 
	 * @return case insensitive map <3
	 */
	public static Map<String, Class<? extends PipelineComponent>> getCGClassesCaseInsensitive() {
		final Map<String, Class<? extends PipelineComponent>> retMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
		// Get all SPLITTER names and then extract all the classes from Components
		final Map<String, Class<? extends PipelineComponent>> namesAndClasses = getNamesAndClassesForType(
				EnumComponentType.CG);
		retMap.putAll(namesAndClasses);
		return retMap;
	}

	/**
	 * Returns a case insensitive copy of an unmodifiable ED map
	 * 
	 * @return case insensitive map <3
	 */
	public static Map<String, Class<? extends PipelineComponent>> getEDClassesCaseInsensitive() {
		final Map<String, Class<? extends PipelineComponent>> retMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
		// Get all SPLITTER names and then extract all the classes from Components
		final Map<String, Class<? extends PipelineComponent>> namesAndClasses = getNamesAndClassesForType(
				EnumComponentType.ED);
		retMap.putAll(namesAndClasses);
		return retMap;
	}

	/**
	 * Return a collection of possible linkers
	 * 
	 * @return a copy of possible linkers
	 */
	public static final Set<String> getLinkerNames() {
		return new HashSet<>(INSTANCE.mapTypeComponentNames.get(EnumComponentType.MD_CG_ED));
		// return new HashSet<>(INSTANCE.linkerClasses.keySet());
	}

	/**
	 * Return a collection of possible translators
	 * 
	 * @return a copy of possible translators
	 */
	public static final Set<String> getTranslatorNames() {
		return new HashSet<>(INSTANCE.mapTypeComponentNames.get(EnumComponentType.TRANSLATOR));
		// return new HashSet<>(INSTANCE.translatorClasses.keySet());
	}

	/**
	 * Return a collection of possible combiners
	 * 
	 * @return a copy of possible combiners
	 */
	public static Collection<? extends String> getCombinerNames() {
		return new HashSet<>(INSTANCE.mapTypeComponentNames.get(EnumComponentType.COMBINER));
	}

	/**
	 * Return a collection of possible splitters
	 * 
	 * @return a copy of possible splitters
	 */
	public static Collection<? extends String> getSplitterNames() {
		return new HashSet<>(INSTANCE.mapTypeComponentNames.get(EnumComponentType.SPLITTER));
		// .splitterClasses.keySet());
	}

	/**
	 * Return a collection of possible filters
	 * 
	 * @return a copy of possible filters
	 */
	public static Collection<? extends String> getFilterNames() {
		if (INSTANCE.mapTypeComponentNames.get(EnumComponentType.FILTER) == null)
			return new HashSet<>();
		return new HashSet<>(INSTANCE.mapTypeComponentNames.get(EnumComponentType.FILTER));
		// return new HashSet<>(INSTANCE.filterClasses.keySet());
	}

	public static Collection<? extends String> getEvaluatorNames() {
		return new HashSet<>(INSTANCE.mapTypeComponentNames.get(EnumComponentType.EVALUATOR));
		// return new HashSet<>(INSTANCE.evaluatorClasses.keySet());
	}

	public static Collection<? extends String> getExplainerNames() {
		return new HashSet<>(INSTANCE.mapTypeComponentNames.get(EnumComponentType.EXPLAINER));
		// return new HashSet<>(INSTANCE.explainerClasses.keySet());
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
