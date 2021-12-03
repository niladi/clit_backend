package linking.linkers;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.google.common.collect.Lists;

import clit.translator.TranslatorWikipediaToDBpediaFast;
import structure.abstractlinker.AbstractLinkerURL;
import structure.abstractlinker.AbstractLinkerURLPOST;
import structure.config.kg.EnumModelType;
import structure.datatypes.Mention;
import structure.datatypes.PossibleAssignment;
import structure.interfaces.clit.Translator;

/**
 * https://sobigdata.d4science.org/group/tagme/tagme-help
 * 
 * @author wf7467
 *
 */
public class TagMeLinker extends AbstractLinkerURLPOST {

	private final String apiParamKey = "gcube-token";
	private final String apiKey = "5c3320e5-78fc-4373-942a-fa3a2bd3561a-843339462";
	private final String keyText = "text";
	private final String apiParamLang = "lang";

	public TagMeLinker(EnumModelType KG) {
		super(KG);
		try {
			init();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public boolean init() throws Exception {
		https();
		url("tagme.d4science.org");
		suffix("/tagme/tag");
		setParam(apiParamKey, this.apiKey);
		setParam(apiParamLang, "en");
		return true;
	}

	@Override
	public AbstractLinkerURL setText(String inputText) {
		setParam(this.keyText, inputText);
		return this;
	}

	@Override
	public String getText() {
		return this.injectParam(this.keyText);
	}

	@Override
	protected HttpURLConnection openConnection(String input) throws URISyntaxException, IOException {
		setParam(this.keyText, input);
		setParam(apiParamKey, this.apiKey);
		setParam(apiParamLang, "en");

		final HttpURLConnection conn = openConnectionWParams();
		return conn;
	}

	@Override
	protected void setupConnectionDetails(HttpURLConnection conn) throws ProtocolException {
		// Add connection-type-specific stuff, for POST add the contents
		// For GET w/e may be needed
		// conn.setRequestProperty("accept", "application/x-turtle");
		// conn.setRequestProperty("Content-Type", "application/x-turtle");
		// conn.setRequestProperty("Accept-Encoding", "gzip");

		try {
			final String postDataStr = injectParams();
			final byte[] postData = postDataStr.getBytes(StandardCharsets.UTF_8);
			final int postDataLength = postData.length;
			conn.setRequestProperty("Content-Length", String.valueOf(postDataLength));

			// Outputs the data
			try (DataOutputStream wr = new DataOutputStream(conn.getOutputStream())) {
				wr.write(postData);
			}
		} catch (IOException e) {
			getLogger().error(e.getLocalizedMessage());
		}
	}

	@Override
	public Collection<Mention> dataToMentions(Object annotatedText) {
		//System.out.println(annotatedText);
		final List<Mention> mentions = Lists.newArrayList();
		// {"test":"5","annotations":[{"spot":"Napoleon","start":0,"link_probability":0.47039562463760376,"rho":0.5553216934204102,"end":8,"id":69880,"title":"Napoleon"},{"spot":"emperor","start":17,"link_probability":0.07003042101860046,"rho":0.3107353448867798,"end":24,"id":70716,"title":"Charles
		// V, Holy Roman Emperor"},{"spot":"First French
		// Empire","start":32,"link_probability":1,"rho":0.8260335922241211,"end":51,"id":21418258,"title":"First
		// French
		// Empire"}],"time":9,"api":"tag","lang":"en","timestamp":"2021-11-25T09:51:00"}
		try {
			final JSONObject json = new JSONObject(annotatedText);
			System.out.println("Keys: " + json.keySet());
			final JSONArray annotations = json.getJSONArray("annotations");
			final Translator translator = new TranslatorWikipediaToDBpediaFast();
			for (int i = 0; i < annotations.length(); ++i) {
				final JSONObject annotation = annotations.getJSONObject(i);
				final String mention = annotation.getString("spot");
				final int offset = annotation.getInt("start");
				final double confidence = annotation.getDouble("link_probability");
				final String wikipediaTitleAssignment = annotation.getString("title");
				final String assignment;
				if (wikipediaTitleAssignment != null) {
					final String wikiLink = "https://en.wikipedia.org/wiki/" + wikipediaTitleAssignment.replace(" ", "_");
					final String dbpediaLink = translator.translate(wikiLink);
					if (dbpediaLink != null) {
						assignment = dbpediaLink;
					} else {
						assignment = wikiLink;
					}
				} else {
					assignment = null;
				}
				// https://en.wikipedia.org/wiki/
				mentions.add(new Mention(mention, new PossibleAssignment(assignment, confidence), offset, 1.0f, mention,
						mention));
			}
		} catch (JSONException e) {
			System.err.println("org.json failed to parse - trying org.json.simple");
			final JSONParser parser = new JSONParser();
			try {
				final org.json.simple.JSONObject json = (org.json.simple.JSONObject) parser
						.parse(annotatedText.toString());
				final org.json.simple.JSONArray annotations = (org.json.simple.JSONArray) json.get("annotations");
				final Translator translator = new TranslatorWikipediaToDBpediaFast();
				for (int i = 0; i < annotations.size(); ++i) {
					final org.json.simple.JSONObject annotation = (org.json.simple.JSONObject) annotations.get(i);
					final String mention = (String) annotation.get("spot");
					final Number offset = (Number) annotation.get("start");
					final Number confidence = (Number) annotation.get("link_probability");
					final String wikipediaTitleAssignment = (String) annotation.get("title");
					final String assignment;
					if (wikipediaTitleAssignment != null) {
						final String wikiLink = "https://en.wikipedia.org/wiki/" + wikipediaTitleAssignment.replace(" ", "_");
						final String dbpediaLink = translator.translate(wikiLink);
						if (dbpediaLink != null) {
							assignment = dbpediaLink;
						} else {
							assignment = wikiLink;
						}
					} else {
						assignment = null;
					}
					// https://en.wikipedia.org/wiki/
					mentions.add(new Mention(mention, new PossibleAssignment(assignment, confidence.doubleValue()), offset.intValue(), 1.0f,
							mention, mention));
				}
			} catch (ParseException e1) {
				e1.printStackTrace();
				System.out.println("org.json.simple Failed to parse.");
			}

		}

		return mentions;
	}
}
