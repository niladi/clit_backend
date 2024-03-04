package experiment;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Experiment {

	private List<ExperimentTask> experimentTasks;
	private int experimentId;

	public Experiment() {
		this.experimentTasks = new ArrayList<>();
	}

	public Experiment(int experimentId) {
		this.experimentTasks = new ArrayList<>();
		this.experimentId = experimentId;
	}

	public List<ExperimentTask> getExperimentTasks() {
		return experimentTasks;
	}

	public void setExperimentTasks(List<ExperimentTask> experimentTasks) {
		this.experimentTasks = experimentTasks;
	}

	public void addExperimentTask(ExperimentTask experimentTask) {
		this.experimentTasks.add(experimentTask);
	}

	public int getExperimentId() {
		return experimentId;
	}

	public void setExperimentId(int experimentId) {
		this.experimentId = experimentId;
	}

}
