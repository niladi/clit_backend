package clit;

import structure.interfaces.pipeline.PipelineComponent;

public abstract class APIComponent implements PipelineComponent {

	private final String urlString;
	private final String displayName;

	public APIComponent(final String urlString, final String displayName) {
		this.urlString = urlString;
		this.displayName = displayName;
	}

	public String getUrlString() {
		return urlString;
	}

	public String getDisplayName() {
		return displayName;
	}
	
}
