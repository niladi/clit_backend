package structure.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;
import java.rmi.UnexpectedException;
import java.util.Collection;
import java.util.List;

import org.aksw.gerbil.io.nif.DocumentListParser;
import org.aksw.gerbil.transfer.nif.Document;
import org.aksw.gerbil.transfer.nif.Marking;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.adapters.RDFReaderRIOT;

import com.google.common.collect.Lists;

import structure.datatypes.Mention;

public class NIFUtils {
	public static List<Document> parseDocuments(File fileKORE50) throws FileNotFoundException {
		return parseDocuments(new BufferedReader(new FileReader(fileKORE50)));
	}

	public static Document parseDocument(Reader reader) throws FileNotFoundException, UnexpectedException {
		final List<Document> documents = parseDocuments(reader);
		if (documents.size() == 1) {
			return documents.get(0);
		} else {
			throw new UnexpectedException("Expected to receive 1 document, instead received "
					+ (documents == null ? "null" : documents.size()));
		}
	}

	public static List<Document> parseDocuments(Reader reader) throws FileNotFoundException {
		final Model nifModel = parseNIFModel(reader, getDefaultModel());
		final DocumentListParser docListParser = new DocumentListParser();
		return docListParser.parseDocuments(nifModel);
	}

	public static Model parseNIFModel(Reader reader, Model nifModel) {
		// RDFReaderRIOT rdfReader = new RDFReaderRIOT_TTL();
		RDFReaderRIOT rdfReader = new RDFReaderRIOT("TTL");
		rdfReader.read(nifModel, reader, "");
		return nifModel;
	}

	public static Model getDefaultModel() {
		Model nifModel = ModelFactory.createDefaultModel();
		// nifModel.setNsPrefixes(NIFTransferPrefixMapping.getInstance());
		return nifModel;
	}

	public static Collection<Mention> transformMarkings(final Collection<Marking> markings) {
		final List<Mention> mentions = Lists.newArrayList();
		for (Marking marking : markings) {
			mentions.add(new Mention(marking));
		}
		return mentions;
	}

}
