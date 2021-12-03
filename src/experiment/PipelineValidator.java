package experiment;

import java.util.Collection;

import structure.exceptions.PipelineException;

public class PipelineValidator {

	private boolean hasMD = false;
	private boolean hasCG = false;
	private boolean hasED = false;

	/**
	 * Validates the pipeline by checking the order of it's components and it's completeness
	 * @throws PipelineException if pipeline is invalid
	 */
	public void validatePipeline(final Pipeline pipeline) throws PipelineException {
		validatePipelineItem(pipeline.getOutputItem());
		//validatePipelineCompleteness();
	}

	/**
	 * Recursively validates the pipeline item by item
	 * @param item
	 * @throws PipelineException
	 */
	private void validatePipelineItem(final PipelineItem item) throws PipelineException {
		registerComponentType(item.getType());
		Collection<PipelineItem> dependencies = item.getDependencies();

		if (item.getID().equals(Pipeline.KEY_OUTPUT_ITEM) && dependencies.size() == 0)
			throw new PipelineException("Output not connected to any linking component");

		for (PipelineItem dependency : dependencies) {
			validatePipelineItemDependencyTypes(item, dependency);
			validatePipelineItem(dependency);
		}
	}

	/**
	 * Validates whether the type of a dependencies of a pipeline component is valid
	 * @param item
	 * @param dependency
	 * @throws PipelineException
	 */
	private void validatePipelineItemDependencyTypes(final PipelineItem item,
			PipelineItem dependency) throws PipelineException {
		EnumComponentType t = dependency.getType();
		switch (item.getType()) {
		case UNSPECIFIED:
		case INPUT:
		case OUTPUT:
			break;
		case ED:
			if (t.equals(null) ||
					t.equals(EnumComponentType.MD) ||
					t.equals(EnumComponentType.MD_CG_ED) ||
					t.equals(EnumComponentType.CG_ED) ||
					t.equals(EnumComponentType.ED))
				throw new PipelineException("Illegal dependency: " + dependency.getID() +
						" -> " + item.getID(), item.getID());
			break;
		case CG:
		case CG_ED:
			if (t.equals(null) ||
					t.equals(EnumComponentType.MD_CG_ED) ||
					t.equals(EnumComponentType.CG) ||
					t.equals(EnumComponentType.CG_ED) ||
					t.equals(EnumComponentType.ED))
				throw new PipelineException("Illegal dependency: " + dependency.getID() +
						" -> " + item.getID(), item.getID());
			break;
		case MD:
		case MD_CG_ED:
			if (t.equals(EnumComponentType.MD) ||
					t.equals(EnumComponentType.MD_CG_ED) ||
					t.equals(EnumComponentType.CG) ||
					t.equals(EnumComponentType.CG_ED) ||
					t.equals(EnumComponentType.ED))
				throw new PipelineException("Illegal dependency: " + dependency.getID() +
						" -> " + item.getID(), item.getID());
			break;
		default:
			break;
		}
	}

	/**
	 * Validates the pipeline by checking if all three major entity linking steps (mention 
	 * detection, candidate generation and entity disambiguation) are contained in the pipeline
	 * @throws PipelineException if the pipeline doesn't implement the whole entity linking
	 * pipeline, i.e. has no mention detection, candidate generation or entity disambiguation
	 */
	private void validatePipelineCompleteness() throws PipelineException {
		if (!hasMD)
			throw new PipelineException(
					"No pipeline component implements mention detection (required)");
		if (!hasCG)
			throw new PipelineException(
					"No pipeline component implements candidate generation (required)");
		if (!hasED)
			throw new PipelineException(
					"No pipeline component implements entity disambiguation (required)");
	}

	/**
	 * Set a certain component type as given for this pipeline.
	 * @param componentType
	 */
	private void registerComponentType(EnumComponentType componentType) {
		switch (componentType) {
		case MD:
			this.hasMD = true;
			break;
		case CG:
			this.hasCG = true;
			break;
		case CG_ED:
			this.hasCG = true;
			this.hasED = true;
			break;
		case ED:
			this.hasED = true;
			break;
		case MD_CG_ED:
			this.hasMD = true;
			this.hasCG = true;
			this.hasED = true;
			break;
		default:
			break;
		}
	}

}
