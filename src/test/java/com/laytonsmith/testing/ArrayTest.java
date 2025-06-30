package com.laytonsmith.testing;

import com.laytonsmith.abstraction.MCPlayer;
import com.laytonsmith.core.exceptions.CRE.CREException;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import static com.laytonsmith.testing.StaticTest.SRun;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 *
 *
 */
public class ArrayTest extends AbstractIntegrationTest {

	MCPlayer fakePlayer;

	public ArrayTest() {
	}

	@Before
	public void setUp() {
		fakePlayer = StaticTest.GetOnlinePlayer();
	}

	@Test
	public void testAssociativeCreation() throws Exception {
		assertEquals("{0: 0, 1: 1}", SRun("array(0: 0, 1: 1)", fakePlayer));
	}

	@Test
	public void testAssociativeCreation2() throws Exception {
		SRun("assign(@arr, array(0, 1))"
				+ "msg(@arr)"
				+ "array_set(@arr, 2, 2)"
				+ "msg(@arr)"
				+ "array_set(@arr, 0, 0)"
				+ "msg(@arr)"
				+ "array_set(@arr, 'key', 'value')"
				+ "msg(@arr)"
				+ "array_set(@arr, 10, 'value')"
				+ "msg(@arr)",
				fakePlayer);

		verify(fakePlayer).sendMessage("{0, 1}");
		verify(fakePlayer, times(2)).sendMessage("{0, 1, 2}");
		verify(fakePlayer).sendMessage("{0: 0, 1: 1, 2: 2, key: value}");
		verify(fakePlayer).sendMessage("{0: 0, 1: 1, 10: value, 2: 2, key: value}");
	}

	@Test
	public void testArrayGetWithAssociativeArray() throws Exception {
		assertEquals("test", SRun("g(assign(@arr, array()) array_set(@arr, 'val', 'test')) array_get(@arr, 'val')", fakePlayer));
	}

	@Test(expected = ConfigRuntimeException.class)
	public void testAssociativeSlicing() throws Exception {
		SRun("array(0: 0, 1: 1, 2: 2)[1..-1]", fakePlayer);
	}

	@Test
	public void testAssociativeCopy() throws Exception {
		SRun("assign(@arr, array(0: 0, 1: 1))"
				+ "assign(@arr2, @arr[])"
				+ "array_set(@arr2, 0, 1)"
				+ "msg(@arr)"
				+ "msg(@arr2)", fakePlayer);
		verify(fakePlayer).sendMessage("{0: 0, 1: 1}");
		verify(fakePlayer).sendMessage("{0: 1, 1: 1}");
	}

	@Test
	public void testArrayKeyNormalization() throws Exception {
		assertEquals("{0: 0}", SRun("array(false: 0)", fakePlayer));
		assertEquals("{false: 0}", SRun("array('false': 0)", fakePlayer));
		assertEquals("{1: 1}", SRun("array(true: 1)", fakePlayer));
		assertEquals("{true: 1}", SRun("array('true': 1)", fakePlayer));
		assertEquals("{: empty}", SRun("array(null: empty)", fakePlayer));
		assertEquals("{null: empty}", SRun("array('null': empty)", fakePlayer));
		assertEquals("{2.3: 2.3}", SRun("array(2.3: 2.3)", fakePlayer));
		assertEquals("{0.3: 0.3}", SRun("array(.3: .3)", fakePlayer));
		assertEquals("{.3: 0.3}", SRun("array('.3': .3)", fakePlayer));
	}

	@Test
	public void testArrayKeys() throws Exception {
		assertEquals("{0, 1, potato}", SRun("array_keys(array(potato: 5, 1: 44, 0: 'i can count to'))", fakePlayer));
	}

	@Test
	public void testArrayNormalize() throws Exception {
		assertEquals("{3, 2, 1}", SRun("array_normalize(array(0: 3, 1: 2, 2: 1))", fakePlayer));
	}

