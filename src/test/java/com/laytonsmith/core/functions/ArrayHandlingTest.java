package com.laytonsmith.core.functions;

import com.laytonsmith.abstraction.MCPlayer;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.constructs.CArray;
import com.laytonsmith.core.constructs.CInt;
import com.laytonsmith.core.constructs.CString;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.environments.CommandHelperEnvironment;
import com.laytonsmith.core.exceptions.CRE.CRECastException;
import com.laytonsmith.core.exceptions.CRE.CREIllegalArgumentException;
import com.laytonsmith.core.exceptions.CancelCommandException;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.natives.interfaces.Mixed;
import com.laytonsmith.testing.C;
import com.laytonsmith.testing.StaticTest;
import static com.laytonsmith.testing.StaticTest.Run;
import static com.laytonsmith.testing.StaticTest.SRun;
import static com.laytonsmith.testing.StaticTest.TestClassDocs;
import static com.laytonsmith.testing.StaticTest.assertCEquals;
import static com.laytonsmith.testing.StaticTest.assertReturn;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.fail;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.hamcrest.core.Is.*;
import static org.junit.Assert.*;

/**
 *
 *
 */
public class ArrayHandlingTest {

	MCPlayer fakePlayer;
	CArray commonArray;
	com.laytonsmith.core.environments.Environment env;

	public ArrayHandlingTest() throws Exception {
		StaticTest.InstallFakeServerFrontend();
		env = Static.GenerateStandaloneEnvironment();
		env = env.cloneAndAdd(new CommandHelperEnvironment());
	}

	@Before
	public void setUp() {
		fakePlayer = StaticTest.GetOnlinePlayer();
		commonArray = new CArray(Target.UNKNOWN, new CInt(1, Target.UNKNOWN), new CInt(2, Target.UNKNOWN), new CInt(3, Target.UNKNOWN));
		env.getEnv(CommandHelperEnvironment.class).SetPlayer(fakePlayer);
	}

	/**
	 * Test of docs method, of class ArrayHandling.
	 */
	@Test(timeout = 10000)
	public void testDocs() {
		TestClassDocs(ArrayHandling.docs(), ArrayHandling.class);
	}

	@Test(timeout = 10000)
	public void testArraySize() throws Exception, CancelCommandException {
		ArrayHandling.array_size a = new ArrayHandling.array_size();
		CArray arr = commonArray;
		Mixed ret = a.exec(Target.UNKNOWN, env, arr);
		assertReturn(ret, C.INT);
		assertCEquals(C.onstruct(3), ret);
	}

	@Test(expected = Exception.class, timeout = 10000)
	public void testArraySizeEx() throws CancelCommandException {
		ArrayHandling.array_size a = new ArrayHandling.array_size();
		a.exec(Target.UNKNOWN, env, C.Int(0));
	}

	@Test//(timeout = 10000)
	public void testArraySet1() throws Exception {
		String script
				= "assign(@array, array(1,2,3)) "
				+ "msg(@array) "
				+ "array_set(@array, 2, 1) "
				+ "msg(@array)";
		StaticTest.Run(script, fakePlayer);
		verify(fakePlayer).sendMessage("{1, 2, 3}");
		verify(fakePlayer).sendMessage("{1, 2, 1}");
	}

	@Test(timeout = 10000)
	public void testArraySet2() throws Exception {
		SRun("assign(@array, array(1, 2)) assign(@array2, @array) array_set(@array, 0, 2) msg(@array) msg(@array2)", fakePlayer);
		verify(fakePlayer, times(2)).sendMessage("{2, 2}");
	}

	@Test(timeout = 10000)
	public void testArrayReferenceBeingCorrect() throws Exception {
		SRun("assign(@array, array(1, 2)) assign(@array2, @array[]) array_set(@array, 0, 2) msg(@array) msg(@array2)", fakePlayer);
		verify(fakePlayer).sendMessage("{2, 2}");
		verify(fakePlayer).sendMessage("{1, 2}");
	}

	@Test(timeout = 10000)
	public void testArrayReferenceBeingCorrectWithArrayGet() throws Exception {
		SRun("assign(@array, array(1, 2)) "
				+ "assign(@array2, array_get(@array)) "
				+ "array_set(@array, 0, 2) "
				+ "msg(@array) "
				+ "msg(@array2)", fakePlayer);
		verify(fakePlayer).sendMessage("{2, 2}");
		verify(fakePlayer).sendMessage("{1, 2}");
	}

