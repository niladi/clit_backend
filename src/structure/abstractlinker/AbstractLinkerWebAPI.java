package structure.abstractlinker;

import java.io.IOException;
import java.util.Collection;

import com.textrazor.AnalysisException;

import structure.config.kg.EnumModelType;
import structure.datatypes.AnnotatedDocument;
import structure.datatypes.Mention;
import structure.exceptions.APILimitException;
import structure.interfaces.linker.APIKey;

public abstract class AbstractLinkerWebAPI extends AbstractLinker {
	public AbstractLinkerWebAPI(EnumModelType KG) {
		super(KG);
	}

	/**
	 * Annotates a document with mentions. Note: This updates the document, it
	 * doesn't create a new one!
	 */
	@Override
	public AnnotatedDocument annotate(final AnnotatedDocument document) throws IOException {
		final Object annotatedText;
		Object tmpAnnotatedText = null;
		boolean apiLimitReached = false;
		try {
			tmpAnnotatedText = sendRequest(document.getText());
		} catch (AnalysisException ae) {
			apiLimitReached = ae.getMessage().contains(
					"You have reached your daily TextRazor request limit. Please contact support@textrazor.com for more information, or visit https://www.textrazor.com to upgrade your account.");
		} catch (IOException ioe) {
			throw ioe;
		} catch (Exception e) {
			e.printStackTrace();
			tmpAnnotatedText = null;
		}

		apiLimitReached |= tmpAnnotatedText != null && (tmpAnnotatedText.toString().contains(
				"Your key is not valid or the daily requests limit has been reached. Please visit http://babelfy.org")
				|| tmpAnnotatedText.toString().contains(
						"You have reached your daily TextRazor request limit. Please contact support@textrazor.com for more information, or visit https://www.textrazor.com to upgrade your account."));
		if (apiLimitReached) {
			if (this instanceof APIKey) {
				final APIKey apiKeyInstance = ((APIKey) this);

				if (apiKeyInstance.switchToUnusedKey()) {
					// Attempting to recover...
					return this.annotate(document);
				} else {
					// Could not get an API key to recover
					// Could not switch to an unused key... so we have reached the end of the rope
					throw new APILimitException(tmpAnnotatedText.toString());
				}
			} else {
				// Should theoretically never happen since this would mean... we are getting an
				// API-related
				// error message, but we are apparently not implementing APIKey
				annotatedText = null;
				throw new APILimitException(tmpAnnotatedText.toString());
			}
		} else {
			annotatedText = tmpAnnotatedText;
		}

		// Parse text (e.g. JSON, CSV, ...) to collection of mentions
		final Collection<Mention> mentions = dataToMentions(annotatedText);
		document.setMentions(mentions);
		return document;
		// TODO or create an AnnotatedDocument here?
		// return new AnnotatedDocument(input.getText(), mentions);
	}

	/**
	 * Transform annotated text to a collection of mentions with their correct
	 * assignment
	 * 
	 * @param annotatedText annotated text
	 * @return collection of mentions for further processing
	 */
	public abstract Collection<Mention> dataToMentions(final Object annotatedText);

	protected abstract Object sendRequest(String text) throws IOException, Exception;

}
