/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.PureUtilities;

import com.laytonsmith.PureUtilities.ClassLoading.ClassDiscovery;
import org.hamcrest.core.Is;
import org.junit.Assert;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertArrayEquals;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author Cailin
 */
public class JSONUtilTest {

	public JSONUtilTest() {
	}

	private static ClassDiscovery cd;
	private static JSONUtil jd;

	@BeforeClass
	public static void setupClass() {
		cd = ClassDiscovery.getDefaultInstance();
		cd.addThisJar();
		jd = new JSONUtil();
	}

	static class FlatObject {
		public String s;
		public int i;
		public Integer I;
		public double d;
		public Double D;
		public boolean b;
		public Boolean B;
		public Object nl;
	}

	@Test
	public void testFlatObject() throws Exception {
		FlatObject fo = jd.deserialize(
				"{\"s\": \"string\","
						+ " \"i\": 1,"
						+ " \"I\": 2,"
						+ " \"d\": 3.0,"
						+ " \"D\": 4.0,"
						+ " \"b\": true,"
						+ " \"B\": false,"
						+ " \"nl\": null}",
				FlatObject.class);
		assertTrue(fo.s.equals("string"));
		assertTrue(fo.i == 1);
		assertTrue(fo.I == 2);
		assertTrue(fo.d == 3.0);
		assertTrue(fo.D == 4.0);
		assertTrue(fo.b == true);
		assertTrue(fo.B == false);
		assertTrue(fo.nl == null);
	}

	static class ArrayObject {
		public String[] s;
		public int[] i;
		public Integer[] I;
	}

	@Test
	public void testArrayObjects() throws Exception {
		ArrayObject ao = jd.deserialize(
				"{\"s\": [\"string\", \"array\"],"
						+ "\"i\": [1, 2, 3],"
						+ "\"I\": [4, 5, 6]}",
				ArrayObject.class);
		assertArrayEquals(new String[]{"string", "array"}, ao.s);
		assertArrayEquals(new int[]{1, 2, 3}, ao.i);
		assertArrayEquals(new Integer[]{4, 5, 6}, ao.I);
	}

	static class SmallFlat {
		public int i;
		public boolean b;
	}

	static class DeepObject {
		public SmallFlat s;
		public String m;
	}

	@Test
	public void testDeepObjects() throws Exception {
		DeepObject d = jd.deserialize(
				"{\"m\": \"string\","
						+ "\"s\": {"
						+ "\"i\": 1,"
						+ "\"b\": true"
						+ "}}",
				DeepObject.class);
		assertTrue(d.m.equals("string"));
		assertTrue(d.s.i == 1);
		assertTrue(d.s.b == true);
	}

	@Test
	public void testExtraValuesInJsonAreIgnored() throws Exception {
		SmallFlat sf = jd.deserialize("{\"i\": 1, \"b\": false, \"pi\": 3.14}", SmallFlat.class);
		assertTrue(sf.i == 1);
		assertTrue(sf.b == false);
	}

	@Test
	public void setNullToPrimitive() throws Exception {
		SmallFlat f = jd.deserialize("{}", SmallFlat.class);
		assertTrue(f.i == 0);
	}

	static class MultiDimensionalArrays {
		public int[][] s;
	}

	@Test
	public void test2DArray() throws Exception {
		MultiDimensionalArrays mda = jd.deserialize(
				"{\"s\": [[1, 2, 3], [4, 5, 6]]}",
				MultiDimensionalArrays.class);
		assertArrayEquals(new int[][]{new int[]{1, 2, 3}, new int[]{4, 5, 6}}, mda.s);
	}

	@Test
	public void testArrayDeserialize() throws Exception {
		Integer[] i = jd.deserializeArray("[1, 2, 3]", Integer.class);
		assertArrayEquals(new Integer[]{1, 2, 3}, i);
	}

	@Test
	public void testArrayWith2DArray() throws Exception {
		Integer[][] i = jd.deserializeArray("[[1,2,3],[4,5,6]]", Integer[].class);
		assertArrayEquals(new Integer[][]{new Integer[]{1, 2, 3}, new Integer[]{4, 5, 6}}, i);
	}

