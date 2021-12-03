package experiment;

import java.util.Collection;
import java.util.Iterator;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import structure.config.constants.EnumPipelineType;
import structure.datatypes.AnnotatedDocument;
import structure.exceptions.PipelineException;
import structure.interfaces.linker.Linker;
import structure.interfaces.pipeline.CandidateGenerator;
import structure.interfaces.pipeline.CandidateGeneratorDisambiguator;
import structure.interfaces.pipeline.Disambiguator;
import structure.interfaces.pipeline.MentionDetector;
import structure.interfaces.pipeline.PipelineComponent;

/**
 * Class acting as a dependency node with details regarding execution and
 * results of said execution
 * 
 * @author Kristian Noullet
 * @author Samuel Printz
 *
 */
public class PipelineItem {
	// What needs to be done prior to this
	private Collection<PipelineItem> dependencies = Lists.newArrayList();
	// ... and the result buckets of those items
	private Collection<PipelineItemResultBucket> dependencyResultBuckets = Lists.newArrayList();
	// What to do next
	private Collection<PipelineItem> targets = Lists.newArrayList();

	private final String id;

	// What may be executed
	private final PipelineComponent component;
	private boolean done = false;
	private Collection<AnnotatedDocument> results = null;

	// Provides one bucket with results for each of its targets (introduced for
	// splitter)
	private Collection<PipelineItemResultBucket> resultBuckets = Lists.newArrayList();

	private final EnumComponentType type;
	
	// Defines which tasks are done (MD, CG and ED) after this item was executed; required for front-end
	private EnumPipelineType pipelineType;

	public PipelineItem(final String id, final PipelineComponent component, final EnumComponentType type,
			final EnumPipelineType pipelineType) {
		this.id = id;
		this.component = component;
		this.type = type;
		this.pipelineType = pipelineType;
	}

	public String getID() {
		return this.id;
	}

	public PipelineComponent getComponent() {
		return this.component;
	}

	/**
	 * Return the class name of the component or null if the component is null.
	 */
	private String getComponentClass() {
		return getComponent() == null ? null : getComponent().getClass().toString();
	}

	// - - - - - - - - - - - - - - - - - - - - - - - - - -
	// - - - - - - - - - - Dependencies  - - - - - - - - -
	// - - - - - - - - - - - - - - - - - - - - - - - - - -

	public Collection<PipelineItem> getDependencies() {
		return this.dependencies;
	}

	public void setDependencies(final Collection<PipelineItem> dependencies) {
		this.dependencies.clear();
		this.dependencies.addAll(dependencies);
	}

	public void addDependency(final PipelineItem dependency) {
		this.dependencies.add(dependency);
	}

	public void addDependencyResultBucket(PipelineItemResultBucket resultBucket) {
		this.dependencyResultBuckets.add(resultBucket);
	}

	/**
	 * Get the results of all dependencies.
	 */
	private Collection<AnnotatedDocument> getAlldependencyResults() {
		final Collection<AnnotatedDocument> documents = Lists.newArrayList();
		for (PipelineItem dependencyItem : this.getDependencies()) {
			documents.addAll(dependencyItem.getResults());
		}
		return documents;
	}

	/**
	 * Get a copy of the results of all dependencies.
	 */
	public Collection<AnnotatedDocument> getCopyOfAllDependencyResults() {
		final Collection<AnnotatedDocument> documents = Lists.newArrayList();
		for (AnnotatedDocument document : this.getAlldependencyResults()) {
			documents.add((AnnotatedDocument) document.clone());
		}
		return documents;
	}

	/**
	 * Get the results of the first dependency.
	 */
	private Collection<AnnotatedDocument> getDependencyResult() {
		return this.dependencyResultBuckets.iterator().next().getResults();
	}

	/**
	 * Similar to getDependencyResult() this returns the result of the dependencies. Difference is that it validates
	 * that there is only one dependency and additionally that this single dependency has only one result. This is used
	 * by all linking components (MD, CG, ED, CG_ED and FULL) that need only one {@link AnnotatedDocument} as input.
	 */
	private AnnotatedDocument getSingleDependencyResult() {
		if (getDependencies().size() != 1)
			throw new IllegalArgumentException("Invalid number of dependencies ("
					+ (getDependencies() == null ? "null" : getDependencies().size()) + ") for this component");

		final Collection<AnnotatedDocument> dependencyResult = getDependencyResult();
		if (dependencyResult.size() != 1)
			throw new IllegalArgumentException(
					"Invalid number of arguments passed (" + dependencyResult.size() + ")");

		return dependencyResult.iterator().next();
	}

