package linking.candidategeneration;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.httpclient.HttpException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import structure.config.kg.EnumModelType;
import structure.datatypes.AnnotatedDocument;
import structure.datatypes.Mention;
import structure.datatypes.PossibleAssignment;
import structure.utils.Loggable;

public class WikidataDictCandidateGenerator extends AbstractCandidateGenerator implements Loggable {

	// TODO Move this to init()
	protected final String url = "http://132.230.150.53:9001";// "http://localhost:5001/";

	public WikidataDictCandidateGenerator(EnumModelType KG) {
		// super(EnumModelType.WIKIDATA);
		init();
	}

	public boolean init() {
		// TODO Use this like in the linkers (instead of defining the URL above as
		// field)
		// http();
		// url("localhost");
		// port(5001);
		// suffix("/");
		return true;
	}

	@SuppressWarnings("unchecked")
	public List<PossibleAssignment> generate(Mention mention) throws IOException {
		List<PossibleAssignment> candidates = new ArrayList<>();

		JSONObject label = new JSONObject();
		label.put("label", mention.getOriginalMention());

		// TODO Refactor this like in structure.linker.AbstractLinkerURL
		URL url = new URL(this.url);

		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
		connection.setRequestProperty("accept", "application/json");
		connection.setDoInput(true);
		connection.setDoOutput(true);

		// try-block for auto-closing of input stream and output stream
		try (OutputStreamWriter wr = new OutputStreamWriter(connection.getOutputStream())) {
			wr.write(label.toString());
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
				Object candidatesObj = jsonObject.get("candidates");
				candidates = readLabels(candidatesObj);
				System.out.println(candidates.toString());

				return candidates;
			}
		}
	}

	private List<PossibleAssignment> readLabels(Object candidatesObj) {
		List<PossibleAssignment> candidates = new ArrayList<>();
		JSONArray candidatesJson = (JSONArray) candidatesObj;

		if (candidatesJson.size() == 0)
			return candidates;

		for (int i = 0; i < candidatesJson.size(); i++) {
			String labelString = (String) candidatesJson.get(i);
			PossibleAssignment possibleAssignment = new PossibleAssignment(labelString);
			candidates.add(possibleAssignment);
		}
		return candidates;
	}

	@Override
	public AnnotatedDocument generate(AnnotatedDocument document) throws IOException {
		for (Mention m : document.getMentions()) {
			m.updatePossibleAssignments(generate(m));
		}
		return document;
	}

}
