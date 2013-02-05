package com.laytonsmith.core.arguments;

import com.laytonsmith.core.constructs.CArray;
import com.laytonsmith.core.constructs.CInt;
import com.laytonsmith.core.constructs.CString;
import com.laytonsmith.core.constructs.Construct;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.functions.Function;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

/**
 *
 * @author lsmith
 */
public class ArgumentBuilderTest {
	
	Function fakeFunction;
	
	public ArgumentBuilderTest() {
	}
	
	@BeforeClass
	public static void setUpClass() {
	}
	
	@AfterClass
	public static void tearDownClass() {
	}
	
	@Before
	public void setUp() {
		fakeFunction = Mockito.mock(Function.class);
		Mockito.when(fakeFunction.getName()).thenReturn("MOCK_FUNCTION");
	}
	
	@After
	public void tearDown() {
	}

	/**
	 * Ensure the count is right, for a larger test case
	 */
	@Test
	public void testBuild1() {
		ArgumentBuilder b = ArgumentBuilder.Build(
					new Signature(1,
						new Argument("", CString.class, "a").setOptional(),
						new Argument("", CArray.class, "b"),
						new Argument("", CString.class, "c").setOptional(),
						new Argument("", CString.class, "d").setOptional()
					)
				);
		assertEquals(6, b.signatureCount());
	}
	
	/**
	 * Ensure the contents are right, for the smaller test case
	 */
	@Test
	public void testBuild2(){
		ArgumentBuilder a = ArgumentBuilder.Build(new Signature(1,
					new Argument("", CString.class, "a").setOptional(),
					new Argument("", CArray.class, "b")
				));
		//Meh. Not super reliable long term, but this will work.
		assertEquals("array b\n[string a], array b", a.toString());
	}
	
	@Test
	public void testBuildWithDuplicates(){
		ArgumentBuilder a = ArgumentBuilder.Build(
					new Argument("", CString.class, "a").setOptional(),
					new Argument("", CString.class, "b").setOptional()
				);
		assertEquals("\n[string a]\n[string a], [string b]", a.toString());
		assertEquals(3, a.signatureCount());
	}
	
	@Test
	public void testParseArgs(){
		CString a = new CString("a", Target.UNKNOWN);
		ArgumentBuilder builder = ArgumentBuilder.Build(
					new Argument("", CString.class, "a").setOptionalDefault(a),
					new Argument("", CInt.class, "b"),
					new Argument("", CArray.class, "c")
				);
		
		CInt b = new CInt(1, Target.UNKNOWN);
		CArray c = new CArray(Target.UNKNOWN, new CString("c", Target.UNKNOWN));
		ArgList list1 = builder.parse(new Construct[]{a, b, c}, fakeFunction, Target.UNKNOWN);
		assertEquals(list1.get("a"), a);
		assertEquals(list1.get("b"), b);
		assertEquals(list1.get("c"), c);
		ArgList list2 = builder.parse(new Construct[]{b, c}, fakeFunction, Target.UNKNOWN);
		assertEquals(list2.get("a"), a);
		assertEquals(list2.get("b"), b);
		assertEquals(list2.get("c"), c);
	}
	
	@Test
	public void testVarArgsWithOutArray(){
		ArgumentBuilder builder = ArgumentBuilder.Build(
					new Argument("", CArray.class, "var").setVarargs()
				);
		ArgList list = builder.parse(new Construct[]{new CString("", Target.UNKNOWN), new CString("", Target.UNKNOWN)}, fakeFunction, Target.UNKNOWN);
		CArray ca = list.get("var");
		assertEquals(2, ca.size());
	}
	
	@Test public void testVarArgsWithArray(){
		ArgumentBuilder builder = ArgumentBuilder.Build(
					new Argument("", CArray.class, "var").setVarargs()
				);
		ArgList list = builder.parse(new Construct[]{
			new CArray(Target.UNKNOWN, new CString("", Target.UNKNOWN), new CString("", Target.UNKNOWN))
		}, fakeFunction, Target.UNKNOWN);
		CArray ca = list.get("var");
		assertEquals(2, ca.size());
	}
	
	@Test public void testMultipleSignatures(){
		ArgumentBuilder builder = ArgumentBuilder.Build(
					new Signature(1, new Argument("", CString.class, "a")),
					new Signature(2, new Argument("", CInt.class, "a"))
				);
		ArgList list1 = builder.parse(new Construct[]{new CString("", Target.UNKNOWN)}, fakeFunction, Target.UNKNOWN);
		assertEquals(CString.class, list1.get("a").getClass());
		ArgList list2 = builder.parse(new Construct[]{new CInt(1, Target.UNKNOWN)}, fakeFunction, Target.UNKNOWN);
		assertEquals(CInt.class, list2.get("a").getClass());
	}
	
	@Test public void testGenericChecking(){
		ArgumentBuilder builder = ArgumentBuilder.Build(
					new Argument("", CArray.class, "a").setGenerics(new Generic(CInt.class))
				);
		try{
			CArray ca = new CArray(Target.UNKNOWN, new CString("", Target.UNKNOWN));
			builder.parse(new Construct[]{ca}, fakeFunction, Target.UNKNOWN);
			fail("Expected a CRE, but none was thrown");
		} catch(ConfigRuntimeException e){
			//Pass
		}
		
		CArray ca = new CArray(Target.UNKNOWN, new CInt(1, Target.UNKNOWN));
		ArgList a = builder.parse(new Construct[]{ca}, fakeFunction, Target.UNKNOWN);
		assertEquals("1", a.get("a").val());
	}
	
	@Test public void testArgumentRanges(){
		fail("Write this test");
	}
}
