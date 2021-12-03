package experiment;

import java.util.Collection;

import structure.datatypes.AnnotatedDocument;

public class PipelineItemResultBucket {

	private PipelineItem parentPipelineItem;
	private PipelineItem targetPipelineItem;
	private Collection<AnnotatedDocument> results = null;

	public PipelineItemResultBucket(PipelineItem parent, PipelineItem target) {
		this.parentPipelineItem = parent;
		this.targetPipelineItem = target;
	}

	public PipelineItem getParentPipelineItem() {
		return parentPipelineItem;
	}

	public void setParentPipelineItem(PipelineItem parentPipelineItem) {
		this.parentPipelineItem = parentPipelineItem;
	}

	public PipelineItem getTargetPipelineItem() {
		return targetPipelineItem;
	}

	public void setTargetPipelineItem(PipelineItem targetPipelineItem) {
		this.targetPipelineItem = targetPipelineItem;
	}

	public Collection<AnnotatedDocument> getResults() {
		return results;
	}

	public void setResults(Collection<AnnotatedDocument> results) {
		this.results = results;
	}

	@Override
	public String toString() {
		return "ResultBucket [parent=" + parentPipelineItem + ", target="
				+ targetPipelineItem + ", results=" + results + "]";
	}

}
