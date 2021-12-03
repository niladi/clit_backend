package linking.disambiguation.consolidation;

import java.util.Map;

import structure.datatypes.AnnotatedDocument;
import structure.interfaces.linker.Linker;

public interface Consolidator {


	
	public Map<String, AnnotatedDocument> mergeByKG(Map<Linker, AnnotatedDocument> mapLinkerMention);
}
