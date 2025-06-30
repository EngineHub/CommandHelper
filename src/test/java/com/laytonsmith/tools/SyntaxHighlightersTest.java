/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.tools;

import com.laytonsmith.testing.AbstractIntegrationTest;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Ignore;

/**
 *
 * @author Cailin
 */
public class SyntaxHighlightersTest extends AbstractIntegrationTest {

	public SyntaxHighlightersTest() {
	}

	@Test
	public void testNpp() {
		assertNotEquals(SyntaxHighlighters.HELP_TEXT, SyntaxHighlighters.generate("npp", "default"));
		assertNotEquals(SyntaxHighlighters.HELP_TEXT, SyntaxHighlighters.generate("npp", "obsidian"));
		assertNotEquals(SyntaxHighlighters.HELP_TEXT, SyntaxHighlighters.generate("npp", "solarized-dark"));
		assertNotEquals(SyntaxHighlighters.HELP_TEXT, SyntaxHighlighters.generate("npp", "solarized-light"));
	}

	@Test
	public void testTextwrangler() {
		assertNotEquals(SyntaxHighlighters.HELP_TEXT, SyntaxHighlighters.generate("textwrangler", ""));
	}

	@Test
	public void testGeshi() {
		assertNotEquals(SyntaxHighlighters.HELP_TEXT, SyntaxHighlighters.generate("geshi", ""));
	}

	@Test
	@Ignore("Ignored while investigation is ongoing. It only fails when all tests are run.")
	public void testVim() {
		assertNotEquals(SyntaxHighlighters.HELP_TEXT, SyntaxHighlighters.generate("vim", ""));
	}

	@Test
	public void testNano() {
		assertNotEquals(SyntaxHighlighters.HELP_TEXT, SyntaxHighlighters.generate("nano", ""));
	}

	@Test
	public void testAtom() {
		assertNotEquals(SyntaxHighlighters.HELP_TEXT, SyntaxHighlighters.generate("atom", ""));
	}

	@Test
	public void testSublime() {
		assertNotEquals(SyntaxHighlighters.HELP_TEXT, SyntaxHighlighters.generate("sublime", ""));
	}

	@Test
	public void testSublime3() {
		assertNotEquals(SyntaxHighlighters.HELP_TEXT, SyntaxHighlighters.generate("sublime3", ""));
	}

}
