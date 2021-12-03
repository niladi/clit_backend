package launcher;

import java.io.IOException;

import linking.linkers.AgnosLinker;
import structure.config.kg.EnumModelType;
import structure.datatypes.AnnotatedDocument;
import structure.interfaces.linker.Linker;

public class LauncherLinking {

	public static void main(String[] args) {
		try {
			new LauncherLinking().run();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void run() throws Exception {
		final EnumModelType KG = EnumModelType.//
		// WIKIDATA//
		// DBPEDIA_FULL//
				DEFAULT//
		;
		final Linker linker = new AgnosLinker(KG);
		linker.init();
		final AnnotatedDocument input = new AnnotatedDocument("hello world");
		linker.annotate(input);
	}
}