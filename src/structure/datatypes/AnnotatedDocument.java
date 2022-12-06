package structure.datatypes;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.common.collect.Lists;

import structure.config.constants.EnumPipelineType;
@JsonIgnoreProperties(ignoreUnknown = true)
public class AnnotatedDocument implements Cloneable, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5127854714578450425L;
	protected String uri;
	protected String text;
	// TODO make List; add constructor that allows Collection and sorts it to List
	protected Collection<Mention> mentions;
	protected String componentId;
	protected EnumPipelineType pipelineType;

	/**
	 * Default constructor for conversion from JSON with Jackson.
	 */
	public AnnotatedDocument() {
		super();
	}

	public AnnotatedDocument(String text) {
		this.text = text;
	}

	public AnnotatedDocument(String text, Collection<Mention> mentions) {
		this.text = text;
		this.mentions = mentions;
	}

	public AnnotatedDocument(String text, Collection<Mention> mentions, String componentId,
			EnumPipelineType pipelineType) {
		this.text = text;
		this.mentions = mentions;
		this.componentId = componentId;
		this.pipelineType = pipelineType;
	}

	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public Collection<Mention> getMentions() {
		return mentions;
	}

	public void setMentions(Collection<Mention> mentions) {
		this.mentions = mentions;
	}

	public String getComponentId() {
		return componentId;
	}

	public void setComponentId(String componentId) {
		this.componentId = componentId;
	}

	public EnumPipelineType getPipelineType() {
		return pipelineType;
	}

	public void setPipelineType(EnumPipelineType pipelineType) {
		this.pipelineType = pipelineType;
	}

	@Override
	public String toString() {
		return "AnnotatedDocument [" + componentId + "] (" + text.substring(0, 12) + "...)";
	}

	/**
	 * Creates a collection of annotated documents from a single annotated document.
	 */
	public Collection<AnnotatedDocument> makeMultiDocuments() {
		final Collection<AnnotatedDocument> retDocuments = Lists.newArrayList();
		retDocuments.add(this);
		return retDocuments;
	}

	/**
	 * Make a deep copy of a document.
	 */
	public Object clone() {
		AnnotatedDocument document = null;
		try {
			document = (AnnotatedDocument) super.clone();
		} catch (CloneNotSupportedException e) {
			document = new AnnotatedDocument(this.getText());
			document.setComponentId(this.getComponentId());
			document.setPipelineType(this.getPipelineType());
			document.setUri(this.getUri());
			Collection<Mention> mentions = new ArrayList<>();
			if (this.getMentions() != null) {
				for (Mention mention : this.getMentions()) {
					mentions.add((Mention) mention.clone());
				}
			}
			document.setMentions(mentions);
		}
		return document;
	}

}
