package com.laytonsmith.tools.langserv;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Converts wiki markup (as used in MethodScript function documentation) to Markdown
 * for display in LSP clients like VS Code.
 */
public final class WikiToMarkdown {

	private WikiToMarkdown() {
		// Utility class
	}

	/**
	 * Converts a wiki-formatted documentation string to Markdown.
	 * @param wiki The wiki-formatted string to convert.
	 * @return The converted Markdown string.
	 */
	public static String convert(String wiki) {
		if(wiki == null || wiki.isEmpty()) {
			return wiki;
		}

		String result = wiki;

		// Extract code from SimpleSyntaxHighlighter HTML blocks first (before other processing)
		result = convertMethodScriptCodeBlocks(result);

		// Convert <pre> blocks to fenced code blocks
		result = convertPreBlocks(result);

		// Convert <code> to inline code
		result = convertCodeTags(result);

		// Handle {{!}} escape (wiki escape for | in templates)
		result = result.replace("{{!}}", "|");

		// Wiki templates
		result = convertTemplates(result);

		// Wiki links
		result = convertWikiLinks(result);
		result = convertExternalLinks(result);

		// Inline formatting (bold before italic, since ''' starts with '')
		result = convertBoldItalic(result);

		// Headings (most = to least, to avoid partial matches)
		result = convertHeadings(result);

		// Tables
		result = convertTables(result);

		// Numbered lists
		result = convertNumberedLists(result);

		// Horizontal rules
		result = result.replaceAll("(?m)^-{4,}\\s*$", "---");

		// Remaining HTML cleanup
		result = convertRemainingHtml(result);

		return result.trim();
	}

	/**
	 * Extracts raw code from SimpleSyntaxHighlighter's methodscript_code divs
	 * and replaces them with fenced code blocks.
	 */
	private static String convertMethodScriptCodeBlocks(String input) {
		StringBuilder result = new StringBuilder(input);
		String marker = "class=\"methodscript_code\"";
		int pos = 0;
		while((pos = result.indexOf(marker, pos)) >= 0) {
			int divStart = result.lastIndexOf("<div", pos);
			if(divStart < 0) {
				break;
			}

			// Find the rawCode div content
			String rawCodeMarker = "class=\"rawCode\"";
			int rawCodePos = result.indexOf(rawCodeMarker, pos);
			if(rawCodePos < 0) {
				break;
			}

			int contentStart = result.indexOf(">", rawCodePos) + 1;
			int contentEnd = result.indexOf("</div>", contentStart);
			if(contentStart <= 0 || contentEnd < 0) {
				break;
			}

			String rawCode = result.substring(contentStart, contentEnd);
			rawCode = unescapeWikiEntities(rawCode);

			// Find the end of the outer div by counting nesting
			int depth = 1;
			int searchPos = result.indexOf(">", divStart) + 1;
			int outerEnd = -1;
			while(depth > 0 && searchPos < result.length()) {
				int nextOpen = result.indexOf("<div", searchPos);
				int nextClose = result.indexOf("</div>", searchPos);
				if(nextClose < 0) {
					break;
				}
				if(nextOpen >= 0 && nextOpen < nextClose) {
					depth++;
					searchPos = nextOpen + 4;
				} else {
					depth--;
					if(depth == 0) {
						outerEnd = nextClose + "</div>".length();
					}
					searchPos = nextClose + "</div>".length();
				}
			}

			if(outerEnd < 0) {
				break;
			}

			String replacement = "\n```mscript\n" + rawCode + "\n```\n";
			result.replace(divStart, outerEnd, replacement);
			pos = divStart + replacement.length();
		}
		return result.toString();
	}

	private static String convertPreBlocks(String input) {
		Pattern p = Pattern.compile("(?si)<pre[^>]*>(.*?)</pre>");
		Matcher m = p.matcher(input);
		StringBuilder sb = new StringBuilder();
		while(m.find()) {
			String code = unescapeWikiEntities(m.group(1));
			m.appendReplacement(sb, Matcher.quoteReplacement("\n```\n" + code + "\n```\n"));
		}
		m.appendTail(sb);
		return sb.toString();
	}

