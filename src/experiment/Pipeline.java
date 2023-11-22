package experiment;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.json.simple.JSONObject;

import com.google.common.collect.Lists;

import structure.config.constants.EnumPipelineType;
import structure.datatypes.AnnotatedDocument;
import structure.exceptions.PipelineException;
import structure.interfaces.pipeline.PipelineComponent;

/**
 * TODO JavaDoc
 * 
 * @author Kristian Noullet
 * @author Samuel Printz
 *
 */
public class Pipeline {
	private boolean finished = false;
	private final Map<String, PipelineItem> pipelineItems = new HashMap<>();
	public static final String KEY_INPUT_ITEM = "input";
	public static final String KEY_OUTPUT_ITEM = "output";
	private final PipelineItem outputItem;
	private final PipelineItem inputItem;
	private boolean hasMD = false;
	private boolean hasCG = false;
	private boolean hasED = false;
	private boolean hasNER = false;
	private JSONObject json = null;

	/**
	 * Defines which tasks this pipeline is capable of, i.e. MD, CG, ED,
	 * combinations of those or the full entity linking pipeline
	 */
	private EnumPipelineType pipelineType;

	public Pipeline() {
		this.inputItem = addItem(KEY_INPUT_ITEM, new PipelineComponent() {
			/**
			 * Set the (unchanged) input document as result such that following components
			 * can grab and process it.
			 */
			@Override
			public Collection<AnnotatedDocument> execute(final PipelineItem callItem,
					final AnnotatedDocument document) {
				Collection<AnnotatedDocument> documents = document.makeMultiDocuments();
				// set result of input pipeline item s.t. following pipeline items can grab it
				callItem.setResults(documents);
				return documents;
			}
		}, EnumComponentType.INPUT, EnumPipelineType.NONE);

		// end should be the results, likely have no appropriate component to execute
		// and be (in)directly connected to
		// all the previous steps
		this.outputItem = addItem(KEY_OUTPUT_ITEM, new PipelineComponent() {
			/**
			 * Handles what to do with the results in the end...<br>
			 * Right now it just adds results together to the result list if there are
			 * results from multiple sources...
			 */
			@Override
			public Collection<AnnotatedDocument> execute(final PipelineItem callItem,
					final AnnotatedDocument input) {
				// combine the results of the multiple precedents
				return callItem.getCopyOfAllDependencyResults();
			}
			// the EnumPipelineType of output is update in the PipelineBuilder after all
			// components were added
		}, EnumComponentType.OUTPUT, EnumPipelineType.NONE);
	}

	/**
	 * Adds an node to the graph
	 * 
	 * @param ID        string identifier
	 * @param component component which will be executed
	 * @return
	 */
	public PipelineItem addItem(final String ID, final PipelineComponent component,
			final EnumComponentType type, final EnumPipelineType pipelineType) {
		final PipelineItem item = createItem(ID, component, type, pipelineType);
		pipelineItems.put(ID, item);
		return item;
	}

	/**
	 * Adds an node to the graph<br>
	 * Adding an unspecified item through this makes its specific behaviour be
	 * executed through the components specific execute(...) function.<br>
	 * If you want to use the default one specified in PipelineItem, use
	 * one of the addMD, addCG, ... or addItem(..., TYPE) where TYPE is the
	 * EnumTypeComponent defining the behaviour if the component is compatible
	 * 
	 * @param ID        string identifier
	 * @param component component which will be executed
	 * @return
	 */
	public PipelineItem addItem(final String ID, final PipelineComponent component) {
		return addItem(ID, component, EnumComponentType.UNSPECIFIED, EnumPipelineType.FULL);
	}

	/**
	 * Adds an MD node to the graph<br>
	 * Difference compared to {@link #addItem(String, PipelineComponent)}:<br>
	 * This method makes the component behave in the default specified way, as
	 * specified in the switch-case in PipelineItem
	 * 
	 * @param ID        string identifier
	 * @param component component which will be executed
	 * @return
	 */
	public PipelineItem addMD(final String ID, final PipelineComponent component) {
		this.hasMD = true;
		return addItem(ID, component, EnumComponentType.MD, EnumPipelineType.MD);
	}

