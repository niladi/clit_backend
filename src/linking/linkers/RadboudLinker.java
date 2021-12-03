package linking.linkers;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;
import java.util.function.BiFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.text.StringEscapeUtils;
import org.apache.http.Consts;
import org.apache.http.HttpHeaders;
import org.json.JSONObject;

import com.google.common.collect.Lists;

import experiment.PipelineItem;
import structure.abstractlinker.AbstractLinkerURL;
import structure.abstractlinker.AbstractLinkerURLPOST;
import structure.config.kg.EnumModelType;
import structure.datatypes.AnnotatedDocument;
import structure.datatypes.Mention;
import structure.datatypes.PossibleAssignment;
import structure.utils.FunctionUtils;

/**
 * https://github.com/informagi/REL
 * 
 * @author wf7467
 *
 */
public class RadboudLinker extends AbstractLinkerURLPOST {

	private final String keycontent = "text";

	public RadboudLinker(final EnumModelType kg) {
		super(kg);
		init();
	}

	public RadboudLinker() {
		super(EnumModelType.DEFAULT);
		init();
	}

	@Override
	public boolean init() {
		https();
		url("rel.cs.ru.nl");
		// port(8080);
		suffix("/api");
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
	public AbstractLinkerURL setText(final String inputText) {
		this.params.put(this.keycontent, inputText);
		return this;
	}

	@Override
	public String getText() {
		return this.params.get(this.keycontent);
	}

	@Override
	protected HttpURLConnection openConnection(final String input) throws URISyntaxException, IOException {
		// setParam(paramContent, input);
		// setParam(confidenceKeyword, confidence);
		setParam(this.keycontent, input);
		setParam(HttpHeaders.ACCEPT_ENCODING, Consts.UTF_8.name());

		final HttpURLConnection conn = openConnectionWParams();
		return conn;
	}

	@Override
	public String translate(String entity) {
		// return super.translate(entity);
		if (entity == null) {
			return super.translate(entity);
		}
		return entity.replace("wikipedia.org/wiki", "dbpedia.org/resource");
	}

	@Override
	protected void setupConnectionDetails(final HttpURLConnection conn) throws ProtocolException {
		// conn.setRequestProperty("accept", "application/json");
		// conn.setRequestProperty("Content-Type", "application/json");
		// conn.setRequestProperty("charset", "utf-8");
		try {
			final String postDataStr = injectParams();
			final byte[] postData = postDataStr.getBytes(StandardCharsets.UTF_8);
			try (DataOutputStream wr = new DataOutputStream(conn.getOutputStream())) {
				wr.write(postData);
			}
		} catch (IOException e) {
			getLogger().error(e.getLocalizedMessage());
		}
	}

	@Override
	protected String injectParams() {
		final JSONObject json = new JSONObject();
		final String content = injectParam(this.keycontent);
		if (content != null) {
			json.put("text", content);
			json.put("span", "[]");
		}
		return json.toString();
	}

	@Override
	public Collection<Mention> dataToMentions(Object annotatedText) {
		final List<Mention> mentions = Lists.newArrayList();
		if (annotatedText == null || annotatedText.toString() == null || annotatedText.toString().length() < 2) {
			return mentions;
		}
		String annotationResults = annotatedText.toString().trim();
		// Ignore first and last character aka. first [ and last ] so we only get the
		// list items
		if (annotatedText == null || annotatedText.toString() == null || annotationResults.length() < 2) {
			return mentions;
		}
		annotationResults = annotationResults.substring(1, annotationResults.length() - 1);
		final int TOKEN_LENGTH = 7;
		// System.out.println("Annotation results:" + annotationResults);
		final Pattern pattern = Pattern.compile("(?<=\\[).*?(?=\\])");
		final Matcher matcher = pattern.matcher(annotationResults);
		while (matcher.find()) {
			final String match = matcher.group();
			// System.out.println("Match:" + match);
			if (match != null && match.length() > 0) {
				// Note: the space in the following split statement is important to not split
				// entities which include commas in their names...
				final String[] entries = match.split(", ");
				if (entries == null || entries.length < TOKEN_LENGTH) {
					continue;
				}
				try {
					int index = 0;
					final Integer startOffset = entries.length > index
							? Integer.valueOf(entries[index].replace("\"", "").trim())
							: null;
					index++;
					final Integer length = entries.length > index
							? Integer.valueOf(entries[index].replace("\"", "").trim())
							: null;
					index++;
					final String mentionText = entries.length > index
							? StringEscapeUtils.unescapeJava(entries[index].replace("\"", "").trim())
							: null;
					index++;
					final String entity = entries.length > index ? "http://wikipedia.org/wiki/" +
					// "https://dbpedia.org/resource/"+
							StringEscapeUtils.unescapeJava(entries[index].replace("\"", "").trim()) : null;
					index++;
					final Double confidence = entries.length > index
							? Double.valueOf(entries[index].replace("\"", "").trim())
							: null;
					index++;
					final Double otherConfidence = entries.length > index
							? Double.valueOf(entries[index].replace("\"", "").trim())
							: null;
					index++;
					final String nerClass = entries.length > index ? entries[index].replace("\"", "").trim() : null;
					final Mention mention = new Mention(mentionText,
							Lists.newArrayList(new PossibleAssignment(translate(entity), confidence)), startOffset,
							0.5f, mentionText, mentionText);
					System.out.println(
							"Assignment[" + mention.getMention() + "]:" + mention.getAssignment().getAssignment());
					mentions.add(mention);
				} catch (NumberFormatException e) {
					// Could not parse entries properly from String values
					e.printStackTrace();
					continue;
				}
			}
		}

		// System.out.println(Arrays.toString(annotationResults.split("\\[")));
		// System.out.println("Return values: " + annotatedText);
		// System.out.println(annotatedText.getClass());
		return mentions;
	}
}
