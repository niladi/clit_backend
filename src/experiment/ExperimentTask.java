package experiment;

import java.util.Collection;

import org.json.simple.JSONObject;

import structure.config.constants.EnumPipelineType;
import structure.config.constants.EnumTaskState;
import structure.datatypes.AnnotatedDocument;

/**
 * An ExperimentTask is characterized by <i>one</i> pipeline applied on a collection of documents.
 * It is the major flow object, also passed to (external) components and also the front and to the front-end as final
 * result.
 * 
 * @author Samuel Printz
 */
public class ExperimentTask {

	/**
	 * ID of the experiment.
	 */
	private int experimentId;

	/**
	 * ID of the experiment task.
	 */
	private int taskId;

	/**
	 * The documents that are to be annotated.
	 * In the beginning, despite the documents are not yet annotated, the {@link AnnotatedDocument} class is used.
	 * 
	 * Use of nested Collections:
	 * - First Collection: multiple documents, i.e. one for each document
	 * - Second Collection: intermediate results, i.e. one for each component
	 */
	private Collection<Collection<AnnotatedDocument>> documents;

	/**
	 * Pipeline configuration in JSON format as configured by the user.
	 * This contains only <i>one</i> pipeline.
	 */
	private JSONObject pipelineConfig;

	/**
	 * Type of the pipeline. This is congruent with the type of the result of the task, e.g. annotated mentions (MD)
	 * or disambiguated mentions (ED). For more types see {@link EnumPipelineType}.
	 * TODO Remove? Doesn't seem to be used for anything.
	 */
	private EnumPipelineType pipelineType;

	/**
	 * ID of the current component, i.e. the component that is executed next.
	 */
	private String currentComponent;

	/**
	 * State of the task, see {@link EnumTaskState}.
	 */
	private EnumTaskState state;

	/**
	 * Error message for failed tasks. Used in the front-end.
	 */
	private String errorMessage;


	/**
	 * Default constructor an experiment task.
	 */
	public ExperimentTask(int experimentId, int taskId, Collection<Collection<AnnotatedDocument>> documents,
			JSONObject pipelineConfig, EnumTaskState state) {
		this.experimentId = experimentId;
		this.taskId = taskId;
		this.documents = documents;
		this.pipelineConfig = pipelineConfig;
		this.pipelineType = null;
		this.currentComponent = null;
		this.state = state;
		this.errorMessage = null;
	}

	/**
	 * Constructor for an experiment task based on another ExperimentTask.
	 */
	public ExperimentTask(ExperimentTask experimentTask, EnumTaskState state,
			Collection<Collection<AnnotatedDocument>> documents) {
		this.experimentId = experimentTask.getExperimentId();
		this.taskId = experimentTask.getTaskId();
		this.documents = documents;
		this.pipelineConfig = experimentTask.getPipelineConfig();
		this.pipelineType = experimentTask.getPipelineType();
		this.currentComponent = null;
		this.state = state;
		this.errorMessage = null;
	}

	/**
	 * Constructor for a failed experiment task.
	 */
	public ExperimentTask(int experimentId, int taskId, JSONObject pipelineConfig, String errorMessage,
			String pipelineBuildLog) {
		this.experimentId = experimentId;
		this.taskId = taskId;
		this.documents = null;
		this.pipelineConfig = pipelineConfig;
		this.pipelineType = null;
		this.currentComponent = null;
		this.state = EnumTaskState.FAILED;
		this.errorMessage = errorMessage;
	}

	/**
	 * Constructor for a failed experiment task based on another ExperimentTask.
	 */
	public ExperimentTask(ExperimentTask experimentTask, String errorMessage) {
		this.experimentId = experimentTask.getExperimentId();
		this.taskId = experimentTask.getTaskId();
		this.documents = null;
		this.pipelineConfig = experimentTask.getPipelineConfig();
		this.pipelineType = experimentTask.getPipelineType();
		this.currentComponent = null;
		this.state = EnumTaskState.FAILED;
		this.errorMessage = errorMessage;
	}

    /**
     * Constructor for failed experiments.
     */
    public ExperimentTask(int id, String errorMessage) {
            this.experimentId = id;
            this.state = EnumTaskState.FAILED;
            this.errorMessage = errorMessage;
    }


	public int getExperimentId() {
		return experimentId;
	}

	public void setExperimentId(int experimentId) {
		this.experimentId = experimentId;
	}

	public int getTaskId() {
		return taskId;
	}

	public void setTaskId(int taskId) {
		this.taskId = taskId;
	}

	public Collection<Collection<AnnotatedDocument>> getDocuments() {
		return documents;
	}

	public void setDocuments(Collection<Collection<AnnotatedDocument>> documents) {
		this.documents = documents;
	}

	public JSONObject getPipelineConfig() {
		return pipelineConfig;
	}

	public void setPipelineConfig(JSONObject pipelineConfig) {
		this.pipelineConfig = pipelineConfig;
	}

	public EnumPipelineType getPipelineType() {
		return pipelineType;
	}

	public void setPipelineType(EnumPipelineType pipelineType) {
		this.pipelineType = pipelineType;
	}

	public String getCurrentComponent() {
		return currentComponent;
	}

	public void setCurrentComponent(String currentComponent) {
		this.currentComponent = currentComponent;
	}

	public EnumTaskState getState() {
		return state;
	}

	public void setState(EnumTaskState state) {
		this.state = state;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

}
