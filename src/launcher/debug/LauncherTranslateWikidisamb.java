package launcher.debug;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.google.common.collect.Lists;

import clit.translator.TranslatorWikidataToDBpedia;
import structure.datatypes.Mention;

public class LauncherTranslateWikidisamb {

	public static void main(String[] args) {
		final String path = "./datasets/wikidata-disambig-dev.json";
		final String outTranslatePath = path + "_translated";
		final Object translatedEntities = parseFile(path);

		try (final BufferedWriter bw = new BufferedWriter(new FileWriter(outTranslatePath))) {
			bw.write(translatedEntities.toString());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static JSONArray parseFile(final String path) {
		final TranslatorWikidataToDBpedia translator = new TranslatorWikidataToDBpedia();
		final Collection<Mention> mentions = Lists.newArrayList();
		// JSON parser object to parse read file
		final JSONParser jsonParser = new JSONParser();
		final File inFile = new File(path);
		try (FileReader reader = new FileReader(inFile)) {
			// Read JSON file
			final Object obj = jsonParser.parse(reader);

			final JSONArray entityDocuments = (JSONArray) obj;

			// Iterate over array of documents
			for (Object doc : entityDocuments) {
				final JSONObject docJSON = (JSONObject) doc;
				final Map<String, String> toTranslate = parseWikidisamb(docJSON, mentions);
				for (Map.Entry<String, String> e : toTranslate.entrySet()) {
					final String translatedEntity = translator.translate(e.getValue());
					docJSON.put(e.getKey(), translatedEntity);
				}
			}
			return entityDocuments;
		} catch (IOException | ParseException e) {
			e.printStackTrace();
			return null;
		}
	}

	private static Map<String, String> parseWikidisamb(JSONObject doc, final Collection<Mention> mentions) {
		final String[] keys = new String[] { "correct_id", "wrong_id" };
		final Map<String, String> ret = new HashMap<>();
		for (String key : keys) {
			ret.put(key, "http://www.wikidata.org/entity/" + doc.get(key).toString());
		}
		return ret;
	}

}