	//This is valid behavior now.
//	@Test(expected=ConfigRuntimeException.class)
//	public void testArraySetEx() throws CancelCommandException, ConfigCompileException{
//		String script =
//				"assign(@array, array()) array_set(@array, 3, 1) msg(@array)";
//		MethodScriptCompiler.execute(MethodScriptCompiler.compile(MethodScriptCompiler.lex(script, null)), env, null, null);
//	}
	@Test(timeout = 10000)
	public void testArrayContains() throws CancelCommandException {
		ArrayHandling.array_contains a = new ArrayHandling.array_contains();
		assertCEquals(C.onstruct(true), a.exec(Target.UNKNOWN, env, commonArray, C.onstruct(1)));
		assertCEquals(C.onstruct(false), a.exec(Target.UNKNOWN, env, commonArray, C.onstruct(55)));
	}

	@Test(timeout = 10000)
	public void testArraySContains() throws CancelCommandException {
		ArrayHandling.array_scontains a = new ArrayHandling.array_scontains();
		assertCEquals(C.onstruct(true), a.exec(Target.UNKNOWN, env, commonArray, C.onstruct(1)));
		assertCEquals(C.onstruct(false), a.exec(Target.UNKNOWN, env, commonArray, C.onstruct(55)));
		assertCEquals(C.onstruct(false), a.exec(Target.UNKNOWN, env, commonArray, new CString("2", Target.UNKNOWN)));
	}

	@Test(expected = Exception.class, timeout = 10000)
	public void testArrayContainsEx() throws CancelCommandException {
		ArrayHandling.array_contains a = new ArrayHandling.array_contains();
		a.exec(Target.UNKNOWN, env, C.Int(0), C.Int(1));
	}

	@Test(timeout = 10000)
	public void testArrayGet() throws CancelCommandException {
		ArrayHandling.array_get a = new ArrayHandling.array_get();
		assertCEquals(C.onstruct(1), a.exec(Target.UNKNOWN, env, commonArray, C.onstruct(0)));
	}

	@Test(expected = Exception.class, timeout = 10000)
	public void testArrayGetEx() throws CancelCommandException {
		ArrayHandling.array_get a = new ArrayHandling.array_get();
		a.exec(Target.UNKNOWN, env, C.Int(0), C.Int(1));
	}

	@Test(expected = ConfigRuntimeException.class, timeout = 10000)
	public void testArrayGetBad() throws CancelCommandException {
		ArrayHandling.array_get a = new ArrayHandling.array_get();
		a.exec(Target.UNKNOWN, env, commonArray, C.onstruct(55));
	}

	@Test(timeout = 10000)
	public void testArrayPush() throws CancelCommandException {
		ArrayHandling.array_push a = new ArrayHandling.array_push();
		assertReturn(a.exec(Target.UNKNOWN, env, commonArray, C.onstruct(4)), C.VOID);
		assertCEquals(C.onstruct(1), commonArray.get(0, Target.UNKNOWN));
		assertCEquals(C.onstruct(2), commonArray.get(1, Target.UNKNOWN));
		assertCEquals(C.onstruct(3), commonArray.get(2, Target.UNKNOWN));
		assertCEquals(C.onstruct(4), commonArray.get(3, Target.UNKNOWN));
	}

	@Test(timeout = 10000)
	public void testArrayPush2() throws Exception {
		SRun("assign(@a, array(1))"
				+ "array_push(@a, 2, 3)"
				+ "msg(@a)", fakePlayer);
		verify(fakePlayer).sendMessage("{1, 2, 3}");
	}

	@Test(expected = Exception.class)
	public void testArrayPushEx() throws CancelCommandException {
		ArrayHandling.array_push a = new ArrayHandling.array_push();
		a.exec(Target.UNKNOWN, env, C.Int(0), C.Int(1));
	}

