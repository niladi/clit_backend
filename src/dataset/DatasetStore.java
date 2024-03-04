package dataset;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.aksw.gerbil.transfer.nif.Document;
import org.aksw.gerbil.transfer.nif.TurtleNIFDocumentParser;

import structure.config.constants.FilePaths;
import structure.config.kg.EnumModelType;
import structure.datatypes.AnnotatedDocument;
import structure.exceptions.PipelineException;

public class DatasetStore {

	private EnumModelType KG;
	private String path;

	public DatasetStore() {
		KG = EnumModelType.DEFAULT;
		path = FilePaths.DIR_EVALUATION_DATASETS.getPath(KG);
	}

	public List<String> getDatasets() {
		List<String> datasetFileNames = new ArrayList<>();

		// read existing files
		File folder = new File(path);
		File[] datasetFiles = folder.listFiles();

		// add file names
		for (File datasetFile : datasetFiles) {
			String fileName = datasetFile.getName();
			datasetFileNames.add(fileName);
		}

		return datasetFileNames;
	}

	public List<Document> readDataset(String datasetName) throws PipelineException {
		String filePath = path + datasetName;
		File file = new File(filePath);
		InputStream inputStream;
		try {
			inputStream = new FileInputStream(file);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			throw new PipelineException("Could not read dataset file", e.getMessage());
		}
		final TurtleNIFDocumentParser parser = new TurtleNIFDocumentParser();
		List<Document> documents;
		try {
			documents = parser.getDocumentsFromNIFStream(inputStream);
		} catch (Exception e) {
			e.printStackTrace();
			throw new PipelineException("Could not parse dataset NIF file", e.getMessage());
		}
		return documents;
	}

	public ArrayList<AnnotatedDocument> readDatasetAsAnnotatedDocument(String datasetName) throws PipelineException {
		ArrayList<AnnotatedDocument> results = new ArrayList<>();
		List<Document> documents = readDataset(datasetName);
		for (Document document : documents) {
			AnnotatedDocument result = makeAnnotatedDocument(document);
			results.add(result);
		}
		return results;
	}

	private AnnotatedDocument makeAnnotatedDocument(Document document) {
		// TODO also add Markings and other fields?
		return new AnnotatedDocument(document.getText());
	}

}
