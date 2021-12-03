package linking.linkers;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.function.BiFunction;

import com.google.common.collect.Lists;
import com.textrazor.TextRazor;
import com.textrazor.annotations.Entity;
import com.textrazor.annotations.Response;

import structure.abstractlinker.AbstractLinkerWebAPI;
import structure.config.kg.EnumModelType;
import structure.datatypes.Mention;
import structure.datatypes.PossibleAssignment;
import structure.interfaces.linker.APIKey;

public class TextRazorLinker extends AbstractLinkerWebAPI implements APIKey {
	// TextRazor java documentation: https://www.textrazor.com/docs/java
	private TextRazor linker;
	private List<String> apiKeys = Lists.newArrayList(new String[] { //
			"f0b9b1e860f6d28032dbf70d7b5c0014a2c5e06e1f08fc0e55ac8389", //
			"3c822f0914f7dac1cda3a89789fd18a4316575db3d76935780700ad3", //
			// L.H.
			"46e4ff83f765211715e483a8466f3cf97a01882813305432372c70cd", //
			"cf890573a6a57fcd40884932724293456e44d34a599d503a6e1a52c2", //
			"431ad8e149f7590cd5f09de58e9cf6105c479feefdcee00acfcfd55f", //
			"5c6c67a884edfe17bbf8d39839879beac7d272c738f00dd700afe627", //
			"527f0d8c7a165e39ea70264f937138f690c25295a1ca60fe05688857"//
	});
	private Collection<String> usedKeys = new HashSet<>();

	public TextRazorLinker(final EnumModelType kg) {
		super(kg);
		// Have to use APIKey at instantiation unfortunately...
		final String usedAPIKey = apiKeys.get(0);

		this.linker = instantiateLinker(usedAPIKey);
		// Keep track of API key
	}

	public TextRazorLinker() {
		this(EnumModelType.DEFAULT);
	}

//	@Override
//	public AnnotatedDocument annotate(final AnnotatedDocument document) throws IOException {
//		final Response response = sendRequest(document.getText());
//		final Object analysis = response.getCleanedText();// .getEntities();
//
//		// call data to mentions
//		final Collection<Mention> mentions = dataToMentions(response);
//		document.setMentions(mentions);
//
//		// System.out.println("Analysis: " + analysis);
//		System.out.println("TextRazorLinker: " + mentions);
//		return document;
//	}

	@Override
	public boolean init() throws Exception {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Number getWeight() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public BiFunction<Number, Mention, Number> getScoreModulationFunction() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> getAPIKeys() {
		return this.apiKeys;
	}

	@Override
	public String getCurrentKey() {
		return this.linker.getApiKey();
	}

	@Override
	public Collection<String> getUsedKeys() {
		return this.usedKeys;
	}

	@Override
	public void setKey(String apiKey) {
		this.linker.setApiKey(apiKey);
		this.usedKeys.add(apiKey);
	}

	@Override
	public boolean switchToUnusedKey() {
		System.out.println("Switching API key.");
		for (String apiKey : getAPIKeys()) {
			if (!this.usedKeys.contains(apiKey)) {
				setKey(apiKey);
				System.out.println("New key used: " + apiKey);
				return true;
			}
		}
		this.linker = instantiateLinker(getCurrentKey());// new TextRazor(getCurrentKey());
		System.err.println("No more unused keys available.");
		return false;
	}

	private TextRazor instantiateLinker(String usedAPIKey) {
		final TextRazor razor = new TextRazor(usedAPIKey);
		razor.setLanguageOverride("eng");
		this.usedKeys.add(usedAPIKey);
		return razor;
	}

	@Override
	public Collection<Mention> dataToMentions(Object response) {
		if (response instanceof Response) {
			final List<Entity> entities = ((Response) response).getEntities();
			// Transformed for us
			final List<Mention> mentions = Lists.newArrayList();
			if (entities != null) {
				for (Entity e : entities) {
					// System.out.println(e.getData());
					// System.out.println(e.getWikiLink());
					final String word = e.getMatchedText();
					final PossibleAssignment assignment = new PossibleAssignment(translate(e.getWikiLink()),
							e.getRelevanceScore());
					final int offset = e.getStartingPos();
					final double detectionConfidence = e.getConfidenceScore();
					final String originalMention = e.getMatchedText();
					final String originalWithoutStopwords = e.getMatchedText();

					final Mention mention = new Mention(word, assignment, offset, detectionConfidence, originalMention,
							originalWithoutStopwords);
					mention.assignBest();
					mentions.add(mention);
				}
			}
			return mentions;
		}
		return null;
	}

	@Override
	protected Response sendRequest(String text) throws Exception {
		final List<String> extractors = Lists.newArrayList(new String[] { "entities", "topics", "words", "phrases",
				"dependency-trees", "relations", "entailments", "senses", "spelling" });
		// Valid Options: entities, topics, words, phrases, dependency-trees, relations,
		// entailments, senses, spelling
		this.linker.setExtractors(extractors);
		// System.out.println(this.linker.getEntityDictionaries());
		final Response response = this.linker.analyze(text).getResponse();
		return response;
	}

}