	/**
	 * Returns a deep copy the result of {@link getSingleDependencyResult()}.
	 * Used when a pipeline component asks for the result of its dependencies to further process them. The results of
	 * the previous component must stay unchanged! 
	 */
	public AnnotatedDocument getCopyOfSingleDependencyResult() {
		return (AnnotatedDocument) getSingleDependencyResult().clone();
	}

	public PipelineItemResultBucket addResultBucket(PipelineItem target) {
		PipelineItemResultBucket resultBucket = new PipelineItemResultBucket(this, target);
		this.resultBuckets.add(resultBucket);
		return resultBucket;
	}

	// - - - - - - - - - - - - - - - - - - - - - - - -
	// - - - - - - - - - - Targets - - - - - - - - - -
	// - - - - - - - - - - - - - - - - - - - - - - - -

	public Collection<PipelineItem> getTargets() {
		return this.targets;
	}

	public void setTargets(final Collection<PipelineItem> targets) {
		this.targets.clear();
		this.targets.addAll(targets);
	}

	public void addTarget(final PipelineItem target) {
		this.targets.add(target);
	}


	// - - - - - - - - - - - - - - - - - - - - - - - -
	// - - - - - - - - - - - Type  - - - - - - - - - -
	// - - - - - - - - - - - - - - - - - - - - - - - -

	public EnumComponentType getType() {
		return this.type;
	}

	public EnumPipelineType getPipelineType() {
		return pipelineType;
	}

	public void setPipelineType(EnumPipelineType pipelineType) {
		this.pipelineType = pipelineType;
	}

	// - - - - - - - - - - - - - - - - - - - - - - - -
	// Whether computation has finished
	// - - - - - - - - - - - - - - - - - - - - - - - -

	public void finished() {
		this.done = true;
	}

	public boolean isFinished() {
		return this.done;
	}

	// - - - - - - - - - - - - - - - - - - - - - - - -
	// - - - - - - - - - - Results - - - - - - - - - -
	// - - - - - - - - - - - - - - - - - - - - - - - -

	public void setResults(final Collection<AnnotatedDocument> documents) {
		if (documents != null) {
			// TODO deep copy? cf. MentionUtils
			//this.results = DocumentUtils.copyDocuments(documents);
			this.results = documents;

			// Also add the results to the result buckets of the pipeline item. They are the
			// place, where the following
			// pipeline items will ask for them
			// TODO Put this into execute() of the PipelineComponent? (It gets the pipeline
			// item as argument (callItem),
			// so it is accessible...)
			if (!this.getType().equals(EnumComponentType.OUTPUT)) {
				int i = 0;
				for (AnnotatedDocument document : documents) {
					Iterables.get(this.resultBuckets, i).setResults(document.makeMultiDocuments());
					i++;
				}
			}
		}
	}

	/**
	 * Return all annotated documents of the results.
	 */
	public Collection<AnnotatedDocument> getResults() {
		return this.results;
	}

	/**
	 * Return the first annotated document of the results.
	 */
	public AnnotatedDocument getFirstResult() {
		if (this.results == null || this.results.size() == 0)
			return null;

		Iterator<AnnotatedDocument> iterator = this.results.iterator();
		if (iterator == null)
			return null;

		return iterator.next();
	}

	public void execute(final PipelineItem prevDependency, final AnnotatedDocument document) throws PipelineException {
		synchronized (this) {
			if (isFinished()) {
				System.out.println("[" + getID() + "] Already finished. You may grab results.");
				return;
			}
			System.out.println("[" + getID() + "] Executing");
			// Calls are always done as follows:
			// mentions; params=text, predecessors, successors
			if (getComponent() != null) {
				// Components get their data through this PipelineItem, grabbing the
				// dependencies and their results

				Collection<AnnotatedDocument> results = null;

				try {
					// System.out.println("Exec type: " + getType());
					EnumComponentType type = getType();
					switch (type) {
					case MD:
						results = md();
						break;
					case CG:
						results = cg();
						break;
					case CG_ED:
						results = cg_ed();
						break;
					case ED:
						results = ed();
						break;
					case MD_CG_ED:
						results = md_cg_ed();
						break;
					case INPUT:
					case OUTPUT:
						results = getComponent().execute(this, document);
						break;
					case COMBINER:
					case UNSPECIFIED:
					case FILTER:
					case SPLITTER:
					case TRANSFORMER:
					case TRANSLATOR:
					default:
						// System.out.println("DEFAULT CASE: Specific execution.");
						results = getComponent().execute(this, document);
						break;
					}

				} catch (Exception e) {
					e.printStackTrace();
					throw new PipelineException(this.getID() + ": " + e.getMessage());
				}

				setPipelineTypeToResultDocuments(results, this.getPipelineType());
				setComponentIdToResultDocuments(results, this.getID());
				setResults(results);
			}
			finished();
			// System.out.println("[" + getID() + "] Finished - Result: " + getResults());
		}
	}

