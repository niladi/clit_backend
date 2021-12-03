package structure.utils;

import java.util.function.Predicate;
import java.util.regex.Pattern;

import org.apache.commons.validator.routines.InetAddressValidator;

public class NetUtils {
	private final static Predicate<String> urlPattern = Pattern.compile("^(https?://)?" + // protocol
			"((([a-z\\d]([a-z\\d-]*[a-z\\d])*)\\.)+[a-z]{2,}|" + // domain name
			"((\\d{1,3}\\.){3}\\d{1,3}))" + // OR ip (v4) address
			"(:\\d+)?(/[-a-z\\d%_.~+]*)*" + // port and path
			"(\\?[;&a-z\\d%_.~+=-]*)?" + // query string
			"(\\#[-a-z\\d_]*)?$", 'i').asPredicate(); // fragment locater

	public static boolean isIRI(final String urlToTest) {
		boolean ret = urlPattern.test(urlToTest);
		// System.out.println("isIRI("+urlToTest+"): "+ret);
		return ret;
	}
	
	/**
	 * Whether a passed URL is an IPv4 address (optionally has a defined port)
	 * @param url url to check
	 * @return true if url is an IPv4 address with a port optionally
	 */
	public static boolean isIPv4Address(final String url) {
		try {
			if (url == null || url.length() < 1) {
				return false;
			}

			final String prefixHttp = "http://";
			final String prefixHttps = "https://";
			String cleanedUrl = url;
			// remove http://
			cleanedUrl = cleanedUrl.startsWith(prefixHttp) ? cleanedUrl.substring(prefixHttp.length()) : cleanedUrl;
			// remove https://
			cleanedUrl = cleanedUrl.startsWith(prefixHttps) ? cleanedUrl.substring(prefixHttps.length()) : cleanedUrl;

			// now we can safely split
			final String colonDelim = ":";
			final String[] parts = cleanedUrl.split(colonDelim);

			if (parts.length > 0) {
				// is the first part a valid address?
				if (parts.length == 1) {
					// no port defined
					return !cleanedUrl.contains(colonDelim)
							&& InetAddressValidator.getInstance().isValidInet4Address(parts[0]);
				} else if (parts.length == 2) {
					// it's got two parts, so the 2nd is a port
					int port;
					try {
						port = Integer.parseInt(parts[1]);
					} catch (NumberFormatException e) {
						return false;
					}

					// is it a reasonable port and is the ipv4 address valid?
					return (port > 0 && port < 65536)
							&& InetAddressValidator.getInstance().isValidInet4Address(parts[0]);
				} else {
					// too many parts for it to be valid...
					return false;
				}
			} else {
				// parts.length is negative...
				return false;
			}
		} catch (Exception e) {
			return false;
		}
	}
}
