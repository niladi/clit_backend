package api;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import structure.config.constants.FilePaths;
import structure.config.kg.EnumModelType;

public class APIKeyLoader {
	private final EnumModelType KG;

	public APIKeyLoader(EnumModelType KG) {
		this.KG = KG;
	}

	public APIKeyLoader() {
		this(EnumModelType.DEFAULT);
	}

	public String loadKey(final String keyname) {
		String key = null;
		try {
			Properties prop = new Properties();
			ClassLoader loader = Thread.currentThread().getContextClassLoader();
			final InputStream stream = loader.getResourceAsStream(FilePaths.FILE_API_KEYS.getPath(KG));
			prop.load(stream);
			final Object o = prop.get(keyname);
			if (o != null) {
				key = o.toString();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		if (key == null) {
			throw new RuntimeException("No API key found for key[" + keyname + "]");
		}

		return key;
	}
}
