package linking.linkers;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.function.BiFunction;

import experiment.PipelineItem;
import structure.abstractlinker.AbstractLinkerURL;
import structure.abstractlinker.AbstractLinkerURLPOST;
import structure.config.kg.EnumModelType;
import structure.datatypes.AnnotatedDocument;
import structure.datatypes.Mention;
import structure.interfaces.linker.LinkerNIF;
import structure.utils.FunctionUtils;
import structure.utils.LinkerUtils;
import structure.utils.MentionUtils;

public class FOXLinker extends AbstractLinkerURLPOST implements LinkerNIF {
	public Number defaultScore = 0.5d;// 1.0d// getWeight()
	;

	public FOXLinker() {
		super(EnumModelType.DBPEDIA_FULL);
		init();
	}

	// TODO Workaround for using reflection in IndexController.java
	public FOXLinker(EnumModelType KG) {
		this();
	}

	private final String keywordContent = "content";
	private final String keywordTask = "task";
	private final String keywordLang = "lang";

	private final String paramTask = "ner";
	private final String paramLang = "en";

	@Override
	public boolean init() {
		https();
		url("fox.demos.dice-research.org");
//		suffix("/fox?task=ner&lang=en");
		suffix("/fox");
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
	public HttpURLConnection openConnection(final String input) throws URISyntaxException, IOException {
		final String nifInput = createNIF(input);
		setParam(keywordContent, nifInput);
		setParam(keywordTask, paramTask);
		setParam(keywordLang, paramLang);
		final HttpURLConnection conn = openConnectionWParams();
		return conn;

//		final String urlParameters  = "task=ner&lang=en";
//		final byte[] postData       = urlParameters.getBytes( StandardCharsets.UTF_8 );
//		final int    postDataLength = postData.length;
//		final String request        = "fox.demos.dice-research.org/fox";
//		final URL    url            = new URL( request );
//		final HttpURLConnection conn = (HttpURLConnection) url.openConnection();           
//		conn.setDoOutput(true);
//		conn.setInstanceFollowRedirects(false);
//		conn.setRequestMethod("POST");
//        conn.setRequestProperty("Content-Type", "application/x-turtle;charset=UTF-8");
//		conn.setRequestProperty("Accept", "application/x-turtle");
//		conn.setRequestProperty("Accept-Charset", "UTF-8");
//		conn.setRequestProperty("Content-Length", Integer.toString(postDataLength));
//		conn.setUseCaches(false);
//		try( DataOutputStream wr = new DataOutputStream(conn.getOutputStream())) {
//		   wr.write(postData);
//		}

	}

	@Override
	public Collection<Mention> dataToMentions(Object annotatedText) {
		// Transform nif to another format
		return LinkerUtils.nifToMentions(annotatedText.toString(), defaultScore, this::translate);
	}

	@Override
	protected String injectParams() {
		final String nifContent = injectParam(keywordContent);
		if (nifContent != null) {
			return nifContent;
		}

		getLogger().error("No parameter passed to POST request...");
		return null;
	}

	@Override
	protected void setupConnectionDetails(HttpURLConnection conn) throws ProtocolException {
		conn.setRequestProperty("Content-Type", "application/x-turtle;charset=UTF-8");
		conn.setRequestProperty("Accept", "application/x-turtle");
		conn.setRequestProperty("Accept-Charset", "UTF-8");
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
	public String getText() {
		return this.params.get(this.keywordContent);
	}

	@Override
	public AbstractLinkerURL setText(final String inputText) {
		this.params.put(this.keywordContent, inputText);
		return this;
	}
}