	/**
	 * Adds an MD node to the graph<br>
	 * Difference compared to {@link #addItem(String, PipelineComponent)}:<br>
	 * This method makes the component behave in the default specified way, as
	 * specified in the switch-case in PipelineItem
	 * 
	 * @param ID        string identifier
	 * @param component component which will be executed
	 * @return
	 */
	public PipelineItem addNER(final String ID, final PipelineComponent component) {
		this.hasNER = true;
		return addItem(ID, component, EnumComponentType.NER, EnumPipelineType.NER);
	}

	/**
	 * Adds a CG node to the graph<br>
	 * Difference compared to {@link #addItem(String, PipelineComponent)}:<br>
	 * This method makes the component behave in the default specified way, as
	 * specified in the switch-case in PipelineItem
	 * 
	 * @param ID        string identifier
	 * @param component component which will be executed
	 * @return
	 */
	public PipelineItem addCG(final String ID, final PipelineComponent component) {
		this.hasCG = true;
		return addItem(ID, component, EnumComponentType.CG, EnumPipelineType.CG);
	}

	/**
	 * Adds an ED node to the graph<br>
	 * Difference compared to {@link #addItem(String, PipelineComponent)}:<br>
	 * This method makes the component behave in the default specified way, as
	 * specified in the switch-case in PipelineItem
	 * 
	 * @param ID        string identifier
	 * @param component component which will be executed
	 * @return
	 */
	public PipelineItem addED(final String ID, final PipelineComponent component) {
		this.hasED = true;
		return addItem(ID, component, EnumComponentType.ED, EnumPipelineType.ED);
	}

	/**
	 * Adds an CG_ED node to the graph<br>
	 * Difference compared to {@link #addItem(String, PipelineComponent)}:<br>
	 * This method makes the component behave in the default specified way, as
	 * specified in the switch-case in PipelineItem
	 * 
	 * @param ID        string identifier
	 * @param component component which will be executed
	 * @return
	 */
	public PipelineItem addCG_ED(final String ID, final PipelineComponent component) {
		this.hasCG = true;
		this.hasED = true;
		return addItem(ID, component, EnumComponentType.CG_ED, EnumPipelineType.ED);
	}

	/**
	 * Adds an MD_CG_ED (=full Linker) node to the graph<br>
	 * Difference compared to {@link #addItem(String, PipelineComponent)}:<br>
	 * This method makes the component behave in the default specified way, as
	 * specified in the switch-case in PipelineItem
	 * 
	 * @param ID        string identifier
	 * @param component component which will be executed
	 * @return
	 */
	public PipelineItem addMD_CG_ED(final String ID, final PipelineComponent component) {
		this.hasMD = true;
		this.hasCG = true;
		this.hasED = true;
		return addItem(ID, component, EnumComponentType.MD_CG_ED, EnumPipelineType.ED);
	}

	/**
	 * Adds a Combiner node to the graph<br>
	 * Difference compared to {@link #addItem(String, PipelineComponent)}:<br>
	 * This method makes the component behave in the default specified way, as
	 * specified in the switch-case in PipelineItem
	 * 
	 * @param ID        string identifier
	 * @param component component which will be executed
	 * @return
	 */
	public PipelineItem addCombiner(final String ID, final PipelineComponent component) {
		return addItem(ID, component, EnumComponentType.COMBINER, determinePipelineType());
	}

	/**
	 * Adds a Splitter node to the graph<br>
	 * Difference compared to {@link #addItem(String, PipelineComponent)}:<br>
	 * This method makes the component behave in the default specified way, as
	 * specified in the switch-case in PipelineItem
	 * 
	 * @param ID        string identifier
	 * @param component component which will be executed
	 * @return
	 */
	public PipelineItem addSplitter(final String ID, final PipelineComponent component) {
		return addItem(ID, component, EnumComponentType.SPLITTER, determinePipelineType());
	}

	/**
	 * Adds a Transformer node to the graph<br>
	 * Difference compared to {@link #addItem(String, PipelineComponent)}:<br>
	 * This method makes the component behave in the default specified way, as
	 * specified in the switch-case in PipelineItem
	 * 
	 * @param ID        string identifier
	 * @param component component which will be executed
	 * @return
	 */
	public PipelineItem addTransformer(final String ID, final PipelineComponent component) {
		return addItem(ID, component, EnumComponentType.TRANSFORMER, determinePipelineType());
	}

