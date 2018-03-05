package com.laytonsmith.PureUtilities.Web;

/**
 * This object wraps an HTTP header, which contains the header name and value.
 */
public final class HTTPHeader {

	private final String header;
	private final String value;

	public HTTPHeader(String header, String value) {
		this.header = header;
		this.value = value;
	}

	public String getHeader() {
		return header;
	}

	public String getValue() {
		return value;
	}

}
