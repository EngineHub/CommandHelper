/* ***** BEGIN LICENSE BLOCK *****
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * ***** END LICENSE BLOCK ***** */
package com.laytonsmith.PureUtilities;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * This Public Suffix Service (PSS) class reads a file of rules describing TLD-like domain names and makes rulings on
 * passed hostnames/domain-names based of the data file content. For a complete description of the expected file format
 * and parsing rules, see <a
 * href="http://wiki.mozilla.org/Gecko:Effective_TLD_Service">Effective TLD Service</a>
 * and <a href="http://www.publicsuffix.org">Public Suffix</a>.
 *
 * <p>
 * This class is a rough port of the c++ file, <a
 * href="http://www.koders.com/cpp/fid3F3968962A800C642821733B91E16316B53753BF.aspx">nsEffectiveTLDService.cpp</a>
 * originally developed by Pamela Greene &lt;pamg.bugs ATGMAILDOTCOM&gt;. The class uses the first
 * <code>effective_tld_names.dat</code> found on the CLASSPATH. The bundled jar has a version of the file taken from
 * <a href="publicsuffix.org">Public Suffix</a> on 10/22/2007.
 *
 * <p>
 * To use this class, instantiate an instance and then call {@link #getEffectiveTLDLength(String)} passing the hostname
 * to interrogate..
 *
 * <p>
 * The following description of how the code works is copied from the head of the c++ file.
 *
 * <p>
 * The list of subdomain rules is stored as a wide tree of SubdomainNodes, primarily to facilitate multiple levels of
 * wildcards. Each node represents one level of a particular rule in the list, and stores meta-information about the
 * rule it represents as well as a list (hash) of all the subdomains beneath it.
 * <p>
 * <ul><li>stopOK: If true, this node marks the end of a rule.</li>
 * <li>exception: If true, this node marks the end of an exception rule.</li>
 * </ul>
 * <p>
 * For example, if the effective-TLD list contains
 * <pre>foo.com
 * *.bar.com
 * !baz.bar.com
 * </pre> then the subdomain tree will look like this (conceptually; the actual order of the nodes in the hashes is not
 * guaranteed):
 * <pre>
 * +--------------+
 * | com          |
 * | exception: 0 |        +--------------+
 * | stopOK: 0    |        | foo          |
 * | children ----|------> | exception: 0 |
 * +--------------+        | stopOK: 1    |
 *                         | children     |
 *                         +--------------+
 *                         | bar          |
 *                         | exception: 0 |        +--------------+
 *                         | stopOK: 0    |        | *            |
 *                         | children ----|------> | exception: 0 |
 *                         +--------------+        | stopOK: 1    |
 *                                                 | children     |
 *                                                 +--------------+
 *                                                 | baz          |
 *                                                 | exception: 1 |
 *                                                 | stopOK: 1    |
 *                                                 | children     |
 *                                                 +--------------+
 * </pre>
 * </p>
 * <p>
 * TODO: Add support for IDN (See java6 java.net.IDN).</p>
 * <p>
 * TODO: Add support for multiple data files</p>
 *
 * @author stack
 */
public final class PublicSuffix {

	private final SubdomainNode root = new SubdomainNode(false, false);
	private static final String WILDCARD = "*";
	private static final String EXCEPTION = "!";
	private static final Pattern WHITESPACE = Pattern.compile("\\s+");
	private static final char DOT = '.';

	private static final String DATA_FILENAME = "public-suffix.txt";

	private static PublicSuffix defaultInstance;

	/**
	 * Returns the PublicSuffix instance based on the default data source
	 */
	public static PublicSuffix get() {
		if(defaultInstance == null) {
			try {
				defaultInstance = new PublicSuffix();
			} catch (Exception ex) {
				throw new RuntimeException(ex);
			}
		}
		return defaultInstance;
	}

	/**
	 * Loads data file and creates a tree of subdomain nodes in memory used finding the effective TLD.
	 *
	 * @throws IOException
	 * @throws UnsupportedEncodingException
	 */
	private PublicSuffix() throws UnsupportedEncodingException, IOException {
		load();
	}

	/**
	 * Finds the length in bytes of the effective TLD for the given <code>hostname</code>
	 *
	 * @param hostname Hostname to check.
	 * @return length of effective-TLD portion in passed <code>hostname</code>. If passed string is all effective-TLD --
	 * e.g. if you '.com' -- then we return -1. If no effective-TLD found, then returns <code>hostname.length()</code>.
	 */
	public int getEffectiveTLDLength(final String hostname) {
		final String normalizedHostname = normalizeHostname(hostname);
		int lastDot = normalizedHostname.length();
		for(SubdomainNode node = this.root; lastDot > 0;) {
			int nextDotLoc = normalizedHostname.lastIndexOf('.', lastDot - 1);
			node = findNode(node,
					normalizedHostname.substring(nextDotLoc + 1, lastDot), false);
			if(node == null) {
				break;
			}
			lastDot = nextDotLoc;
			if(node.isException()) {
				// Exception rules use one fewer levels than were matched.
				break;
			}
			if(node.isStopOK()) {
				break;
			}
		}
		return lastDot;
	}

	/**
	 * Normalizes characters of <code>hostname</code>. ASCII names are lower-cased. TOOD: If names using other
	 * characters than ASCII need to be normalized with a IIDNService::Normalize, RFC 3454.
	 *
	 * @param hostname
	 * @return normalized hostname.
	 */
	private String normalizeHostname(final String hostname) {
		boolean isLowercase = true;
		boolean isAscii = true;
		for(int i = 0; i < hostname.length(); i++) {
			char c = hostname.charAt(i);
			if(c >= 128) {
				isAscii = false;
				break;
			}
			if(!Character.isLowerCase(c)) {
				isLowercase = false;
			}
		}
		if(!isAscii) {
			// TODO: If java 6, then there is java.net.IDN#toAscii(hostname).
			throw new UnsupportedOperationException("No support yet for IDN: TODO");
		}
		return isLowercase ? hostname : hostname.toLowerCase();
	}

	/**
	 * Load effective TLD file from the CLASSPATH.
	 *
	 * @throws IOException
	 * @throws UnsupportedEncodingException
	 */
	private void load()
			throws UnsupportedEncodingException, IOException {
		URL u = this.getClass().getResource("/" + DATA_FILENAME);
		if(u == null) {
			throw new FileNotFoundException(DATA_FILENAME + " not on CLASSPATH");
		}
		BufferedReader br
				= new BufferedReader(new InputStreamReader(u.openStream(), "UTF-8"));
		for(String line = null; (line = br.readLine()) != null;) {
			if(line.length() <= 0 || line.startsWith("//")) {
				continue;
			}
			addEffectiveTLDEntry(this.root, line);
		}
	}

	/**
	 * Given a parent node and a candidate <code>subdomain</code>, searches the parent's children for a matching
	 * subdomain and returns a pointer to the matching node if one was found. If no exact match was found and
	 * <code>create</code> is true, creates a new child node for the given <code>subdomain</code> and returns that. If
	 * no exact match was found an <code>create</code> is false, looks for a wildcard node (*) instead. If no wildcard
	 * node is found either, returns null.
	 *
	 * @param node
	 * @param subdomain
	 * @param create Typically true when the subdomain tree is being built, and false when it is being searched to
	 * determine a hostname's effective TLD.
	 * @return
	 */
	private SubdomainNode findNode(final SubdomainNode node,
			final String subdomain, final boolean create) {
		boolean exception = subdomain != null && subdomain.startsWith(EXCEPTION);
		String key = exception ? subdomain.substring(1) : subdomain;
		SubdomainNode newNode = node.getChildren().get(key);
		if(newNode != null) {
			return newNode;
		}
		if(create) {
			// Create it and add to parent.
			SubdomainNode subNode = new SubdomainNode(exception, false);
			node.getChildren().put(key, subNode);
			return subNode;
		}
		return node.getChildren().get(WILDCARD);
	}

	/**
	 * Adds the given domain name rule to the effective-TLD tree.
	 *
	 * @param m Map to add too.
	 * @param line Line that starts with a hostname.
	 */
	private void addEffectiveTLDEntry(SubdomainNode node,
			final String line) {
		String hostname = WHITESPACE.split(line, 2)[0];
		for(int dotLoc = hostname.length(); dotLoc >= 0;) {
			int nextDocLoc = hostname.lastIndexOf(DOT, dotLoc - 1);
			String subdomain = hostname.substring(nextDocLoc + 1, dotLoc);
			dotLoc = nextDocLoc;
			node = findNode(node, subdomain, true);
		}

		// The last node in an entry is by definition a stop-OK node.
		node.setStopOK();
	}

	private void dump() {
		dump(this.root.getChildren(), 0);
	}

	private void dump(final Map<String, SubdomainNode> node, final int offset) {
		if(node == null || node.isEmpty()) {
			return;
		}
		for(Map.Entry<String, SubdomainNode> e : node.entrySet()) {
			for(int i = 0; i < offset; i++) {
				System.out.print(" ");
			}
			System.out.println(e.getKey() + ": " + e.getValue());
			dump(e.getValue().getChildren(), offset + 1);
		}
	}

	/**
	 * Immmutable subdomain node.
	 */
	private static class SubdomainNode {

		final boolean exception;
		boolean stopOK;
		final Map<String, SubdomainNode> children;

		/**
		 * Create node with no children.
		 *
		 * @param ex
		 * @param stop
		 */
		public SubdomainNode(final boolean ex, final boolean stop) {
			this(ex, stop, new HashMap<String, SubdomainNode>());
		}

		public SubdomainNode(final boolean ex, final boolean stop,
				final Map<String, SubdomainNode> c) {
			this.exception = ex;
			this.children = c;
			this.stopOK = stop;
		}

		public boolean isException() {
			return this.exception;
		}

		public boolean isStopOK() {
			return this.stopOK;
		}

		public void setStopOK() {
			this.stopOK = true;
		}

		public Map<String, SubdomainNode> getChildren() {
			return this.children;
		}

		@Override
		public String toString() {
			return "exception: " + this.exception + ", stopOK: " + this.stopOK
					+ ", children: " + (this.children == null ? 0 : this.children.size());
		}
	}
}
