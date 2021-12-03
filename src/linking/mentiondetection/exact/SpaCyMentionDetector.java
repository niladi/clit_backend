package linking.mentiondetection.exact;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.httpclient.HttpException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import linking.mentiondetection.AbstractMentionDetector;
import structure.config.kg.EnumModelType;
import structure.datatypes.AnnotatedDocument;
import structure.datatypes.Mention;
import structure.utils.Loggable;

public class SpaCyMentionDetector extends AbstractMentionDetector implements Loggable {

	// TODO Move this to init()
	protected final String url = "http://aifbnike.aifb.kit.edu/clit/";// "http://localhost:5000/";

	public SpaCyMentionDetector(EnumModelType KG) {
		this();
	}

	public SpaCyMentionDetector() {
		init();
	}

	@Override
	public boolean init() {
		// TODO Use this like in the linkers (instead of defining the URL above as
		// field)
		// http();
		// url("localhost");
		// port(5001);
		// suffix("/");
		return true;
	}

	@Override
	public AnnotatedDocument detect(final AnnotatedDocument document) throws IOException {
		return detect(document, null);
	}

	@SuppressWarnings("unchecked")
	@Override
	public AnnotatedDocument detect(final AnnotatedDocument document, final String source) throws IOException {
		Collection<Mention> mentions = new ArrayList<>();

		JSONObject doc = new JSONObject();
		JSONObject text = new JSONObject();
		text.put("text", document.getText());
		doc.put("doc", text);

		// TODO Refactor this like in structure.linker.AbstractLinkerURL
		URL url = new URL(this.url);

		HttpURLConnection connection = (HttpURLConnection) url.openConnection();

		connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
		connection.setRequestProperty("accept", "application/json");
		connection.setDoInput(true);
		connection.setDoOutput(true);

		// try-block for auto-closing of input stream and output stream
		try (OutputStreamWriter wr = new OutputStreamWriter(connection.getOutputStream())) {
			wr.write(doc.toString());
			wr.flush();

			int responseCode = connection.getResponseCode();

			if (responseCode != 200) {
				String responseMessage = connection.getResponseMessage();
				throw new HttpException("Request failed: " + responseCode + " " + responseMessage);
			}

			try (InputStream responseStream = connection.getInputStream()) {
				JSONParser jsonParser = new JSONParser();
				InputStreamReader inputStreamReader = new InputStreamReader(responseStream, "UTF-8");
				JSONObject jsonObject;
				try {
					jsonObject = (JSONObject) jsonParser.parse(inputStreamReader);
				} catch (ParseException e) {
					throw new IOException(e.getMessage(), e);
				}
				Object docObj = jsonObject.get("doc");
				mentions = readMentions(docObj);
				System.out.println(mentions.toString());

				document.setMentions(mentions);
				return document;
			}
		}
	}

	private Collection<Mention> readMentions(Object docObj) {
		Collection<Mention> mentions = new ArrayList<>();

		JSONObject docJson = (JSONObject) docObj;
		JSONArray mentionsJson = (JSONArray) docJson.get("mentions");

		if (mentionsJson.size() == 0)
			return mentions;

		for (int i = 0; i < mentionsJson.size(); i++) {
			JSONObject mentionObj = (JSONObject) mentionsJson.get(i);
			String sf = (String) mentionObj.get("sf");
			int start = Math.toIntExact((long) mentionObj.get("start"));
			Mention mention = new Mention(sf, start);
			mentions.add(mention);
		}
		return mentions;
	}

}
