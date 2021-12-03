package structure.interfaces.linker;

import java.util.Collection;
import java.util.List;

public interface APIKey {
	public List<String> getAPIKeys();

	public String getCurrentKey();

	public Collection<String> getUsedKeys();

	public void setKey(final String apiKey);

	public boolean switchToUnusedKey();
}
