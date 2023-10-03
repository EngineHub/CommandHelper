/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.core.constructs;

import com.laytonsmith.core.Static;
import com.laytonsmith.core.environments.Environment;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

/**
 *
 *
 */
public class CByteArrayTest {

	Environment env;
	public CByteArrayTest() {
	}

	@BeforeClass
	public static void setUpClass() {
	}

	@AfterClass
	public static void tearDownClass() {
	}

	@Before
	public void setUp() throws Exception {
		env = Static.GenerateStandaloneEnvironment();
	}

	@After
	public void tearDown() {
	}

	@Test
	public void testBasic1() {
		CByteArray ba = new CByteArray(Target.UNKNOWN, 0, env);
		ba.putByte((byte) 1, null);
		assertArrayEquals(new byte[]{(byte) 1}, ba.asByteArrayCopy());
	}

	@Test
	public void testBasic2() {
		CByteArray ba = new CByteArray(Target.UNKNOWN, 0, env);
		ba.putByte((byte) 1, null);
		ba.putByte((byte) 1, null);
		ba.putByte((byte) 1, null);
		assertEquals(3, ba.asByteArrayCopy().length);
	}

	@Test
	public void testBasic3() {
		CByteArray ba = new CByteArray(Target.UNKNOWN, 1024, env);
		ba.putByte((byte) 1, null);
		assertEquals(1, ba.asByteArrayCopy().length);
	}

	@Test
	public void testBasicWithPosition() {
		CByteArray ba = new CByteArray(Target.UNKNOWN, env);
		ba.putByte((byte) 1, null);
		ba.putByte((byte) 1, 0);
		assertEquals(1, ba.asByteArrayCopy().length);
	}

	@Test
	public void testBasicGet() {
		CByteArray ba = new CByteArray(Target.UNKNOWN, env);
		ba.putByte((byte) 5, null);
		assertEquals(5, ba.getByte(0));
	}

	@Test
	public void testShort() {
		CByteArray ba = new CByteArray(Target.UNKNOWN, env);
		ba.putShort((short) 1, null);
		assertEquals(2, ba.asByteArrayCopy().length);
	}

	@Test
	public void testString1() throws Exception {
		CByteArray ba = new CByteArray(Target.UNKNOWN, env);
		ba.writeUTF8String("1", null, null);
		assertEquals(3, ba.asByteArrayCopy().length);
	}

	@Test
	public void testString2() throws Exception {
		CByteArray ba = new CByteArray(Target.UNKNOWN, env);
		ba.writeUTF8String("String", null, null);
		assertEquals("String", ba.readUTF8String(0, null));
	}

	@Test
	public void testBytes1() throws Exception {
		CByteArray ba1 = new CByteArray(Target.UNKNOWN, env);
		ba1.writeUTF8String("A string", null, null);
		CByteArray ba2 = new CByteArray(Target.UNKNOWN, env);
		ba2.putBytes(ba1, null);
		assertArrayEquals(ba1.asByteArrayCopy(), ba2.asByteArrayCopy());
	}
}