	/**
	 * Add to the result documents which steps of the pipeline were already executed (MD, CG, ED) to enable the
	 * front-end to display the results correctly.
	 */
	private void setPipelineTypeToResultDocuments(Collection<AnnotatedDocument> documents, EnumPipelineType type) {
		for (AnnotatedDocument document : documents) {
			document.setPipelineType(type);
			document.setComponentId(id);
		}
	}

	/**
	 * Add the component ID to the result documents.
	 */
	private void setComponentIdToResultDocuments(Collection<AnnotatedDocument> documents, String id) {
		for (AnnotatedDocument document : documents) {
			document.setComponentId(id);
		}
	}

	/**
	 * Get the result of the previous component (dependency) and execute the mention detection.
	 * There must be only one dependency (a single previous component) and a single result of this dependency.
	 */
	private Collection<AnnotatedDocument> md() throws Exception {
		if (!EnumComponentType.MD.isInstance(getComponent()))
			throw new RuntimeException("Component class (" + getComponentClass() + ") does not match expected type");

		final AnnotatedDocument document = getCopyOfSingleDependencyResult();
		final MentionDetector mentionDetector = (MentionDetector) getComponent();
		return mentionDetector.detect(document).makeMultiDocuments();
	}

	/**
	 * TODO
	 */
	private Collection<AnnotatedDocument> cg() throws Exception {
		if (!EnumComponentType.CG.isInstance(getComponent()))
			throw new RuntimeException("Component class (" + getComponentClass() + ") does not match expected type");

		final AnnotatedDocument document = getCopyOfSingleDependencyResult();
		final CandidateGenerator candidateGenerator = (CandidateGenerator) getComponent();
		return candidateGenerator.generate(document).makeMultiDocuments();
	}

	/**
	 * TODO
	 */
	private Collection<AnnotatedDocument> md_cg_ed() throws Exception {
		if (!EnumComponentType.MD_CG_ED.isInstance(getComponent()))
			throw new RuntimeException("Component class (" + getComponentClass() + ") does not match expected type");

		final AnnotatedDocument document = getCopyOfSingleDependencyResult();
		//final FullAnnotator fullAnnotator = (FullAnnotator) getComponent();
		//return fullAnnotator.annotate(document).makeMultiDocuments();
		final Linker linker = (Linker) getComponent();
		return linker.annotate(document).makeMultiDocuments();
	}

	/**
	 * TODO
	 * @throws Exception 
	 */
	private Collection<AnnotatedDocument> cg_ed() throws Exception {
		if (!EnumComponentType.CG_ED.isInstance(getComponent()))
			throw new RuntimeException("Component class (" + getComponentClass() + ") does not match expected type");

		final AnnotatedDocument document = getCopyOfSingleDependencyResult();
		final CandidateGeneratorDisambiguator candidateGenDisamb = (CandidateGeneratorDisambiguator) getComponent();
		return candidateGenDisamb.generateDisambiguate(document).makeMultiDocuments();
	}

	/**
	 * TODO
	 */
	private Collection<AnnotatedDocument> ed() throws Exception {
		if (!EnumComponentType.ED.isInstance(getComponent()))
			throw new RuntimeException("Component class (" + getComponentClass() + ") does not match expected type");

		final AnnotatedDocument document = getCopyOfSingleDependencyResult();
		final Disambiguator disambiguator = (Disambiguator) getComponent();
		return disambiguator.disambiguate(document).makeMultiDocuments();
	}

	/**
	 * Resets this pipeline item by removing prior results and switching the "done"
	 * FLAG to FALSE<br>
	 * After calling this reset, this pipeline item may be used again.
	 * 
	 * @return true if properly reset
	 */
	public boolean reset() {
		this.done = false;
		if (this.results != null) {
			this.results.clear();
		}
		return true;
	}

	@Override
	public String toString() {
		// return "PipelineItem [" + id + ", done=" + done + "]";
		return id;
	}
}
