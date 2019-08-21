package com.laytonsmith.PureUtilities;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicInteger;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Works similarly to {@link XMLDocument}, however, this uses a SAX based parser, which is useful in cases where you are
 * reading (not writing) data from a large XML document, or streaming document. It uses XPath expressions to identify
 * segments of a document that are of interest, and triggers once the element is fully read in. This should be mostly
 * possible to replace SAX parsers in all cases.
 */
public class SAXDocument {

	private final InputStream stream;
	private final Map<String, List<ElementCallback>> callbacks = new HashMap<>();

	/**
	 * Creates a new SAXDocument, based on the input stream provided.
	 *
	 * @param stream
	 */
	public SAXDocument(InputStream stream) {
		this.stream = stream;
	}

	/**
	 * Creates a new SAXDocument. If you already have the document in memory though, you may consider using
	 * {@link XMLDocument} instead, since you've obviously already got the whole document in memory.
	 *
	 * @param document The XML document in a string
	 * @param encoding The encoding of the stream. If null, UTF-8 is used.
	 * @throws java.io.UnsupportedEncodingException
	 */
	public SAXDocument(String document, String encoding) throws UnsupportedEncodingException {
		this(new ByteArrayInputStream(document.getBytes(encoding == null ? "UTF-8" : encoding)));
	}

