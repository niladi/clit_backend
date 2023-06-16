package linking.linkers;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import com.google.common.collect.Lists;

import structure.abstractlinker.AbstractLinker;
import structure.config.kg.EnumModelType;
import structure.datatypes.AnnotatedDocument;
import structure.datatypes.Mention;
import structure.datatypes.PossibleAssignment;

/**
 * https://github.com/informagi/REL
 * 
 * @author wf7467
 *
 */
public class OpenTapiocaLinkerWordlift extends AbstractLinker {

	private final String urlString = "https://opentapioca.wordlift.io/api/annotate";

	public OpenTapiocaLinkerWordlift(final EnumModelType kg) {
		super(kg);
	}

	@Override
	public boolean init() throws Exception {
		return true;
	}

	@Override
	public AnnotatedDocument annotate(AnnotatedDocument document) throws Exception {
		final CloseableHttpClient httpclient = HttpClients.createDefault();
		final HttpPost httppost = new HttpPost(urlString);

		// Request parameters and other properties.
		final List<NameValuePair> params = new ArrayList<NameValuePair>(1);
		params.add(new BasicNameValuePair("query", document.getText()));
		httppost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
		// Execute and get the response.
		final HttpResponse response = httpclient.execute(httppost);
		final HttpEntity entity = response.getEntity();

		if (entity != null) {
			try (InputStream instream = entity.getContent()) {
				// do something useful
				final StringBuilder sb = new StringBuilder();
				try (BufferedReader brIn = new BufferedReader(new InputStreamReader(instream))) {
					String line = null;
					while ((line = brIn.readLine()) != null) {
						sb.append(line);
					}
				}
				final JSONObject retJson = new JSONObject(sb.toString());
				System.out.println("------------------------------------------");
				final JSONArray annotations = retJson.optJSONArray("annotations");
				System.out.println("-------------" + annotations);
				List<Mention> mentionsList = new ArrayList<>(annotations.length());
				for (int i = 0; i < annotations.length(); i++) {
				    JSONObject annotation = annotations.getJSONObject(i);
				    //String bestQid = annotation.optString("best_qid");
				    double logLikelihood = annotation.optDouble("log_likelihood");
				    int start = annotation.optInt("start");
				    //int end = annotation.optInt("end");
				    String bestTagLabel = annotation.optString("best_tag_label");
				    if(bestTagLabel.equals((""))) continue;

				    final Mention mention = new Mention(bestTagLabel,
							Lists.newArrayList(new PossibleAssignment(null, logLikelihood)), start,
							0.5f, bestTagLabel, bestTagLabel);
				    mentionsList.add(mention);
				}
				document.setMentions(mentionsList);
			}
		}

		return document;
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

}
