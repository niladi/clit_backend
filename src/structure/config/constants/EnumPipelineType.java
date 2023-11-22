package structure.config.constants;

/**
 * Defines what a linker or a pipeline is capable of. Difference to
 * EnumComponentType is that this is defined for a whole pipeline or linker,
 * while the EnumComponentType is defined for a single component (mention
 * detector, splitter, ...) only.
 * 
 * @author Samuel Printz
 */
public enum EnumPipelineType {

	NONE("none", "None"), //
	MD("md", "Mention detection"), //
	CG("cg", "Candidate generation"), //
	ED("ed", "Entity disambiguation"), //
	NER("ner", "Entity typing"), //
	MD_CG("md_cg", "Mention detection and candidate generation"), //
	CG_ED("cg_ed", "Candidate generation and entity disambiguation"), //
	FULL("full", "Mention detection, candidate generation and entity disambiguation")//
	;

	private String name;
	private String label;

	EnumPipelineType(String name, String label) {
		this.name = name;
		this.label = label;
	}

	public String getName() {
		return this.name;
	}

	public String getLabel() {
		return this.label;
	}

	public static EnumPipelineType getByName(String name) {
		for (EnumPipelineType e : values()) {
			if (e.name.equals(name)) {
				return e;
			}
		}
		throw new IllegalArgumentException("No pipeline type with name '" + name + "'");
	}

}
