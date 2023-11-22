package experiment;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import structure.config.constants.EnumPipelineType;
import structure.datatypes.AnnotatedDocument;
import structure.datatypes.Mention;
import structure.datatypes.PossibleAssignment;
import structure.exceptions.PipelineException;
import structure.interfaces.clit.Combiner;
import structure.interfaces.clit.Filter;
import structure.interfaces.clit.Splitter;
import structure.interfaces.clit.Translator;
import structure.interfaces.linker.Linker;
import structure.interfaces.pipeline.CandidateGenerator;
import structure.interfaces.pipeline.CandidateGeneratorDisambiguator;
import structure.interfaces.pipeline.Disambiguator;
import structure.interfaces.pipeline.MentionDetector;
import structure.interfaces.pipeline.PipelineComponent;
import structure.interfaces.pipeline.Typing;
import structure.utils.datastructure.CandidateUtils;
import structure.utils.datastructure.MentionUtils;

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

	// Defines which tasks are done (MD, CG and ED) after this item was executed;
	// required for front-end
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
	// - - - - - - - - - - Dependencies - - - - - - - - -
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
	 * Similar to getDependencyResult() this returns the result of the dependencies.
	 * Difference is that it validates that there is only one dependency and
	 * additionally that this single dependency has only one result. This is used by
	 * all linking components (MD, CG, ED, CG_ED and FULL) that need only one
	 * {@link AnnotatedDocument} as input.
	 */
	private AnnotatedDocument getSingleDependencyResult() {
		if (getDependencies().size() != 1)
			throw new IllegalArgumentException("Invalid number of dependencies ("
					+ (getDependencies() == null ? "null" : getDependencies().size()) + ") for this component");

		final Collection<AnnotatedDocument> dependencyResult = getDependencyResult();
		if (dependencyResult.size() != 1)
			throw new IllegalArgumentException("Invalid number of arguments passed (" + dependencyResult.size() + ")");

		return dependencyResult.iterator().next();
	}

	/**
	 * Returns a deep copy the result of {@link getSingleDependencyResult()}. Used
	 * when a pipeline component asks for the result of its dependencies to further
	 * process them. The results of the previous component must stay unchanged!
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
	// - - - - - - - - - - - Type - - - - - - - - - -
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
			// this.results = DocumentUtils.copyDocuments(documents);
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
						case INPUT:
						case OUTPUT:
							results = getComponent().execute(this, document);
							break;
						case MD:
							results = md();
							break;
						case NER:
							results = ner();
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
						case COMBINER:
							results = combine();
							break;
						case SPLITTER:
							results = split();
							break;
						case FILTER:
							results = filter();
							break;
						case TRANSLATOR:
							results = translate();
							break;
						case TRANSFORMER:
						case UNSPECIFIED:
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

	private Collection<AnnotatedDocument> translate() {
		if (!EnumComponentType.TRANSLATOR.isInstance(getComponent()))
			throw new RuntimeException("Component class (" + getComponentClass() + ") does not match expected type");

		// Grab a copy to not overwrite the original
		final AnnotatedDocument document = getCopyOfSingleDependencyResult();

		// Get the mention detector component
		final Translator translator = (Translator) getComponent();
		final AnnotatedDocument translatedDocument = translator.translate(document);

		return translatedDocument.makeMultiDocuments();
	}

	/**
	 * Added generic filter functionality (not via execute()).
	 * 
	 * @return filter document
	 */
	private Collection<AnnotatedDocument> filter() {
		if (!EnumComponentType.FILTER.isInstance(getComponent()))
			throw new RuntimeException("Component class (" + getComponentClass() + ") does not match expected type");

		// Grab a copy to not overwrite the original
		final AnnotatedDocument document = getCopyOfSingleDependencyResult();

		// Get the mention detector component
		final Filter filter = (Filter) getComponent();
		final AnnotatedDocument filterDocument = filter.filter(document);

		return filterDocument.makeMultiDocuments();
	}

	/**
	 * Added generic splitter functionality (not via execute()).
	 * 
	 * @return split document
	 */
	private Collection<AnnotatedDocument> split() {
		if (!EnumComponentType.SPLITTER.isInstance(getComponent()))
			throw new RuntimeException("Component class (" + getComponentClass() + ") does not match expected type");

		// Grab a copy to not overwrite the original
		final AnnotatedDocument document = getCopyOfSingleDependencyResult();

		// Get the mention detector component
		final Splitter splitter = (Splitter) getComponent();
		final Collection<AnnotatedDocument> documents = splitter.split(document, getTargets().size());

		// Safety mechanism --> splitter should give us getTargets().size() number of
		// documents
		if (documents.size() != getTargets().size()) {
			throw new RuntimeException("Invalid number of documents returned by split (Expected: " + getTargets().size()
					+ "; Received: " + documents.size() + ")");
		}

		return documents;
	}

	/**
	 * Added generic combiner functionality (not via execute()).
	 * 
	 * @return combined document
	 */
	private Collection<AnnotatedDocument> combine() {

		if (!EnumComponentType.COMBINER.isInstance(getComponent()))
			throw new RuntimeException("Component class (" + getComponentClass() + ") does not match expected type");

		// Grab a copy to not overwrite the original
		final Collection<AnnotatedDocument> documents = getCopyOfAllDependencyResults();

		// Get the mention detector component
		final Combiner combiner = (Combiner) getComponent();
		final AnnotatedDocument document = combiner.combine(documents);

		return document.makeMultiDocuments();
	}

	/**
	 * Add to the result documents which steps of the pipeline were already executed
	 * (MD, CG, ED) to enable the front-end to display the results correctly.
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
	 * <b>Important Note</b>: This method will ONLY update the MENTIONS of the data
	 * structure aka. the textual mention along with startOffset etc., but not the
	 * candidates nor the disambiguated entity<br>
	 * Get the result of the previous component (dependency) and execute the mention
	 * detection. There must be only one dependency (a single previous component)
	 * and a single result of this dependency.<br>
	 */
	private Collection<AnnotatedDocument> ner() throws Exception {
		if (!EnumComponentType.NER.isInstance(getComponent()))
			throw new RuntimeException("Component class (" + getComponentClass() + ") does not match expected type");

		// Grab a copy to not overwrite the original
		final AnnotatedDocument document = getCopyOfSingleDependencyResult();
		// Get the mention detector component
		final Typing typer = (Typing) getComponent();
		final AnnotatedDocument documentNer = typer.ner(document);

		document.setMentions(MentionUtils.mergeEntityTypes(document.getMentions(), documentNer.getMentions()));
		return documentNer.makeMultiDocuments();
	}

	/**
	 * <b>Important Note</b>: This method will ONLY update the MENTIONS of the data
	 * structure aka. the textual mention along with startOffset etc., but not the
	 * candidates nor the disambiguated entity<br>
	 * Get the result of the previous component (dependency) and execute the mention
	 * detection. There must be only one dependency (a single previous component)
	 * and a single result of this dependency.<br>
	 */
	private Collection<AnnotatedDocument> md() throws Exception {
		if (!EnumComponentType.MD.isInstance(getComponent()))
			throw new RuntimeException("Component class (" + getComponentClass() + ") does not match expected type");

		// Grab a copy to not overwrite the original
		final AnnotatedDocument document = getCopyOfSingleDependencyResult();
		// Get the mention detector component
		final MentionDetector mentionDetector = (MentionDetector) getComponent();
		final AnnotatedDocument documentMentionDetection = mentionDetector.detect(document);

		// Two different ways we can understand the default behaviour...
		// Either simply keep the old ones and add new detected ones to it (if they are
		// the same mention, simply keep the old one)
		// OR: take the new mentions as reference and only keep the old ones for which a
		// new corresponding one exists
		final boolean ADDNEW_OR_REMOVENOTNEWLYDETECTEDPLUSADDNEW = true;

		if (ADDNEW_OR_REMOVENOTNEWLYDETECTEDPLUSADDNEW) {
			final Collection<Mention> newTextMentions = MentionUtils.unionAndKeepOldReferences(document.getMentions(),
					documentMentionDetection.getMentions());

			// Make sure MD only overwrites mentions in terms of start/end
			document.setMentions(newTextMentions);
		} else {
			final Collection<Mention> newTextMentions = MentionUtils.intersectAndAddNew(document.getMentions(),
					documentMentionDetection.getMentions());
			document.setMentions(newTextMentions);
		}

		return document.makeMultiDocuments();
	}

	/**
	 * <b>Important Note</b>: This method will ONLY update the candidates (aka.
	 * possible assignments) for a given mention. If the mention passed does not
	 * exist that these candidates belong to, they are IGNORED.
	 */
	private Collection<AnnotatedDocument> cg() throws Exception {
		if (!EnumComponentType.CG.isInstance(getComponent()))
			throw new RuntimeException("Component class (" + getComponentClass() + ") does not match expected type");

		final AnnotatedDocument document = getCopyOfSingleDependencyResult();
		final CandidateGenerator candidateGenerator = (CandidateGenerator) getComponent();
		final AnnotatedDocument docNewCandidates = candidateGenerator.generate(document);

		// Updates candidates in "document" variable with the candidates from
		// docNewCandidates
		// Add the new candidates to the input document
		// Note that 2 candidates may be merged in different ways (e.g. score max or
		// score avg)
		CandidateUtils.mergeDocumentCandidates(document, docNewCandidates);

		return document.makeMultiDocuments();
	}

	/**
	 * <b>Note on Assumption</b>: Everything may be overwritten due to entire
	 * pipeline being made up of it
	 */
	private Collection<AnnotatedDocument> md_cg_ed() throws Exception {
		if (!EnumComponentType.MD_CG_ED.isInstance(getComponent()))
			throw new RuntimeException("Component class (" + getComponentClass() + ") does not match expected type");

		final AnnotatedDocument document = getCopyOfSingleDependencyResult();
		// final FullAnnotator fullAnnotator = (FullAnnotator) getComponent();
		// return fullAnnotator.annotate(document).makeMultiDocuments();
		final Linker linker = (Linker) getComponent();
		return linker.annotate(document).makeMultiDocuments();
	}

	/**
	 * TODO
	 * 
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
	 * <b>Note</b>: Only chosen entity may be updated through this method
	 */
	private Collection<AnnotatedDocument> ed() throws Exception {
		if (!EnumComponentType.ED.isInstance(getComponent()))
			throw new RuntimeException("Component class (" + getComponentClass() + ") does not match expected type");

		final AnnotatedDocument document = getCopyOfSingleDependencyResult();
		final Disambiguator disambiguator = (Disambiguator) getComponent();
		final AnnotatedDocument docDisambiguated = disambiguator.disambiguate(document);

		//
		final Map<String, Mention> mapExistingMentions = new HashMap<>();
		for (Mention existingMention : document.getMentions()) {
			final String mentionStr = MentionUtils.mentionToUniqueStr(existingMention);
			mapExistingMentions.put(mentionStr, existingMention);
		}

		for (Mention newMention : docDisambiguated.getMentions()) {
			final String mentionStr = MentionUtils.mentionToUniqueStr(newMention);
			final Mention existingMention;
			final PossibleAssignment newAssignment = newMention.getAssignment();
			if ((existingMention = mapExistingMentions.get(mentionStr)) != null) {
				// Found matching mentions!

				if (newAssignment == null || newAssignment.getAssignment() == null
						|| newAssignment.getAssignment().length() == 0) {
					// Empty new assignment for found mention --> ignore new disambiguated entity
					// Keep the old assignment on
					// existingMention.setAssignment(existingMention.getAssignment());
					continue;
				}

				// go through all possible assignments, if the assigned one is not part of it,
				// add it
				// else just set it as the wanted assignment

				// Should a disambiguated (found) entity have to be part of the possible
				// assignments?
				// IGNORE_WHETHER_IN_POSSIBLE_CANDIDATES==TRUE --> Nope.
				// IGNORE_WHETHER_IN_POSSIBLE_CANDIDATES==FALSE --> Yes, it has to be part of
				// the possible
				// assignments
				final boolean IGNORE_WHETHER_IN_POSSIBLE_CANDIDATES = true;
				if (IGNORE_WHETHER_IN_POSSIBLE_CANDIDATES) {
					// We ignore whether an assignment is part of the candidates, so we just set it
					existingMention.setAssignment(newMention.getAssignment());
				} else {
					boolean candidateFound = false;
					for (PossibleAssignment candidate : existingMention.getPossibleAssignments()) {
						if (candidate == null || candidate.getAssignment() == null)
							continue;

						if (candidate.getAssignment().equals(newAssignment.getAssignment())) {
							candidateFound = true;
							break;
						}
					}

					if (!candidateFound) {
						// Candidate not yet in existing possible assignments, so... we're not setting
						// it as a new one, move on to next one.
						continue;
					} else {
						existingMention.setAssignment(newMention.getAssignment());
					}
				}
			} else {
				// Clause will be cleaned by compiler, but here some reasoning behind the
				// behaviour for maintainability purposes
				//
				// Ignored: Disambiguated entities generated by component which do not have a
				// possible mention detected via prior mention detection
				// --> TODO: only for MD+CG/MD+CG+ED combined should it not be ignored
			}
		}

		return document.makeMultiDocuments();
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
