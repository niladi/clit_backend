package structure.datatypes;

import org.json.simple.JSONObject;

/**
 * Contains all information required by external IP-based pipeline components (called "Super-JSON" previously).
 * This is what is send to external components and what is expected to be received by external components.
 * This is serialized to and deserialized from JSON by Jackson.
 * 
 * @author Samuel Printz
 */
public class APITaskContainer {

	private AnnotatedDocument document;
	private JSONObject pipelineConfig;
	private String componentId;

	/**
	 * Default constructor for conversion from JSON with Jackson.
	 */
	public APITaskContainer() {
		super();
	}

	public APITaskContainer(final AnnotatedDocument document, final JSONObject pipelineConfig, final String componentId) {
		this.document = document;
		this.pipelineConfig = pipelineConfig;
		this.componentId = componentId;
	}

	public AnnotatedDocument getDocument() {
		return document;
	}

	public void setDocument(AnnotatedDocument document) {
		this.document = document;
	}

	public JSONObject getPipelineConfig() {
		return pipelineConfig;
	}

	public void setPipelineConfig(JSONObject pipelineConfig) {
		this.pipelineConfig = pipelineConfig;
	}

	public String getComponentId() {
		return componentId;
	}

	public void setComponentId(String componentId) {
		this.componentId = componentId;
	}

}
