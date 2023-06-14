package experiment;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class ExperimentCache {
	private final String path = "";
	// Key: input text and pipeline stuff together
	// Value: Path of file containing the infos
	private final Map<String, String> cache = new HashMap<>();
	
	
	public void loadCache(final String path)
	{
		cache.clear();
		final File[] files = new File(path).listFiles();
		for (final File file : files)
		{
			// Decode a key from the file contents
			// aka. Open file, parse contents, generate key
			
		}
	}
	
	public void loadCache()
	{
		loadCache(this.path);
	}
	
	private String generateInputTextKey(final String inputText)
	{
		return null;
	}
	
	private String generatePipelineKey(final String pipeline)
	{
		return null;
	}
}
