package linking.candidategeneration;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import com.google.common.collect.Lists;

import structure.config.kg.EnumModelType;
import structure.datatypes.AnnotatedDocument;
import structure.datatypes.Mention;
import structure.datatypes.PossibleAssignment;

public class Falcon2CandidateGenerator extends AbstractCandidateGenerator {
	/**
	 * https://labs.tib.eu/falcon/falcon2/api-use
	 */
	private final String searchURL = "https://labs.tib.eu/falcon/falcon2/api?mode=long&k=";
	private final EnumModelType KG;
	private final int topKCount = 50;
	private final String keycontent = "text";

	public Falcon2CandidateGenerator(final EnumModelType KG) {
		this.KG = KG;
	}

	public List<PossibleAssignment> generate(Mention mention) throws IOException {
		final List<PossibleAssignment> candidates;
		final String urlPOST = searchURL + String.valueOf(topKCount);
		URL url;
		try {
			url = new URI(urlPOST).toURL();
			final HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setDoOutput(true);
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Content-Type", "application/json");
			final OutputStream os = conn.getOutputStream();
			final OutputStreamWriter osw = new OutputStreamWriter(os, "UTF-8");
			final JSONObject jsonRequest = new JSONObject();
			// Generate candidates mention by mention
			jsonRequest.accumulate(this.keycontent, mention.getMention());
			osw.write(jsonRequest.toString());
			osw.flush();
			osw.close();
			os.close(); // don't forget to close the OutputStream

			conn.connect();

			try (final BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
				String line = null;
				StringBuilder sbCandidates = new StringBuilder();
				while ((line = br.readLine()) != null) {
					sbCandidates.append(line);
				}
				candidates = extractCandidate(new JSONObject(sbCandidates.toString()));
			}
			return candidates;
		} catch (MalformedURLException | URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return Lists.newArrayList();
	}

	/**
	 * Extracts candidates from
	 * 
	 * @param jsonObject
	 * @return
	 */
	private List<PossibleAssignment> extractCandidate(final JSONObject jsonObject) {
		final List<PossibleAssignment> retList = Lists.newArrayList();
		if (jsonObject == null) {
			return null;
		}

		// Key: "entities_wikidata" -> JSONArray
		// Subkeys: ["surface form", "URI"]

		final JSONArray jsonArrDocs = jsonObject.optJSONArray("entities_wikidata");
		// surface form -> mention
		// URI -> WD entity
		if (jsonArrDocs == null) {
			return null;
		}

		for (int i = 0; i < jsonArrDocs.length(); ++i) {
			final JSONObject jsonObj = jsonArrDocs.optJSONObject(i);
			final String mention = jsonObj.optString("surface form");
			final String entity = jsonObj.optString("URI");
			// System.out.println(mention + " -> " + entity);
			if (jsonObj == null || mention == null || mention.length() == 0 || entity == null || entity.length() == 0) {
				// Something wrong, so go to next one...
				continue;
			}

			retList.add(new PossibleAssignment(entity));
		}

		return retList;
	}

	@Override
	public AnnotatedDocument generate(AnnotatedDocument document) throws IOException {
		for (final Mention m : document.getMentions()) {
			final String originalMention;
			if (m == null || (originalMention = m.getOriginalMention()) == null || originalMention.length() < 1) {
				// cannot look up candidates... so continue to next mention
				continue;
			}

			final List<PossibleAssignment> possibleAssignments = generate(m);
			if (possibleAssignments == null) {
				continue;
			}
			m.updatePossibleAssignments(possibleAssignments);
		}
		return document;
	}

}
