package com.laytonsmith.PureUtilities;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;

/**
 *
 */
public class SimpleVersionTest {

	public SimpleVersionTest() {
	}

	@Before
	public void setUp() {

	}

	@Test
	public void testParsing() {
		SimpleVersion v = new SimpleVersion("1.2.3 beta");
		assertEquals(1, v.getMajor());
		assertEquals(2, v.getMinor());
		assertEquals(3, v.getSupplemental());
		assertEquals("beta", v.getTag());
	}

	@Test
	public void testLT() {
		assertTrue(new SimpleVersion("1.1.1").lt(new SimpleVersion("1.1.2")));
		assertTrue(new SimpleVersion("1.1.1").lt(new SimpleVersion("1.2.1")));
		assertTrue(new SimpleVersion("1.1.1").lt(new SimpleVersion("2.1.1")));
		assertTrue(new SimpleVersion("1.1.1").lt(new SimpleVersion("2.2.2")));
		assertFalse(new SimpleVersion("1.1.1").lt(new SimpleVersion("1.1.1")));
		assertFalse(new SimpleVersion("1.1.1").lt(new SimpleVersion("0.0.0")));
	}

	@Test
	public void testLTE() {
		assertTrue(new SimpleVersion("1.1.1").lte(new SimpleVersion("1.1.2")));
		assertTrue(new SimpleVersion("1.1.1").lte(new SimpleVersion("1.2.1")));
		assertTrue(new SimpleVersion("1.1.1").lte(new SimpleVersion("2.1.1")));
		assertTrue(new SimpleVersion("1.1.1").lte(new SimpleVersion("2.2.2")));
		assertTrue(new SimpleVersion("1.1.1").lte(new SimpleVersion("1.1.1")));
		assertFalse(new SimpleVersion("1.1.1").lte(new SimpleVersion("0.0.0")));
	}

	@Test
	public void testGT() {
		assertFalse(new SimpleVersion("1.1.1").gt(new SimpleVersion("1.1.2")));
		assertFalse(new SimpleVersion("1.1.1").gt(new SimpleVersion("1.2.1")));
		assertFalse(new SimpleVersion("1.1.1").gt(new SimpleVersion("2.1.1")));
		assertFalse(new SimpleVersion("1.1.1").gt(new SimpleVersion("2.2.2")));
		assertFalse(new SimpleVersion("1.1.1").gt(new SimpleVersion("1.1.1")));
	}

	@Test
	public void testGTE() {
		assertFalse(new SimpleVersion("1.1.1").gte(new SimpleVersion("1.1.2")));
		assertFalse(new SimpleVersion("1.1.1").gte(new SimpleVersion("1.2.1")));
		assertFalse(new SimpleVersion("1.1.1").gte(new SimpleVersion("2.1.1")));
		assertFalse(new SimpleVersion("1.1.1").gte(new SimpleVersion("2.2.2")));
		assertTrue(new SimpleVersion("1.1.1").gte(new SimpleVersion("1.1.1")));
	}

}
