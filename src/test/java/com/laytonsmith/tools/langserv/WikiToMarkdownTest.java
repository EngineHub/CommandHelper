package com.laytonsmith.tools.langserv;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class WikiToMarkdownTest {

	@Test
	public void testNullAndEmpty() {
		assertNull(WikiToMarkdown.convert(null));
		assertEquals("", WikiToMarkdown.convert(""));
	}

	@Test
	public void testBold() {
		assertEquals("This is **bold** text", WikiToMarkdown.convert("This is '''bold''' text"));
	}

	@Test
	public void testItalic() {
		assertEquals("This is *italic* text", WikiToMarkdown.convert("This is ''italic'' text"));
	}

	@Test
	public void testBoldAndItalic() {
		assertEquals("**bold** and *italic*",
				WikiToMarkdown.convert("'''bold''' and ''italic''"));
	}

	@Test
	public void testHeadings() {
		assertEquals("## Heading 2", WikiToMarkdown.convert("== Heading 2 =="));
		assertEquals("### Heading 3", WikiToMarkdown.convert("=== Heading 3 ==="));
		assertEquals("#### Heading 4", WikiToMarkdown.convert("==== Heading 4 ===="));
		assertEquals("##### Heading 5", WikiToMarkdown.convert("===== Heading 5 ====="));
		assertEquals("###### Heading 6", WikiToMarkdown.convert("====== Heading 6 ======"));
	}

	@Test
	public void testWikiLinks() {
		assertEquals("SomePage", WikiToMarkdown.convert("[[SomePage]]"));
		assertEquals("Label", WikiToMarkdown.convert("[[SomePage|Label]]"));
		assertEquals("Label", WikiToMarkdown.convert("[[SomePage#anchor|Label]]"));
	}

	@Test
	public void testExternalLinks() {
		assertEquals("[Click here](http://example.com)",
				WikiToMarkdown.convert("[http://example.com Click here]"));
		assertEquals("[Docs](https://docs.example.com/page)",
				WikiToMarkdown.convert("[https://docs.example.com/page Docs]"));
	}

	@Test
	public void testFunctionTemplate() {
		assertEquals("Use `array_push()` to add elements",
				WikiToMarkdown.convert("Use {{function|array_push}} to add elements"));
	}

	@Test
	public void testKeywordTemplate() {
		assertEquals("The `if` keyword",
				WikiToMarkdown.convert("The {{keyword|if}} keyword"));
	}

	@Test
	public void testObjectTemplate() {
		assertEquals("Returns a `string`",
				WikiToMarkdown.convert("Returns a {{object|string}}"));
	}

	@Test
	public void testTakeNoteTemplate() {
		String result = WikiToMarkdown.convert("{{TakeNote|text=This is important}}");
		assertEquals("> **Note:** This is important", result);
	}

	@Test
	public void testWarningTemplate() {
		String result = WikiToMarkdown.convert("{{Warning|text=Be careful}}");
		assertEquals("> **Warning:** Be careful", result);
	}

	@Test
	public void testTemplateEscapePipe() {
		assertEquals("string|array", WikiToMarkdown.convert("string{{!}}array"));
	}

	@Test
	public void testPreBlock() {
		String input = "<pre class=\"pre\">some code here</pre>";
		String result = WikiToMarkdown.convert(input);
		assertEquals("```\nsome code here\n```", result);
	}

	@Test
	public void testCodeTag() {
		assertEquals("Use `getValue` here", WikiToMarkdown.convert("Use <code>getValue</code> here"));
	}

	@Test
	public void testHtmlStrong() {
		assertEquals("This is **bold**", WikiToMarkdown.convert("This is <strong>bold</strong>"));
	}

	@Test
	public void testHtmlEm() {
		assertEquals("This is *italic*", WikiToMarkdown.convert("This is <em>italic</em>"));
	}

	@Test
	public void testHorizontalRule() {
		assertEquals("above\n---\nbelow", WikiToMarkdown.convert("above\n----\nbelow"));
	}

	@Test
	public void testNumberedList() {
		assertEquals("1. First\n1. Second", WikiToMarkdown.convert("# First\n# Second"));
	}

	@Test
	public void testHtmlEntities() {
		assertEquals("<tag>", WikiToMarkdown.convert("&lt;tag&gt;"));
	}

	@Test
	public void testWikiEscapedEntities() {
		String input = "&lsqb;optional&rsqb; &ast;bold&ast;";
		String result = WikiToMarkdown.convert(input);
		assertEquals("[optional] *bold*", result);
	}

	@Test
	public void testSimpleTable() {
		String input = "{| class=\"wikitable\"\n"
				+ "|-\n"
				+ "! Name !! Type\n"
				+ "|-\n"
				+ "| foo || string\n"
				+ "|-\n"
				+ "| bar || int\n"
				+ "|}";
		String result = WikiToMarkdown.convert(input);
		assertTrue(result.contains("| Name | Type |"));
		assertTrue(result.contains("|---|---|"));
		assertTrue(result.contains("| foo | string |"));
		assertTrue(result.contains("| bar | int |"));
	}

	@Test
	public void testPreBlockUnescapesEntities() {
		String input = "<pre class=\"pre\">&lt;profile&gt;\n\t&lt;type&gt;email&lt;/type&gt;\n&lt;/profile&gt;</pre>";
		String result = WikiToMarkdown.convert(input);
		assertTrue(result.contains("<profile>"));
		assertTrue(result.contains("<type>email</type>"));
	}

	@Test
	public void testMethodScriptCodeBlock() {
		String input = "<div style=\"padding: 10px\" class=\"methodscript_code\">"
				+ "<div class=\"rawCode\" style=\"display: none;\">"
				+ "msg&lpar;&quot;hello&quot;&rpar;"
				+ "</div>"
				+ "<div class=\"copyButton\"><img src=\"images/clipboard.png\" alt=\"\"> Copy Code</div>"
				+ "<span style=\"color:blue\">msg</span>"
				+ "</div>";
		String result = WikiToMarkdown.convert(input);
		assertTrue(result.contains("```mscript"));
		assertTrue(result.contains("msg(\"hello\")"));
	}

	@Test
	public void testLearningTrailRemoved() {
		assertEquals("Some text", WikiToMarkdown.convert("{{LearningTrail}}Some text"));
	}

	@Test
	public void testUnimplementedTemplate() {
		String result = WikiToMarkdown.convert("{{unimplemented}}");
		assertTrue(result.contains("not yet implemented"));
	}

	@Test
	public void testComplexDoc() {
		String input = "Returns the value. See [[Functions|this page]] for details.\n"
				+ "Use {{function|array_get}} to retrieve values.\n"
				+ "'''Note:''' This is ''important''.";
		String result = WikiToMarkdown.convert(input);
		assertTrue(result.contains("this page"));
		assertFalse(result.contains("[["));
		assertTrue(result.contains("`array_get()`"));
		assertTrue(result.contains("**Note:**"));
		assertTrue(result.contains("*important*"));
	}

	@Test
	public void testRealisticFunctionDoc() {
		String input = "The settings object has fields:\n"
				+ "{| class=\"wikitable\"\n"
				+ "|-\n"
				+ "! Key !! Type !! Default !! Description\n"
				+ "|-\n"
				+ "| method || string || null || One of the HTTP methods\n"
				+ "|-\n"
				+ "| success || closure || null || The {{keyword|closure}} called on success\n"
				+ "|}\n"
				+ "{{Warning|text=There are a limited number of slots.}}\n"
				+ "Use {{function|http_request}} with '''caution'''.\n"
				+ "See [[Networking|this page]] for details.\n"
				+ "=== Example ===\n"
				+ "<pre class=\"pre\">http_request&lpar;&amp;quot;http://example.com&amp;quot;&rpar;</pre>";
		String result = WikiToMarkdown.convert(input);

		// Table
		assertTrue(result.contains("| Key | Type | Default | Description |"));
		assertTrue(result.contains("|---|---|---|---|"));
		assertTrue(result.contains("| method | string | null | One of the HTTP methods |"));

		// Templates
		assertTrue(result.contains("`closure`"));
		assertTrue(result.contains("`http_request()`"));

		// Warning
		assertTrue(result.contains("> **Warning:**"));

		// Bold
		assertTrue(result.contains("**caution**"));

		// Wiki link → label only
		assertTrue(result.contains("this page"));
		assertFalse(result.contains("[["));

		// Heading
		assertTrue(result.contains("### Example"));

		// Pre block
		assertTrue(result.contains("```"));

		// No leftover wiki/HTML
		assertFalse(result.contains("{{"));
		assertFalse(result.contains("{|"));
		assertFalse(result.contains("|}"));
		assertFalse(result.contains("'''"));
	}
}