	private static String convertCodeTags(String input) {
		Pattern p = Pattern.compile("(?si)<code>(.*?)</code>");
		Matcher m = p.matcher(input);
		StringBuilder sb = new StringBuilder();
		while(m.find()) {
			String code = unescapeWikiEntities(m.group(1));
			m.appendReplacement(sb, Matcher.quoteReplacement("`" + code + "`"));
		}
		m.appendTail(sb);
		return sb.toString();
	}

	private static String convertTemplates(String input) {
		String result = input;

		// {{function|name}} → `name()`
		result = result.replaceAll("\\{\\{function\\|([^}]+)\\}\\}", "`$1()`");

		// {{keyword|name}} → `name`
		result = result.replaceAll("\\{\\{keyword\\|([^}]+)\\}\\}", "`$1`");

		// {{object|name}} → `name`
		result = result.replaceAll("\\{\\{object\\|([^}]+)\\}\\}", "`$1`");

		// {{TakeNote|text=...}} → blockquote note
		result = result.replaceAll("(?s)\\{\\{TakeNote\\|text=(.*?)\\}\\}", "\n> **Note:** $1\n");

		// {{Warning|text=...}} → blockquote warning
		result = result.replaceAll("(?s)\\{\\{Warning\\|text=(.*?)\\}\\}", "\n> **Warning:** $1\n");

		// {{unimplemented}} → warning
		result = result.replace("{{unimplemented}}", "\n> **Warning:** This feature is not yet implemented.\n");

		// {{LearningTrail}} → empty (navigation aid, not useful in tooltips)
		result = result.replace("{{LearningTrail}}", "");

		return result;
	}

	private static String convertWikiLinks(String input) {
		// [[Page#anchor|label]] or [[Page|label]] → label
		String result = input;
		result = result.replaceAll("\\[\\[(?:[^|\\]]*?)\\|([^\\]]+?)\\]\\]", "$1");
		// [[Page]] → Page
		result = result.replaceAll("\\[\\[([^\\]]+?)\\]\\]", "$1");
		return result;
	}

	private static String convertExternalLinks(String input) {
		// [http://url label] → [label](url)
		return input.replaceAll("\\[(https?://\\S+)\\s+([^\\]]+)\\]", "[$2]($1)");
	}

	private static String convertBoldItalic(String input) {
		String result = input;
		// Bold must be before italic since ''' starts with ''
		result = result.replaceAll("'''(.*?)'''", "**$1**");
		result = result.replaceAll("''(.*?)''", "*$1*");
		return result;
	}

	private static String convertHeadings(String input) {
		String result = input;
		result = result.replaceAll("(?m)^======\\s*(.*?)\\s*======\\s*$", "###### $1");
		result = result.replaceAll("(?m)^=====\\s*(.*?)\\s*=====\\s*$", "##### $1");
		result = result.replaceAll("(?m)^====\\s*(.*?)\\s*====\\s*$", "#### $1");
		result = result.replaceAll("(?m)^===\\s*(.*?)\\s*===\\s*$", "### $1");
		result = result.replaceAll("(?m)^==\\s*(.*?)\\s*==\\s*$", "## $1");
		return result;
	}

	private static String convertTables(String input) {
		Pattern tablePattern = Pattern.compile("(?s)\\{\\|[^\n]*\n(.*?)\\|\\}");
		Matcher m = tablePattern.matcher(input);
		StringBuilder sb = new StringBuilder();
		while(m.find()) {
			String tableContent = m.group(1);
			String mdTable = convertTableContent(tableContent);
			m.appendReplacement(sb, Matcher.quoteReplacement(mdTable));
		}
		m.appendTail(sb);
		return sb.toString();
	}

