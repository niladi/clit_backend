package test;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

import experiment.Experiment;
import experiment.ExperimentStore;

public class ExperimentStoreTest {

	@Test
	public void testExperimentStore() {
		// create a dummy file
		final ExperimentStore store = new ExperimentStore();
		final int id = store.getNextExperimentId();
		final Experiment experiment = new Experiment(id);
		store.writeExperimentResultToJsonFile(experiment);
		// create another file
		final int id2 = store.getNextExperimentId();
		final Experiment experiment2 = new Experiment(id2);
		store.writeExperimentResultToJsonFile(experiment2);
		assertEquals(id + 1, store.getLastExperimentId());
	}

}
