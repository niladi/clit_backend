package launcher.debug;

import linking.linkers.TextRazorLinker;
import structure.datatypes.AnnotatedDocument;

public class LauncherTestTextRazor {

	public static void main(String[] args) {
		new TextRazorLinker().execute(null, new AnnotatedDocument("Joan Baez and Steve Jobs are famous people."));

	}

}
