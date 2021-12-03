package structure.interfaces.linker;

import structure.datatypes.AnnotatedDocument;
import structure.datatypes.Mention;
import structure.interfaces.Weighable;
import structure.interfaces.clit.Translator;
import structure.interfaces.pipeline.CandidateGeneratorDisambiguator;
import structure.interfaces.pipeline.FullAnnotator;
import structure.interfaces.pipeline.MentionDetector;
import structure.utils.Loggable;

public interface Linker extends FullAnnotator, MentionDetector, CandidateGeneratorDisambiguator, Weighable<Mention>, Loggable {

	public boolean init() throws Exception;

	// TODO Move to FullAnnotator?
	// TODO Rename to annotate()?
	public AnnotatedDocument annotate(final AnnotatedDocument document) throws Exception;

	// TODO Would this make sense?
	// public AnnotatedDocument annotateMentions(final String text) throws
	// Exception;

	public String getKG();

	@Override
	public int hashCode();

	@Override
	boolean equals(Object obj);

	public long getID();

	default int nullHash(Object o, int otherwise) {
		return o == null ? otherwise : o.hashCode();
	}

	public Linker translator(final Translator translator);
}
