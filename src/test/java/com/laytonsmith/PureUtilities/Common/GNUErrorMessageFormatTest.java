/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.PureUtilities.Common;

import org.junit.Test;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

/**
 *
 * @author cailin
 */
public class GNUErrorMessageFormatTest {

	@Test
	public void test1() {
		String test = "sourcefile:40: message";
		GNUErrorMessageFormat gnu = new GNUErrorMessageFormat(test);
		assertEquals("sourcefile", gnu.file());
		assertEquals(40, gnu.fromLine());
		assertEquals(40, gnu.toLine());
		assertEquals(0, gnu.fromColumn());
		assertEquals(0, gnu.toColumn());
		assertEquals("message", gnu.message());
	}

	@Test
	public void test2() {
		String test = "sourcefile:40:5: message: here";
		GNUErrorMessageFormat gnu = new GNUErrorMessageFormat(test);
		assertEquals("sourcefile", gnu.file());
		assertEquals(40, gnu.fromLine());
		assertEquals(40, gnu.toLine());
		assertEquals(5, gnu.fromColumn());
		assertEquals(5, gnu.toColumn());
		assertEquals("message: here", gnu.message());
	}

	@Test
	public void test3() {
		String test = "sourcefile:40.5: message: here";
		GNUErrorMessageFormat gnu = new GNUErrorMessageFormat(test);
		assertEquals("sourcefile", gnu.file());
		assertEquals(40, gnu.fromLine());
		assertEquals(40, gnu.toLine());
		assertEquals(5, gnu.fromColumn());
		assertEquals(5, gnu.toColumn());
		assertEquals("message: here", gnu.message());
	}

	@Test
	public void test4() {
		String test = "sourcefile:40.5-41.8: message";
		GNUErrorMessageFormat gnu = new GNUErrorMessageFormat(test);
		assertEquals("sourcefile", gnu.file());
		assertEquals(40, gnu.fromLine());
		assertEquals(41, gnu.toLine());
		assertEquals(5, gnu.fromColumn());
		assertEquals(8, gnu.toColumn());
		assertEquals("message", gnu.message());
	}

	@Test
	public void test5() {
		String test = "sourcefile:40.5-6: message";
		GNUErrorMessageFormat gnu = new GNUErrorMessageFormat(test);
		assertEquals("sourcefile", gnu.file());
		assertEquals(40, gnu.fromLine());
		assertEquals(40, gnu.toLine());
		assertEquals(5, gnu.fromColumn());
		assertEquals(6, gnu.toColumn());
		assertEquals("message", gnu.message());
	}

	@Test
	public void test6() {
		String test = "sourcefile:40-41: message";
		GNUErrorMessageFormat gnu = new GNUErrorMessageFormat(test);
		assertEquals("sourcefile", gnu.file());
		assertEquals(40, gnu.fromLine());
		assertEquals(41, gnu.toLine());
		assertEquals(0, gnu.fromColumn());
		assertEquals(0, gnu.toColumn());
		assertEquals("message", gnu.message());
	}

	@Test
	public void test7() {
		String test = "sourcefile:40.5: message: with: more: colons";
		GNUErrorMessageFormat gnu = new GNUErrorMessageFormat(test);
		assertEquals("sourcefile", gnu.file());
		assertEquals(40, gnu.fromLine());
		assertEquals(40, gnu.toLine());
		assertEquals(5, gnu.fromColumn());
		assertEquals(5, gnu.toColumn());
		assertEquals("message: with: more: colons", gnu.message());
	}

	@Test
	public void test8() {
		String test = "sourcefile:40.5: message";
		assertTrue(new GNUErrorMessageFormat(test).equals(new GNUErrorMessageFormat(test)));
	}

	@Test(expected = IllegalArgumentException.class)
	@SuppressWarnings("ResultOfObjectAllocationIgnored")
	public void test9() {
		String test = "file:invalid: message";
		new GNUErrorMessageFormat(test).parse();
	}

	@Test
	public void test10() {
		String test = "sourcefile:40.5: message";
		assertEquals(test, new GNUErrorMessageFormat(test).getOriginalErrorLine());
	}

	@Test
	public void test11() {
		String test = "file:10: error: It's broken";
		assertEquals(GNUErrorMessageFormat.MessageType.ERROR, new GNUErrorMessageFormat(test).messageType());
	}

	@Test
	public void test12() {
		String test = "file:10: WaRnInG: It's broken";
		assertEquals(GNUErrorMessageFormat.MessageType.WARNING, new GNUErrorMessageFormat(test).messageType());
	}

	@Test
	public void test13() {
		String test = "file:10: INFO: It's broken";
		assertEquals(GNUErrorMessageFormat.MessageType.INFO, new GNUErrorMessageFormat(test).messageType());
	}

	@Test
	public void test14() {
		String test = "file:10: who knows";
		assertEquals(GNUErrorMessageFormat.MessageType.UNKNOWN, new GNUErrorMessageFormat(test).messageType());
	}

}
