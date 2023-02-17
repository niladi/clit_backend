package clit;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.function.BiFunction;

import org.json.simple.JSONObject;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;

import experiment.PipelineItem;
import structure.abstractlinker.AbstractLinkerURL;
import structure.abstractlinker.AbstractLinkerURLPOST;
import structure.config.kg.EnumModelType;
import structure.datatypes.AnnotatedDocument;
import structure.datatypes.Mention;
import structure.interfaces.clit.Combiner;
import structure.interfaces.clit.Filter;
import structure.interfaces.clit.Splitter;
import structure.interfaces.clit.Translator;
import structure.interfaces.pipeline.CandidateGenerator;
import structure.interfaces.pipeline.Disambiguator;
import structure.interfaces.pipeline.MentionDetector;
import structure.utils.LinkerUtils;

public class APIComponentCommunicator extends AbstractLinkerURLPOST
		implements Combiner, Translator, Filter, Splitter, MentionDetector, CandidateGenerator, Disambiguator {

	final URL urlObj;
	private final String keywordContent = "content";

	/**
	 * Required for passing the whole pipeline to external components.
	 */
	private final JSONObject pipelineConfig;

	/**
	 * ID of the current component. This is used by external components to identify
	 * what is their task on receiving a request.
	 */
	private final String componentId;

	public APIComponentCommunicator(final EnumModelType KG, final String componentId, final String urlStr,
			final JSONObject pipelineConfig) throws MalformedURLException {
		super(KG);
		this.urlObj = new URL(urlStr);
		this.pipelineConfig = pipelineConfig;
		this.componentId = componentId;
		init();
	}

	@Override
	public boolean init() {
		// TODO This is kind of reverse engineered, following the interfaces. Find a
		// better way...
		// https();
		url(urlObj.getHost());
		port(urlObj.getPort());
		if (urlObj.getQuery() == null) {
			suffix(urlObj.getPath());
		} else {
			suffix(urlObj.getPath() + urlObj.getQuery());
		}
		return true;
	}

	@Override
	public Collection<AnnotatedDocument> execute(final PipelineItem callItem, final AnnotatedDocument document) {
		System.out.println("API Execute!");
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AnnotatedDocument disambiguate(final AnnotatedDocument document) throws InterruptedException, IOException {
		// TODO Auto-generated method stub
		return annotate(document);
	}

//	@Override
//	public AnnotatedDocument generate(final AnnotatedDocument document) {
//		// TODO Auto-generated method stub
//		System.out.println("API Generate (candidates#2)!");
//		return null;
//	}

	@Override
	public AnnotatedDocument detect(final AnnotatedDocument document) throws Exception {
		return detect(document, null);
	}

	@Override
	// TODO [Task] Would it be better to let a component process all documents
	// directly? We'd have only one request
	// then. But currently the control flow is within the Experimenter who executes
	// a component for one document
	// after another.
	public AnnotatedDocument detect(final AnnotatedDocument input, final String source) throws Exception {
		return annotate(input);
	}

	@Override
	public AnnotatedDocument translate(AnnotatedDocument input) {
		System.out.println("API Call - Translate document");
		return null;
	}

	@Override
	public AnnotatedDocument filter(AnnotatedDocument document) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AnnotatedDocument generate(AnnotatedDocument document) throws IOException {
		AnnotatedDocument doc = annotate(document);
		return doc;
	}

	@Override
	public Collection<AnnotatedDocument> split(final AnnotatedDocument document) {
		return split(document, 2);
	}

	@Override
	public Collection<AnnotatedDocument> split(final AnnotatedDocument documentToSplit, final int copies) {
		// TODO Auto-generated method stub
		return split(documentToSplit, copies, null);
	}

	@Override
	public Collection<AnnotatedDocument> split(final AnnotatedDocument documentToSplit, final int copies,
			final String[] params) {
		System.out.println("API Split called! [copies: " + copies + ", params:"
				+ (params == null ? "null" : Arrays.toString(params)) + "]");
		String request;
		ArrayList<AnnotatedDocument> splitDocuments = new ArrayList<AnnotatedDocument>();
		try {
			request = LinkerUtils.documentToAPIJSON(documentToSplit, pipelineConfig, componentId);

			// send request
			final String response = sendRequest(request);

			// TODO: transform response into Collection<AnnotatedDocument>
			// e.g. split the string "somehow" into multiple documents
			// For each document, call: LinkerUtils.apiJSONToDocument

			// Aggregate these into a collection (e.g. arraylist)

			// TODO: Return Collection<AnnotatedDocument>
		    final org.json.JSONObject responseAsJson = new org.json.JSONObject(response);
		    org.json.JSONArray documents = responseAsJson.getJSONArray("documents");
		    for (int i = 0; i < documents.length(); i++) {
				splitDocuments.add(LinkerUtils.apiJSONToDocument(documents.getJSONObject(i).toString()));
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
		return splitDocuments;
	}

	@Override
	public String translate(String entity) {
		// TODO Auto-generated method stub
		System.out.println("API Translate!");
		return null;
	}

	@Override
	public AnnotatedDocument combine(Collection<AnnotatedDocument> multiItems) {
		// TODO Auto-generated method stub
		System.out.println("API Combine!");
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

	@Override
	public String getText() {
		return this.params.get(this.keywordContent);
	}

	@Override
	public AbstractLinkerURL setText(final String inputText) {
		this.params.put(this.keywordContent, inputText);
		return this;
	}

	@Override
	public int hashCode() {
		int ret = 0;
		ret += nullHash(this.keywordContent, 2);
		ret += nullHash(getClass(), 4);
		ret += nullHash(getKG(), 8);
		ret += nullHash(getScoreModulationFunction(), 16);
		ret += nullHash(getUrl(), 32);
		ret += nullHash(getTimeout(), 64);
		ret += nullHash(this.params, 128);
		ret += nullHash(this.getUrl(), 256);
		return ret + super.hashCode();
	}

	@Override
	protected HttpURLConnection openConnection(String input) throws URISyntaxException, IOException {
		// TODO currently, input is only a String. We want to send a whole
		// ExperimentTask (?) here.
		// Change type of input to ExperimentTask? This would require to change the
		// interfaces.
		// Internal components receive only the input text; API components the whole
		// ExperimentTask?
		setParam(keywordContent, input);
		final HttpURLConnection conn = openConnectionWParams();
		return conn;
	}

	@Override
	protected void setupConnectionDetails(HttpURLConnection conn) throws ProtocolException {
		conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
		conn.setRequestProperty("Accept", "application/json");
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

	/**
	 * Build the request JSON from - The document to annotate - The configuration
	 * JSON of the whole pipeline - The ID of current component to which the request
	 * is sent
	 */
	@Override
	public AnnotatedDocument annotate(final AnnotatedDocument document) throws IOException {
		// create task package that contains all information required by external
		// components
		final String request = LinkerUtils.documentToAPIJSON(document, pipelineConfig, componentId);

		// send request
		final String response = sendRequest(request);

		// extract document from response
		final AnnotatedDocument result = responseToDocument(response);
		return result;
	}

	/**
	 * Extract the document from the response JSON string.
	 */
	public AnnotatedDocument responseToDocument(String response) throws JsonMappingException, JsonProcessingException {
		return LinkerUtils.apiJSONToDocument(response);
	}

	@Override
	public Collection<Mention> dataToMentions(Object annotatedText) {
		return null;
	}

	@Override
	protected String injectParams() {
		final String jsonContent = injectParam(keywordContent);
		if (jsonContent != null) {
			return jsonContent;
		}

		getLogger().error("No parameter passed to POST request...");
		return null;
	}
}
