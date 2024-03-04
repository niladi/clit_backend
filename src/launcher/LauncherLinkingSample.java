package launcher;

import java.util.Collection;
import java.util.HashMap;

import experiment.PipelineItem;
import linking.candidategeneration.CandidateGeneratorMap;
import linking.mentiondetection.InputProcessor;
import linking.mentiondetection.exact.MentionDetectorMap;
import structure.config.kg.EnumModelType;
import structure.datatypes.AnnotatedDocument;
import structure.interfaces.pipeline.CandidateGenerator;
import structure.interfaces.pipeline.Disambiguator;
import structure.interfaces.pipeline.MentionDetector;
import structure.utils.datastructure.MentionUtils;

public class LauncherLinkingSample {

	public static void main(String[] args) {
		try {
			new LauncherLinkingSample().run();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void run() throws Exception {
		final EnumModelType KG = EnumModelType.DEFAULT;// DBPEDIA_FULL;
		final AnnotatedDocument document = new AnnotatedDocument("");
		final HashMap<String, Collection<String>> surfaceFormLinks = new HashMap<>();
		final MentionDetector md = new MentionDetectorMap(surfaceFormLinks, new InputProcessor(null));
		final CandidateGenerator cg = new CandidateGeneratorMap(surfaceFormLinks);
		final Disambiguator d = new Disambiguator() {

			@Override
			public Collection<AnnotatedDocument> execute(PipelineItem callItem, AnnotatedDocument document)
					throws Exception {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public AnnotatedDocument disambiguate(AnnotatedDocument document) throws Exception {
				// TODO Auto-generated method stub
				return null;
			}
		};
		md.detect(document);
		cg.generate(document);
		d.disambiguate(document);
		MentionUtils.displayMentions(document.getMentions());
	}
}
