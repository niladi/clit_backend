package linking.linkers;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;
import java.util.function.BiFunction;

import org.json.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.google.common.collect.Lists;

import structure.abstractlinker.AbstractLinkerURL;
import structure.abstractlinker.AbstractLinkerURLPOST;
import structure.config.kg.EnumModelType;
import structure.datatypes.Mention;
import structure.datatypes.PossibleAssignment;
import structure.utils.FunctionUtils;
import structure.utils.TextUtils;

/**
 * https://github.com/SDM-TIB/falcon2.0
 * 
 * curl --header "Content-Type: application/json" \ --request POST \ --data
 * '{"text":"Who painted The Storm on the Sea of Galilee?"}' \
 * https://labs.tib.eu/falcon/falcon2/api?mode=long
 * 
 * @author wf7467
 *
 */
public class Falcon2Linker extends AbstractLinkerURLPOST {

	private final String keycontent = "text";

	public Falcon2Linker(final EnumModelType kg) {
		super(kg);
		init();
	}

	public Falcon2Linker() {
		super(EnumModelType.DEFAULT);
		init();
	}

	@Override
	public boolean init() {
		// https://labs.tib.eu/falcon/falcon2/api?mode=long
		https();
		url("labs.tib.eu");
		// port(8080);
		suffix("/falcon/falcon2/api?mode=long");
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
	public AbstractLinkerURL setText(final String inputText) {
		this.params.put(this.keycontent, inputText);
		return this;
	}

	@Override
	public String getText() {
		return this.params.get(this.keycontent);
	}

	@Override
	protected HttpURLConnection openConnection(final String input) throws URISyntaxException, IOException {
		// setParam(paramContent, input);
		// setParam(confidenceKeyword, confidence);
		setParam(this.keycontent, input);
		setParam("mode", "long");
		// setParam(HttpHeaders.ACCEPT_ENCODING, Consts.UTF_8.name());

		final HttpURLConnection conn = openConnectionWParams();
		return conn;
	}

	@Override
	public String translate(String entity) {
		return super.translate(entity);
	}

	@Override
	public HttpURLConnection openConnectionWParams() throws URISyntaxException, IOException {
		// For GET - preprocess
		final URL url = makeURL("");
		final HttpURLConnection conn = openConnection(url);
		// For POST - postprocess
		return conn;
	}

	public URL makeURL(final String query) throws URISyntaxException, MalformedURLException {
		if (getPort() != invalidPort) {
			return new URL(getScheme(), getUrl(), getPort(), getSuffix() + query);
		} else {
			return new URL(getScheme(), getUrl(), getSuffix() + query);
		}
	}

	@Override
	protected void setupConnectionDetails(final HttpURLConnection conn) throws ProtocolException {
		// conn.setRequestProperty("accept", "application/json");
		conn.setRequestProperty("Content-Type", "application/json");
		// conn.setRequestProperty("charset", "utf-8");
		try {
			final String postDataStr = injectParams();
			final byte[] postData = postDataStr.getBytes(StandardCharsets.UTF_8);
			try (DataOutputStream wr = new DataOutputStream(conn.getOutputStream())) {
				wr.write(postData);
			}
		} catch (IOException e) {
			getLogger().error(e.getLocalizedMessage());
		}
	}

	@Override
	protected String injectParams() {
		final JSONObject json = new JSONObject();
		final String content = injectParam(this.keycontent);
		if (content != null) {
			json.put("text", content);
		}
		return json.toString();
	}

	@Override
	public Collection<Mention> dataToMentions(Object annotatedText) {
		final List<Mention> mentions = Lists.newArrayList();
		if (annotatedText == null || annotatedText.toString() == null || annotatedText.toString().length() <= 2) {
			return mentions;
		}

		// {"entities_wikidata":[["Napoleon","<http://www.wikidata.org/entity/Q986312>"],["French
		// Empire","<http://www.wikidata.org/entity/Q71092>"]],"relations_wikidata":[["emperor","<http://www.wikidata.org/entity/P35>"]]}
		final JSONParser parser = new JSONParser();
		final String text = getText();
		try {
			final org.json.simple.JSONObject json = (org.json.simple.JSONObject) parser.parse(annotatedText.toString());
			final org.json.simple.JSONArray annotations = (org.json.simple.JSONArray) json.get("entities_wikidata");
			int prevIndex = 0;
			for (int i = 0; i < annotations.size(); ++i) {
				final org.json.simple.JSONArray annotation = (org.json.simple.JSONArray) annotations.get(i);
				final String mention = (String) annotation.get(0);
				final String wikidataAssignment = TextUtils.stripArrowSigns(((String) annotation.get(1)));

				final int offset = text.indexOf(mention, prevIndex);
				prevIndex = offset;
				// System.out.println("Mention["+mention+"]-->"+wikidataAssignment);
				mentions.add(new Mention(mention, new PossibleAssignment(wikidataAssignment), offset, 1.0f, mention,
						mention));
			}
		} catch (ParseException e1) {
			e1.printStackTrace();
			System.err.println("org.json.simple Failed to parse.");
		}
		return mentions;
	}
}
