package clit.recommender;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.commons.text.similarity.LevenshteinDistance;
import org.json.simple.JSONObject;

import com.google.common.collect.Lists;

import experiment.ExperimentSettings;
import linking.linkers.RadboudLinker;
import structure.config.constants.Strings;
import structure.datatypes.AnnotatedDocument;
import structure.datatypes.Mention;
import structure.interfaces.pipeline.PipelineComponent;
import structure.utils.LinkerUtils;
import structure.utils.Loggable;

/**
 * curl http://127.0.0.1:5002/ --header "Content-Type: application/json"
 * --request POST -d '{"document" : {"text": "", "mentions": []},
 * "pipelineConfig" : "<config>", "componentId" : "123"}
 * 
 * @author wf7467
 *
 */
public class LinkerRecommender implements Loggable {
	private final String url;

	public LinkerRecommender() {
		this("http://host.docker.internal:5002");
	}

	public LinkerRecommender(final String url) {
		this.url = url;
	}

	public Class<? extends PipelineComponent> recommend(final String text) {
		try {
			// Transform input text to Document/JSON object to transmit to server for
			// document processing
			final AnnotatedDocument inDocument = new AnnotatedDocument(text);
			final JSONObject pipelineConfig = new JSONObject();
			final String componentId = "";
			final String json = LinkerUtils.documentToAPIJSON(inDocument, pipelineConfig, componentId);

			// API/Web-related stuff
			final URL url = new URL(this.url);
			final URLConnection conn = url.openConnection();
			final HttpURLConnection http = (HttpURLConnection) conn;
			http.setRequestMethod("POST"); // PUT is another valid option
			http.setDoOutput(true);
			conn.setRequestProperty("Content-Type", "application/json");
			conn.setRequestProperty("charset", "utf-8");
			conn.setRequestProperty("accept", "application/json");

			final String postDataStr = json;
			final byte[] postData = postDataStr.getBytes(StandardCharsets.UTF_8);
			final int postDataLength = postData.length;
			conn.setRequestProperty("Content-Length", String.valueOf(postDataLength));

			// Outputs the data
			try (DataOutputStream wr = new DataOutputStream(conn.getOutputStream())) {
				wr.write(postData);
			}

			// What does the server say?
			final StringBuilder ret = new StringBuilder();
			try (final InputStreamReader is = new InputStreamReader(conn.getInputStream());
					final BufferedReader br = new BufferedReader(is)) {
				String line = null;
				while ((line = br.readLine()) != null) {
					ret.append(line);
					ret.append(Strings.NEWLINE.val);
					// ret.append("\n");
				}
			}

			// System.out.println("Response: " + ret.toString());
			// Parse JSON response...
			final AnnotatedDocument document = LinkerUtils.apiJSONToDocument(ret.toString());
			if (document == null)
				return null;
			final String toFind;
			if (document.getMentions() != null) {
				final Collection<Mention> mentions = document.getMentions();
				// there should only be one...
				String foundMention = null;
				for (Mention m : mentions) {
					if (m.getOffset() == 0) {
						foundMention = m.getMention();
						break;
					}
				}
				toFind = foundMention;
			} else {
				toFind = null;
			}

			// Check which linker is meant by the response...
			// find the appropriate one and return it
			if (toFind == null)
				return null;

			final LevenshteinDistance distance = new LevenshteinDistance();
			final Map<String, Class<? extends PipelineComponent>> m = ExperimentSettings
					.getLinkerClassesCaseInsensitive();
			final List<Class<? extends PipelineComponent>> linkerNames = Lists.newArrayList(m.values());
			int minDist = Integer.MAX_VALUE;
			Class<? extends PipelineComponent> minLinker = null;
			for (int i = 0; i < linkerNames.size(); ++i) {
				Class<? extends PipelineComponent> linkerClass = linkerNames.get(i);
				final int dist = distance.apply(toFind, linkerClass.getName());
				if (dist < minDist) {
					minLinker = linkerClass;
					minDist = dist;
				}
			}

			if (minLinker != null) {
				return minLinker;
			}

		} catch (IOException e) {
			e.printStackTrace();
			getLogger().error(e.getLocalizedMessage());
		}

		return RadboudLinker.class;
	}
}