	@Test(timeout = 10000)
	public void testArrayResize() throws Exception {
		String script = "assign(@array, array(1)) msg(@array) array_resize(@array, 2) msg(@array) array_resize(@array, 3, 'hello') msg(@array)";
		StaticTest.Run(script, fakePlayer);
		verify(fakePlayer).sendMessage("{1}");
		verify(fakePlayer).sendMessage("{1, null}");
		verify(fakePlayer).sendMessage("{1, null, hello}");
	}

	/**
	 * Because we are testing a loop, we put in an infinite loop detection of 10 seconds
	 *
	 * @throws Exception
	 */
	@Test(timeout = 10000)
	public void testRange() throws Exception {
		assertEquals("{0, 1, 2, 3, 4, 5, 6, 7, 8, 9}", SRun("range(10)", fakePlayer));
		assertEquals("{1, 2, 3, 4, 5, 6, 7, 8, 9, 10}", SRun("range(1, 11)", fakePlayer));
		assertEquals("{0, 5, 10, 15, 20, 25}", SRun("range(0, 30, 5)", fakePlayer));
		assertEquals("{0, 3, 6, 9}", SRun("range(0, 10, 3)", fakePlayer));
		assertEquals("{0, -1, -2, -3, -4, -5, -6, -7, -8, -9}", SRun("range(0, -10, -1)", fakePlayer));
		assertEquals("{}", SRun("range(0)", fakePlayer));
		assertEquals("{}", SRun("range(1, 0)", fakePlayer));
	}

	@Test
	public void testArraySliceAndNegativeIndexes() throws Exception {
		assertEquals("{a, b}", SRun("array(a, b, c, d, e)[..1]", null));
		assertEquals("e", SRun("array(a, e)[-1]", null));
		assertEquals("{a, b, c, d, e}", SRun("array(a, b, c, d, e)[]", null));
		assertEquals("{b, c}", SRun("array(a, b, c, d, e)[1..2]", null));
		assertEquals("{b, c, d, e}", SRun("array(a, b, c, d, e)[1..-1]", null));
		assertEquals("1", SRun("array(a, array(1, 2), c, d, e)[0..1][1][0]", null));
		assertEquals("{c, d, e}", SRun("array(a, b, c, d, e)[2..]", null));
		assertEquals("{}", SRun("array(1, 2, 3, 4, 5)[3..0]", null));
		assertEquals("{a, b}", SRun("array_get(array(a, b))", null));
		assertEquals("{2, 3}", SRun("array(1, 2, 3)[1..-1]", null));
		assertEquals("{2}", SRun("array(1, 2)[1..-1]", null));
		assertEquals("{}", SRun("array(1)[1..-1]", null));
	}

	@Test(timeout = 10000)
	public void testArrayMergeNormal() throws Exception {
		assertEquals("{1, 2, 3, 4, 5, {6, 7}}", SRun("array_merge(array(1, 2, 3), array(4, 5, array(6, 7)))", fakePlayer));
	}

	@Test(timeout = 10000)
	public void testArrayMergeAssociative() throws Exception {
		assertEquals("{a: a, b: b, c: c, d: {1, 2}}", SRun("array_merge(array(a: a, b: b), array(c: c, d: array(1, 2)))", fakePlayer));
	}

	@Test(timeout = 10000)
	public void testArrayRemove() throws Exception {
		SRun("assign(@a, array(1, 2, 3)) array_remove(@a, 1) msg(@a)", fakePlayer);
		verify(fakePlayer).sendMessage("{1, 3}");
		SRun("assign(@a, array(a: a, b: b, c: c)) array_remove(@a, 'b') msg(@a)", fakePlayer);
		verify(fakePlayer).sendMessage("{a: a, c: c}");
	}

	@Test()
	public void testStringSlice1() throws Exception {
		SRun("msg('slice'[2..])", fakePlayer);
		verify(fakePlayer).sendMessage("ice");
	}

	@Test()
	public void testStringSlice2() throws Exception {
		try {
			SRun("msg('slice'[2..8])", fakePlayer);
			fail("Did not expect this test to pass.");
		} catch (ConfigRuntimeException e) {
		}
	}

	@Test()
	public void testStringSlice3() throws Exception {
		SRun("msg('slice'[1..3])", fakePlayer);
		verify(fakePlayer).sendMessage("lic");
	}

	@Test()
	public void testStringGet1() throws Exception {
		SRun("msg('get'[1])", fakePlayer);
		verify(fakePlayer).sendMessage("e");
	}