	@Test
	public void testArrayPushOnAssociativeArray() throws Exception {
		SRun("assign(@arr, array(0: 0, 1: 1, potato: potato))"
				+ "msg(@arr)"
				+ "array_push(@arr, tomato)"
				+ "msg(@arr)", fakePlayer);
		verify(fakePlayer).sendMessage("{0: 0, 1: 1, potato: potato}");
		verify(fakePlayer).sendMessage("{0: 0, 1: 1, 2: tomato, potato: potato}");
	}

	@Test
	public void testPushingANegativeIndexOnArray() throws Exception {
		SRun("assign(@arr, array(0, 1, 2))"
				+ "array_set(@arr, -1, -1)"
				+ "msg(@arr[-1])", fakePlayer);
		verify(fakePlayer).sendMessage("-1");
	}

	@Test
	public void testAssociativeArraySerialization() throws Exception {
		when(fakePlayer.isOp()).thenReturn(true);
		SRun("assign(@arr, array(0: 0, 1: 1, potato: potato))"
				+ "store_value(potato, @arr)"
				+ "msg(get_value(potato))", fakePlayer);
		verify(fakePlayer).sendMessage("{0: 0, 1: 1, potato: potato}");
	}

	@Test
	public void testIsAssociative() throws Exception {
		assertEquals("true", SRun("is_associative(array(1: 1))", fakePlayer));
		assertEquals("false", SRun("is_associative(array(1))", fakePlayer));
	}

	@Test
	public void testFunctionResultAsAssociativeValue() throws Exception {
		assertEquals("{1: this was concated, 2: this was too}", SRun("array(1: this was concated, 2: this was too)", fakePlayer));
		assertEquals("{1: thiswasconcated}", SRun("array(1: concat('this', was, concated))", fakePlayer));
	}

	@Test
	public void testDocumentationExample1() throws Exception {
		SRun("assign(@arr, array('string key': 'value', 'string key 2': 'value')) msg(@arr['string key'])", fakePlayer);
		verify(fakePlayer).sendMessage("value");
	}

	@Test
	public void testDocumentationExample2() throws Exception {
		SRun("assign(@arr, array(0, 1, 2, 3)) "
				+ "msg(is_associative(@arr))"
				+ "array_set(@arr, 4, 4)"
				+ "msg(is_associative(@arr))"
				+ "array_set(@arr, 0, 0)"
				+ "msg(is_associative(@arr))"
				+ "array_push(@arr, 5)"
				+ "msg(is_associative(@arr))"
				+ "msg(@arr)"
				+ "array_set(@arr, 'key', 'value')"
				+ "msg(is_associative(@arr))",
				fakePlayer);
		verify(fakePlayer, times(4)).sendMessage("false");
		verify(fakePlayer).sendMessage("{0, 1, 2, 3, 4, 5}");
		verify(fakePlayer).sendMessage("true");
	}

	@Test
	public void testDocumentationExample3() throws Exception {
		SRun("assign(@arr, array(0, 1: 1, 2, 3: 3, a: 'a'))"
				+ "msg(array_keys(@arr))",
				fakePlayer);
		verify(fakePlayer).sendMessage("{0, 1, 2, 3, a}");
	}

	@Test
	public void testDocumentationExample4() throws Exception {
		SRun("msg(array(0: 0, 2: 2, 1))", fakePlayer);
		SRun("assign(@arr, array()) array_set(@arr, 0, 0) array_set(@arr, 2, 2) array_push(@arr, 1)", fakePlayer);
		verify(fakePlayer).sendMessage("{0: 0, 2: 2, 3: 1}");
	}

	@Test
	public void testDocumentationExample5() throws Exception {
		SRun("assign(@arr, array(0, 1: 1, 2, 3: 3, a: 'a'))\n"
				+ "foreach(array_keys(@arr), @key, #array_keys returns {0, 1, 2, 3, a}\n"
				+ "msg(@arr[@key]) #Messages the value\n"
				+ ")", fakePlayer);
		verify(fakePlayer).sendMessage("0");
		verify(fakePlayer).sendMessage("1");
		verify(fakePlayer).sendMessage("2");
		verify(fakePlayer).sendMessage("3");
		verify(fakePlayer).sendMessage("a");
	}

