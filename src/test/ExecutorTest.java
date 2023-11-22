package test;

import static org.junit.Assert.assertNotNull;

import java.io.IOException;

import java.nio.file.Files;

import java.nio.file.Paths;

import org.junit.Test;

import experiment.Experiment;
import experiment.ExperimentBuilder;
import experiment.Experimenter;

public class ExecutorTest {

    private static final String PATH = "test/resources/";

    private static enum TestFileEnum {
        EXPERIMENT_NER("experiment_ner.json");

        final String path;

        TestFileEnum(String path) {
            this.path = path;
        }
    };

    @Test
    public void nerTest() {
        final Experiment ex = new ExperimentBuilder(loadPipelineJson(TestFileEnum.EXPERIMENT_NER.path))
                .buildExperiment();
        final Experiment result = new Experimenter(ex).run();
        assertNotNull(result);
    }

    private String loadPipelineJson(String jsonFileName) {
        try {
            return new String(Files.readAllBytes(Paths.get(PATH, jsonFileName)));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
}
