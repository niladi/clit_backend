package clit.eval.datatypes.result;

import java.util.Collection;

import com.google.common.collect.Lists;

import clit.eval.interfaces.AnnotationResult;

public class DatasetResult implements AnnotationResult {
	public Collection<DocumentResult> documentResults = Lists.newArrayList();

	public void addDocumentResult(final DocumentResult documentResult) {
		this.documentResults.add(documentResult);
	}
	
	public Collection<DocumentResult> getDocumentResults()
	{
		return this.documentResults;
	}


}
