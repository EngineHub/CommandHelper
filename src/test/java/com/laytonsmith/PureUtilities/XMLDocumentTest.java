package com.laytonsmith.PureUtilities;

import javax.xml.xpath.XPathExpressionException;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.xml.sax.SAXException;

/**
 *
 *
 */
public class XMLDocumentTest {

	XMLDocument doc;
	static String testDoc = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>"
			+ "<root>"
			+ "<node1 attribute=\"value\">Text</node1>"
			+ "<nodes>"
			+ "<inode attribute=\"1\">value</inode>"
			+ "<!-- This is 2 ^ 33 -->"
			+ "<inode attribute=\"1.5\">8589934592</inode>"
			+ "<inode>true</inode>"
			+ "</nodes>"
			+ "</root>";

	public XMLDocumentTest() {
	}

	@BeforeClass
	public static void setUpClass() {
	}

	@AfterClass
	public static void tearDownClass() {
	}

	@Before
	public void setUp() throws SAXException {
		doc = new XMLDocument(testDoc);
	}

	@After
	public void tearDown() {
	}

	@Test
	public void testNewDocument() {
		String output = new XMLDocument().getXML();
		assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>", output);
	}

	/**
	 * Test of setNode method, of class XMLDocument.
	 */
	@Test
	public void testSetNode() throws XPathExpressionException {
		doc = new XMLDocument();
		doc.setNode("/root/newNode/@attribute", "attribute");
		assertEquals("attribute", doc.getNode("/root/newNode/@attribute"));
		doc.setNode("/root/newNode", "test");
		assertEquals("test", doc.getNode("/root/newNode"));
		doc.setNode("/root/other/node[1]/inner", "value1");
		doc.setNode("/root/other/node[2]/inner", "value2");
		assertEquals("value1", doc.getNode("/root/other/node[1]/inner"));
		assertEquals("value2", doc.getNode("/root/other/node[2]/inner"));
		try {
			doc.setNode("/root/other/node[4]/node", "value");
			fail("Did not expect this to pass");
		} catch (XPathExpressionException e) {
			//Pass
		}
		assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>"
				+ "<root>"
				+ "<newNode attribute=\"attribute\">test</newNode>"
				+ "<other>"
				+ "<node><inner>value1</inner></node>"
				+ "<node><inner>value2</inner></node>"
				+ "</other>"
				+ "</root>",
				doc.getXML());
	}

	/**
	 * Test of getNode method, of class XMLDocument.
	 */
	@Test
	public void testGetNode() throws Exception {
		assertEquals("Text", doc.getNode("/root/node1"));
	}

	/**
	 * Test of getBoolean method, of class XMLDocument.
	 */
	@Test
	public void testGetBoolean() throws Exception {
		assertTrue(doc.getBoolean("/root/nodes/inode[3]"));
	}

	/**
	 * Test of getInt method, of class XMLDocument.
	 */
	@Test
	public void testGetInt() throws Exception {
		assertEquals(1, doc.getInt("/root/nodes/inode[1]/@attribute"));
	}

	/**
	 * Test of getLong method, of class XMLDocument.
	 */
	@Test
	public void testGetLong() throws Exception {
		assertEquals(8589934592L, doc.getLong("/root/nodes/inode[2]"));
	}

	/**
	 * Test of getDouble method, of class XMLDocument.
	 */
	@Test
	public void testGetDouble() throws Exception {
		assertEquals(1.5, doc.getDouble("/root/nodes/inode[2]/@attribute"), 0.001);
	}

	/**
	 * Test of nodeExists method, of class XMLDocument.
	 */
	@Test
	public void testNodeExists() throws XPathExpressionException {
		assertTrue(doc.nodeExists("/root/nodes/inode[1]"));
		assertFalse(doc.nodeExists("/does/not/exist"));
	}

	/**
	 * Test of getXML method, of class XMLDocument.
	 */
	@Test
	public void testGetXML() {
		assertEquals(testDoc, doc.getXML());
	}

	@Test
	public void testPrettyPrint() throws XPathExpressionException {
		doc = new XMLDocument();
		doc.setNode("/root/node", "value");
		assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n"
				+ "<root>\n"
				+ "    <node>value</node>\n"
				+ "</root>\n", doc.getXML(true).replace("\r\n", "\n").replace("\n\r", "\n"));
	}

	@Test
	public void testWithNamespace() throws XPathExpressionException, SAXException {
		doc = new XMLDocument("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n"
				+ "<root>\n"
				+ "    <ns:node>value</ns:node>\n"
				+ "</root>\n");
		assertEquals("value", doc.getNode("/root/node"));
	}

}
