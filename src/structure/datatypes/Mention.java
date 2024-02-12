package structure.datatypes;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.aksw.gerbil.transfer.nif.Marking;
import org.aksw.gerbil.transfer.nif.Meaning;
import org.aksw.gerbil.transfer.nif.ScoredMarking;
import org.aksw.gerbil.transfer.nif.Span;
import org.aksw.gerbil.transfer.nif.TypedMarking;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Lists;

import structure.utils.Loggable;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Mention implements Cloneable, Loggable, Serializable {
	private static final long serialVersionUID = -2779470073095478510L;
	protected String mention;
	protected PossibleAssignment assignment;
	protected int offset = -1;
	protected double detectionConfidence = -1;
	protected Collection<PossibleAssignment> possibleAssignments;
	protected String originalMention;
	protected String originalWithoutStopwords;

	public Mention() {
	}

	public Mention(final String word, final PossibleAssignment assignment, final int offset,
			final double detectionConfidence, final String originalMention, final String originalWithoutStopwords) {
		this(word, Optional.ofNullable(assignment).map(e -> Arrays.asList(e)).orElse(new ArrayList<>()), offset,
				detectionConfidence,
				originalMention, originalWithoutStopwords);
	}

	public Mention(final String word, final Collection<PossibleAssignment> possibleAssignments, final int offset,
			final double detectionConfidence, final String originalMention, final String originalWithoutStopwords) {
		this.mention = word;
		this.offset = offset;
		this.detectionConfidence = detectionConfidence;
		this.originalMention = originalMention;
		this.originalWithoutStopwords = originalWithoutStopwords;
		this.possibleAssignments = possibleAssignments;
		// TODO References to interface static methods are allowed only at source level
		// 1.8 or above
		assignBest();
	}

	public Mention(final String word, final Collection<PossibleAssignment> possibleAssignments, final int offset) {
		this.mention = word;
		this.offset = offset;
		this.detectionConfidence = -1;
		this.originalMention = mention;
		this.originalWithoutStopwords = mention;
		this.assignment = null;
		this.possibleAssignments = possibleAssignments;
	}

	/**
	 * This is used as default constructor by Jackson when mapping from JSON.
	 */
	public Mention(@JsonProperty("mention") final String word, @JsonProperty("offset") final int offset) {
		this(word, new ArrayList<>(), offset, -1, word, word);
	}

	public Mention(final String word, final PossibleAssignment assignment, final int offset) {
		// -1 being as 'not set'
		this(word, assignment, offset, -1, word, word);
	}

	/**
	 * Copy constructor
	 * 
	 * @param mention
	 */
	public Mention(Mention mention) {
		this.mention = mention.getMention();
		this.offset = mention.getOffset();
		this.detectionConfidence = mention.getDetectionConfidence();
		this.originalMention = mention.getOriginalMention();
		this.originalWithoutStopwords = mention.getOriginalWithoutStopwords();
		this.possibleAssignments = new ArrayList<>();
		if (mention.getPossibleAssignments() != null) {
			for (PossibleAssignment assignment : mention.getPossibleAssignments()) {
				if (assignment != null) {
					this.possibleAssignments.add(new PossibleAssignment(assignment));
				}
			}
		}
		assignBest();
	}

	/**
	 * Preferably use {@link #Mention(Marking, String)} to have meaningful mention
	 * objects. This method calls said constructor w/ text = "" (empty string)
	 * 
	 * @param marking marking to be copied
	 */
	public Mention(final Marking marking) {
		this(marking, "");
	}

	/**
	 * Transforms a marking into a mention by copying all relevant information
	 * depending on its implementing INTERFACES as described in:
	 * https://github.com/dice-group/gerbil/wiki/Document-Markings-in-gerbil.nif.transfer
	 * 
	 * @param marking marking to be copied
	 * @param text    string based on which we extract offset and length, generating
	 *                the wanted mention string
	 */
	public Mention(final Marking marking, final String text) {
		// https://github.com/dice-group/gerbil/wiki/Document-Markings-in-gerbil.nif.transfer
		double confidence = 0;

		if (marking instanceof ScoredMarking) {
			ScoredMarking cast = (ScoredMarking) marking;
			confidence = cast.getConfidence();
		}

		// Add possible assignments
		if (marking instanceof Meaning) {
			Meaning cast = (Meaning) marking;
			final List<PossibleAssignment> assignments = Lists.newArrayList();
			for (String uri : cast.getUris()) {
				assignments.add(new PossibleAssignment(uri, confidence));
			}
			this.possibleAssignments = assignments;
			this.assignment = assignments != null && assignments.size() > 0 ? assignments.get(0) : null;
		}

		// Offset and mention texts (from offset -> length)
		if (marking instanceof Span) {
			Span cast = (Span) marking;
			this.offset = cast.getStartPosition();
			final String mentionText;
			if (text != null && text.length() > cast.getStartPosition() + cast.getLength()) {
				mentionText = text.substring(cast.getStartPosition(), cast.getStartPosition() + cast.getLength());
				;
			} else {
				mentionText = "";
			}
			this.mention = mentionText;
			this.originalMention = mentionText;
			this.originalWithoutStopwords = mentionText;
		} else {
			this.mention = "";
			this.originalMention = "";
			this.originalWithoutStopwords = "";
		}

		// May
		if (marking instanceof TypedMarking) {
			TypedMarking cast = (TypedMarking) marking;
		}

	}

	@Override
	public String toString() {
		// return "[" + getMention() + "/" + getOriginalMention() + "/" +
		// getOriginalWithoutStopwords() + " Chosen{"
		// + (this.assignment == null ? "no assngmt" : this.assignment) + "}" + " -
		// Possible{"
		// + this.possibleAssignments + "} ]";
		return "[" + getMention() + " (chosen{" + (this.assignment == null ? "" : this.assignment) + "}" + ", possible{"
				+ this.possibleAssignments + "})]";
	}

	/**
	 * Assigns this mention to a specific URL
	 * 
	 * @param assignment
	 */
	public void setAssignment(final PossibleAssignment assignment) {
		this.assignment = assignment;
	}

	public void assignBest() {
		final List<PossibleAssignment> listAssignments;
		if (possibleAssignments instanceof List) {
			listAssignments = (List) possibleAssignments;
		} else {
			if (possibleAssignments != null) {
				listAssignments = new ArrayList<>(possibleAssignments);
			} else if (this.assignment != null) {
				listAssignments = new ArrayList<>();
				listAssignments.add(this.assignment);
			} else {
				return;
			}
		}
		Collections.sort(listAssignments, Comparator.reverseOrder());
		if (listAssignments.size() > 0) {
			setAssignment(listAssignments.get(0));
		}
	}

	public PossibleAssignment getAssignment() {
		return this.assignment;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Mention && obj != null) {
			@SuppressWarnings("rawtypes")
			final Mention m = ((Mention) obj);
			boolean ret = true;
			ret &= (m.assignment == null && this.assignment == null) ? true
					: ((m.assignment == null) ? false : this.assignment.equals(m.assignment));
			ret &= (m.getMention() == null && this.getMention() == null) ? true
					: ((m.getMention() == null || getMention() == null) ? false
							: this.getMention().equals(m.getMention()));
			ret &= (m.getOriginalWithoutStopwords() == null && this.getOriginalWithoutStopwords() == null) ? true
					: ((m.getOriginalWithoutStopwords() == null || getOriginalWithoutStopwords() == null) ? false
							: this.getOriginalWithoutStopwords().equals(m.getOriginalWithoutStopwords()));
			ret &= (m.getOriginalMention() == null && this.getOriginalMention() == null) ? true
					: ((m.getOriginalMention() == null || getOriginalMention() == null) ? false
							: this.getOriginalMention().equals(m.getOriginalMention()));
			ret &= (m.getDetectionConfidence() == this.getDetectionConfidence());
			ret &= (this.hashCode() == obj.hashCode());
			return ret;
		}
		return super.equals(obj);
	}

	@Override
	public int hashCode() {
		// If all constituents are null, make it a weird sum, so there is no collision
		// with anything else
		return ((this.assignment == null) ? 2 : this.assignment.hashCode())
				+ ((this.getMention() == null) ? 4 : (this.getMention().hashCode()))
				+ ((this.getOriginalMention() == null) ? 8 : this.getOriginalMention().hashCode())
				+ ((this.getOriginalWithoutStopwords() == null) ? 16 : this.getOriginalWithoutStopwords().hashCode())
				+ this.offset + (int) (10d * this.detectionConfidence)
				+ (this.assignment == null ? 32 : this.assignment.hashCode());
	}

	public String getMention() {
		return mention;
	}

	public int getOffset() {
		return offset;
	}

	public void addPossibleAssignment(PossibleAssignment entityCandidate) {
		if (this.possibleAssignments == null) {
			this.possibleAssignments = new ArrayList<>(Arrays.asList(entityCandidate));
		} else {
			this.possibleAssignments.add(entityCandidate);
		}
	}

	public void updatePossibleAssignments(Collection<PossibleAssignment> possibleAssignments) {
		this.possibleAssignments = possibleAssignments;
	}

	public Collection<PossibleAssignment> getPossibleAssignments() {
		return this.possibleAssignments;
	}

	public double getDetectionConfidence() {
		return detectionConfidence;
	}

	public String getOriginalMention() {
		return this.originalMention;
	}

	public void setOriginalMention(String originalMention) {
		this.originalMention = originalMention;
	}

	public void updateOffset(int offset) {
		this.offset = offset;
	}

	/**
	 * Copies disambiguation results from given mention to this one <b>NOTE</b>:
	 * This only crushes the current mention's possible assignments by the passed
	 * one's. If disambiguation evolves to make differences even for mentions linked
	 * to the same detected word, then this should be changed or scores will not
	 * reflect the ideas properly.
	 * 
	 * @param fromMention mention from which to copy
	 */
	public void copyResults(Mention fromMention) {
		// Make sure it's the same mention word
		if (fromMention.getMention() == null || getMention() == null
				|| !getMention().equals(fromMention.getMention())) {
			getLogger().error(
					"Consistency error. Both mentions should have the same mention token in order to copy disambiguation results.");
		}
		updatePossibleAssignments(fromMention.getPossibleAssignments());
		setAssignment(fromMention.getAssignment());
	}

	public String getOriginalWithoutStopwords() {
		return originalWithoutStopwords;
	}

	public void setOriginalWithoutStopwords(String originalWithoutStopwords) {
		this.originalWithoutStopwords = originalWithoutStopwords;
	}

	public void setMention(String mention) {
		this.mention = mention;
	}

	public void setOffset(int offset) {
		this.offset = offset;
	}

	public void setDetectionConfidence(double detectionConfidence) {
		this.detectionConfidence = detectionConfidence;
	}

	/**
	 * Whether this and the passed mentions overlap (based on offset and original
	 * mention length)
	 * 
	 * @param otherMention other mention
	 * @return true if overlapping, false otherwise
	 */
	public boolean overlaps(final Mention otherMention) {
		final int rightOffset = getOffset();
		final int leftOffset = otherMention.getOffset();
		final int rightLength = getOriginalMention().length();
		final int leftLength = otherMention.getOriginalMention().length();
		return ((leftOffset <= rightOffset) && (rightOffset <= leftOffset + leftLength))
				|| ((rightOffset <= leftOffset) && (leftOffset <= rightOffset + rightLength));
	}

	// TODO Remove this workaround
	public void toMentionDetectionResult() {
		this.assignment = null;
		this.possibleAssignments = new ArrayList<>();
	}

	// TODO Remove this workaround
	public void toCandidateGenerationResult() {
		this.assignment = null;
	}

	/**
	 * Make a deep copy of a mention.
	 */
	public Object clone() {
		Mention mention = null;
		try {
			mention = (Mention) super.clone();
		} catch (CloneNotSupportedException e) {
			mention = new Mention(this); // use copy constructor above
		}
		return mention;
	}

}
