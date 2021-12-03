package api;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.aksw.gerbil.transfer.nif.Document;
import org.aksw.gerbil.transfer.nif.Marking;
import org.aksw.gerbil.transfer.nif.Span;
import org.aksw.gerbil.transfer.nif.TurtleNIFDocumentCreator;
import org.aksw.gerbil.transfer.nif.TurtleNIFDocumentParser;
import org.aksw.gerbil.transfer.nif.data.ScoredNamedEntity;
import org.apache.commons.lang3.StringUtils;

import com.beust.jcommander.internal.Lists;

import structure.config.constants.Comparators;
import structure.datatypes.AnnotatedDocument;
import structure.datatypes.Mention;
import structure.utils.Loggable;
import structure.utils.Stopwatch;
import structure.utils.TextUtils;

/**
 * Class handling annotation tasks for GERBIL
 * 
 * @author Kristian Noullet
 *
 */
public class APIDatasetGrabber implements Loggable {


	public APIDatasetGrabber() {
	}


	/**
	 * Do not change unless you have changed the call on the WEB API
	 * 
	 * @param inputStream NIFInputStream
	 * @return
	 */
	public String annotate(final InputStream inputStream) {
		if (false) {
			try (final BufferedReader brIn = new BufferedReader(new InputStreamReader(inputStream))) {
				String line = null;
				getLogger().info("Input from GERBIL - START:");
				while ((line = brIn.readLine()) != null) {
					getLogger().error(line);
				}
				getLogger().info("Input from GERBIL - END");
			} catch (IOException e) {
				getLogger().error("IOException", e);
			}
			return "";
		}

		// 1. Generate a Reader, an InputStream or a simple String that contains the NIF
		// sent by GERBIL
		// 2. Parse the NIF using a Parser (currently, we use only Turtle)
		final TurtleNIFDocumentParser parser = new TurtleNIFDocumentParser();
		final Document document;
		try {
			document = parser.getDocumentFromNIFStream(inputStream);
		} catch (Exception e) {
			getLogger().error("Exception while reading request.", e);
			return "";
		}

		return annotateDocument(document);
	}

	public String annotateNIF(final String nifInput) {
		// Parse the NIF using a Parser (currently, we use only Turtle)
		final TurtleNIFDocumentParser parser = new TurtleNIFDocumentParser();
		final Document document;
		try {
			document = parser.getDocumentFromNIFString(nifInput);
		} catch (Exception e) {
			getLogger().error("Exception while reading request.", e);
			return "";
		}
		return annotateDocument(document);
	}

	public String annotateDocument(final Document nifDocument) {
		// We got the document, so let's just save it to a list and finally output it

		final String text = nifDocument.getText();

		// 5. Generate a String containing the NIF and send it back to GERBIL
		final TurtleNIFDocumentCreator creator = new TurtleNIFDocumentCreator();
		final String retNifDocumentStr = creator.getDocumentAsNIFString(nifDocument);
		return retNifDocumentStr;
	}
}