	@Test()
	public void testStringGet2() throws Exception {
		try {
			SRun("msg('get'[5])", fakePlayer);
			fail("Did not expect this test to pass.");
		} catch (ConfigRuntimeException e) {
		}
	}

	@Test
	public void testArraySort1() throws Exception {
		Run("msg(array_sort(array(3, 1, 2)))", fakePlayer);
		verify(fakePlayer).sendMessage("{1, 2, 3}");
	}

	@Test
	public void testArraySort2() throws Exception {
		Run("msg(array_sort(array('002', '1', '03')))", fakePlayer);
		verify(fakePlayer).sendMessage("{1, 002, 03}");
	}

	@Test
	public void testArraySort3() throws Exception {
		Run("msg(array_sort(array('002', '1', '03'), STRING))", fakePlayer);
		verify(fakePlayer).sendMessage("{002, 03, 1}");
	}

	@Test
	public void testAssociativeArraySort() throws Exception {
		Run("msg(array_sort(array('a': '002', 'b': '1', 'c': '03')))", fakePlayer);
		verify(fakePlayer).sendMessage("{1, 002, 03}");
	}

	@Test
	public void testArrayImplode1() throws Exception {
		Run("msg(array_implode(array(1,2,3,4,5,6,7,8,9,1,2,3,4,5)))", fakePlayer);
		verify(fakePlayer).sendMessage("1 2 3 4 5 6 7 8 9 1 2 3 4 5");
	}

	@Test
	public void testArrayRemoveValues() throws Exception {
		Run("assign(@array, array(1, 2, 2, 3)) array_remove_values(@array, 2) msg(@array)", fakePlayer);
		verify(fakePlayer).sendMessage("{1, 3}");
		Run("@array = array(array(1, 2, 3)); array_remove_values(@array, array(1, 2, 3)); msg(@array);", fakePlayer);
		verify(fakePlayer).sendMessage("{}");
	}

	@Test
	public void testArrayIndex() throws Exception {
		Run("assign(@array, array(1, 2, 2, 3)) msg(array_index(@array, 2))", fakePlayer);
		verify(fakePlayer).sendMessage("1");
	}

	@Test
	public void testArrayIndexMissing() throws Exception {
		Run("assign(@array, array(1, 3)) msg(array_index(@array, 2))", fakePlayer);
		verify(fakePlayer).sendMessage("null");
	}

	@Test
	public void testArrayIndexes() throws Exception {
		Run("assign(@array, array(1, 2, 2, 3)) msg(array_indexes(@array, 2))", fakePlayer);
		verify(fakePlayer).sendMessage("{1, 2}");
	}

	@Test
	public void testArrayIndexesMissing() throws Exception {
		Run("assign(@array, array(1, 3)) msg(array_indexes(@array, 2))", fakePlayer);
		verify(fakePlayer).sendMessage("{}");
	}

	@Test
	public void testArrayRand() throws Exception {
		assertEquals("{1}", SRun("array_rand(array(1, 1, 1), 1, false)", null));
		String output = SRun("array_rand(array('a', 'b', 'c'))", null);
		if(!"{0}".equals(output) && !"{1}".equals(output) && !"{2}".equals(output)) {
			throw new Exception("Did not return the expected value");
		}
		output = SRun("array_rand(array('a', 'b'), 2)", null);
		if(!"{0, 1}".equals(output) && !"{1, 0}".equals(output)) {
			throw new Exception("Did not return the expected value");
		}
	}

	@Test
	public void testArrayUnique1() throws Exception {
		assertEquals("{1}", SRun("array_unique(array(1, 1, 1), false)", fakePlayer));
	}

	@Test
	public void testArrayUnique2() throws Exception {
		assertEquals("{1, 1}", SRun("array_unique(array(1, '1'), true)", fakePlayer));
	}

	@Test
	public void testArrayUnique3() throws Exception {
		assertEquals("{1}", SRun("array_unique(array(1, '1'), false)", fakePlayer));
	}

	@Test
	public void testArrayUnique4() throws Exception {
		assertEquals("{1, 1}", SRun("array_unique(array(1, '1', 1), true)", fakePlayer));
	}

