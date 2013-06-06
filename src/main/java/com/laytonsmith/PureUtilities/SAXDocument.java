
package com.laytonsmith.PureUtilities;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.xpath.XPath;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Works similarly to {@link XMLDocument}, however, this uses a SAX based
 * parser, which is useful in cases where you are reading (not writing) data 
 * from a large XML document, or streaming document. It uses XPath expressions
 * to identify segments of a document that are of interest, and triggers once the
 * element is fully read in. This should be mostly possible to replace SAX parsers
 * in all cases.
 */
public class SAXDocument {
	
	private final InputStream stream;
	private final Map<String, List<ElementCallback>> callbacks = new HashMap<String, List<ElementCallback>>();
	
	/**
	 * Creates a new SAXDocument, based on the input stream provided.
	 * @param stream 
	 */
	public SAXDocument(InputStream stream){
		this.stream = stream;
	}
	
	/**
	 * Creates a new SAXDocument. If you already have the document in memory
	 * though, you may consider using {@link XMLDocument} instead, since you've
	 * obviously already got the whole document in memory.
	 * @param document The XML document in a string
	 * @param encoding The encoding of the stream. If null, UTF-8 is used.
	 */
	public SAXDocument(String document, String encoding) throws UnsupportedEncodingException{
		this(new ByteArrayInputStream(document.getBytes(encoding==null?"UTF-8":encoding)));
	}
	
	/**
	 * Parses the XML document. As elements are loaded, if they match, they are
	 * sent to the listeners that match the element.
	 * @throws SAXException
	 * @throws IOException 
	 */
	public void parse() throws SAXException, IOException{
		SAXParserFactory factory = SAXParserFactory.newInstance();
		SAXParser saxParser;
		try {
			saxParser = factory.newSAXParser();
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
		saxParser.parse(stream, new DefaultHandler(){
			Stack<String> nodeNames = new Stack<String>();
			Stack<Map<String, AtomicInteger>> nodeCount = new Stack<Map<String, AtomicInteger>>();
			String lastElement = "";
			int depth = 0;

			@Override
			public void startDocument() throws SAXException {
				nodeCount.push(new HashMap<String, AtomicInteger>());
			}
			
			@Override
			public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
				depth++;
				nodeNames.push(qName);
				Map<String, AtomicInteger> c = nodeCount.peek();
				if(!c.containsKey(qName)){
					c.put(qName, new AtomicInteger(1));
				} else {
					c.get(qName).incrementAndGet();
				}
				Map<String, AtomicInteger> counts = new HashMap<String, AtomicInteger>();
				nodeCount.push(counts);
				String path = pathFromMarkers(nodeNames, nodeCount);
				
			}

			@Override
			public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {
				String s = fromChars(ch, start, length);
				super.ignorableWhitespace(ch, start, length);
			}

			@Override
			public void characters(char[] ch, int start, int length) throws SAXException {
				String s = fromChars(ch, start, length);
				super.characters(ch, start, length);
			}

			@Override
			public void endElement(String uri, String localName, String qName) throws SAXException {
				String path = pathFromMarkers(nodeNames, nodeCount);
				if(hasListener(path)){
					notifyListeners(path, null, null, null);
				}
				
				depth--;
				nodeNames.pop();
				nodeCount.pop();
			}
			
		});
	}
	
	private String pathFromMarkers(Stack<String> elementStack, Stack<Map<String, AtomicInteger>> nodeCounts){
		List<String> elementList = new ArrayList<String>(elementStack);
		List<Map<String, AtomicInteger>> nodeList = new ArrayList<Map<String, AtomicInteger>>(nodeCounts);
		StringBuilder b = new StringBuilder();
		for(int i = 0; i < elementList.size(); i++){
			b.append("/")
					.append(elementList.get(i))
					.append("[")
					.append(nodeList.get(i).get(elementList.get(i)).get())
					.append("]");
		}
		return b.toString();
	}
	
	private String fromChars(char[] ch, int start, int length){
		StringBuilder b = new StringBuilder();
		for(int i = start; i < start + length; i++){
			b.append(ch[i]);
		}
		return b.toString();
	}
	
	private void notifyListeners(String xpath, String tag, Map<String, String> attr, String contents){
		for(String key : callbacks.keySet()){
			if(xpath.matches(key)){
				for(ElementCallback c : callbacks.get(key)){
					c.handleElement(xpath, tag, attr, contents);
				}
			}
		}
	}
	
	private boolean hasListener(String xpath){
		for(String key : callbacks.keySet()){
			if(xpath.matches(key)){
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Adds a new listener. When you call parse(), any elements that match
	 * these listeners are triggered. Any number of listeners can be added per
	 * xpath.
	 * @param xpath The XPath to match against. This can only be a simple XPath,
	 * and cannot include functions or attributes, for instance /path/to/node. The exception is that
	 * wildcard matching is allowed for element indexes, (which is implied for elements with
	 * no index ascribed). For instance: /path/to[*]/node would match all "to" sub elements within
	 * "path", however /path/to[1]/node would only match the first one. Leaving off [*] is acceptable,
	 * as this is implied if there is no index given.
	 * @param callback The callback to run when an element is matched
	 */
	public void addListener(String xpath, ElementCallback callback){
		if(xpath.contains(" ")){
			throw new IllegalArgumentException("The xpath may not contain spaces");
		}
		if(xpath.startsWith("//") || !xpath.startsWith("/")){
			throw new IllegalArgumentException("The xpath must be absolute, meaning it must start with exactly 1 forward slash");
		}
		//Standardize our xpath
		String [] parts = xpath.substring(1).split("/");
		StringBuilder b = new StringBuilder();
		for(int i = 0; i < parts.length; i++){
			String part = parts[i];
			if(!part.matches(".*\\[(?:\\d+|\\*)\\]$")){
				//Add the identifier. Implied *
				part += "[*]";
			}
			b.append("/").append(part);
		}
		xpath = "^" + b.toString().replace("[*]", "[\\d+]").replace("[", "\\[").replace("]", "\\]") + "$";
		if(!callbacks.containsKey(xpath)){
			callbacks.put(xpath, new ArrayList<ElementCallback>());
		}
		callbacks.get(xpath).add(callback);
	}

	
	public static interface ElementCallback {
		/**
		 * Called when a matched element is fully read in.
		 * @param path The XPath of this element. This will be a canonical reference to the element,
		 * so will not necessarily match the XPath you passed in to tag this element with.
		 * @param tag The element name
		 * @param attr The attributes on the element
		 * @param contents The contents of the element
		 */
		void handleElement(String xpath, String tag, Map<String, String> attr, String contents);
	}
}