	/**
	 * Adds a Translator node to the graph<br>
	 * Difference compared to {@link #addItem(String, PipelineComponent)}:<br>
	 * This method makes the component behave in the default specified way, as
	 * specified in the switch-case in PipelineItem
	 * 
	 * @param ID        string identifier
	 * @param component component which will be executed
	 * @return
	 */
	public PipelineItem addTranslator(final String ID, final PipelineComponent component) {
		return addItem(ID, component, EnumComponentType.TRANSLATOR, determinePipelineType());
	}

	/**
	 * Adds a Filter node to the graph<br>
	 * Difference compared to {@link #addItem(String, PipelineComponent)}:<br>
	 * This method makes the component behave in the default specified way, as
	 * specified in the switch-case in PipelineItem
	 * 
	 * @param ID        string identifier
	 * @param component component which will be executed
	 * @return
	 */
	public PipelineItem addFilter(final String ID, final PipelineComponent component) {
		return addItem(ID, component, EnumComponentType.FILTER, determinePipelineType());
	}

	/**
	 * 
	 * @param ID        identifier referring to the pipeline item
	 * @param component what should be executed
	 * @param type      type of component that will be executed. Important if
	 *                  default behaviour defined in PipelineItem should
	 *                  be applied (if none is defined, it will take the one
	 *                  defined)
	 * @return
	 */
	private PipelineItem createItem(final String ID, final PipelineComponent component,
			final EnumComponentType type, final EnumPipelineType pipelineType) {
		return new PipelineItem(ID, component, type, pipelineType);
	}

	public void addConnection(final String sourceID, final String targetID) throws PipelineException {
		// If any of them does not yet exist --> needs to be added
		final PipelineItem source = pipelineItems.get(sourceID);
		final PipelineItem target = pipelineItems.get(targetID);
		if (source == null)
			throw new PipelineException(
					"[" + sourceID + " -> " + targetID + "] "
							+ "Source has not been added yet. Please add it prior to creating of connection.",
					targetID);
		if (target == null)
			throw new PipelineException(
					"[" + sourceID + " -> " + targetID + "] "
							+ "Target has not been added yet. Please add it prior to creating of connection.",
					sourceID);
		if (source.getDependencies().contains(target)) {
			System.err.println("Warning: Skipping cyclic dependency " + sourceID + " -> " + targetID);
			return;
		}
		if (target.getTargets().contains(source)) {
			System.err.println("Warning: Skipping cyclic dependency " + sourceID + " -> " + targetID);
			return;
		}

		source.addTarget(target);
		target.addDependency(source);

		// The dependencies (follow pipeline items) are attached not only to this item,
		// but also to a specific result
		// bucket of this item. From this bucket, the dependency grabs the result
		// (introduced for splitter)
		PipelineItemResultBucket resultBucket = source.addResultBucket(target);
		target.addDependencyResultBucket(resultBucket);

		System.out.println("Info: Added connection " + sourceID + " -> " + targetID);
	}

	public void execute(final AnnotatedDocument document) throws PipelineException {
		resolve(this.outputItem, document);
		finished = true;
	}

	/**
	 * Dependency resolution through proper execution order.
	 * 
	 * @param item
	 * @param text
	 * @throws PipelineException
	 */
	private void resolve(final PipelineItem item, final AnnotatedDocument document) throws PipelineException {
		PipelineItem prevDependency = null;
		for (PipelineItem dependency : item.getDependencies()) {
			resolve(dependency, document);
			prevDependency = dependency;
		}
		try {
			item.execute(prevDependency, document);
		} catch (RuntimeException e) {
			String errorMessage = e.getMessage();
			if (e.getMessage() == null) {
				StringWriter sw = new StringWriter();
				e.printStackTrace(new PrintWriter(sw));
				errorMessage = sw.toString();
			}
			String message = item.getID() + ": " + errorMessage;
			throw new PipelineException(message, item.getID());
		}
	}

	public PipelineItem getInputItem() {
		return inputItem;
	}

	public PipelineItem getOutputItem() {
		return outputItem;
	}

	public boolean hasMD() {
		return hasMD;
	}

	public boolean hasCG() {
		return hasCG;
	}

	public boolean hasED() {
		return hasED;
	}

	public EnumPipelineType getPipelineType() {
		return pipelineType;
	}