	@Test
	public void testArrayGetClone() throws Exception { // This is expected to be a deep clone.
		Run("@a = array(array(array('value'))); @b = @a[]; @b[0][0][0] = 'changedValue'; msg(@a[0][0][0]); msg(@b[0][0][0]);", fakePlayer);
		verify(fakePlayer).sendMessage("value");
		verify(fakePlayer).sendMessage("changedValue");
	}

	@Test
	public void testArrayDeepClone() throws Exception {
		Run("@a = array(array(array('value'))); @b = array_deep_clone(@a); @b[0][0][0] = 'changedValue'; msg(@a[0][0][0]); msg(@b[0][0][0]);", fakePlayer);
		verify(fakePlayer).sendMessage("value");
		verify(fakePlayer).sendMessage("changedValue");
	}

	@Test
	public void testArrayShallowClone() throws Exception {
		Run("@a = array(array('value')); @b = array_shallow_clone(@a); @b[0][0] = 'changedValue'; msg(equals(@a[0][0], @b[0][0]));"
				+ "msg(ref_equals(@a, @b)); msg(@a[0][0])", fakePlayer);
		verify(fakePlayer).sendMessage("true");
		verify(fakePlayer).sendMessage("false");
		verify(fakePlayer).sendMessage("changedValue");
	}

	@Test
	public void testArrayGetCloneRefCouples() throws Exception {
		Run("@a = array('Meow'); @b = array(@a, @a, array(@a)); @c = @b[]; msg((ref_equals(@c[0], @c[1]) && ref_equals(@c[0], @c[2][0])));", fakePlayer);
		verify(fakePlayer).sendMessage("true");
	}

	@Test
	public void testArrayGetCloneRecursiveArray() throws Exception {
		Run("@a = array(); @b = array(); @a[0] = @b; @b[0] = @a; @c = @a[]; msg((ref_equals(@c[0], @c[0][0][0]) && ref_equals(@c, @c[0][0])));", fakePlayer);
		verify(fakePlayer).sendMessage("true");
	}

	@Test
	public void testArrayIterate() throws Exception {
		Run("@a = array(1, 2, 3); array_iterate(@a, closure(@k, @v){ msg(@v); });", fakePlayer);
		verify(fakePlayer).sendMessage("1");
		verify(fakePlayer).sendMessage("2");
		verify(fakePlayer).sendMessage("3");
	}

	@Test
	public void testArrayIterateAssociative() throws Exception {
		Run("@a = array(a: 1, b: 2, c: 3); array_iterate(@a, closure(@k, @v){ msg(\"@k: @v\"); });", fakePlayer);
		verify(fakePlayer).sendMessage("a: 1");
		verify(fakePlayer).sendMessage("b: 2");
		verify(fakePlayer).sendMessage("c: 3");
	}

	@Test
	public void testArrayReduce() throws Exception {
		Run("msg(array_reduce(array(1, 2, 3), closure(@soFar, @next){ return(@soFar + @next); }));", fakePlayer);
		verify(fakePlayer).sendMessage("6");
	}

	@Test
	public void testArrayReduce2() throws Exception {
		Run("msg(array_reduce(array('a', 'b', 'c'), closure(@soFar, @next){ return(@soFar . @next); }));", fakePlayer);
		verify(fakePlayer).sendMessage("abc");
	}

	@Test
	public void testArrayReduceRight() throws Exception {
		Run("msg(array_reduce_right(array(1, 2, 3), closure(@soFar, @next){ return(@soFar + @next); }));", fakePlayer);
		verify(fakePlayer).sendMessage("6");
	}

	@Test
	public void testArrayReduceRight2() throws Exception {
		Run("msg(array_reduce_right(array('a', 'b', 'c'), closure(@soFar, @next){ return(@soFar . @next); }));", fakePlayer);
		verify(fakePlayer).sendMessage("cba");
	}

	@Test
	public void testArrayEvery1() throws Exception {
		Run("msg(array_every(array(1, 1, 1), closure(@value){ return(@value == 1); }));", fakePlayer);
		verify(fakePlayer).sendMessage("true");
	}

	@Test
	public void testArrayEvery2() throws Exception {
		Run("msg(array_every(array(1, 1, 2), closure(@value){ return(@value == 1); }));", fakePlayer);
		verify(fakePlayer).sendMessage("false");
	}