	@Test
	public void testDocumentationExample6() throws Exception {
		SRun("assign(@arr, array(-1: -1, 0, 1, 2: 2)) msg(@arr[-1])", fakePlayer);
		verify(fakePlayer).sendMessage("-1");
	}

	@Test
	public void testDocumentationExample7() throws Exception {
		SRun("assign(@arr, array(-1: -1, 0, 1, 2: 2)) msg(@arr)", fakePlayer);
		verify(fakePlayer).sendMessage("{-1: -1, 0: 0, 1: 1, 2: 2}");
	}

	@Test
	public void testArraysReference1() throws Exception {
		assertEquals("3", SRun("proc(_test, @array, return(array_size(@array))) _test(array(1, 2, 3))", fakePlayer));
	}

	@Test
	public void testArraysReturned() throws Exception {
		SRun("proc(_test, return(array(1, 2, 3))) foreach(_test(), @i, msg(@i))", fakePlayer);
		verify(fakePlayer).sendMessage("1");
		verify(fakePlayer).sendMessage("2");
		verify(fakePlayer).sendMessage("3");
	}

	@Test
	public void testArrayception1() throws Exception {
		SRun("assign(@t, array("
				+ "     bla: 1, "
				+ "     tro: array('a', 'b')"
				+ " )"
				+ ")"
				+ "foreach(array_keys(@t), @u,"
				+ "     msg(is_array(@t[@u]))"
				+ ")", fakePlayer);
		verify(fakePlayer).sendMessage("false");
		verify(fakePlayer).sendMessage("true");
	}

	@Test
	public void testArrayception2() throws Exception {
		when(fakePlayer.isOp()).thenReturn(Boolean.TRUE);
		SRun("assign(@t, array(bla: 1))"
				+ "array_set(@t, 'tro', array(a, b))"
				+ "foreach(array_keys(@t), @u,"
				+ " msg(is_array(@t[@u]))"
				+ ")"
				+ "store_value('bugtest_bug4', @t)  "
				+ "assign(@t, null)  "
				+ "assign(@t, get_value('bugtest_bug4'))  "
				+ "foreach(array_keys(@t), @u,    "
				+ " msg(is_array(@t[@u]))"
				+ ")", fakePlayer);
		verify(fakePlayer, times(2)).sendMessage("false");
		verify(fakePlayer, times(2)).sendMessage("true");
	}

	@Test
	public void testArrayNPE1() throws Exception {
		SRun("assign(@glyphs, array('0': 6, '1': 6,))"
				+ "msg(array_index_exists(@glyphs, '1'))", fakePlayer);
		verify(fakePlayer).sendMessage("true");
	}

	@Test
	public void testArrayKeys1() throws Exception {
		SRun("assign(@a, array('1 ': 1, ' 1 ': 3)) msg(@a)", fakePlayer);
		verify(fakePlayer).sendMessage("{ 1 : 3, 1 : 1}");
	}

	@Test
	public void testArrayAssign1() throws Exception {
		SRun("assign(@array, array())\n"
				+ "assign(@array[0], 'value')\n"
				+ "msg(@array[0])", fakePlayer);
		verify(fakePlayer).sendMessage("value");
	}

	@Test
	public void testArrayAssign2() throws Exception {
		//Essentially, we want to replicate the behavior of java
		String value = "value";
		String[] array = new String[1];
		array[0] = value;
		value = "failure";

		SRun("assign(@value, 'value')\n"
				+ "assign(@array, array())\n"
				+ "assign(@array[0], @value)\n"
				+ "assign(@value, 'failure')\n"
				+ "msg(@array[0])", fakePlayer);
		verify(fakePlayer).sendMessage(array[0]);
	}

	@Test
	public void testArrayAssign3() throws Exception {
		String value = "value";
		String[][] arrayOut = new String[1][1];
		String[] arrayIn = new String[1];
		arrayOut[0] = arrayIn;
		arrayIn[0] = value;
		value = "failure";

		SRun("assign(@value, 'value')\n"
				+ "assign(@arrayOut, array())\n"
				+ "assign(@arrayIn, array())\n"
				+ "assign(@arrayOut[0], @arrayIn)\n"
				+ "assign(@arrayIn[0], @value)\n"
				+ "msg(@arrayOut[0][0])", fakePlayer);

		verify(fakePlayer).sendMessage(arrayOut[0][0]);
	}

