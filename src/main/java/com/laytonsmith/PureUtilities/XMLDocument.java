package com.laytonsmith.PureUtilities;

import com.laytonsmith.PureUtilities.Common.ArrayUtils;
import com.laytonsmith.PureUtilities.Common.StringUtils;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import javax.xml.xpath.XPathFactoryConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * This class abstracts up and simplifies XML document parsing. You give it an XML string, and it gives you the ability
 * to manipulate and query the document. This works via a DOM implementation.
 *
 */
public class XMLDocument {

	private DocumentBuilder docBuilder;
	private Document doc;
	private XPath xpath;
	private boolean uglyDirty = true;
	private boolean prettyDirty = true;
	private String uglyRender;
	private String prettyRender;

	/**
	 * Creates a new, blank XMLDocument.
	 */
	public XMLDocument() {
		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			dbf.setNamespaceAware(false);
			docBuilder = dbf.newDocumentBuilder();
			doc = docBuilder.newDocument();
			XPathFactory xpf = XPathFactory.newInstance(XPathFactory.DEFAULT_OBJECT_MODEL_URI,
					"com.sun.org.apache.xpath.internal.jaxp.XPathFactoryImpl", XMLDocument.class.getClassLoader());
			xpath = xpf.newXPath();
		} catch (ParserConfigurationException | XPathFactoryConfigurationException ex) {
			throw new RuntimeException(ex);
		}
	}

	/**
	 * Given an XML document in a string, creates a new XMLDocument.
	 *
	 * @param document
	 * @throws IOException If any IO error occurs
	 */
	public XMLDocument(String document, String encoding) throws UnsupportedEncodingException, SAXException {
		this();
		try {
			doc = docBuilder.parse(new ByteArrayInputStream(document.getBytes(encoding)));
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}

	/**
	 * Creates a new XMLDocument from an XML string, assuming UTF-8 encoding.
	 *
	 * @param document
	 * @throws SAXException
	 */
	public XMLDocument(String document) throws SAXException {
		this();
		try {
			doc = docBuilder.parse(new ByteArrayInputStream(document.getBytes("UTF-8")));
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}

	/**
	 * Creates a new XMLDocument from an InputStream that represents an XML document.
	 *
	 * @param in
	 * @throws SAXException
	 * @throws IOException
	 */
	public XMLDocument(InputStream in) throws SAXException, IOException {
		this();
		doc = docBuilder.parse(in);
	}

	/**
	 * Returns an xpath expression from a given xpath string
	 *
	 * @param xpath
	 * @return
	 * @throws XPathExpressionException
	 */
	private XPathExpression getXPath(String xpath) throws XPathExpressionException {
		return this.xpath.compile(xpath);
	}

	/**
	 * Sets the text value of a node, creating nodes as needed. If a node already exists and has content, the content is
	 * replaced. All XPath expressions are considered absolute, even if they don't start with a '/'.
	 *
	 * @param xpath
	 * @param value
	 * @throws XPathExpressionException
	 */
	public void setNode(String xpath, Object value) throws XPathExpressionException {
		String sval = "";
		if(value != null) {
			sval = value.toString();
		}
		getXPath(xpath); //Verifies this is a generally valid xpath, so we can roll with that assumption
		while(xpath.startsWith("/")) {
			xpath = xpath.substring(1);
		}
		String[] xpathParts = xpath.split("/");
		int count = xpathParts.length;
		while(count > 0) {
			String newXPath = "/" + StringUtils.Join(ArrayUtils.slice(xpathParts, 0, count - 1), "/");
			if(!nodeExists(newXPath)) {
				count--;
			} else {
				break;
			}
		}
		if(count == xpathParts.length) {
			//We're at the node already, so just set it and bail
			getElement(xpath).setTextContent(sval);
			setDirty();
			return;
		}
		//Ok, count now points to the topmost actually existing node, so we need to go down each part and
		//create nodes as we go
		Element parent = null;
		Element newNode = null;
		do {
			String part = xpathParts[count];
			String nodeName = getNodeName(part);
			if(count > 0) {
				parent = getElement("/" + StringUtils.Join(ArrayUtils.slice(xpathParts, 0, count - 1), "/"));
			}
			if(nodeName == null) {
				//This is an attribute, edit the node above us
				parent.setAttribute(getAttributeName(part), sval);
				setDirty();
				return; //Go ahead and bail
			} else {
				int position = getNodeIndex(part);
				if(count == 0 && position != -1) {
					throw new XPathExpressionException("The root node cannot have multiple instances.");
				}
				newNode = doc.createElement(nodeName);
				if(position == -1) {
					if(count == 0) {
						//Special case, we need to create a new element and put it in the root
						doc.appendChild(newNode);
					} else {
						parent.appendChild(newNode);
					}
				} else {
					//It's an array
					if(!(countNodeChildren(parent) + 1 >= position)) {
						//If /root/node[1] exists, but they try to create /root/node[3], this exception is thrown
						throw new XPathExpressionException("Will not tolerate a jump in node numbers, will only create the next node in sequence.");
					}
					parent.appendChild(newNode);
				}
			}
			count++;
		} while(count < xpathParts.length);
		newNode.setTextContent(sval);
		setDirty();
	}

	private int countNodeChildren(Element e) {
		Node child = e.getFirstChild();
		if(child == null) {
			return 0;
		}
		int counter = 1;
		while((child = child.getNextSibling()) != null) {
			counter++;
		}
		return counter;
	}

	/**
	 * Returns the node name, or null if this is an attribute.
	 *
	 * @param node
	 * @return
	 */
	private static String getNodeName(String node) {
		if(node.startsWith("@")) {
			return null;
		}
		int firstBracket = node.indexOf("[");
		if(firstBracket != -1) {
			return node.substring(0, firstBracket).trim();
		} else {
			return node.trim();
		}
	}

	/**
	 * Gets the position of the node, for instance, node[1] would return 1. If no node position is specified, -1 is
	 * returned.
	 *
	 * @param node
	 * @return
	 */
	private static int getNodeIndex(String node) {
		int indexFirst = node.indexOf("[");
		int indexLast = node.indexOf("]");
		if(indexFirst == -1) {
			return -1;
		} else {
			return Integer.parseInt(node.substring(indexFirst + 1, indexLast).trim());
		}
	}

	/**
	 * Returns the attribute name, or null if this is not an attribute.
	 *
	 * @param node
	 * @return
	 */
	private static String getAttributeName(String node) {
		if(node.trim().startsWith("@")) {
			return node.trim().substring(1);
		} else {
			return null;
		}
	}

	/**
	 * Returns the text value at a particular node. All XPath expressions are considered absolute, even if they don't
	 * start with a '/'
	 *
	 * @param xpath
	 * @return
	 * @throws XPathExpressionException
	 */
	public String getNode(String xpath) throws XPathExpressionException {
		return getXPath(xpath).evaluate(doc);
	}

	/**
	 * Shorthand for Boolean.parseBoolean(getNode(xpath))
	 *
	 * @param xpath
	 * @return
	 * @throws XPathExpressionException
	 */
	public boolean getBoolean(String xpath) throws XPathExpressionException {
		return Boolean.parseBoolean(getNode(xpath));
	}

	/**
	 * Shorthand for Integer.parseInt(getNode(xpath))
	 *
	 * @param xpath
	 * @return
	 * @throws XPathExpressionException
	 */
	public int getInt(String xpath) throws XPathExpressionException {
		return Integer.parseInt(getNode(xpath));
	}

	/**
	 * Shorthand for Long.parseLong(getNode(xpath))
	 *
	 * @param xpath
	 * @return
	 * @throws XPathExpressionException
	 */
	public long getLong(String xpath) throws XPathExpressionException {
		return Long.parseLong(getNode(xpath));
	}

	/**
	 * Shorthand for Double.parseDouble(getNode(xpath))
	 *
	 * @param xpath
	 * @return
	 * @throws XPathExpressionException
	 */
	public double getDouble(String xpath) throws XPathExpressionException {
		return Double.parseDouble(getNode(xpath));
	}

	/**
	 * Checks to see if a node exists or not.
	 *
	 * @param xpath
	 * @return
	 * @throws XPathExpressionException
	 */
	public boolean nodeExists(String xpath) throws XPathExpressionException {
		Object o = getXPath(xpath).evaluate(doc, XPathConstants.NODE);
		return o != null;
	}

	private Element getElement(String xpath) throws XPathExpressionException {
		return (Element) getXPath(xpath).evaluate(doc, XPathConstants.NODE);
	}

	/**
	 * Counts the number of direct descendants of this node.
	 *
	 * @param xpath
	 * @return
	 */
	public int countChildren(String xpath) throws XPathExpressionException {
		Element e = getElement(xpath);
		return e.getChildNodes().getLength();
	}

	/**
	 * Counts the number of elements that exist at this level. For instance, if the xml were:
	 * <pre>
	 * &lt;xmlroot&gt;
	 *	&lt;elem /&gt;
	 *	&lt;elem /&gt;
	 * &lt;/xmlroot&gt;
	 * </pre> And the xpath were <code>/xmlroot/elem</code>, then this would return 2.
	 *
	 * @param xpath
	 * @return
	 * @throws XPathExpressionException
	 */
	public int countNodes(String xpath) throws XPathExpressionException {
		return ((Number) (getXPath("count(" + xpath + ")").evaluate(doc, XPathConstants.NUMBER))).intValue();
	}

	/**
	 * Returns a list of all the child element names at the specified location. For instance, in
	 * <pre>
	 * &lt;root&gt;
	 *  &lt;elem /&gt;
	 *  &lt;elem /&gt;
	 *  &lt;elem2 /&gt;
	 * &lt;/root&gt;
	 * </pre> The list for "/root" would contain [elem, elem, elem2]. This is useful for examining undefined or variable
	 * xml elements. If this is a text node or has no children, an empty list is returned. The elements will be listed
	 * in the order they are defined in the xml.
	 *
	 * @param xpath
	 * @return
	 * @throws XPathExpressionException
	 */
	public List<String> getChildren(String xpath) throws XPathExpressionException {
		List<String> list = new ArrayList<String>();
		NodeList o = (NodeList) getXPath(xpath + "/child::*").evaluate(doc, XPathConstants.NODESET);
		for(int i = 0; i < o.getLength(); i++) {
			Node n = o.item(i);
			list.add(n.getNodeName());
		}
		return list;
	}

	/**
	 * Signals to the getXML function that the cache is no longer valid.
	 */
	private void setDirty() {
		uglyDirty = true;
		prettyDirty = true;
	}

	/**
	 * Equivalent to getXML(false);
	 *
	 * @return
	 */
	public String getXML() {
		return getXML(false);
	}

	/**
	 * Renders the XML as it currently stands. If pretty is true, it is formatted with indentation, otherwise, no
	 * indentation is used.
	 *
	 * @param pretty
	 * @return
	 */
	public String getXML(boolean pretty) {
		if(uglyDirty || prettyDirty) {
			try {
				Transformer transformer = TransformerFactory.newInstance().newTransformer();
				DOMSource source = new DOMSource(doc);
				StringWriter writer = new StringWriter();
				StreamResult result = new StreamResult(writer);
				if(pretty) {
					transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
					transformer.setOutputProperty(OutputKeys.INDENT, "yes");
				}
				transformer.transform(source, result);
				if(pretty) {
					prettyRender = writer.toString();
					prettyDirty = false;
				} else {
					uglyRender = writer.toString();
					uglyDirty = false;
				}
			} catch (Exception ex) {
				throw new RuntimeException(ex);
			}
		}
		if(pretty) {
			return prettyRender;
		} else {
			return uglyRender;
		}
	}

	@Override
	public String toString() {
		return getXML(true);
	}

}
