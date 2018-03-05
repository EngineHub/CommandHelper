package com.laytonsmith.PureUtilities;

import java.util.HashMap;
import java.util.Map;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;

/**
 *
 *
 */
public class SmartCommentTest {

	public SmartCommentTest() {
	}

	@Before
	public void setUp() {

	}

	@Test
	public void testSimple() {
		SmartComment c = new SmartComment(
				"/**\n"
				+ " * This is a comment\n"
				+ " */");
		assertEquals("This is a comment", c.getBody());
	}

	@Test
	public void testSimpleFormatting() {
		SmartComment c = new SmartComment(
				"/**\n"
				+ " * This is a comment\n"
				+ " * with a  newline \n"
				+ " */");
		assertEquals("This is a comment\nwith a  newline", c.getBody());
	}

	@Test
	public void testGetAnnotations() {
		SmartComment c = new SmartComment(
				"/**\n"
				+ " * This is a comment\n"
				+ " * with a  newline \n"
				+ " * @param one\n"
				+ " * @param two\n"
				+ " */");
		assertTrue(c.getAnnotations("param").contains("one"));
		assertTrue(c.getAnnotations("param").contains("two"));
		assertTrue(c.getAnnotations("param").size() == 2);
	}

	@Test
	public void testEmbeddedAnnotations() {
		Map<String, SmartComment.Replacement> reps = new HashMap<String, SmartComment.Replacement>();
		reps.put("code", new SmartComment.Replacement() {

			@Override
			public String replace(String data) {
				return "<code>" + data + "</code>";
			}
		});
		SmartComment c = new SmartComment(
				"/**\n"
				+ " * {@code code}\n"
				+ " * {@unknown text}\n"
				+ " * @param one\n"
				+ " * @param two\n"
				+ " */", reps);
		assertTrue(c.getAnnotations("param").contains("one"));
		assertTrue(c.getAnnotations("param").contains("two"));
		assertTrue(c.getAnnotations("param").size() == 2);
		assertEquals("<code>code</code>\ntext", c.getBody());
	}

	@Test
	public void testEmbeddedAnnotationsInAnnotations() {
		Map<String, SmartComment.Replacement> reps = new HashMap<String, SmartComment.Replacement>();
		reps.put("code", new SmartComment.Replacement() {

			@Override
			public String replace(String data) {
				return "<code>" + data + "</code>";
			}
		});
		SmartComment c = new SmartComment(
				"/**\n"
				+ " * {@code code}\n"
				+ " * {@unknown text}\n"
				+ " * @param one {@code code}\n"
				+ " * @param two\n"
				+ " */", reps);
		assertTrue(c.getAnnotations("param").contains("one <code>code</code>"));
		assertTrue(c.getAnnotations("param").contains("two"));
		assertTrue(c.getAnnotations("param").size() == 2);
		assertEquals("<code>code</code>\ntext", c.getBody());
	}

}
