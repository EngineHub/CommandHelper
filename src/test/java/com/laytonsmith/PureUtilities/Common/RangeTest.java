package com.laytonsmith.PureUtilities.Common;

import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 */
public class RangeTest {

	public RangeTest() {
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
	public void testRangeSize() {
		assertEquals(4, new Range(1, 4, true, true).getRange().size());
		assertEquals(3, new Range(1, 4, false, true).getRange().size());
		assertEquals(3, new Range(1, 4, true, false).getRange().size());
		assertEquals(2, new Range(1, 4, false, false).getRange().size());
	}

	@Test
	public void testRangeValuesAscending() {
		assertArrayEquals(new Object[]{1, 2, 3, 4}, new Range(1, 4, true, true).getRange().toArray());
		assertArrayEquals(new Object[]{2, 3, 4}, new Range(1, 4, false, true).getRange().toArray());
		assertArrayEquals(new Object[]{1, 2, 3}, new Range(1, 4, true, false).getRange().toArray());
		assertArrayEquals(new Object[]{2, 3}, new Range(1, 4, false, false).getRange().toArray());
	}

	@Test
	public void testRangeValuesDecending() {
		assertArrayEquals(new Object[]{4, 3, 2, 1}, new Range(4, 1, true, true).getRange().toArray());
		assertArrayEquals(new Object[]{3, 2, 1}, new Range(4, 1, false, true).getRange().toArray());
		assertArrayEquals(new Object[]{4, 3, 2}, new Range(4, 1, true, false).getRange().toArray());
		assertArrayEquals(new Object[]{3, 2}, new Range(4, 1, false, false).getRange().toArray());
	}

	@Test
	public void testMin() {
		assertEquals(1, new Range(1, 4, true, true).getMin());
		assertEquals(2, new Range(1, 4, false, true).getMin());
		assertEquals(1, new Range(4, 1, true, true).getMin());
		assertEquals(2, new Range(4, 1, true, false).getMin());
	}

	@Test
	public void testMax() {
		assertEquals(4, new Range(1, 4, true, true).getMax());
		assertEquals(3, new Range(1, 4, true, false).getMax());
		assertEquals(4, new Range(4, 1, true, true).getMax());
		assertEquals(3, new Range(4, 1, false, true).getMax());
	}

	@Test
	public void testContains() {
		Range r1 = new Range(1, 4);
		Range r2 = new Range(1, 4, false, false);
		assertTrue(r1.contains(1));
		assertFalse(r2.contains(1));
		assertTrue(r1.contains(4));
		assertFalse(r2.contains(4));
		assertFalse(r1.contains(0));
		assertFalse(r2.contains(0));
		assertFalse(r1.contains(5));
		assertFalse(r2.contains(5));
	}

}
