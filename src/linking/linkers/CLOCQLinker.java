package linking.linkers;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

import structure.abstractlinker.AbstractLinkerURL;
import structure.abstractlinker.AbstractLinkerURLGET;
import structure.config.kg.EnumModelType;
import structure.datatypes.AnnotatedDocument;
import structure.datatypes.Mention;
import structure.datatypes.PossibleAssignment;
import structure.interfaces.pipeline.CandidateGenerator;
import structure.utils.FunctionUtils;
import structure.utils.LinkerUtils;

public class CLOCQLinker extends AbstractLinkerURLGET implements CandidateGenerator {

	private String textKeyword = "question";
	private Float confidence = 1.0f;
	private String kKeyword = "k";

	public CLOCQLinker(EnumModelType KG) {
		super(KG);
		init();
	}

	public static void main(String[] args) {
		final CLOCQLinker linker = new CLOCQLinker(EnumModelType.DEFAULT);
		try {
			final AnnotatedDocument doc = linker
					.annotate(new AnnotatedDocument("Napoleon and Steve Jobs are both apple fanboys."));
			System.out.println(doc.getMentions());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public AnnotatedDocument annotate(final AnnotatedDocument document) throws IOException {
		try {
			AnnotatedDocument doc = super.annotate(document);
			return doc;
		} catch (IOException ioe) {
			ioe.printStackTrace();
			return null;
		}
	}

	@Override
	public boolean init() {
		// sets the scheme
		https();
		// http();
		// sets the url
		url("clocq.mpi-inf.mpg.de");
		// sets the suffix
		suffix("/linking_api/entity_linking");
		setParam(kKeyword, "AUTO");

		return true;
	}

	@Override
	public Number getWeight() {
		return 1.0f;
	}

	@Override
	public BiFunction<Number, Mention, Number> getScoreModulationFunction() {
		return FunctionUtils::returnScore;
	}

	@Override
	public AbstractLinkerURL setText(String inputText) {
		this.params.put(this.textKeyword, inputText);
		return this;
	}

	@Override
	public String getText() {
		return this.params.get(this.textKeyword);
	}

	@Override
	protected HttpURLConnection openConnection(String input) throws URISyntaxException, IOException {
		// final String query = textKeyword + "=" + input + "&" + confidenceKeyword +
		// "=" + confidence;
		setParam(textKeyword, input);
		final HttpURLConnection conn = openConnectionWParams();
		return conn;
	}

	@Override
	protected void setupConnectionDetails(HttpURLConnection conn) throws ProtocolException {
		conn.setRequestProperty("accept", "application/json");
	}

	@Override
	public Collection<Mention> dataToMentions(final Object annotatedText) {
		return LinkerUtils.clocqJSONtoMentions(getText(), annotatedText.toString(), this.confidence);
	}

	@Override
	public AnnotatedDocument generate(AnnotatedDocument document) throws IOException {
		// Step 0: Make a new annotated document with just the text
		final AnnotatedDocument docToGenerate = new AnnotatedDocument(document.getText());
		// Step 1: Annotate it "normally"
		final AnnotatedDocument generatedCandidates = annotate(docToGenerate);

		final Map<Integer, Mention> mapOffsetMentions = new HashMap<>();
		for (Mention m : generatedCandidates.getMentions()) {
			mapOffsetMentions.put(m.getOffset(), m);
		}

		final Collection<Mention> mentions = document.getMentions();

		// Step 2: If there is a mention that matches (based on offset), add the found
		// candidates!
		for (Mention m : mentions) {
			final Mention mentionWithCandidates = mapOffsetMentions.get(m.getOffset());
			if (mentionWithCandidates != null) {
				// Add each found candidate to the return document
				for (PossibleAssignment ass : mentionWithCandidates.getPossibleAssignments()) {
					m.addPossibleAssignment(ass);
				}
			}
		}
		return document;
	}

}
