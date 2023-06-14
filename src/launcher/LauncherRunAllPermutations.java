package launcher;

import java.util.Map;
import java.util.Map.Entry;

import experiment.ExperimentSettings;
import structure.interfaces.pipeline.PipelineComponent;

public class LauncherRunAllPermutations {

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

	public static void runAllSingle() {
		final Map<String, Class<? extends PipelineComponent>> mentionDetectors = ExperimentSettings
				.getMDClassesCaseInsensitive();
		final Map<String, Class<? extends PipelineComponent>> candidateGenerators = ExperimentSettings
				.getCGClassesCaseInsensitive();
		final Map<String, Class<? extends PipelineComponent>> entityDisambiguators = ExperimentSettings
				.getEDClassesCaseInsensitive();
		// For each MD
		for (Entry<String, Class<? extends PipelineComponent>> md : mentionDetectors.entrySet()) {
			// For each CG
			for (Entry<String, Class<? extends PipelineComponent>> cg : candidateGenerators.entrySet()) {
				// For each ED
				for (Entry<String, Class<? extends PipelineComponent>> ed : entityDisambiguators.entrySet()) {
					// Instantiate the setting and grab the results if there already are some
					// Introduce caching mechanism
				}

			}
		}

		ExperimentSettings.getLinkerNames();

	}

}
