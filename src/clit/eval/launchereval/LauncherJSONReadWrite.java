package clit.eval.launchereval;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;

import experiment.Experiment;
import experiment.ExperimentStore;
import experiment.ExperimentTask;
import structure.datatypes.AnnotatedDocument;

public class LauncherJSONReadWrite {

	public static void main(String[] args) {

		final String realDocument = "C:\\Users\\wf7467\\Desktop\\Evaluation Datasets\\Datasets\\CLiT_executions\\MDOnly\\test.json";

		// Writing
		final String testOut = "C:\\Users\\wf7467\\Desktop\\Evaluation Datasets\\Datasets\\CLiT_executions\\MDOnly\\herp.json";
		ExperimentStore store = new ExperimentStore() {
			@Override
			public File getFile(String[] outputPath) {
				return new File(testOut);
			}
		};
		store.writeExperimentResultToJsonFile(new Experiment());
		System.out.println("Finished Writing successfully!");

		try {
			// Attempting to read
			final ObjectReader or = new ObjectMapper().readerFor(Experiment.class);
			Experiment experiment = or.readValue(new File(realDocument));
			System.out.println(experiment.getExperimentId());
			final List<ExperimentTask> tasks = experiment.getExperimentTasks();
			for (ExperimentTask task : tasks) {
				final Collection<Collection<AnnotatedDocument>> documents = task.getDocuments();
				System.out.println(task.getPipelineConfig());

				for (Collection<AnnotatedDocument> docs : documents) {
					for (AnnotatedDocument doc : docs) {
						System.out.println(doc.getMentions());
					}
				}
			}
			System.out.println("Read experiment successfully!");

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
