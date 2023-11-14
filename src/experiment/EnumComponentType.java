package experiment;

import clit.eval.interfaces.Explainer;
import structure.interfaces.clit.Combiner;
import structure.interfaces.clit.Filter;
import structure.interfaces.clit.Splitter;
import structure.interfaces.clit.Transformer;
import structure.interfaces.clit.Translator;
import structure.interfaces.linker.Linker;
import structure.interfaces.pipeline.CandidateGenerator;
import structure.interfaces.pipeline.CandidateGeneratorDisambiguator;
import structure.interfaces.pipeline.Disambiguator;
import structure.interfaces.pipeline.Evaluator;
import structure.interfaces.pipeline.MentionDetector;
import structure.interfaces.pipeline.PipelineComponent;
import structure.interfaces.pipeline.Typing;

/**
 * Defines the type of a component, i.e. what is the component capable of.
 * Difference to EnumPipelineType is that this is defined for a single
 * component, while the EnumPipelineType is defined for a whole pipeline or
 * linker.
 * 
 * @author Kris Noullet, Samuel Printz
 */
public enum EnumComponentType {
	UNSPECIFIED(null, "NULL", "unspecified", "Unspecified"), //
	INPUT(null, "INPUT", "input", "Input"), //
	OUTPUT(null, "OUTPUT", "output", "Output"), //
	MD(MentionDetector.class, "MD", "md", "Mention Detector"),
	CG(CandidateGenerator.class, "CG", "cg", "Candidate Generator"),
	ED(Disambiguator.class, "ED", "ed", "Disambiguator"), //
	NER(Typing.class, "NER", "ner", "Entity Typing"),
	CG_ED(CandidateGeneratorDisambiguator.class, "CG_ED", "cg_ed", "Candidate Generator Disambiguator"), //
	MD_CG_ED(Linker.class, "MD_CG_ED", "md_cg_ed", "Full"), //
	COMBINER(Combiner.class, "CO", "combiner", "Combiner"), //
	SPLITTER(Splitter.class, "SP", "splitter", "Splitter"), //
	TRANSLATOR(Translator.class, "TR", "translator", "Translator"), //
	TRANSFORMER(Transformer.class, "TRF", "transformer", "Transformer"), //
	FILTER(Filter.class, "FI", "filter", "Filter"), //
	EVALUATOR(Evaluator.class, "EVAL", "evaluator", "Evaluator"), //
	EXPLAINER(Explainer.class, "EXPL", "explainer", "Explainer"),//

	;

	public final Class<? extends PipelineComponent> type;
	public final String id;
	public final String name;
	public final String displayName;

	EnumComponentType(final Class<? extends PipelineComponent> clazz, final String id, final String name,
			final String displayName) {
		this.type = clazz;
		this.id = id;
		this.name = name;
		this.displayName = displayName;
	}

	public boolean isInstance(Object o) {
		if (this.type == null || o == null) {
			return false;
		}
		return this.type.isInstance(o);
	}

	/**
	 * Returns the display name of a component type given its ID.
	 * 
	 * @param id
	 * @return
	 */
	public static String getDisplayNameById(String id) {
		for (EnumComponentType e : values()) {
			if (e.id.equals(id)) {
				return e.displayName;
			}
		}
		throw new IllegalArgumentException("No component type with ID '" + id + "'");
	}

	/**
	 * Returns the display name of a component type given its name.
	 * 
	 * @param name
	 * @return
	 */
	public static String getDisplayNameByName(String name) {
		for (EnumComponentType e : values()) {
			if (e.name.equals(name)) {
				return e.displayName;
			}
		}
		throw new IllegalArgumentException("No component type with name '" + name + "'");
	}

	/**
	 * Returns the ID of a component type given its name.
	 * 
	 * @param name
	 * @return
	 */
	public static String getIDByName(String name) {
		for (EnumComponentType e : values()) {
			if (e.name.equals(name)) {
				return e.id;
			}
		}
		throw new IllegalArgumentException("No component type with name '" + name + "'");
	}

	public static EnumComponentType getByName(String name) {
		for (EnumComponentType e : values()) {
			if (e.name.equals(name)) {
				return e;
			}
		}
		throw new IllegalArgumentException("No component type with name '" + name + "'");
	}

}
