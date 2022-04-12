package com.laytonsmith.PureUtilities;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Provides helper methods related to URIs.
 */
public final class URIUtils {
	private URIUtils() {}

	/**
	 * Canonicalizes a URI. The URI {@code file:/file.txt} and {@code file:///file.txt} point to the same
	 * resource, but are neither toString.equal, or URI.equal. This method canonicalizes the second format
	 * into the first, as well as calling normalize on the URI.
	 * @param uri
	 * @return
	 */
	public static URI canonicalize(URI uri) {
		uri = uri.normalize();
		URI newURI;
		try {
			newURI = new URI(uri.getScheme(), uri.getUserInfo(), uri.getHost(), uri.getPort(), uri.getPath(),
					uri.getQuery(), uri.getFragment());
		} catch(URISyntaxException ex) {
			throw new Error(ex);
		}
		return newURI;
	}

	/**
	 * Creates and canonicalizes a URI from a String. See {@link #canonicalize}.
	 * @param uri
	 * @return
	 */
	public static URI canonicalize(String uri) {
		return canonicalize(URI.create(uri));
	}
}