	public void setPipelineType(EnumPipelineType pipelineType) {
		this.pipelineType = pipelineType;
		// The task type of the output item must always be equal to the task type of the
		// whole pipeline
		this.outputItem.setPipelineType(pipelineType);
	}

	public JSONObject getJson() {
		return json;
	}

	public void setJson(JSONObject json) {
		this.json = json;
	}

	/**
	 * Executes pipeline if it hasn't been yet and returns final results. If it has
	 * been executed already, it will just return the results.
	 * 
	 * @param text
	 * @return
	 * @throws PipelineException
	 */
	public Collection<AnnotatedDocument> getResults(final AnnotatedDocument document) throws PipelineException {
		// TODO Do we need this case?
		if (!finished) {
			execute(document);
		}
		return this.outputItem.getResults();
	}

	/**
	 * Returns the results of the pipeline as annotated documents. The list contains
	 * one document for each component
	 * that was linked to the final component (intermediate results). Each document
	 * has an attribute with the ID of the
	 * component whose result it is and which task was solved at this step (MD, CG
	 * or ED).
	 * 
	 * @return Collection of annotated documents
	 */
	public Collection<AnnotatedDocument> getResultDocuments() {
		// iterate all dependencies of the final component
		final Collection<PipelineItem> dependencies = this.outputItem.getDependencies();
		final Collection<AnnotatedDocument> results = Lists.newArrayList();
		for (PipelineItem dependency : dependencies) {
			// get the result of the dependency
			final AnnotatedDocument document = dependency.getFirstResult();
			results.add(document);
		}
		return results;
	}

	/**
	 * Resets this entire pipeline, as well as all dependent PipelineItem
	 * instances, so they may be reused for reuse with another input text
	 * (especially useful in the case of multiple sentences from data sets being
	 * sent out one by one)
	 * 
	 * @return
	 */
	public boolean reset() {
		reset(this.outputItem);
		finished = false;
		return true;
	}

	/**
	 * Recursive reset to re-execute the pipeline if need be.
	 * 
	 * @param item item passed to backtrack through dependencies for reset
	 * @return
	 */
	public boolean reset(final PipelineItem item) {
		boolean ret = true;
		for (PipelineItem dependency : item.getDependencies()) {
			ret &= reset(dependency);
		}
		ret &= item.reset();
		// System.out.println("[" + item.getID() + "] Resetting");
		return ret;
	}

	/**
	 * Determines which tasks the pipeline is capable of.
	 * 
	 * Can be called also during building the pipeline, when not all components were
	 * added yet. It returns the task
	 * type of the respective component then.
	 * TODO Sure? What if first a CG_ED was added, and then an MD? Wouldn't the MD
	 * get task type "ED" then as well?
	 * 
	 * @param pipeline
	 * @return Task type of the pipeline
	 * @throws PipelineException
	 */
	public EnumPipelineType determinePipelineType() {
		if (hasMD()) {
			if (hasCG()) {
				if (hasED()) {
					return EnumPipelineType.FULL;
				} else {
					return EnumPipelineType.MD_CG;
				}
			} else {
				if (hasED()) {
					// throw new PipelineException("Pipeline capable of mention detection and
					// entity"
					// + "disambiguation but not of candidate generation");
					return EnumPipelineType.ED;
				} else {
					return EnumPipelineType.MD;
				}
			}
		} else {
			if (hasCG()) {
				if (hasED()) {
					return EnumPipelineType.CG_ED;
				} else {
					return EnumPipelineType.CG;
				}
			} else {
				if (hasED()) {
					return EnumPipelineType.ED;
				} else {
					// throw new PipelineException("Pipeline isn't capable of any task type");
					return EnumPipelineType.NONE;
				}
			}
		}
	}

	@Override
	public String toString() {
		String pipelineItemsList = "";
		int i = 0;
		for (String pipelineItem : pipelineItems.keySet()) {
			if (i > 0) {
				pipelineItemsList += ", ";
			}
			pipelineItemsList += pipelineItem;
			i++;
		}
		return "Pipeline [" + pipelineItemsList + "]";
	}

	public Set<Entry<String, PipelineItem>> getPipelineItems() {
		return this.pipelineItems.entrySet();
	}

	public String getKeyOutputItem() {
		return KEY_OUTPUT_ITEM;
	}

}
