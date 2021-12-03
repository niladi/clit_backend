package experiment;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import structure.config.constants.EnumPipelineType;
import structure.config.kg.EnumModelType;
import structure.exceptions.PipelineException;
import structure.interfaces.pipeline.CandidateGeneratorDisambiguator;
import structure.interfaces.pipeline.FullAnnotator;
import structure.interfaces.pipeline.MentionDetector;
import structure.interfaces.pipeline.PipelineComponent;

public class PipelineBuilder extends PipelineInstantiationHelper {

	private static final List<EnumComponentType> LINKING_COMPONENT_TYPES = Arrays.asList(EnumComponentType.MD,
			EnumComponentType.CG, EnumComponentType.ED, EnumComponentType.CG_ED, EnumComponentType.MD_CG_ED);
	private static final List<EnumComponentType> INTER_COMPONENT_PROCESSOR_TYPES = Arrays.asList(
			EnumComponentType.COMBINER, EnumComponentType.SPLITTER, EnumComponentType.TRANSFORMER,
			EnumComponentType.TRANSLATOR, EnumComponentType.FILTER);
	private static final List<EnumComponentType> PIPELINE_COMPONENT_TYPES = Stream
			.of(LINKING_COMPONENT_TYPES, INTER_COMPONENT_PROCESSOR_TYPES).flatMap(Collection::stream)
			.collect(Collectors.toList());

	private static final String JSON_KEY_COMPONENTS = "components";
	private static final String JSON_KEY_CONNECTIONS = "connections";
	private static final String JSON_KEY_START_COMPONENTS = "startComponents";
	private static final String JSON_KEY_END_COMPONENTS = "endComponents";

	private static final EnumModelType DEFAULT_KNOWLEDGEBASE = EnumModelType.DEFAULT;

	private final JSONObject pipelineConfig;
	private final ExperimentTask experimentTask;
	private final EnumModelType knowledgeBase;

	public PipelineBuilder(JSONObject pipelineConfig, ExperimentTask experimentTask) {
		this.pipelineConfig = pipelineConfig;
		this.experimentTask = experimentTask;
		this.knowledgeBase = DEFAULT_KNOWLEDGEBASE;
	}

	/**
	 * Read linker configurations from JSON and create a list of pipelines
	 * 
	 * @param pipelineConfig
	 * @return List of StandardLinkerConfig and CustomLinkerConfig objects
	 * @throws PipelineException
	 */
	public Pipeline buildPipeline() throws PipelineException {
		Pipeline pipeline = new Pipeline();

		String pipelineConfigType = (String) pipelineConfig.get("pipelineConfigType");

		if (pipelineConfigType.equals("standard")) {
			// Canned linker (Configurability: not v. interesting)
			readStandardLinkerConfig(pipeline);
		} else if (pipelineConfigType.equals("custom")) {
			// Simple linker (Configurability: a bit more interesting)
			readSimplePipelineConfig(pipeline);
		} else if (pipelineConfigType.equals("complex")) {
			// Simple linker (Configurability: highly interesting)
			pipeline = readComplexPipelineConfig(pipelineConfig);
		} else {
			System.err.println("Warning: Invalid linker type (" + pipelineConfigType + "), skipping");
		}

		PipelineValidator pipelineValidator = new PipelineValidator();
		pipelineValidator.validatePipeline(pipeline); // throws exception if invalid

		EnumPipelineType pipelineType = pipeline.determinePipelineType();
		pipeline.setPipelineType(pipelineType);

		return pipeline;
	}

	/**
	 * Transform JSON config of a standard linker into a Pipeline object
	 * 
	 * @param pipeline
	 * @throws PipelineException
	 */
	private void readStandardLinkerConfig(Pipeline pipeline) throws PipelineException {
		String linkerName = (String) pipelineConfig.get("linker");
		FullAnnotator annotator = null;
		Class<? extends PipelineComponent> linkerClassName = ExperimentSettings.getLinkerClassesCaseInsensitive()
				.get(linkerName);
		try {
			annotator = (FullAnnotator) linkerClassName// Class.forName(linkerClassName)
					.getDeclaredConstructor(EnumModelType.class).newInstance(knowledgeBase);
			String itemId = EnumComponentType.MD_CG_ED.id + "1";
			pipeline.addMD_CG_ED(itemId, annotator);
			pipeline.addConnection(Pipeline.KEY_INPUT_ITEM, itemId);
			pipeline.addConnection(itemId, Pipeline.KEY_OUTPUT_ITEM);
		} catch (ClassCastException e) {
			throw new PipelineException("Annotator '" + linkerName + "' cannot be instantiated");
		} catch (Exception e) {
			e.printStackTrace();
			throw new PipelineException(
					"Error while instantiating the annotator '" + linkerName + "': " + e.getMessage());
		}
	}

