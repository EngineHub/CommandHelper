package com.laytonsmith.PureUtilities;

import com.laytonsmith.PureUtilities.Common.StringUtils;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;

/**
 *
 *
 */
public class StringUtilsTest {

	public StringUtilsTest() {
	}
	Set<String> set3;
	List<String> list3;
	String[] array3;
	Map<String, String> map3;
	Set<String> set2;
	List<String> list2;
	String[] array2;
	Map<String, String> map2;
	Set<String> set1;
	List<String> list1;
	String[] array1;
	Map<String, String> map1;
	Set<String> set0;
	List<String> list0;
	String[] array0;
	Map<String, String> map0;

	@Before
	public void setUp() {
		set3 = new LinkedHashSet<String>();
		set3.add("1");
		set3.add("2");
		set3.add("3");
		list3 = new ArrayList<String>();
		list3.add("1");
		list3.add("2");
		list3.add("3");
		array3 = new String[]{"1", "2", "3"};
		map3 = new LinkedHashMap<String, String>();
		map3.put("one", "1");
		map3.put("two", "2");
		map3.put("three", "3");

		set2 = new LinkedHashSet<String>();
		set2.add("1");
		set2.add("2");
		list2 = new ArrayList<String>();
		list2.add("1");
		list2.add("2");
		array2 = new String[]{"1", "2"};
		map2 = new LinkedHashMap<String, String>();
		map2.put("one", "1");
		map2.put("two", "2");

		set1 = new LinkedHashSet<String>();
		set1.add("1");
		list1 = new ArrayList<String>();
		list1.add("1");
		array1 = new String[]{"1"};
		map1 = new LinkedHashMap<String, String>();
		map1.put("one", "1");

		set0 = new LinkedHashSet<String>();
		list0 = new ArrayList<String>();
		array0 = new String[]{};
		map0 = new LinkedHashMap<String, String>();

	}

	/**
	 * Test of Join method, of class StringUtils.
	 */
	@Test
	public void testJoin_Set() {
		assertEquals("1, 2, 3", StringUtils.Join(set3, ", "));
		assertEquals("1, 2, and 3", StringUtils.Join(set3, ", ", ", and "));
		assertEquals("1 and 2", StringUtils.Join(set2, ", ", ", and ", " and "));
		assertEquals("1", StringUtils.Join(set1, ", ", ", and ", " and "));
		assertEquals("", StringUtils.Join(set0, ", ", ", and ", " and ", ""));
		assertEquals("test", StringUtils.Join(set0, ", ", ", and ", " and ", "test"));
	}

	/**
	 * Test of Join method, of class StringUtils.
	 */
	@Test
	public void testJoin_Array() {
		assertEquals("1, 2, 3", StringUtils.Join(array3, ", "));
		assertEquals("1, 2, and 3", StringUtils.Join(array3, ", ", ", and "));
		assertEquals("1 and 2", StringUtils.Join(array2, ", ", ", and ", " and "));
		assertEquals("1", StringUtils.Join(array1, ", ", ", and ", " and "));
		assertEquals("", StringUtils.Join(array0, ", ", ", and ", " and ", ""));
		assertEquals("test", StringUtils.Join(array0, ", ", ", and ", " and ", "test"));
	}

	/**
	 * Test of Join method, of class StringUtils.
	 */
	@Test
	public void testJoin_List() {
		assertEquals("1, 2, 3", StringUtils.Join(list3, ", "));
		assertEquals("1, 2, and 3", StringUtils.Join(list3, ", ", ", and "));
		assertEquals("1 and 2", StringUtils.Join(list2, ", ", ", and ", " and "));
		assertEquals("1", StringUtils.Join(list1, ", ", ", and ", " and "));
		assertEquals("", StringUtils.Join(list0, ", ", ", and ", " and ", ""));
		assertEquals("test", StringUtils.Join(list0, ", ", ", and ", " and ", "test"));
	}

	@Test
	public void testJoin_Map() {
		assertEquals("one=1, two=2, three=3", StringUtils.Join(map3, "=", ", "));
		assertEquals("one=1, two=2, and three=3", StringUtils.Join(map3, "=", ", ", ", and "));
		assertEquals("one=1 and two=2", StringUtils.Join(map2, "=", ", ", ", and ", " and "));
		assertEquals("one=1", StringUtils.Join(map1, "=", ", ", ", and ", " and "));
		assertEquals("", StringUtils.Join(map0, "=", ", ", ", and ", " and ", ""));
		assertEquals("test", StringUtils.Join(map0, "=", ", ", ", and ", " and ", "test"));
	}

	/**
	 * Test of LevenshteinDistance method, of class StringUtils.
	 */
	@Test
	public void testLevenshteinDistance() {
		assertEquals(1, StringUtils.LevenshteinDistance("123", "133"));
		assertEquals(0, StringUtils.LevenshteinDistance("123", "123"));
	}

	/**
	 * Test of ArgParser method, of class StringUtils.
	 */
	@Test
	public void testArgParser() {
		String test = "this is \"a 'quoted'\" '\\'string\\''";
		String[] expected = new String[]{"this", "is", "a 'quoted'", "'string'"};
		assertArrayEquals(expected, StringUtils.ArgParser(test).toArray());
	}

	/**
	 * Test of trimLeft method, of class StringUtils.
	 */
	@Test
	public void testTrimLeft() {
		assertEquals("trim   ", StringUtils.trimLeft("   trim   "));
	}

	/**
	 * Test of trimRight method, of class StringUtils.
	 */
	@Test
	public void testTrimRight() {
		assertEquals("   trim", StringUtils.trimRight("   trim   "));
	}

	/**
	 * Test of trimSplit method, of class StringUtils.
	 */
	@Test
	public void testTrimSplit() {
		String[] expected = new String[]{"1", "2", "3"};
		assertArrayEquals(expected, StringUtils.trimSplit("1 , 2 , 3", ","));
	}

	/**
	 * Test of replaceLast method, of class StringUtils.
	 */
	@Test
	public void testReplaceLast() {
		assertEquals("123456", StringUtils.replaceLast("123123", "123", "456"));
	}

	@Test
	public void testPluralHelper1() {
		assertEquals("There is 1 car", "There " + StringUtils.PluralHelper(1, "car"));
		assertEquals("There are 2 cars", "There " + StringUtils.PluralHelper(2, "car"));
	}

	@Test
	public void testPluralHelper2() {
		assertEquals("There is 1 fish", "There " + StringUtils.PluralHelper(1, "fish", "fish"));
		assertEquals("There are 2 fish", "There " + StringUtils.PluralHelper(2, "fish", "fish"));
	}

	@Test
	public void testPluralTemplateHelper() {
		assertEquals("There is 1 fish, and it is red.",
				StringUtils.PluralTemplateHelper(1, "There is %d fish, and it is red.", "There are %d fish, and they are red."));
		assertEquals("There are 4 fish, and they are red.",
				StringUtils.PluralTemplateHelper(4, "There is %d fish, and it is red.", "There are %d fish, and they are red."));
	}

}