	static class TClass<M> {
		public int id;
		public M object;

		public M getObject() {
			return object;
		}

		public Class<? super String> getClassASDF(Class<?> c) {
			return null;
		}
	}

	static class ImplClass extends TClass<SubObject> {
		@SuppressWarnings("FieldNameHidesFieldInSuperclass")
		public SubObject object;
	}

	static class SubObject {
		public int value;
	}

	static class ImplClass2 extends TClass<SubObject2> {
		@SuppressWarnings("FieldNameHidesFieldInSuperclass")
		public SubObject2 object;
	}

	static class SubObject2 {
		public String value;
	}

	@Test
	public void testGenericExpansion() throws Exception {
		ImplClass s = jd.deserialize("{\"object\": {\"value\": 1}}", ImplClass.class);
		assertTrue(s.object.value == 1);
	}

	@Test
	public void testGenericExpansion2() throws Exception {
		ImplClass2 s = jd.deserialize("{\"object\": {\"value\": \"str\"}}", ImplClass2.class);
		assertTrue(s.object.value.equals("str"));
	}

	@Test
	public void testSerialize() throws Exception {
		DeepObject d = new DeepObject();
		d.m = "string";
		d.s = new SmallFlat();
		d.s.b = true;
		d.s.i = 5;
		String s = jd.serialize(d);
		Assert.assertThat(s, Is.is("{\"s\":{\"b\":true,\"i\":5},\"m\":\"string\"}"));
	}

	@Test
	public void testSerializeArray() throws Exception {
		ArrayObject a = new ArrayObject();
		a.i = new int[]{1, 2, 3};
		a.I = new Integer[]{};
		a.s = new String[]{"s", "t", "r"};
		String s = jd.serialize(a);
		Assert.assertThat(s, Is.is("{\"s\":[\"s\",\"t\",\"r\"],\"i\":[1,2,3],\"I\":[]}"));
	}

	static enum EnumTest {
		ZERO, ONE, TWO
	}

	static class EnumContainer {
		public int id;
		public EnumTest enumValue;
	}

	@Test
	public void testEnumDeserialization() throws Exception {
		EnumContainer ec = jd.deserialize("{\"id\": 55, \"enumValue\": 2}", EnumContainer.class);
		assertTrue(ec.id == 55);
		assertTrue(ec.enumValue == EnumTest.TWO);
	}

	@Test
	public void testEnumSerialization() throws Exception {
		EnumContainer c = new EnumContainer();
		c.id = 45;
		c.enumValue = EnumTest.ONE;
		String s = jd.serialize(c);
		Assert.assertThat(s, Is.is("{\"enumValue\":1,\"id\":45}"));
	}

	static enum EnumTest2 implements JSONUtil.CustomLongEnum<EnumTest2> {
		FIRST(100),
		SECOND(200),
		THIRD(300);

		private final long id;

		private EnumTest2(int id) {
			this.id = id;
		}

		@Override
		public EnumTest2 getFromValue(Long value) {
			for(EnumTest2 e : values()) {
				if(value == e.id) {
					return e;
				}
			}
			return null;
		}

		@Override
		public Long getValue() {
			return id;
		}
	}

	static class EnumContainer2 {
		public int id;
		public EnumTest2 et;
	}

	@Test
	public void testEnumDeserializationCustom() throws Exception {
		EnumContainer2 ec = jd.deserialize("{\"id\": 55, \"et\": 200}", EnumContainer2.class);
		assertTrue(ec.id == 55);
		assertTrue(ec.et == EnumTest2.SECOND);
	}

	@Test
	public void testEnumSerializationCustom() throws Exception {
		EnumContainer2 c = new EnumContainer2();
		c.id = 45;
		c.et = EnumTest2.FIRST;
		String s = jd.serialize(c);
		Assert.assertThat(s, Is.is("{\"id\":45,\"et\":100}"));
	}
}
