package experiment;

import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.validator.UrlValidator;
import org.json.simple.JSONObject;

import clit.APIComponent;
import clit.APIComponentCommunicator;
import structure.config.kg.EnumModelType;
import structure.exceptions.PipelineException;
import structure.interfaces.clit.Combiner;
import structure.interfaces.clit.Filter;
import structure.interfaces.clit.Splitter;
import structure.interfaces.clit.Translator;
import structure.interfaces.pipeline.CandidateGenerator;
import structure.interfaces.pipeline.CandidateGeneratorDisambiguator;
import structure.interfaces.pipeline.Disambiguator;
import structure.interfaces.pipeline.MentionDetector;
import structure.interfaces.pipeline.PipelineComponent;
import structure.utils.Loggable;
import structure.utils.NetUtils;

public class PipelineInstantiationHelper implements Loggable {

	/**
	 * Add a linking component (MD, CG, ED, CG_ED) found in the pipeline config to
	 * the pipeline
	 * 
	 * @param knowledgeBase
	 * @param pipeline
	 * @param linkingComponentType
	 * @param componentId
	 * @param componentValue
	 * @throws PipelineException
	 */
	protected void instantiateLinkingComponent(final JSONObject jsonPipeline, final EnumModelType knowledgeBase,
			final Pipeline pipeline, final EnumComponentType linkingComponentType, final String componentId,
			final String componentValue) throws PipelineException {
		final PipelineComponent apiComponentInstance = ExperimentSettings.getAPIComponentClassesCaseInsensitive()
				.get(componentValue);
		final Class<? extends PipelineComponent> clazz = ExperimentSettings.getComponentClassesCaseInsensitive()// ;//getComponentNamesCaseInsensitive();//.getLinkerClassesCaseInsensitive()
				.get(componentValue);
		System.out.println("Component Value: " + componentValue);
		boolean isValidIP = isIPBasedComponent(componentValue);

		if (clazz == null && !isValidIP) {
			throw new RuntimeException(
					"No adequate class found nor is it an IP-based component... [" + componentValue + "]");
		}

		final String apiURLStr;
		final boolean useAPICommunicator;
		if (clazz != null && APIComponent.class.isAssignableFrom(clazz)) {
			// It's an API component
			System.out.println("It's an API component!");
			// in this case componentValue will be the display name of the APIComponent
			useAPICommunicator = true;
			APIComponent apiComponent = ((APIComponent) apiComponentInstance);
			apiURLStr = apiComponent.getUrlString();
			isValidIP = isIPBasedComponent(apiURLStr);
		} else {
			apiURLStr = componentValue;
			useAPICommunicator = (clazz == null) && isValidIP;
		}

		getLogger().info("Linking component type: " + linkingComponentType);
		try {
			switch (linkingComponentType) {
			case MD:
				final MentionDetector mentionDetector;
				System.out.println("isValidIP: " + isValidIP);
				System.out.println("useAPI: " + useAPICommunicator);

				if (isValidIP && useAPICommunicator) {
					// Use IP to communicate with a MD API
					final APIComponentCommunicator apiCommunicator = new APIComponentCommunicator(knowledgeBase,
							componentId, apiURLStr, jsonPipeline);
					mentionDetector = (MentionDetector) apiCommunicator;
				} else {
					mentionDetector = (MentionDetector) clazz// Class.forName(className)
							.getDeclaredConstructor(EnumModelType.class).newInstance(knowledgeBase);
				}

				pipeline.addMD(componentId, mentionDetector);
				getLogger().info("Info: Added mention detector '" + componentValue + "' with ID '" + componentId + "'");
				break;
			case CG:
				final CandidateGenerator candidateGenerator;
				if (useAPICommunicator) {
					// Use IP to communicate with a CG API
					final APIComponentCommunicator apiCommunicator = new APIComponentCommunicator(knowledgeBase,
							componentId, apiURLStr, jsonPipeline);
					candidateGenerator = (CandidateGenerator) apiCommunicator;

				} else {
					candidateGenerator = (CandidateGenerator) clazz// Class.forName(className)
							.getDeclaredConstructor(EnumModelType.class).newInstance(knowledgeBase);
				}
				pipeline.addCG(componentId, candidateGenerator);
				getLogger()
						.info("Info: Added candidate generator '" + componentValue + "' with ID '" + componentId + "'");
				break;
			case ED:
				final Disambiguator disambiguator;
				if (useAPICommunicator) {
					// Use IP to communicate with a CG API
					final APIComponentCommunicator apiCommunicator = new APIComponentCommunicator(knowledgeBase,
							componentId, apiURLStr, jsonPipeline);
					disambiguator = (Disambiguator) apiCommunicator;
				} else {
					disambiguator = (Disambiguator) clazz// Class.forName(className)
							.getDeclaredConstructor(EnumModelType.class).newInstance(knowledgeBase);
				}
				pipeline.addED(componentId, disambiguator);
				getLogger().info(
						"Info: Added entity disambiguator '" + componentValue + "' with ID '" + componentId + "'");
				break;
			case CG_ED:
				final CandidateGeneratorDisambiguator candidateGeneratorDisambiguator;
				if (useAPICommunicator) {
					// Use IP to communicate with a CG API
					final APIComponentCommunicator apiCommunicator = new APIComponentCommunicator(knowledgeBase,
							componentId, componentValue, jsonPipeline);
					candidateGeneratorDisambiguator = (CandidateGeneratorDisambiguator) apiCommunicator;
				} else {
					candidateGeneratorDisambiguator = (CandidateGeneratorDisambiguator) clazz// Class.forName(className)
							.getDeclaredConstructor(EnumModelType.class).newInstance(knowledgeBase);
				}
				pipeline.addCG_ED(componentId, candidateGeneratorDisambiguator);
				getLogger().info("Info: Added candidate generator disambiguator '" + componentValue + "' with ID '"
						+ componentId + "'");
				break;
			default:
				throw new PipelineException(componentId + ": Linking component type '"
						+ linkingComponentType.displayName + "' is not implemented", componentId);
			}
		} catch (ClassCastException e) {
			throw new PipelineException(
					componentId + ": " + componentValue + " cannot be used as " + linkingComponentType.displayName,
					componentId);
		} catch (Exception e) {
			e.printStackTrace();
			throw new PipelineException(
					"Error while instantiating the pipeline item with ID '" + componentId + "': " + e.getMessage(),
					componentId);
		}
	}