	/**
	 * Parses the XML document. As elements are loaded, if they match, they are sent to the listeners that match the
	 * element.
	 *
	 * @throws SAXException
	 * @throws IOException
	 */
	public void parse() throws SAXException, IOException {
		SAXParserFactory factory = SAXParserFactory.newInstance();
		SAXParser saxParser;
		try {
			saxParser = factory.newSAXParser();
		} catch (ParserConfigurationException | SAXException ex) {
			throw new RuntimeException(ex);
		}
		saxParser.parse(stream, new DefaultHandler() {
			Stack<String> nodeNames = new Stack<>();
			Stack<Map<String, AtomicInteger>> nodeCount = new Stack<>();
			String lastElement = "";
			Map<String, StringBuilder> contents = new HashMap<>();
			Stack<Attributes> attributeStack = new Stack<>();

			@Override
			public void startDocument() throws SAXException {
				nodeCount.push(new HashMap<>());
			}

			@Override
			public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
				nodeNames.push(qName);
				Map<String, AtomicInteger> c = nodeCount.peek();
				if(!c.containsKey(qName)) {
					c.put(qName, new AtomicInteger(1));
				} else {
					c.get(qName).incrementAndGet();
				}
				Map<String, AtomicInteger> counts = new HashMap<>();
				nodeCount.push(counts);
				if(!contents.isEmpty()) {
					StringBuilder b = new StringBuilder();
					b.append("<").append(qName).append("");
					for(int i = 0; i < attributes.getLength(); i++) {
						b.append(" ").append(attributes.getQName(i))
								.append("=\"").append(attributes.getValue(i).replace("\"", "&quot;")).append("\"");
					}
					b.append(">");
					appendAll(b.toString());
				}
				String path = pathFromMarkers(nodeNames, nodeCount);
				if(hasListener(path)) {
					contents.put(getListenerPath(path), new StringBuilder());
					attributeStack.push(attributes);
				}
			}

			@Override
			public void characters(char[] ch, int start, int length) throws SAXException {
				if(!contents.isEmpty()) {
					String s = fromChars(ch, start, length);
					appendAll(s);
				}
			}

			@Override
			public void endElement(String uri, String localName, String qName) throws SAXException {
				String path = pathFromMarkers(nodeNames, nodeCount);
				if(hasListener(path)) {
					String key = getListenerPath(path);
					StringBuilder b = contents.remove(key);
					Attributes attr = attributeStack.pop();
					Map<String, String> attributes = new LinkedHashMap<>();
					for(int i = 0; i < attr.getLength(); i++) {
						attributes.put(attr.getQName(i), attr.getValue(i));
					}
					notifyListeners(path, qName, attributes, b.toString());
				}
				if(!contents.isEmpty()) {
					appendAll("</" + qName + ">");
				}

				nodeNames.pop();
				nodeCount.pop();
			}

			private void appendAll(String s) {
				for(StringBuilder b : contents.values()) {
					b.append(s);
				}
			}

		});
	}

	private String pathFromMarkers(Stack<String> elementStack, Stack<Map<String, AtomicInteger>> nodeCounts) {
		List<String> elementList = new ArrayList<>(elementStack);
		List<Map<String, AtomicInteger>> nodeList = new ArrayList<>(nodeCounts);
		StringBuilder b = new StringBuilder();
		for(int i = 0; i < elementList.size(); i++) {
			b.append("/")
					.append(elementList.get(i))
					.append("[")
					.append(nodeList.get(i).get(elementList.get(i)).get())
					.append("]");
		}
		return b.toString();
	}

	private String fromChars(char[] ch, int start, int length) {
		StringBuilder b = new StringBuilder();
		for(int i = start; i < start + length; i++) {
			b.append(ch[i]);
		}
		return b.toString();
	}

	private void notifyListeners(String xpath, String tag, Map<String, String> attr, String contents) {
		for(String key : callbacks.keySet()) {
			if(xpath.matches(key)) {
				for(ElementCallback c : callbacks.get(key)) {
					c.handleElement(xpath, tag, attr, contents);
				}
			}
		}
	}

	private boolean hasListener(String xpath) {
		for(String key : callbacks.keySet()) {
			if(xpath.matches(key)) {
				return true;
			}
		}
		return false;
	}

	private String getListenerPath(String xpath) {
		for(String key : callbacks.keySet()) {
			if(xpath.matches(key)) {
				return key;
			}
		}
		return null;
	}

	/**
	 * Adds a new listener. When you call parse(), any elements that match these listeners are triggered. Any number of
	 * listeners can be added per xpath.
	 *
	 * @param xpath The XPath to match against. This can only be a simple XPath, and cannot include functions or
	 * attributes, for instance /path/to/node. The exception is that wildcard matching is allowed for element indexes,
	 * (which is implied for elements with no index ascribed). For instance: /path/to[*]/node would match all "to" sub
	 * elements within "path", however /path/to[1]/node would only match the first one. Leaving off [*] is acceptable,
	 * as this is implied if there is no index given.
	 * @param callback The callback to run when an element is matched
	 */
	public void addListener(String xpath, ElementCallback callback) {
		if(xpath.contains(" ")) {
			throw new IllegalArgumentException("The xpath may not contain spaces");
		}
		if(xpath.startsWith("//") || !xpath.startsWith("/")) {
			throw new IllegalArgumentException("The xpath must be absolute, meaning it must start with exactly 1 forward slash");
		}
		//Standardize our xpath
		xpath = standardizeXpath(xpath);
		if(!callbacks.containsKey(xpath)) {
			callbacks.put(xpath, new ArrayList<>());
		}
		callbacks.get(xpath).add(callback);
	}

	private String standardizeXpath(String xpath) {
		String[] parts = xpath.substring(1).split("/");
		StringBuilder b = new StringBuilder();
		for(String part : parts) {
			if(!part.matches(".*\\[(?:\\d+|\\*)\\]$")) {
				//Add the identifier. Implied *
				part += "[*]";
			}
			b.append("/").append(part);
		}
		xpath = "^" + b.toString().replace("[*]", "[\\d+]").replace("[", "\\[").replace("]", "\\]") + "$";
		return xpath;
	}

	public static interface ElementCallback {

		/**
		 * Called when a matched element is fully read in.
		 *
		 * @param xpath The XPath of this element. This will be a canonical reference to the element, so will not
		 * necessarily match the XPath you passed in to tag this element with.
		 * @param tag The element name
		 * @param attr The attributes on the element
		 * @param contents The contents of the element
		 */
		void handleElement(String xpath, String tag, Map<String, String> attr, String contents);
	}
}
