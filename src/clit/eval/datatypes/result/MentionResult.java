package clit.eval.datatypes.result;

import java.util.Set;

import org.aksw.gerbil.transfer.nif.Marking;
import org.aksw.gerbil.transfer.nif.Meaning;
import org.aksw.gerbil.transfer.nif.Span;

import clit.eval.interfaces.AnnotationResult;
import structure.datatypes.Mention;

public class MentionResult implements AnnotationResult {

	// Stand-alone features - they help to figure out WHY something doesn't match
	// (e.g. whether TP TN FP FN)
	public String text = null;
	public String foundUri = null;
	public Set<String> goldUris = null;
	public String foundMention = null, goldMention = null;

	// Comparative features (to each other)
	public boolean match = false;
	public int containsUriMatches = 0;
	public boolean inCandidates = false;

	public MentionResult unpackFrom(Marking mark, Mention m) {
		if (m == null && mark == null) {
			return this;
		}

		unpackMention(m);
		unpackMarking(mark);

		// Cannot compare them if any of them is null...
		if (mark == null || m == null) {
			return this;
		}

		return this;
	}

	public MentionResult unpackMarking(Marking mark) {
		if (mark == null)
			return this;
		if (mark instanceof Meaning) {
			final Meaning meaning = (Meaning) mark;
			this.goldUris = meaning.getUris();
		}

		if (mark instanceof Span) {
			final Span span = (Span) mark;
			this.goldMention = span.toString();
		}

		return this;
	}

	public MentionResult unpackMention(Mention m) {
		if (m == null)
			return this;
		this.foundMention = m.getMention();
		if (m.getAssignment() == null) {
			this.foundUri = "Assignment-not-found";
		} else {
			this.foundUri = m.getAssignment().getAssignment();
		}
		return this;
	}

}
