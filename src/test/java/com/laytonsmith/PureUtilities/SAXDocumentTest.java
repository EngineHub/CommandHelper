package com.laytonsmith.PureUtilities;

import com.laytonsmith.PureUtilities.Common.MutableObject;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 */
public class SAXDocumentTest {

	static String testDoc = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>"
			+ "<root>"
			+ "<node1 attribute=\"value\">Text</node1>"
			+ "<nodes>"
			+ "<inode attribute=\"1\">value</inode>"
			+ "<!-- This is 2 ^ 33 -->"
			+ "<inode attribute=\"1.5\">8589934592</inode>"
			+ "<inode>true</inode>"
			+ "</nodes>"
			+ "<outer><inner attr=\"&quot;attr&quot;\">text</inner></outer>"
			+ "<selfclosed attribute=\"val\" />"
			+ "</root>";
	SAXDocument doc;

	public SAXDocumentTest() throws Exception {
		doc = new SAXDocument(testDoc, "UTF-8");
	}

	@BeforeClass
	public static void setUpClass() {
	}

	@AfterClass
	public static void tearDownClass() {
	}

	@Before
	public void setUp() {
	}

	@After
	public void tearDown() {
	}

	@Test
	public void testBasic() throws Exception {
		final AtomicInteger i = new AtomicInteger(0);
		doc.addListener("/root/nodes/inode", new SAXDocument.ElementCallback() {

			@Override
			public void handleElement(String xpath, String tag, Map<String, String> attr, String contents) {
				i.incrementAndGet();
			}
		});
		doc.parse();
		assertEquals(3, i.get());
	}

	@Test
	public void testIndexWorks() throws Exception {
		final AtomicInteger i = new AtomicInteger(0);
		doc.addListener("/root/nodes/inode[1]", new SAXDocument.ElementCallback() {

			@Override
			public void handleElement(String xpath, String tag, Map<String, String> attr, String contents) {
				i.incrementAndGet();
			}
		});
		doc.parse();
		assertEquals(1, i.get());
	}

	@Test
	public void testSimpleContents() throws Exception {
		final MutableObject m = new MutableObject();
		doc.addListener("/root/nodes/inode[1]", new SAXDocument.ElementCallback() {

			@Override
			public void handleElement(String xpath, String tag, Map<String, String> attr, String contents) {
				m.setObject(contents);
			}
		});
		doc.parse();
		assertEquals("value", m.getObject());
	}

	@Test
	public void testComplexContents() throws Exception {
		final MutableObject m = new MutableObject();
		doc.addListener("/root/outer", new SAXDocument.ElementCallback() {

			@Override
			public void handleElement(String xpath, String tag, Map<String, String> attr, String contents) {
				m.setObject(contents);
			}
		});
		doc.parse();
		assertEquals("<inner attr=\"&quot;attr&quot;\">text</inner>", m.getObject());
	}

	@Test
	public void testAttributes() throws Exception {
		final MutableObject m = new MutableObject();
		doc.addListener("/root/node1", new SAXDocument.ElementCallback() {

			@Override
			public void handleElement(String xpath, String tag, Map<String, String> attr, String contents) {
				m.setObject(attr.get("attribute"));
			}
		});
		doc.parse();
		assertEquals("value", m.getObject());
	}
}
