package structure.config.constants;

/**
 * Defines the state of an experiment task.
 * 
 * @author Samuel Printz
 */
public enum EnumTaskState {

	/**
	 * Experiment task was built but not executed yet.
	 */
	BUILD(),

	/**
	 * Experiment task was build and successfully executed.
	 */
	DONE(),

	/**
	 * Experiment task either could not be build or could not be executed.
	 */
	FAILED();
	
}