	@Test
	public void testArrayAssign4() throws Exception {
		SRun("assign(@array, array(outer: array(middle: array(inner: failure))))"
				+ "assign(@array['outer']['middle']['inner'], 'value')\n"
				+ "msg(@array['outer']['middle']['inner'])", fakePlayer);
		verify(fakePlayer).sendMessage("value");
	}

	@Test
	public void testArrayAssign5() throws Exception {
		SRun("assign(@array, array())\n"
				+ "assign(@array['outer']['middle']['inner'], 'value')\n"
				+ "msg(@array)\n"
				+ "msg(@array['outer']['middle']['inner'])", fakePlayer);
		verify(fakePlayer).sendMessage("{outer: {middle: {inner: value}}}");
		verify(fakePlayer).sendMessage("value");
	}

	@Test
	public void testArrayAssign6() throws Exception {
		SRun("assign(@array, array(1))\n"
				+ "assign(@array[1], 2)\n"
				+ "msg(@array)", fakePlayer);
		verify(fakePlayer).sendMessage("{1, 2}");
	}

	@Test
	public void testArrayAssign7() throws Exception {
		SRun("assign(@array, array(1: 1))\n"
				+ "assign(@array[0], array(1))\n"
				+ "assign(@array[0][1], 2)\n"
				+ "msg(@array)", fakePlayer);
		verify(fakePlayer).sendMessage("{0: {1, 2}, 1: 1}");
	}

	@Test
	public void testInnerArrayIsArray() throws Exception {
		SRun("assign(@pdata, array(value: array(1)))\n"
				+ "msg(array_size(@pdata[value]))", fakePlayer);
		verify(fakePlayer).sendMessage("1");
	}

	@Test
	public void testArrayKeysSortOrder() throws Exception {
		for(int i = 0; i < 5; i++) {
			SRun("assign(@array, array())"
					+ "array_push(@array, array(1))"
					+ "array_push(@array, array(2))"
					+ "array_push(@array, array(3))"
					+ "foreach(array_keys(@array), @key, msg(@key))", fakePlayer);
			verify(fakePlayer).sendMessage("0");
			verify(fakePlayer).sendMessage("1");
			verify(fakePlayer).sendMessage("2");
			setUp();
		}
	}

	@Test
	public void testArrayKeysSortOrderWithPersistence() throws Exception {
		for(int i = 0; i < 5; i++) {
			InOrder inOrder = inOrder(fakePlayer);
			when(fakePlayer.isOp()).thenReturn(true);
			SRun("assign(@array, array())"
					+ "array_push(@array, array(1))"
					+ "array_push(@array, array(2))"
					+ "array_push(@array, array(3))"
					+ "store_value('array', @array)"
					+ "assign(@array2, get_value('array'))"
					+ "foreach(array_keys(@array2), @key, msg(@key))", fakePlayer);
			inOrder.verify(fakePlayer).sendMessage("0");
			inOrder.verify(fakePlayer).sendMessage("1");
			inOrder.verify(fakePlayer).sendMessage("2");
			setUp();
		}
	}

	@Test
	public void testArrayForeachWithConstructorInitialization() throws Exception {
		SRun("assign(@pdata, array('some': 'thing', 'is': 67890, 'over': 'there'))"
				+ " msg(@pdata['some'])"
				+ " foreach(@pdata, @thing, msg(@thing))", fakePlayer);
		verify(fakePlayer, times(2)).sendMessage("thing");
		verify(fakePlayer).sendMessage("67890");
		verify(fakePlayer).sendMessage("there");
	}

	@Test
	public void testComplexGetter() throws Exception {
		SRun("assign(@array, array(blah: 'blarg'))"
				+ "msg(@array[to_lower('BLAH')])", fakePlayer);
		verify(fakePlayer).sendMessage("blarg");
	}

	@Test(expected = CREException.class)
	public void testArrayUsageBeforeDefined() throws Exception {
		SRun("@a[1]", fakePlayer);
	}

