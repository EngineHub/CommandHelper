/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.PureUtilities;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Cailin
 */
@SuppressWarnings("ResultOfObjectAllocationIgnored")
public class SemVer2Test {

	public SemVer2Test() {
	}

	@Test
	public void testSimpleParsing() throws Exception {
		new SemVer2("1.0.0");
		new SemVer2("1.0.0-alpha");
		new SemVer2("1.0.0-alpha+build");
		new SemVer2("1.0.0+build");
	}

	@Test(expected = Throwable.class)
	public void testFailureParsing() throws Exception {
		new SemVer2("bad");
	}

	@Test
	public void testSimplePrecedence() {
		assertTrue(new SemVer2("1.0.0").lt(new SemVer2("2.0.0")));
		assertTrue(new SemVer2("1.0.1").lt(new SemVer2("2.0.0")));
		assertTrue(new SemVer2("1.0.1").lt(new SemVer2("1.0.2")));
		assertTrue(new SemVer2("1.0.0").lt(new SemVer2("1.1.0")));
	}

	@Test
	public void testPrecedenceWithPrerelease() {
		assertTrue(new SemVer2("1.0.0-beta.2").lt(new SemVer2("1.0.0-beta.11")));
		assertTrue(new SemVer2("1.0.0-alpha").lt(new SemVer2("1.0.0-alpha.1")));
		assertTrue(new SemVer2("1.0.0-alpha").lt(new SemVer2("1.0.0")));
		assertTrue(new SemVer2("1.0.0-alpha.1").lt(new SemVer2("1.0.0-alpha.beta")));
		assertTrue(new SemVer2("1.0.0-alpha.beta").lt(new SemVer2("1.0.0-beta")));
		assertTrue(new SemVer2("1.0.0-beta").lt(new SemVer2("1.0.0-beta.2")));
		assertTrue(new SemVer2("1.0.0-beta.11").lt(new SemVer2("1.0.0-rc.1")));
		assertTrue(new SemVer2("1.0.0-rc.1").lt(new SemVer2("1.0.0")));
		assertTrue(new SemVer2("1.0.0-rc.1").lte(new SemVer2("1.0.0")));
		assertTrue(new SemVer2("1.0.0").lte(new SemVer2("1.0.0")));
		assertTrue(new SemVer2("1.0.0").gte(new SemVer2("1.0.0")));
		assertTrue(new SemVer2("1.0.0").gt(new SemVer2("1.0.0-beta")));
		assertTrue(new SemVer2("1.0.0").gte(new SemVer2("1.0.0-beta")));
		assertTrue(new SemVer2("1.0.0+1").gte(new SemVer2("1.0.0+2")));
		assertTrue(new SemVer2("1.0.0+1").lte(new SemVer2("1.0.0+2")));
		assertTrue(new SemVer2("1.0.0+1").equals(new SemVer2("1.0.0+2")));
	}
}
