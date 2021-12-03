package structure.exceptions;

import java.io.IOException;

public class APILimitException extends IOException {

	public APILimitException(String annotatedText) {
		super(annotatedText);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 7182178874756239154L;

}
