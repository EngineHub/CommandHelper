package com.laytonsmith.PureUtilities.Web;

import com.laytonsmith.PureUtilities.Common.StringUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class wraps and represents a list of HTTP headers, and includes methods for easily parsing common headers.
 */
public class HTTPHeaders implements Iterable<HTTPHeader> {

	private final List<HTTPHeader> model;

	/**
	 * Creates a new HTTPHeaders object.
	 * @param model
	 */
	public HTTPHeaders(List<HTTPHeader> model) {
		this.model = model;
	}

	public int size() {
		return model.size();
	}

	@Override
	public Iterator<HTTPHeader> iterator() {
		return model.iterator();
	}

	@Override
	public String toString() {
		StringBuilder b = new StringBuilder();
		for(HTTPHeader h : model) {
			b.append(h.getHeader()).append(": ").append(h.getValue()).append("\n");
		}
		return b.toString();
	}



	/**
	 * <strong>(Read the note below if you're using this for "Set-Cookie" headers!)</strong>
	 * <p>
	 * Combines the values of all the given header with commas.
	 * <p>
	 * According to the HTTP specification <a href="https://tools.ietf.org/html/rfc7230#section-3.2.2">
	 * https://tools.ietf.org/html/rfc7230#section-3.2.2</a>:
	 * <pre><code>
	 * A sender MUST NOT generate multiple header fields with the same field
	 * name in a message unless either the entire field value for that
	 * header field is defined as a comma-separated list [i.e., #(values)]
	 * or the header field is a well-known exception (as noted below).
	 *
	 * A recipient MAY combine multiple header fields with the same field
	 * name into one "field-name: field-value" pair, without changing the
	 * semantics of the message, by appending each subsequent field value to
	 * the combined field value in order, separated by a comma.  The order
	 * in which header fields with the same field name are received is
	 * therefore significant to the interpretation of the combined field
	 * value; a proxy MUST NOT change the order of these field values when
	 * forwarding a message.
	 * </code></pre>
	 *
	 * Therefore, this method is a convenience method to implementing the correct symantics described here. If
	 * there are multiple headers with the same name, then they are concatenated together with a comma, and returned
	 * as if they were a single header.
	 *
	 * In practice, however, there is a caveat with Set-Cookie headers:
	 *
	 * <pre><code>
	 * Note: In practice, the "Set-Cookie" header field ([RFC6265]) often
	 * appears multiple times in a response message and does not use the
	 * list syntax, violating the above requirements on multiple header
	 * fields with the same name.  Since it cannot be combined into a
	 * single field-value, recipients ought to handle "Set-Cookie" as a
	 * special case while processing header fields.  (See Appendix A.2.3
	 * of [Kri2001] for details.)
	 * </code></pre>
	 *
	 * In this case, generally one should just use {@link #getHeaders(java.lang.String)} and loop through the results.
	 * @param key The header to look up.
	 * @return
	 */
	public String getCombinedHeader(String key) {
		return StringUtils.Join(getHeaders(key), ",").trim();
	}

	/**
	 * Gets the value of the first header returned. If the header isn't set, null is returned.
	 *
	 * Generally, you should use {@link #getCombinedHeader} instead, but this may not be desirable in all cases.
	 *
	 * @param key
	 * @return
	 */
	public String getFirstHeader(String key) {
		for(HTTPHeader header : model) {
			// header.getHeader() can return null. This means that it's the first header, the status header.
			if(key == null && header.getHeader() == null) {
				return header.getValue();
			}
			if(key == null) {
				continue;
			}
			if(key.equalsIgnoreCase(header.getHeader())) {
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
		Set<String> set = new HashSet<>();
		for(HTTPHeader h : model) {
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
		List<String> list = new ArrayList<>();
		for(HTTPHeader header : model) {
			if((header.getHeader() == null && key == null) || (header.getHeader() != null
					&& header.getHeader().equalsIgnoreCase(key))) {
				list.add(header.getValue());
			}
		}
		return list;
	}

	/**
	 * Returns the content type. If there was no content type present, null is returned.
	 * @return
	 */
	public ContentType getContentType() {
		String header = getFirstHeader("Content-Type");
		if(header == null) {
			return null;
		}
		return new ContentType(header);
	}

	public static class MIMEType {
		/**
		 * The full MIME type, for instance "text/html". Should never be null.
		 */
		public final String mediaType;

		/**
		 * The primary type of the media type. For instance in "text/html" it would be "text". Should never be null.
		 */
		public final String type;

		/**
		 * The subtype of the media type. For instance, in "text/html" it would be "html". Should never be null.
		 */
		public final String subtype;

		public MIMEType(String mediaType) {
			this.mediaType = mediaType;
			String[] parts = mediaType.split("/");
			this.type = parts[0];
			this.subtype = parts[1];
		}
	}

	public static class ContentType {
		/**
		 * The MIME type of the content. Should never be null.
		 */
		public final MIMEType mimeType;

		/**
		 * The charset of the content type. This will be null if not present, and should be null if the type was
		 * "multipart". If null, ISO-8859-1 is the default charset of HTTP 1.1, and is a reasonable default.
		 */
		public final String charset;

		/**
		 * In a multipart form, this is the boundary separator. Should always be null if the content type was not
		 * "multipart".
		 */
		public final String boundary;

		private static final Pattern CONTENT_TYPE_PATTERN = Pattern.compile("^(.*?)(?:;\\s*(.*?))?$");

		public ContentType(String contentType) {
			Matcher m = CONTENT_TYPE_PATTERN.matcher(contentType);
			m.find();
			mimeType = new MIMEType(m.group(1));
			if(m.group(2) != null) {
				String parameter = m.group(2);
				String[] parts = parameter.split("=");
				if("charset".equalsIgnoreCase(parts[0])) {
					charset = parts[1];
					boundary = null;
				} else if("boundary".equalsIgnoreCase(parts[0])) {
					boundary = parts[1];
					charset = null;
				} else {
					charset = null;
					boundary = null;
				}
			} else {
				charset = null;
				boundary = null;
			}
		}
	}

	/**
	 * Reads the link headers (if present) and finds the link with the given relation. If there are no link headers,
	 * or there are but none with the specified relation, null is returned. Note that the returned link may be
	 * either relative or absolute, so if the value starts with /, one should use the existing domain as the url base.
	 * @param rel
	 * @return
	 */
	public String getLink(String rel) {
		String links = getCombinedHeader("link");
		if(links.isEmpty()) {
			return null;
		}
		List<String> headers = Arrays.asList(links.split(","));
		for(String link : headers) {
			link = link.trim();
			if(link.contains("rel=\"" + rel + "\"")) {
				return link.replaceAll("<(.*)>.*", "$1");
			}
		}
		return null;
	}

}
