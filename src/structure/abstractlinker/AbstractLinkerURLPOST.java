package structure.abstractlinker;

import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Iterator;
import java.util.Map;
import java.util.function.BiFunction;

import structure.config.kg.EnumModelType;
import structure.datatypes.Mention;
import structure.interfaces.linker.LinkerURLPOST;
import structure.utils.FunctionUtils;

public abstract class AbstractLinkerURLPOST extends AbstractLinkerURL implements LinkerURLPOST {

	public AbstractLinkerURLPOST(EnumModelType KG) {
		super(KG);
	}

	protected void setupRequestMethod(final HttpURLConnection conn) throws ProtocolException {
		conn.setRequestMethod("POST");
	}

	@Override
	protected URI makeURI() throws URISyntaxException {
		return makeURI(null);
	}

	@Override
	protected String injectParams() {
		final StringBuilder params = new StringBuilder();
		final Iterator<Map.Entry<String, String>> it = this.params.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<String, String> e = it.next();
			params.append(e.getKey() + equalSymbol + e.getValue());
			if (it.hasNext()) {
				params.append(ampersandSymbol);
			}
		}
		return params.toString();
	}

	@Override
	public BiFunction<Number, Mention, Number> getScoreModulationFunction() {
		return FunctionUtils::returnScore;
	}

	@Override
	public Number getWeight() {
		return 1.0f;
	}

}
