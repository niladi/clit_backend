package structure.interfaces.linker;

public interface URLComponent {
	static final String https = "https";
	static final String http = "http";

	/**
	 * Sets to HTTP Scheme
	 * 
	 * @return
	 */
	public URLComponent http();

	/**
	 * Sets to HTTPS Scheme
	 * 
	 * @return
	 */
	public URLComponent https();

	/**
	 * Sets the base URL
	 * 
	 * @param url
	 * @return
	 */
	public URLComponent url(final String url);

	/**
	 * 
	 * @param suffix suffix to set for the URL
	 * @return
	 */
	public URLComponent suffix(final String suffix);

	/**
	 * Set the timeout for this URL request. May be ignored by some linker while it
	 * may be required by others
	 * 
	 * @param timeout
	 * @return
	 */
	public URLComponent timeout(final int timeout);

	/**
	 * Set the port number to connect to
	 * @param port to connect to
	 * @return chaining pattern
	 */
	public URLComponent port(final int port);
	
	/**
	 * Sets specific parameter with given value
	 * 
	 * @param paramName  parameter name
	 * @param paramValue value of the parameter
	 * @return
	 */
	public URLComponent setParam(final String paramName, final String paramValue);

}