	/**
	 * Transform JSON config of a complex pipeline into a Pipeline object
	 * 
	 * @param pipeline
	 * @throws PipelineException
	 */
	private void readSimplePipelineConfig(Pipeline pipeline) throws PipelineException {
		String mentionDetectorName = (String) pipelineConfig.get("mentionDetector");
		String candidateGeneratorDisambiguatorName = (String) pipelineConfig.get("candidateGeneratorDisambiguator");
		MentionDetector mentionDetector = null;
		CandidateGeneratorDisambiguator candidateGeneratorDisambiguator = null;
		Class<? extends PipelineComponent> mentionDetectorClassName = ExperimentSettings
				.getLinkerClassesCaseInsensitive().get(mentionDetectorName);
		Class<? extends PipelineComponent> candidateGeneratorDisambiguatorClassName = ExperimentSettings
				.getLinkerClassesCaseInsensitive().get(candidateGeneratorDisambiguatorName);

		try {
			mentionDetector = (MentionDetector) mentionDetectorClassName// Class.forName(mentionDetectorClassName)
					.getDeclaredConstructor(EnumModelType.class).newInstance(knowledgeBase);
		} catch (ClassCastException e) {
			throw new PipelineException(mentionDetectorName + " cannot be used as Mention Detector");
		} catch (Exception e) {
			e.printStackTrace();
			throw new PipelineException(
					"Error while instantiating '" + mentionDetectorClassName + "': " + e.getMessage());
		}

		try {
			candidateGeneratorDisambiguator = (CandidateGeneratorDisambiguator) candidateGeneratorDisambiguatorClassName// Class.forName(candidateGeneratorDisambiguatorClassName)
					.getDeclaredConstructor(EnumModelType.class).newInstance(knowledgeBase);
		} catch (ClassCastException e) {
			throw new PipelineException(candidateGeneratorDisambiguatorName + " cannot be used as Candidate Generator");
		} catch (Exception e) {
			e.printStackTrace();
			throw new PipelineException(
					"Error while instantiating '" + candidateGeneratorDisambiguatorName + "': " + e.getMessage());
		}

		String mdId = EnumComponentType.MD.id + "1";
		String cgEdId = EnumComponentType.CG_ED.id + "1";
		pipeline.addMD(mdId, mentionDetector);
		pipeline.addCG_ED(cgEdId, candidateGeneratorDisambiguator);
		pipeline.addConnection(Pipeline.KEY_INPUT_ITEM, mdId);
		pipeline.addConnection(mdId, cgEdId);
		pipeline.addConnection(cgEdId, Pipeline.KEY_OUTPUT_ITEM);
	}

	/**
	 * Transform JSON config of a complex pipeline into a Pipeline object
	 * 
	 * @param linkerConfig
	 * @param knowledgeBase
	 * @return
	 * @throws PipelineException
	 */
	private Pipeline readComplexPipelineConfig(JSONObject linkerConfigJson) throws PipelineException {
		Pipeline pipeline = new Pipeline();
		// Links entire pipeline's JSON to the pipeline
		pipeline.setJson(linkerConfigJson);
		readLinkingComponents(linkerConfigJson, linkerConfigJson.get(JSON_KEY_COMPONENTS), pipeline);
		readPipelineConnections(linkerConfigJson.get(JSON_KEY_CONNECTIONS), pipeline);
		readStartComponents(linkerConfigJson.get(JSON_KEY_START_COMPONENTS), pipeline);
		readEndComponents(linkerConfigJson.get(JSON_KEY_END_COMPONENTS), pipeline);
		return pipeline;
	}

	/**
	 * Adds the connection between the first components to the input component.
	 * 
	 * @param startComponentsObject
	 * @param pipeline
	 * @throws PipelineException
	 */
	private void readStartComponents(final Object startComponentsObject, final Pipeline pipeline)
			throws PipelineException {
		if (startComponentsObject == null)
			throw new PipelineException("No start component specified");

		if (!(startComponentsObject instanceof JSONArray))
			throw new PipelineException("Could not read JSON (start components): " + startComponentsObject.toString());

		JSONArray startComponentsArray = (JSONArray) startComponentsObject;

		if (startComponentsArray.size() == 0)
			throw new PipelineException("No start component specified");

		for (final Object startComponentObject : startComponentsArray) {
			String startComponentString = startComponentObject.toString();
			pipeline.addConnection(pipeline.getInputItem().getID(), startComponentString);
		}
	}

