package launcher;

import java.io.IOException;

import linking.linkers.RadboudLinker;
import structure.datatypes.AnnotatedDocument;

public class LauncherTestRadboudLinker {

	public static void main(String[] args) {
		final String text = "If you're going to try, go all the way - Charles Bukowski and bill Gates";
		try {
			final AnnotatedDocument doc = new RadboudLinker().annotate(new AnnotatedDocument(text));
			System.out.println("Mentions: " + doc.getMentions());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