	@Test
	public void testDirectSquareBracketUsage() throws Exception {
		SRun("msg(array(0, 1, 2)[0])", fakePlayer);
		verify(fakePlayer).sendMessage("0");
	}

	@Test
	public void testArraySets() throws Exception {
		SRun("@arr = array()\n"
				+ "array_set(@arr,0,array())\n"
				+ "array_remove(@arr,0)\n"
				+ "array_set(@arr,0,array())", fakePlayer);
	}

	@Test
	public void testArraySetWithInternalVariable() throws Exception {
		SRun("@array = associative_array()\n"
				+ "@value = 'herp'\n"
				+ "@md5 = md5(@value)\n"
				+ "@array[md5(@value)] = 'derp'\n"
				+ "msg(string(@array) == '{'.@md5.': derp}')", fakePlayer);
		verify(fakePlayer).sendMessage("true");
	}

	@Test
	public void testArrayPushOperator() throws Exception {
		SRun("@array = array()\n"
				+ "@array[] = 'value'\n"
				+ "msg(string(@array) == '{value}')", fakePlayer);
		verify(fakePlayer).sendMessage("true");
	}

	//Data is lost in the compiler somewhere, so this isn't as easy to fix in
	//the master branch. This test will be re-added in the NewCompiler branch.
//	@Test(expected = ConfigCompileException.class)
//	public void testArrayPushOperatorFailure() throws Exception {
//		SRun("@array = array()\n"
//				+ "@array[] = array()\n"
//				+ "@array[][] = 5\n", fakePlayer);
//	}
	@Test
	public void testArrayMultiDimension() throws Exception {
		SRun("@a = array()\n"
				+ "@a[1][2] = 5\n"
				+ "msg(@a[1][2])", fakePlayer);
		verify(fakePlayer).sendMessage("5");
	}

	@Test
	public void testArrayPostIncrement() throws Exception {
		SRun("@a = array(1)\n"
				+ "(@a[0]++)\n"
				+ "msg(@a[0])", fakePlayer);
		verify(fakePlayer).sendMessage("2");
	}

	@Test
	public void testArrayPreIncrement() throws Exception {
		SRun("@a = array(1)\n"
				+ "(++@a[0])\n"
				+ "msg(@a[0])", fakePlayer);
		verify(fakePlayer).sendMessage("2");
	}

	@Test
	public void testArrayPostDecrement() throws Exception {
		SRun("@a = array(1)\n"
				+ "(@a[0]--)\n"
				+ "msg(@a[0])", fakePlayer);
		verify(fakePlayer).sendMessage("0");
	}

	@Test
	public void testArrayPreDecrement() throws Exception {
		SRun("@a = array(1)\n"
				+ "(--@a[0])\n"
				+ "msg(@a[0])", fakePlayer);
		verify(fakePlayer).sendMessage("0");
	}

	@Test
	public void testArrayRecursionToString() throws Exception {
		SRun("@a = array(); @b = array(@a); @a[] = @b; msg(@a);", fakePlayer);
		verify(fakePlayer).sendMessage("{{{*recursion*}}}");
	}

	@Test
	public void testArraySiblingsToString() throws Exception {
		SRun("@a = array(); @b = array(); @a[] = @b; @a[] = @b; msg(@a);", fakePlayer);
		verify(fakePlayer).sendMessage("{{}, {}}");
	}

	@Test
	public void testArrayDirtyOrderToString() throws Exception {
		SRun("@a = array(); @b = array(); @c = array(); @a[] = @b; @b[] = @c;\n"
				+ "concat(@a); @b[] = null; @c[] = null; msg(@a);", fakePlayer);
		verify(fakePlayer).sendMessage("{{{null}, null}}");
	}

	/*
	Uncomment when CArray can have multiple parents
	@Test
	public void testArrayDirtyMultiParentToString() throws Exception {
		SRun("@a = array(); @b = array(); @c = array(); @a[] = @c; @b[] = @c;\n"
				+ "concat(@a); @c[] = null; msg(@a);", fakePlayer);
		verify(fakePlayer).sendMessage("{{null}}");
	}
	 */
}
