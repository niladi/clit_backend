package launcher;

import java.util.Collection;
import java.util.HashMap;

import linking.candidategeneration.CandidateGeneratorMap;
import linking.disambiguation.DisambiguatorAgnos;
import linking.mentiondetection.InputProcessor;
import linking.mentiondetection.exact.MentionDetectorMap;
import structure.config.kg.EnumModelType;
import structure.datatypes.AnnotatedDocument;
import structure.interfaces.pipeline.CandidateGenerator;
import structure.interfaces.pipeline.MentionDetector;
import structure.utils.MentionUtils;

public class LauncherLinkingSample {

	public static void main(String[] args) {
		try {
			new LauncherLinkingSample().run();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void run() throws Exception {
		final EnumModelType KG = EnumModelType.DEFAULT;//DBPEDIA_FULL;
		final AnnotatedDocument document = new AnnotatedDocument("");
		final HashMap<String, Collection<String>> surfaceFormLinks = new HashMap<>();
		final MentionDetector md = new MentionDetectorMap(surfaceFormLinks, new InputProcessor(null));
		final CandidateGenerator cg = new CandidateGeneratorMap(surfaceFormLinks);
		final DisambiguatorAgnos d = new DisambiguatorAgnos(KG);
		md.detect(document);
		cg.generate(document);
		d.disambiguate(document);
		MentionUtils.displayMentions(document.getMentions());
	}
}
