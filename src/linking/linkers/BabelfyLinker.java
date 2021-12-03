package linking.linkers;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.function.BiFunction;

import com.google.common.collect.Lists;

import structure.abstractlinker.AbstractLinkerURL;
import structure.abstractlinker.AbstractLinkerURLGET;
import structure.config.constants.Strings;
import structure.config.kg.EnumModelType;
import structure.datatypes.Mention;
import structure.interfaces.linker.APIKey;
import structure.utils.FunctionUtils;
import structure.utils.LinkerUtils;

public class BabelfyLinker extends AbstractLinkerURLGET implements APIKey {

	private List<String> apiKeys = Lists.newArrayList(new String[] { //
			Strings.BABELFY_KEY.val, //
			// L.H.
			"89aad51f-fff9-4acb-a080-10da4903f346", //
			"80efb44a-c263-4d29-b8e2-66f99b0ec40a", //
			"50a668d5-148e-4406-b5f8-0cfbf642b1cc", //
			"3d6a1134-e8fc-49be-aa33-e8fa86434230", //
	});
	private Collection<String> usedKeys = new HashSet<>();

	final String keywordText = "text";
	final String keywordLang = "lang";
	final String keywordAPIKey = "key";

	final String paramLang = "EN";
	private String paramAPIKey = apiKeys.get(0);

	public BabelfyLinker() {
		this(EnumModelType.WORDNET);
	}

	public BabelfyLinker(EnumModelType KG) {
		super(KG);
		init();
	}

	@Override
	public boolean init() {
		https();
		url("babelfy.io");
		suffix("/v1/disambiguate");
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
	protected HttpURLConnection openConnection(String input) throws URISyntaxException, IOException {
		// Set the GET parameters
		setParam(keywordAPIKey, paramAPIKey);
		setParam(keywordLang, paramLang);
		setParam(keywordText, input);
		final HttpURLConnection conn = openConnectionWParams();
		// System.out.println("URL: " + conn.getURL());
		return conn;
	}

	@Override
	protected void setupConnectionDetails(HttpURLConnection conn) throws ProtocolException {
		// conn.setRequestProperty("Accept-Encoding", "gzip");
		conn.setRequestProperty("Accept-Encoding", "application/json");
	}

	@Override
	public Collection<Mention> dataToMentions(Object annotatedText) {
		// TODO catch exceeded API limit
		final String inputText = this.params.get(keywordText);
		if (inputText == null) {
			System.err.println("No input defined");
		}
		return LinkerUtils.babelfyJSONtoMentions(annotatedText.toString(), inputText);
	}

	@Override
	public String getText() {
		return this.params.get(this.keywordText);
	}

	@Override
	public AbstractLinkerURL setText(final String inputText) {
		this.params.put(this.keywordText, inputText);
		return this;
	}

	@Override
	public List<String> getAPIKeys() {
		return this.apiKeys;
	}

	@Override
	public String getCurrentKey() {
		return this.paramAPIKey;
	}

	@Override
	public Collection<String> getUsedKeys() {
		return this.usedKeys;
	}

	@Override
	public void setKey(String apiKey) {
		this.paramAPIKey = apiKey;
		this.usedKeys.add(apiKey);
	}

	@Override
	public boolean switchToUnusedKey() {
		for (String apiKey : getAPIKeys()) {
			if (!this.usedKeys.contains(apiKey)) {
				setKey(apiKey);
				return true;
			}
		}
		System.err.println("No more unused keys available.");
		return false;
	}
}
