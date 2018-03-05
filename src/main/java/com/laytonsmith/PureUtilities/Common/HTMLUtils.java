package com.laytonsmith.PureUtilities.Common;

/**
 *
 */
public class HTMLUtils {

	/**
	 * Given a raw string, escapes html special characters. For instance, turning &lt; into &amp;lt;
	 * <!-- for those reading the javadoc directly in code, that's: -->
	 * <!-- < into &lt; --> @
	 *
	 *
	 * param raw
	 * @return
	 */
	public static String escapeHTML(String raw) {
		return raw.replace("&", "&amp;")
				.replace("<", "&lt;")
				.replace(">", "&gt;")
				.replace("\"", "&quot;")
				.replace("'", "&apos;");
	}

	/**
	 * Given escaped html characters, unescapes them. For instance, turning &amp;lt; into &lt;
	 * <!-- for those reading the javadoc directly in code, that's: -->
	 * <!-- &lt; into < --> @param ht
	 *
	 * ml
	 *
	 * @return
	 */
	public static String unescapeHTML(String html) {
		return html.replace("&apos;", "'")
				.replace("&quot;", "\"")
				.replace("&gt;", ">")
				.replace("&lt;", "<")
				.replace("&amp;", "&");
	}
}
