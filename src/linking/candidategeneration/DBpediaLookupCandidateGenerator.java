package linking.candidategeneration;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import com.google.common.collect.Lists;

import structure.config.kg.EnumModelType;
import structure.datatypes.AnnotatedDocument;
import structure.datatypes.Mention;
import structure.datatypes.PossibleAssignment;

public class DBpediaLookupCandidateGenerator extends AbstractCandidateGenerator {

	private final String searchURL = "https://lookup.dbpedia.org/api/search?format=JSON&query=";
	private final EnumModelType KG;

	public DBpediaLookupCandidateGenerator(final EnumModelType KG) {
		this.KG = KG;
	}

	public List<PossibleAssignment> generate(Mention mention) throws IOException {
		final List<PossibleAssignment> candidates;
		final String urlGET = searchURL + mention.getOriginalMention();
		final URL url = new URL(urlGET);
		final HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setDoOutput(true);
		conn.setRequestMethod("GET");
		conn.setRequestProperty("Content-Type", "application/json");
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
		// Key: "docs" -> JSONArray
		// Subkeys: [score, refCount, resource, redirectlabel, typeName, comment, label,
		// type, category]

		final JSONArray jsonArrDocs = jsonObject.optJSONArray("docs");
		if (jsonArrDocs == null) {
			return null;
		}

		for (int i = 0; i < jsonArrDocs.length(); ++i) {
			final JSONObject jsonObj = jsonArrDocs.optJSONObject(i);
			final JSONArray jsonArrResource, jsonArrScore;
			final String entity;
			final Number score;

			if (jsonObj == null || (jsonArrResource = jsonObj.optJSONArray("resource")) == null
					|| jsonArrResource.length() == 0 || (entity = jsonArrResource.optString(0)) == null
					|| (jsonArrScore = jsonObj.optJSONArray("score")) == null || jsonArrScore.length() == 0
					|| (score = jsonArrScore.optDouble(0)) == null) {
				// Something wrong, so go to next one...
				continue;
			}

			retList.add(new PossibleAssignment(entity, score));
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
