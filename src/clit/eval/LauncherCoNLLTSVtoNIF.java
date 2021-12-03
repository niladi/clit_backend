package clit.eval;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.aksw.gerbil.io.nif.NIFParser;
import org.aksw.gerbil.io.nif.NIFWriter;
import org.aksw.gerbil.io.nif.impl.TurtleNIFParser;
import org.aksw.gerbil.io.nif.impl.TurtleNIFWriter;
import org.aksw.gerbil.transfer.nif.Document;
import org.aksw.gerbil.transfer.nif.data.DocumentImpl;
import org.aksw.gerbil.transfer.nif.data.TypedNamedEntity;
import org.apache.commons.io.FileUtils;

import com.google.common.collect.Lists;

/**
 * Annotated CoNLL data set.
 * https://www.mpi-inf.mpg.de/departments/databases-and-information-systems/research/ambiverse-nlu/aida/downloads
 * Compiled data set:
 * https://github.com/patverga/torch-ner-nlp-from-scratch/blob/master/data/conll2003/eng.train
 * 
 * @author wf7467
 *
 */
public class LauncherCoNLLTSVtoNIF {

	public static void main(String[] args) {
//		Lines with tabs are tokens the are part of a mention:
//			- column 1 is the token
//			- column 2 is either B (beginning of a mention) or I (continuation of a mention)
//			- column 3 is the full mention used to find entity candidates
//			- column 4 is the corresponding YAGO2 entity (in YAGO encoding, i.e. unicode characters are backslash encoded and spaces are replaced by underscores, see also the tools on the YAGO2 website), OR --NME--, denoting that there is no matching entity in YAGO2 for this particular mention, or that we are missing the connection between the mention string and the YAGO2 entity.
//			- column 5 is the corresponding Wikipedia URL of the entity (added for convenience when evaluating against a Wikipedia based method)
//			- column 6 is the corresponding Wikipedia ID of the entity (added for convenience when evaluating against a Wikipedia based method - the ID refers to the dump used for annotation, 2010-08-17)
//			- column 7 is the corresponding Freebase mid, if there is one (thanks to Massimiliano Ciaramita from Google Zürich for creating the mapping and making it available to us)

		final String inCoNLLTSVPath = "C:/Users/wf7467/Desktop/Evaluation Datasets/Datasets/entity_linking/conll_aida-yago2-dataset/AIDA-YAGO2-dataset.tsv";
		final String outNIFPath = inCoNLLTSVPath + "_nif";
		final List<Document> documents = new ArrayList<Document>();
		int documentCounter = 0;
		final String documentBaseURI = "https://aifb.kit.edu/conll/";
		try (final BufferedReader brIn = new BufferedReader(new FileReader(new File(inCoNLLTSVPath)))) {
			Document document = null;

			String line = null;
			String document_text = "";
			int offset = 0, startOffset = 0, endOffset = 0;
			while ((line = brIn.readLine()) != null) {
				if (line.contains("-DOCSTART-")) {
					// document start aka. previous document must have ended --> create a new one
					document = new DocumentImpl();
					document.setDocumentURI(generateDocumentURI(documentBaseURI, documentCounter++));
					documents.add(document);
					// reinit document-related aggregation variables...
					document_text = "";
					// offsets must be reset for the current text
					offset = 0;
					startOffset = 0;
					endOffset = 0;
				} else {
					final String[] tokens = line.split("\t");
					String begin_or_cont = null, full_mention = null, yago_entity = null, wikipedia_entity = null,
							wikipedia_id = null, freebase_mid = null;
					int next = 0;
//					- column 1 is the token
					final String text = tokens[0];
//					- column 2 is either B (beginning of a mention) or I (continuation of a mention)
					if (tokens.length > ++next) {
						begin_or_cont = tokens[next];
					}
//					- column 3 is the full mention used to find entity candidates
					if (tokens.length > ++next) {
						full_mention = tokens[next];
					}
//					- column 4 is the corresponding YAGO2 entity (in YAGO encoding, i.e. unicode characters are backslash encoded and spaces are replaced by underscores, see also the tools on the YAGO2 website), OR --NME--, denoting that there is no matching entity in YAGO2 for this particular mention, or that we are missing the connection between the mention string and the YAGO2 entity.
					if (tokens.length > ++next) {
						yago_entity = tokens[next];
					}
//					- column 5 is the corresponding Wikipedia URL of the entity (added for convenience when evaluating against a Wikipedia based method)
					if (tokens.length > ++next) {
						wikipedia_entity = tokens[next];
					}
//					- column 6 is the corresponding Wikipedia ID of the entity (added for convenience when evaluating against a Wikipedia based method - the ID refers to the dump used for annotation, 2010-08-17)
					if (tokens.length > ++next) {
						wikipedia_id = tokens[next];
					}
//					- column 7 is the corresponding Freebase mid, if there is one (thanks to Massimiliano Ciaramita from Google Zürich for creating the mapping and making it available to us)
					if (tokens.length > ++next) {
						freebase_mid = tokens[next];
					}

					// update offsets
					// B (beginning of a mention) or I (continuation of a mention)
					if (begin_or_cont != null && wikipedia_entity != null) {
						if (begin_or_cont.equalsIgnoreCase("B")) {
							// we are starting so... just update endOffset w/ new token and startOffset to
							// current offset

							startOffset = offset;
							// endOffset = document_text.length();
							endOffset = startOffset + full_mention.length();
						} else if (begin_or_cont.equalsIgnoreCase("I")) {
							endOffset = startOffset + full_mention.length();// add 1 due to space in between
							// endOffset = document_text.length();
						} else {
							throw new RuntimeException(
									"Logic error - should not be able to find a character other than B or I");
						}
					}

					if (document_text != null) {
						if (document_text.length() > 0) {
							// Add a space in between...
							document_text += " " + text;
						} else {
							document_text += text;
						}
					}

					// Update text in document
					document.setText(document_text);
					// System.out.println(document_text + "(" + document_text.length() + ")");
					// offset += text.length() + 1;
					// System.out.println("Offset moving to: " + offset + " --> " + (offset +
					// document_text.length()));
					// offset += text.length();
					offset = document.getText().length() + 1;

					// add entity to document
					if (wikipedia_entity != null && full_mention != null) {
						final TypedNamedEntity namedEntity = new TypedNamedEntity(startOffset, endOffset - startOffset,
								new HashSet<>(Lists.newArrayList(wikipedia_entity)), new HashSet<>());
						// System.out.println("[" + documentCounter + "] Adding marking: " +
						// namedEntity);
						document.addMarking(namedEntity);
					}
				}
			}

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		final NIFWriter writer = new TurtleNIFWriter();
		final String nifString = writer.writeNIF(documents);
		try {
			FileUtils.write(new File(outNIFPath), nifString);
			System.out.println("Finished outputting resulting NIF document(s) to: " + outNIFPath);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// System.out.println(nifString);
		System.out.println("Executing parse check");
		final NIFParser parser = new TurtleNIFParser();
		final List<Document> nifDocuments = parser.parseNIF(nifString);
		if (nifDocuments.size() == documentCounter) {
			System.out.println("Parse check succeeded");
		} else {
			System.out.println("FAILED(" + nifDocuments.size() + " vs. " + documentCounter + ")");
		}
	}

	private static String generateDocumentURI(String documentBaseURI, int i) {
		return documentBaseURI + i;
	}

}
