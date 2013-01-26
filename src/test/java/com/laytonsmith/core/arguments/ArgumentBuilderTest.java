package com.laytonsmith.core.arguments;

import com.laytonsmith.core.constructs.CArray;
import com.laytonsmith.core.constructs.CString;
import com.laytonsmith.core.natives.interfaces.Mixed;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author lsmith
 */
public class ArgumentBuilderTest {
	
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
					new Signature(
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
		ArgumentBuilder a = ArgumentBuilder.Build(new Signature(
					new Argument("", CString.class, "a").setOptional(),
					new Argument("", CArray.class, "b")
				));
		//Meh. Not super reliable long term, but this will work.
		assertEquals("CArray b\n[CString a], CArray b", a.toString());
	}
	
	@Test
	public void testBuildWithDuplicates(){
		ArgumentBuilder a = ArgumentBuilder.Build(
					new Argument("", CString.class, "a").setOptional(),
					new Argument("", CString.class, "b").setOptional()
				);
		assertEquals("\n[CString a]\n[CString a], [CString b]", a.toString());
		assertEquals(3, a.signatureCount());
	}
	
	@Test
	public void testParseArgs(){
		
	}
}
