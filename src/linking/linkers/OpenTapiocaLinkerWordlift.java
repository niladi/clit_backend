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

import structure.abstractlinker.AbstractLinker;
import structure.config.kg.EnumModelType;
import structure.datatypes.AnnotatedDocument;
import structure.datatypes.Mention;

/**
 * https://github.com/informagi/REL
 * 
 * @author wf7467
 *
 */
public class OpenTapiocaLinkerWordlift extends AbstractLinker {

	private final String urlString = "\"https://opentapioca.wordlift.io/api/annotate\"";

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
				final JSONArray annotations = retJson.optJSONArray("annotations");
				// TODO: Finish parsing and create Mention objects which we then set to the
				// AnnotatedDocument instance
			}
		}

		return null;
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