	@Test
	public void testArrayEvery3() throws Exception {
		Run("msg(array_every(array(), closure(@value){ return(@value == 1); }));", fakePlayer);
		verify(fakePlayer).sendMessage("true");
	}

	@Test
	public void testArraySome1() throws Exception {
		Run("msg(array_some (array(1, 1, 2), closure(@value){ return(@value == 1); }));", fakePlayer);
		verify(fakePlayer).sendMessage("true");
	}

	@Test
	public void testArraySome2() throws Exception {
		Run("msg(array_some (array(2, 2, 2), closure(@value){ return(@value == 1); }));", fakePlayer);
		verify(fakePlayer).sendMessage("false");
	}

	@Test
	public void testArraySome3() throws Exception {
		Run("msg(array_some (array(), closure(@value){ return(@value == 1); }));", fakePlayer);
		verify(fakePlayer).sendMessage("false");
	}

	@Test
	public void testArrayMap() throws Exception {
		Run("@areaOfSquare = closure(@sideLength){\n"
				+ "\treturn(@sideLength * @sideLength);\n"
				+ "};\n"
				+ "// A collection of square sides\n"
				+ "@squares = array(1, 4, 8);\n"
				+ "@areas = array_map(@squares, @areaOfSquare);\n"
				+ "msg(@areas);", fakePlayer);
		verify(fakePlayer).sendMessage("{1, 16, 64}");
	}

	@Test
	public void testArrayReverse() throws Exception {
		Run("@array = array(1, 2, 3, 4);\n"
				+ "array_reverse(@array);\n"
				+ "msg(@array);\n", fakePlayer);
		verify(fakePlayer).sendMessage("{4, 3, 2, 1}");
	}

	@Test
	public void testArrayIntersect() throws Exception {
		assertThat(SRun("array_intersect(array(one: 1, two: 2), array(one: 1, three: 3))", fakePlayer), is("{one: 1}"));
		try {
			SRun("array_intersect(array(one: 1, two: 2), array(one: 1, three: 3), EQUALS)", fakePlayer);
			fail("Did not expect 3 arguments to pass");
		} catch (CREIllegalArgumentException e) {
			// Pass
		}


		assertThat(SRun("array_intersect(array(1, 2, 3), array(2, 3, 4), HASH)", fakePlayer), is("{2, 3}"));
		assertThat(SRun("array_intersect(array(1, 2, 3), array(4, 5, 6), HASH)", fakePlayer), is("{}"));

		assertThat(SRun("array_intersect(array(1, 2, 3), array(2, 3, 4), EQUALS)", fakePlayer), is("{2, 3}"));
		assertThat(SRun("array_intersect(array(1, 2, 3), array(4, 5, 6), EQUALS)", fakePlayer), is("{}"));

		assertThat(SRun("array_intersect(array('1', '2', '3'), array(1, 2, 3), STRICT_EQUALS)", fakePlayer), is("{}"));
		assertThat(SRun("array_intersect(array('1', '2', '3'), array('1', 2, 3), STRICT_EQUALS)", fakePlayer), is("{1}"));

		assertThat(SRun("array_intersect(array(1, 2, 3), array(4, 5, 6), closure(@a, @b) { return(true); })", fakePlayer),
				is("{1, 2, 3}"));
		assertThat(SRun("array_intersect(array(1, 2, 3), array(1, 2, 3), closure(@a, @b) { return(false); })", fakePlayer),
				is("{}"));

		assertThat(SRun("array_intersect(array(array(id: 1, qty: 2), array(id: 2, qty: 8)),"
				+ " array(array(id: 1, qty: 19), array(id: 6, qty: 2)), closure(@a, @b){ return(@a[id] == @b[id]); })", fakePlayer),
				is("{{id: 1, qty: 2}}"));

	}

	@Test
	public void testMapImplode() throws Exception {
		assertThat(SRun("map_implode(array('a': 'b'), '=', '&')", null), is("a=b"));
		assertThat(SRun("map_implode(array('a': '1', 'b': '2'), '=', '&')", null), is("a=1&b=2"));
		try {
			SRun("map_implode(array(), '', '')", null);
			fail();
		} catch (CRECastException ex) {
			// pass
		}
	}

}
