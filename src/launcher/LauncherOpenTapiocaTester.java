package launcher;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;

public class LauncherOpenTapiocaTester {
	public static void main(String[] args) throws ClientProtocolException, IOException {
		final CloseableHttpClient httpclient = HttpClients.createDefault();
		final HttpPost httppost = new HttpPost("https://opentapioca.wordlift.io/api/annotate");

		// Request parameters and other properties.
		final List<NameValuePair> params = new ArrayList<NameValuePair>(2);
		params.add(new BasicNameValuePair("query", "Whats up in Germany nowadays?"));
		httppost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
		// Execute and get the response.
		final HttpResponse response = httpclient.execute(httppost);
		final HttpEntity entity = response.getEntity();

		if (entity != null) {
			try (InputStream instream = entity.getContent()) {
				// do something useful
				try (BufferedReader brIn = new BufferedReader(new InputStreamReader(instream))) {
					String line = null;
					while ((line = brIn.readLine()) != null)
					{
						System.out.println(line);
					}

				}
			}
		}
	}
}