	/**
	 * Adds the connection between the last pipeline components to the output
	 * component. When executing the pipeline, dependencies are grabbed through this
	 * output component.
	 * 
	 * @param startComponentsObject
	 * @param pipeline
	 * @throws PipelineException
	 */
	private void readEndComponents(Object endComponentsObject, Pipeline pipeline) throws PipelineException {
		if (endComponentsObject == null)
			throw new PipelineException("No end component specified");

		if (!(endComponentsObject instanceof JSONArray))
			throw new PipelineException("Could not read JSON (end components): " + endComponentsObject.toString());

		JSONArray endComponentsArray = (JSONArray) endComponentsObject;

		if (endComponentsArray.size() == 0)
			throw new PipelineException("No end component specified");

		for (Object endComponentObject : endComponentsArray) {
			String endComponentString = endComponentObject.toString();
			pipeline.addConnection(endComponentString, pipeline.getOutputItem().getID());
		}
	}

	/**
	 * Read linking components from pipeline config JSON
	 * 
	 * @param linkerConfigObject
	 * @param pipeline
	 * @throws PipelineException
	 */
	private void readLinkingComponents(final JSONObject jsonPipeline, final Object linkerConfigObject,
			final Pipeline pipeline) throws PipelineException {
		if (!(linkerConfigObject instanceof JSONObject))
			throwUnreadableException(linkerConfigObject);
		JSONObject linkerConfigJson = (JSONObject) linkerConfigObject;

		for (EnumComponentType pipelineComponentType : PIPELINE_COMPONENT_TYPES) {
			String pipelineComponentTypeStr = pipelineComponentType.name;
			Object arrayObj = linkerConfigJson.get(pipelineComponentTypeStr);

			// no entry in Map
			if (arrayObj == null) {
				System.out.println("Info: No component of type '" + pipelineComponentTypeStr + "' specified");
				continue;
			}

			if (!(arrayObj instanceof JSONArray))
				// invalid type returned...
				throwUnreadableException(arrayObj);

			JSONArray itemArray = (JSONArray) arrayObj;

			if (itemArray.size() == 0) {
				// empty array
				System.out.println("Info: No component of type '" + pipelineComponentTypeStr + "' specified");
				continue;
			}

			for (Object itemObj : itemArray) {
				if (!(itemObj instanceof JSONObject))
					throwUnreadableException(itemObj);
				JSONObject itemJson = (JSONObject) itemObj;
				String keyStr = itemJson.get("id").toString();

				Object valueObj = itemJson.get("value");
				if (valueObj == null)
					throw new PipelineException(keyStr + " has no value defined", keyStr);
				String valueStr = valueObj.toString();

				if (LINKING_COMPONENT_TYPES.contains(pipelineComponentType)) {
					instantiateLinkingComponent(jsonPipeline, knowledgeBase, pipeline, pipelineComponentType, keyStr,
							valueStr);
				} else if (INTER_COMPONENT_PROCESSOR_TYPES.contains(pipelineComponentType)) {
					instantiateInterComponentProcessor(jsonPipeline, pipeline, pipelineComponentType, keyStr, valueStr);
				} else {
					throw new PipelineException("Invalid entry '" + valueStr + "' in pipeline config", keyStr);
				}
			}
		}
	}

	private void throwUnreadableException(Object obj) throws PipelineException {
		throw new PipelineException("Could not read JSON (" + JSON_KEY_COMPONENTS + "): " + obj.toString());
		// throw new PipelineException("Could not read JSON (" + JSON_KEY_COMPONENTS +
		// "): " + itemObj.toString());
		// throw new PipelineException("Could not read JSON (" + JSON_KEY_COMPONENTS +
		// "): " + arrayObj.toString());

	}

	/**
	 * Read connections from pipeline config JSON
	 * 
	 * @param linkerConfigObject
	 * @param pipeline
	 * @throws PipelineException
	 */
	private void readPipelineConnections(Object connectionsObject, Pipeline pipeline) throws PipelineException {
		if (!(connectionsObject instanceof JSONArray))
			throw new PipelineException(
					"Could not read JSON (" + JSON_KEY_CONNECTIONS + "): " + connectionsObject.toString());

		JSONArray connectionsArray = (JSONArray) connectionsObject;

		if (connectionsArray.size() == 0)
			System.out.println("Info: No connections specified");

		for (Object connectionObject : connectionsArray) {
			if (!(connectionObject instanceof JSONObject))
				throw new PipelineException(
						"Could not read JSON (" + JSON_KEY_CONNECTIONS + "): " + connectionObject.toString());
			JSONObject connectionJson = (JSONObject) connectionObject;
			try {
				String sourceStr = connectionJson.get("source").toString();
				String targetStr = connectionJson.get("target").toString();
				instantiatePipelineConnection(pipeline, sourceStr, targetStr);
			} catch (NullPointerException e) {
				throw new PipelineException(
						"Could not read JSON (" + JSON_KEY_CONNECTIONS + "): " + connectionObject.toString());
			}
		}
	}

	public ExperimentTask getExperimentTask() {
		return experimentTask;
	}

}
