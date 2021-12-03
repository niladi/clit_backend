package clit.eval.datatypes.result;

import java.util.Collection;

import com.google.common.collect.Lists;

import clit.eval.interfaces.AnnotationResult;

public class DocumentResult implements AnnotationResult {
	public Collection<MentionResult> mentionResults = Lists.newArrayList();

	public void addMentionResult(final MentionResult mentionResult) {
		this.mentionResults.add(mentionResult);
	}

	public Collection<MentionResult> getMentionResults() {
		return this.mentionResults;
	}
}
