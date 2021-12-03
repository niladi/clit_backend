package experiment;

import java.util.Collection;

import com.google.common.collect.Lists;

import org.json.simple.JSONObject;

import structure.config.constants.EnumPipelineType;
import structure.config.constants.EnumTaskState;
import structure.datatypes.AnnotatedDocument;
import structure.exceptions.PipelineException;

/**
 * Executes an {@link Experiment} by executing one {@link ExperimentTask} after another. 
 * TODO Omit this class and implement an Experiment.run() instead?
 * 
 * @author Samuel Printz
 */
public class Experimenter {

	/**
	 * Experiment that is to be executed. Consist of multiple experiment tasks.
	 */
	private Experiment experiment;

	/**
	 * Connector to the file system for storing the final result of the experiment as JSON.
	 */
	private ExperimentStore experimentStore;


	public Experimenter(Experiment experiment) {
		this.experiment = experiment;
		this.experimentStore = new ExperimentStore();
		
	}

	/**
	 * Executes a list of experiment tasks. Depending on wheather an input text or a dataset is specified, the
	 * respective method is called.
	 * Returns a failed ExperimentTaskResult in case none or both of them are defined.
	 */
	public Experiment run() {
		// TODO Don't create new Experiment but update the Tasks of the old Experiment instead? Maybe giving them a
		// state?
		Experiment experimentResult = new Experiment(experiment.getExperimentId());

		// process the experiment tasks
		for (ExperimentTask experimentTask : experiment.getExperimentTasks()) {

			try {
				// build pipeline
				final JSONObject pipelineConfig = experimentTask.getPipelineConfig();
				Pipeline pipeline = new PipelineBuilder(pipelineConfig, experimentTask).buildPipeline();

				// determine the pipeline type (not a property of the pipeline JSON but of the experiment task)
				EnumPipelineType pipelineType = pipeline.determinePipelineType();
				experimentTask.setPipelineType(pipelineType);

				// run the task
				final Collection<Collection<AnnotatedDocument>> annotatedDocumentCollection =
						runTask(pipeline, experimentTask.getDocuments());
				final ExperimentTask result = new ExperimentTask(experimentTask, EnumTaskState.DONE,
						annotatedDocumentCollection);
				experimentResult.addExperimentTask(result);

			} catch (PipelineException e) {
				e.printStackTrace();
				final ExperimentTask result = new ExperimentTask(experimentTask, e.getMessage());
				experimentResult.addExperimentTask(result);
			}
		}

		// write result to JSON file
		experimentStore.writeExperimentResultToJsonFile(experimentResult);

		return experimentResult;
	}

	/**
	 * Run the experiment task and return a list of documents, each one represented by another list with one document
	 * for each intermediate result.
	 */
	private Collection<Collection<AnnotatedDocument>> runTask(Pipeline pipeline,
			Collection<Collection<AnnotatedDocument>> documents) throws PipelineException {
		final Collection<Collection<AnnotatedDocument>> annotatedDocumentCollection = Lists.newArrayList();
		for (Collection<AnnotatedDocument> documentCollection : documents) {
			// get the first document, represented by a list of documents having one element representing the unmodified
			// documents (the following, yet empty list entries are for intermediate results)
			final AnnotatedDocument document = documentCollection.iterator().next();
			pipeline.reset();
			pipeline.execute(document);
			final Collection<AnnotatedDocument> annotatedDocuments = pipeline.getResultDocuments();
			annotatedDocumentCollection.add(annotatedDocuments);
		}
		return annotatedDocumentCollection;
	}
}