	private static String convertTableContent(String tableContent) {
		List<String[]> rows = new ArrayList<>();
		boolean hasHeader = false;
		String[] currentRow = null;

		for(String line : tableContent.split("\n")) {
			line = line.trim();
			if(line.isEmpty() || line.startsWith("{|") || line.startsWith("|}")) {
				continue;
			}
			if(line.equals("|-")) {
				if(currentRow != null) {
					rows.add(currentRow);
				}
				currentRow = null;
				continue;
			}
			if(line.startsWith("!")) {
				hasHeader = true;
				String headerLine = line.substring(1).trim();
				currentRow = splitTableCells(headerLine, "!!");
			} else if(line.startsWith("|")) {
				String dataLine = line.substring(1).trim();
				currentRow = splitTableCells(dataLine, "\\|\\|");
			}
		}
		if(currentRow != null) {
			rows.add(currentRow);
		}

		if(rows.isEmpty()) {
			return "";
		}

		int cols = 0;
		for(String[] row : rows) {
			if(row.length > cols) {
				cols = row.length;
			}
		}

		StringBuilder sb = new StringBuilder("\n");
		for(int i = 0; i < rows.size(); i++) {
			String[] row = rows.get(i);
			sb.append("|");
			for(int j = 0; j < cols; j++) {
				String cell = j < row.length ? row[j].trim() : "";
				sb.append(" ").append(cell).append(" |");
			}
			sb.append("\n");

			if(i == 0) {
				sb.append("|");
				for(int j = 0; j < cols; j++) {
					sb.append("---|");
				}
				sb.append("\n");
			}
		}
		sb.append("\n");
		return sb.toString();
	}

	private static String[] splitTableCells(String line, String separator) {
		String[] cells = line.split(separator);
		for(int i = 0; i < cells.length; i++) {
			cells[i] = cells[i].trim();
			// Cell might have format: style="..." | actual content (HTML attributes)
			if(cells[i].contains("|")) {
				cells[i] = cells[i].substring(cells[i].lastIndexOf("|") + 1).trim();
			}
		}
		return cells;
	}

	private static String convertNumberedLists(String input) {
		// # item → 1. item (wiki numbered list syntax)
		return input.replaceAll("(?m)^#\\s+", "1. ");
	}

	private static String convertRemainingHtml(String input) {
		String result = input;

		// Convert HTML formatting tags to markdown (safe everywhere)
		result = result.replaceAll("(?si)<strong>(.*?)</strong>", "**$1**");
		result = result.replaceAll("(?si)<em>(.*?)</em>", "*$1*");
		result = result.replaceAll("(?i)<br\\s*/?>", "\n");
		result = result.replaceAll("(?i)<hr\\s*/?>", "\n---\n");

		// Strip remaining HTML tags and unescape entities, but only outside code blocks
		StringBuilder sb = new StringBuilder();
		boolean inFencedBlock = false;
		for(String line : result.split("\n", -1)) {
			if(line.startsWith("```")) {
				inFencedBlock = !inFencedBlock;
				sb.append(line).append("\n");
			} else if(inFencedBlock) {
				sb.append(line).append("\n");
			} else {
				String processed = line.replaceAll("<[^>]+>", "");
				processed = unescapeWikiEntities(processed);
				sb.append(processed).append("\n");
			}
		}
		if(sb.length() > 0 && sb.charAt(sb.length() - 1) == '\n') {
			sb.setLength(sb.length() - 1);
		}
		return sb.toString();
	}

	/**
	 * Reverses the escapeWiki() encoding from DocGenTemplates, plus standard HTML entities.
	 */
	static String unescapeWikiEntities(String input) {
		String result = input;
		result = result.replace("&lsqb;", "[");
		result = result.replace("&rsqb;", "]");
		result = result.replace("&lpar;", "(");
		result = result.replace("&rpar;", ")");
		result = result.replace("&lcub;", "{");
		result = result.replace("&rcub;", "}");
		result = result.replace("&ast;", "*");
		result = result.replace("&verbar;", "|");
		result = result.replace("&equals;", "=");
		result = result.replace("&num;", "#");
		result = result.replace("&lt;", "<");
		result = result.replace("&gt;", ">");
		result = result.replace("&quot;", "\"");
		result = result.replace("&amp;", "&"); // Must be last
		return result;
	}
}
