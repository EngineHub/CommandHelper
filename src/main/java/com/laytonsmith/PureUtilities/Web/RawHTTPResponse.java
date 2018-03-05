package com.laytonsmith.PureUtilities.Web;

import java.io.InputStream;
import java.net.HttpURLConnection;

/**
 *
 */
public class RawHTTPResponse {

	private final HttpURLConnection connection;
	private final InputStream stream;

	public RawHTTPResponse(HttpURLConnection connection, InputStream stream) {
		this.connection = connection;
		this.stream = stream;
	}

	/**
	 * Returns the underlying HttpURLConnection.
	 *
	 * @return
	 */
	public HttpURLConnection getConnection() {
		return connection;
	}

	/**
	 * Returns the raw HTTP stream. This is already wrapped in an appropriate decoding stream if the content is
	 * compressed.
	 *
	 * @return
	 */
	public InputStream getStream() {
		return stream;
	}

}
