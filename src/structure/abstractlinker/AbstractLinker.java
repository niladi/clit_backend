package structure.abstractlinker;

import java.io.IOException;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicLong;

import com.google.common.collect.Lists;

import experiment.PipelineItem;
import structure.config.kg.EnumModelType;
import structure.datatypes.AnnotatedDocument;
import structure.datatypes.Mention;
import structure.interfaces.clit.Translator;
import structure.interfaces.linker.Linker;
import structure.utils.MentionUtils;

public abstract class AbstractLinker implements Linker {
	private static AtomicLong counter = new AtomicLong(0);
	private final long id;

	protected final EnumModelType KG;
	private Translator translator = null;

	public AbstractLinker(EnumModelType KG) {
		this.KG = KG;
		synchronized (counter) {
			this.id = counter.incrementAndGet();
		}
	}

	@Override
	public Collection<AnnotatedDocument> execute(PipelineItem callItem, AnnotatedDocument document) {
		try {
			final AnnotatedDocument doc = annotate(document);
			if (doc != null) {
				return doc.makeMultiDocuments();
			} else {
				return Lists.newArrayList();
			}
		} catch (IOException e) {
			e.printStackTrace();
		} // .iterator().next().toString();
		catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Workaround for all linkers whose APIs don't provide combined candidate
	 * generation and entity disambiguation (CG_ED).
	 * 
	 * @param inputText
	 * @param mentions
	 * @return
	 */
	@Override
	public AnnotatedDocument generateDisambiguate(final AnnotatedDocument document) throws Exception {
		System.out.println("Warning: " + this.getClass().toString()
				+ " does not provide candidate generation disamibiguation by its native API; using workaround");
		// Workaround: Send full text to linker, receive the result and restrict it to
		// the previously detected mentions
		// from the MD step; therefore save a copy of the original mentions to filter
		// the annotation result
		final Collection<Mention> originalMentions = MentionUtils.copyMentions(document.getMentions());
		annotate(document);
		Collection<Mention> mentions = MentionUtils.filterMentionsByMentions(document.getMentions(), originalMentions);
		document.setMentions(mentions);
		return document;
	}

	@Override
	public AnnotatedDocument detect(final AnnotatedDocument document, String source) throws Exception {
		return detect(document);
	}

	@Override
	public AnnotatedDocument detect(final AnnotatedDocument document) throws Exception {
		System.out.println("Warning: " + this.getClass().toString()
				+ " does not provide mention detection by its native API; using workaround");
		annotate(document);
		// Workaround: remove candidates manually, keep only the markings
		for (Mention mention : document.getMentions()) {
			mention.toMentionDetectionResult();
		}
		return document;
	}

	@Override
	public long getID() {
		return this.id;
	}

	@Override
	public String getKG() {
		return this.KG.name();
	}

	@Override
	public int hashCode() {
		return super.hashCode() + getClass().hashCode() + getKG().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Linker) {
			final Linker linker = (Linker) obj;
			return super.equals(obj) && (getID() == linker.getID()) && getClass().equals(obj.getClass())
					&& getKG().equals(linker.getKG());
		}
		return false;
		// super.equals(obj);
	}

	@Override
	public Linker translator(final Translator translator) {
		this.translator = translator;
		return this;
	}

	public String translate(final String entity) {
		if (this.translator == null || entity == null) {
			return entity;
		}
		return this.translator.translate(entity);
	}

	@Override
	public String toString() {
		// return super.toString();
		return this.getClass().getName();
	}
}
