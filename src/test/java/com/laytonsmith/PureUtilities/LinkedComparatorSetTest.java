package com.laytonsmith.PureUtilities;

import java.util.Arrays;
import java.util.Set;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertArrayEquals;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 *
 */
public class LinkedComparatorSetTest {

	public LinkedComparatorSetTest() {
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
	public void testCreation() {
		assertArrayEquals(new Object[]{"b", "a"}, new LinkedComparatorSet<Object>(Arrays.asList(new Object[]{"b", "B", "a"}), new LinkedComparatorSet.EqualsComparator() {

			@Override
			public boolean checkIfEquals(Object val1, Object val2) {
				return val1.toString().equalsIgnoreCase(val2.toString());
			}
		}).toArray());
	}

	@Test
	public void testInsertion() {
		Object[] expected = new Object[]{"A"};
		Set<Object> set = new LinkedComparatorSet<Object>(new LinkedComparatorSet.EqualsComparator() {

			@Override
			public boolean checkIfEquals(Object val1, Object val2) {
				return val1.toString().equalsIgnoreCase(val2.toString());
			}
		});
		set.add("A");
		set.add("a");
		set.add("a");
		assertArrayEquals(expected, set.toArray());
	}
}