	/**
	 * Add a inter-component processor (combiner, splitter, translator, filter)
	 * found in the pipeline config to the pipeline
	 * 
	 * @param pipeline
	 * @param interComponentProcessorType
	 * @param keyStr
	 * @param valueStr
	 * @param jsonPipeline
	 * @throws PipelineException
	 */
	protected void instantiateInterComponentProcessor(final JSONObject jsonPipeline, final Pipeline pipeline,
			final EnumComponentType interComponentProcessorType, final String keyStr, final String valueStr)
			throws PipelineException {
		try {
			final Class<? extends PipelineComponent> className;

			switch (interComponentProcessorType) {
			case COMBINER:
				className = ExperimentSettings.getCombinerClassesCaseInsensitive().get(valueStr);
				// Instantiates combiner correctly (web API or predefined, etc)
				final Combiner combiner = instantiateCombiner(className, keyStr, valueStr, jsonPipeline);

				if (combiner != null) {
					// Adds combiner
					pipeline.addCombiner(keyStr, combiner);
					getLogger().info("Info: Added combiner '" + valueStr + "' with ID '" + keyStr + "'");
				}

				break;
			case SPLITTER:
				className = ExperimentSettings.getSplitterClassesCaseInsensitive().get(valueStr);
				// Instantiates splitter correctly (web API or predefined, etc)
				final Splitter splitter = instantiateSplitter(className, keyStr, valueStr, jsonPipeline);

				if (splitter != null) {
					// Adds splitter
					pipeline.addSplitter(keyStr, splitter);
					getLogger().info("Info: Added splitter '" + valueStr + "' with ID '" + keyStr + "'");
				}
				break;
			case TRANSLATOR:
				className = ExperimentSettings.getTranslatorClassesCaseInsensitive().get(valueStr);
				// Instantiates translator correctly (web API or predefined, etc)
				final Translator translator = instantiateTranslator(className, keyStr, valueStr, jsonPipeline);

				if (translator != null) {
					// Adds translator
					pipeline.addTranslator(keyStr, translator);
					getLogger().info("Info: Added translator '" + valueStr + "' with ID '" + keyStr + "'");
				}
				break;
			case FILTER:
				className = ExperimentSettings.getFilterClassesCaseInsensitive().get(valueStr);
				// Instantiates filter correctly (web API or predefined, etc)
				final Filter filter = instantiateFilter(className, keyStr, valueStr, jsonPipeline);

				if (filter != null) {
					// Adds filter
					pipeline.addFilter(keyStr, filter);
					getLogger().info("Info: Added filter '" + valueStr + "' with ID '" + keyStr + "'");
				}
				break;
			default:
				throw new PipelineException(keyStr + ": Inter component processor type '"
						+ interComponentProcessorType.displayName + "' is not implemented", keyStr);
			}
		} catch (ClassNotFoundException | ClassCastException cnfe) {

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public Filter instantiateFilter(final Class<? extends PipelineComponent> className, final String componentId,
			final String componentValue, final JSONObject jsonPipeline)
			throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException,
			NoSuchMethodException, SecurityException, ClassNotFoundException, PipelineException {
		// normal component
		if (className != null) {
			return (Filter) className.getDeclaredConstructor().newInstance();
		}

		// IP-based API component
		if (isIPBasedComponent(componentValue)) {
			try {
				APIComponentCommunicator apiCommunicator = new APIComponentCommunicator(null, componentId,
						componentValue, jsonPipeline);
				return (Filter) apiCommunicator;
			} catch (MalformedURLException e) {
				e.printStackTrace();
				throw new PipelineException("Filter '" + componentId + "' has malformed URL '" + componentValue + "'",
						componentId);
			}
		}

		// not IP-based nor do we know a class for it, so there has been an error
		throw new PipelineException("Filter '" + componentId + "' has invalid value '" + componentValue + "'",
				componentId);
	}

	public Combiner instantiateCombiner(final Class<? extends PipelineComponent> className, final String componentId,
			final String componentValue, final JSONObject jsonPipeline)
			throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException,
			NoSuchMethodException, SecurityException, ClassNotFoundException, PipelineException {
		// normal component
		if (className != null) {
			return (Combiner) className.getDeclaredConstructor().newInstance();
		}

		// IP-based API component
		if (isIPBasedComponent(componentValue)) {
			try {
				APIComponentCommunicator apiCommunicator = new APIComponentCommunicator(null, componentId,
						componentValue, jsonPipeline);
				return (Combiner) apiCommunicator;
			} catch (MalformedURLException e) {
				e.printStackTrace();
				throw new PipelineException("Combiner '" + componentId + "' has malformed URL '" + componentValue + "'",
						componentId);
			}
		}

		// not IP-based nor do we know a class for it, so there has been an error
		throw new PipelineException("Combiner '" + componentId + "' has invalid value '" + componentValue + "'",
				componentId);
	}

	public Splitter instantiateSplitter(final Class<? extends PipelineComponent> className, final String componentId,
			final String componentValue, final JSONObject jsonPipeline)
			throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException,
			NoSuchMethodException, SecurityException, ClassNotFoundException, PipelineException {
		// normal component
		if (className != null) {
			return (Splitter) className.getDeclaredConstructor().newInstance();
		}

		// IP-based API component
		if (isIPBasedComponent(componentValue)) {
			try {
				APIComponentCommunicator apiCommunicator = new APIComponentCommunicator(null, componentId,
						componentValue, jsonPipeline);
				return (Splitter) apiCommunicator;
			} catch (MalformedURLException e) {
				e.printStackTrace();
				throw new PipelineException("Splitter '" + componentId + "' has malformed URL '" + componentValue + "'",
						componentId);
			}
		}

		// not IP-based nor do we know a class for it, so there has been an error
		throw new PipelineException("Splitter '" + componentId + "' has invalid value '" + componentValue + "'",
				componentId);
	}

	public Translator instantiateTranslator(final Class<? extends PipelineComponent> className,
			final String componentId, final String componentValue, final JSONObject jsonPipeline)
			throws PipelineException, InstantiationException, IllegalAccessException, IllegalArgumentException,
			InvocationTargetException, NoSuchMethodException, SecurityException, ClassNotFoundException {
		// normal component
		if (className != null) {
			return (Translator) className.getDeclaredConstructor().newInstance();
		}

		// IP-based API component
		if (isIPBasedComponent(componentValue)) {
			try {
				APIComponentCommunicator apiCommunicator = new APIComponentCommunicator(null, componentId,
						componentValue, jsonPipeline);
				return (Translator) apiCommunicator;
			} catch (MalformedURLException e) {
				e.printStackTrace();
				throw new PipelineException(
						"Translator '" + componentId + "' has malformed URL '" + componentValue + "'", componentId);
			}
		}

		// not IP-based nor do we know a class for it, so there has been an error
		throw new PipelineException("Translator '" + componentId + "' has invalid value '" + componentValue + "'",
				componentId);
	}

	/**
	 * Add a connection between two pipeline components found in the pipeline config
	 * to the pipeline
	 * 
	 * @param pipeline
	 * @param sourceStr
	 * @param targetStr
	 * @throws PipelineException
	 */
	public void instantiatePipelineConnection(final Pipeline pipeline, String sourceStr, String targetStr)
			throws PipelineException {
		pipeline.addConnection(sourceStr, targetStr);
	}

	/**
	 * Checks whether the passed String argument is an IPv4 address or an IRI
	 * 
	 * @param urlStr URL string to be checked whether it is actually a URL
	 * @return
	 */
	private boolean isIPBasedComponent(final String urlStr) {
		try {
			new URL(urlStr);
			return true;
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			return false;
		}
	}

}
