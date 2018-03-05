package com.laytonsmith.PureUtilities.Web;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This class wraps all the data that an HTTP response contains.
 */
public final class HTTPResponse {

	private String rawResponse = null;
	private final List<HTTPHeader> headers = new LinkedList<HTTPHeader>();
	private final String responseText;
	private final int responseCode;
	private final String content;
	private final String httpVersion;

	/**
	 * Creates a new HTTP Response object, which wraps all the data that a HTTP response would contain.
	 *
	 * @param responseText The raw response text associated with the response code, for instance "OK" for a 200
	 * response.
	 * @param responseCode The response code, for instance 404.
	 * @param headers The headers returned by the server
	 * @param response The response body
	 * @param httpVersion The HTTP version that the server is using, for instance "1.0"
	 */
	public HTTPResponse(String responseText, int responseCode, Map<String, List<String>> headers,
			String response, String httpVersion) {
		this.responseText = responseText;
		this.responseCode = responseCode;
		for(String key : headers.keySet()) {
			for(String value : headers.get(key)) {
				this.headers.add(new HTTPHeader(key, value));
			}
		}
		this.content = response;
		this.httpVersion = httpVersion;
	}

	/**
	 * Gets the contents of this HTTP request. If this request was a download request, this will be null.
	 *
	 * @return
	 */
	public String getContent() {
		return this.content;
	}

	/**
	 * Returns the HTTP version that the server is using. This string will be in the format HTTP/1.0 for instance.
	 *
	 * @return
	 */
	public String getHttpVersion() {
		return httpVersion;
	}

	/**
	 * Gets the value of the first header returned. If the header isn't set, null is returned.
	 *
	 * @param key
	 * @return
	 */
	public String getFirstHeader(String key) {
		for(HTTPHeader header : headers) {
			if(header.getHeader().equalsIgnoreCase(key)) {
				return header.getValue();
			}
		}
		return null;
	}

	/**
	 * Returns a list of all the header names that are set in this request.
	 *
	 * @return
	 */
	public Set<String> getHeaderNames() {
		Set<String> set = new HashSet<String>();
		for(HTTPHeader h : headers) {
			if(h.getHeader() != null) {
				set.add(h.getHeader());
			}
		}
		return set;
	}

	/**
	 * Returns all the headers for a given key. If there are no headers set for this key, an empty list is returned.
	 *
	 * @param key
	 * @return
	 */
	public List<String> getHeaders(String key) {
		List<String> list = new ArrayList<String>();
		for(HTTPHeader header : headers) {
			if((header.getHeader() == null && key == null) || (header.getHeader() != null && header.getHeader().equalsIgnoreCase(key))) {
				list.add(header.getValue());
			}
		}
		return list;
	}

	/**
	 * Returns the response code, for instance 404.
	 *
	 * @return
	 */
	public int getResponseCode() {
		return responseCode;
	}

	/**
	 * Returns the response text, for instance for a 404 page, "Not Found"
	 *
	 * @return
	 */
	public String getResponseText() {
		return responseText;
	}

	@Override
	public String toString() {
		if(rawResponse == null) {
			rawResponse = "HTTP/" + httpVersion + " " + responseCode + " " + responseText + "\n";
			for(HTTPHeader h : headers) {
				if(h.getHeader() == null) {
					continue;
				}
				rawResponse += h.getHeader() + ": " + h.getValue() + "\n";
			}
			rawResponse += "\n" + content;
		}
		return rawResponse;
	}

}
