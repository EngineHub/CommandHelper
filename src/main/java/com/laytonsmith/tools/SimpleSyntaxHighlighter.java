package com.laytonsmith.tools;

import com.laytonsmith.PureUtilities.Common.HTMLUtils;
import java.awt.Color;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.regex.Pattern;

/**
 * The SimpleSyntaxHighlighter class contains a method to do HTML syntax highlighting on
 * a given block of plain text code.
 */
public class SimpleSyntaxHighlighter {
	
	/**
	 * A list of keywords in the MethodScript language.
	 */
	public static final List<String> KEYWORDS;
	
	static {
		KEYWORDS = Collections.unmodifiableList(Arrays.asList(new String[]{
			"true", "false", "null", "else", "case", "default", "as", "in"
		}));
	}
	
	private static final EnumMap<ElementTypes, Color> CLASSES = new EnumMap<>(ElementTypes.class);
	static{
		CLASSES.put(ElementTypes.COMMENT, new Color(0x88, 0x88, 0x88));
		CLASSES.put(ElementTypes.SINGLE_STRING, new Color(0xFF, 0x99, 0x00));
		CLASSES.put(ElementTypes.DOUBLE_STRING, new Color(0xCC, 0x99, 0x00));
		CLASSES.put(ElementTypes.VAR, new Color(0x00, 0x99, 0x33));
		CLASSES.put(ElementTypes.DVAR, new Color(0x00, 0xCC, 0xFF));
		CLASSES.put(ElementTypes.BACKGROUND_COLOR, new Color(0xF9, 0xF9, 0xF9));
		CLASSES.put(ElementTypes.BORDER_COLOR, new Color(0xA7, 0xD7, 0xF9));
		CLASSES.put(ElementTypes.KEYWORD, Color.BLUE);
		CLASSES.put(ElementTypes.LINE_NUMBER, new Color(0xBD, 0xC4, 0xB1));
		CLASSES.put(ElementTypes.FUNCTION, new Color(0x00, 0x00, 0x00));
	}
	
	private final EnumMap<ElementTypes, Color> classes;
	private final String code;
	
	private SimpleSyntaxHighlighter(EnumMap<ElementTypes, Color> classes, String code){
		this.classes = classes;
		this.code = code;
	}
	
	
	
	private String getColor(ElementTypes type){
		Color c = classes.get(type);
		if(c == null){
			c = CLASSES.get(type);
		}
		return "color: #" + getRGB(c) + ";";
	}
	
	private String getRGB(Color c){
		return String.format("%02X%02X%02X", c.getRed(), c.getGreen(), c.getBlue());
	}
	
	private String highlight(){
		String[] lines = code.split("\r\n|\n\r|\n");
		StringBuilder out = new StringBuilder();
		boolean blankLine = false;
		//We're gonna write a mini parser here, so here's our state variables.
		boolean inDoubleString = false;
		boolean inSingleString = false;
		boolean inLineComment = false;
		boolean inBlockComment = false;
		boolean inDollarVar = false;
		boolean inVar = false;
		for (int i = 0; i < lines.length; i++) {
			if (i == 0 && "".equals(lines[0].trim())) {
				blankLine = true;
				continue;
			}
			StringBuilder lout = new StringBuilder();
			if (inBlockComment) {
				lout.append("<span style=\"").append(getColor(ElementTypes.COMMENT)).append("\">");
			}
			if (inDoubleString) {
				lout.append("<span style=\"").append(getColor(ElementTypes.DOUBLE_STRING)).append("\">");
			}
			if (inSingleString) {
				lout.append("<span style=\"").append(getColor(ElementTypes.SINGLE_STRING)).append("\">");
			}
			String buffer = "";
			for (int j = 0; j < lines[i].length(); j++) {
				char c = lines[i].charAt(j);
				char c2 = (j + 1 < lines[i].length() ? lines[i].charAt(j + 1) : '\0');
				if (inSingleString) {
					if (c == '\\' && c2 == '\'') {
						buffer += "\\&apos;";
						j++;
						continue;
					} else if (c == '\'') {
						inSingleString = false;
						lout.append("<span style=\"").append(getColor(ElementTypes.SINGLE_STRING)).append("\">").append(buffer).append("&apos;</span>");
						buffer = "";
						continue;
					}
				}
				if (inDoubleString) {
					if (c == '\\' && c2 == '"') {
						buffer += "\\&quot;";
						j++;
						continue;
					} else if (c == '"') {
						inDoubleString = false;
						lout.append("<span style=\"").append(getColor(ElementTypes.DOUBLE_STRING)).append("\">").append(buffer).append("&quot;</span>");
						buffer = "";
						continue;
					}
				}
				if (inVar) {
					if (!Character.toString(c).matches("[a-zA-Z0-9_]")) {
						lout.append(buffer).append("</span>");
						buffer = "";
						inVar = false;
					}
				}
				if (inDollarVar) {
					if (!Character.toString(c).matches("[a-zA-Z0-9_]")) {
						lout.append(buffer).append("</span>");
						buffer = "";
						inDollarVar = false;
					}
				}
				if (!inDoubleString && !inSingleString && !inBlockComment && (c == '#' || (c == '/' && c2 == '/'))) {
					lout.append(processBuffer(buffer)).append("<span style=\"").append(getColor(ElementTypes.COMMENT)).append("\">");
					if(c == '#'){
						lout.append("#");
					} else {
						lout.append("//");
						j++;
					}
					buffer = "";
					inLineComment = true;
					continue;
				}
				if (!inDoubleString && !inSingleString && !inLineComment && c == '/' && c2 == '*') {
					lout.append(processBuffer(buffer));
					buffer = "";
					lout.append("<span style=\"").append(getColor(ElementTypes.COMMENT)).append("\">/*");
					j++;
					inBlockComment = true;
					continue;
				}
				if (c == '\'' && !inDoubleString && !inLineComment && !inBlockComment) {
					lout.append(processBuffer(buffer));
					buffer = "";
					inSingleString = true;
				}
				if (c == '"' && !inSingleString && !inLineComment && !inBlockComment) {
					lout.append(processBuffer(buffer));
					buffer = "";
					inDoubleString = true;
				}
				if (c == '*' && c2 == '/') {
					lout.append(buffer).append("*/</span>");
					buffer = "";
					j++;
					inBlockComment = false;
					continue;
				}
				if (!inDoubleString && !inSingleString && !inLineComment && !inBlockComment && c == '@') {
					lout.append(processBuffer(buffer)).append("<span style=\"").append(getColor(ElementTypes.VAR)).append("\">");
					buffer = "";
					inVar = true;
				}
				if (!inDoubleString && !inSingleString && !inLineComment && !inBlockComment && c == '$') {
					lout.append(processBuffer(buffer)).append("<span style=\"").append(getColor(ElementTypes.DVAR)).append("\">$");
					buffer = "";
					if (!Character.toString(c2).matches("[a-zA-Z0-9_]")) {
						//Done, it's final var
						lout.append("</span>");
					} else {
						inDollarVar = true;
					}
					continue;
				}
				buffer += c;
			}
			if(inLineComment){
				lout.append(buffer);
			} else {
				lout.append(processBuffer(buffer));
			}
			if (inBlockComment || inVar || inLineComment || inDollarVar || inSingleString || inDoubleString) {
				inVar = false;
				inLineComment = false;
				inDollarVar = false;
				lout.append("</span>");
			}
			out.append("<span style=\"font-style: italic; ").append(getColor(ElementTypes.LINE_NUMBER))
					.append("\">").append(blankLine ? i : i + 1).append("</span>&nbsp;&nbsp;&nbsp;").append(lout.toString()).append("<br />\n");
		}
		return "<div style=\"font-family: 'Consolas','DejaVu Sans','Lucida Console',monospace; background-color: #" 
					+ getRGB(classes.get(ElementTypes.BACKGROUND_COLOR)) + ";"
					+ " border-color: #" + getRGB(classes.get(ElementTypes.BORDER_COLOR)) 
					+ "; border-style: solid; border-width: 1px 0px 1px 0px; margin: 1em 2em;"
					+ " padding: 0 0 0 1em;\">\n" + out.toString().replace("\t", "&nbsp;&nbsp;&nbsp;") + "</div>\n";
	}
	
	private static final String FUNCTION_PATTERN = "([^a-zA-Z0-9_])?([a-zA-Z_]*[a-zA-Z0-9_]+)((?:&nbsp;)*\\()";
	/**
	 * Unknown buffer text should be sent here for processing for keywords.
	 * @param buffer
	 * @return 
	 */
	private String processBuffer(String buffer){
		buffer = HTMLUtils.escapeHTML(buffer).replace(" ", "&nbsp;");
		for(String keyword : KEYWORDS){
			buffer = buffer.replaceAll("([^a-zA-Z0-9_])(" + Pattern.quote(keyword) + ")([^a-zA-Z0-9_])", 
					"$1<span style=\"" + getColor(ElementTypes.KEYWORD) + "\">$2</span>$3");
		}
		buffer = buffer.replaceAll(FUNCTION_PATTERN, "$1<span style=\"font-style: italic; " + getColor(ElementTypes.FUNCTION) + "\">$2</span>$3");
		return buffer;
	}

	/**
	 * Highlights the given code, using the default color scheme.
	 * @param code The plain text code
	 * @return The HTML highlighted code
	 */
	public static String Highlight(String code) {
		return new SimpleSyntaxHighlighter(CLASSES, code).highlight();
	}
	
	/**
	 * Highlights the given code, using the specified color scheme. If any
	 * elements are missing from the EnumMap, the default color is used.
	 * @param colors A list of colors for each element type
	 * @param code The plain text code
	 * @return The HTML highlighted code
	 */
	public static String Highlight(EnumMap<ElementTypes, Color> colors, String code){
		return new SimpleSyntaxHighlighter(colors, code).highlight();
	}
	
	public enum ElementTypes {
		/**
		 * Denotes a comment, either line or block
		 */
		COMMENT,
		/**
		 * Denotes a string with single quotes
		 */
		SINGLE_STRING,
		/**
		 * Denotes a string with double quotes
		 */
		DOUBLE_STRING,
		/**
		 * Denotes a @var
		 */
		VAR,
		/**
		 * Denotes a $var
		 */
		DVAR,
		/**
		 * The background color of the text field
		 */
		BACKGROUND_COLOR,
		/**
		 * The border color of the text field
		 */
		BORDER_COLOR,
		/**
		 * Denotes a keyword, "true", "false", "null", etc.
		 */
		KEYWORD,
		/**
		 * The color of the line numbers in the left margin
		 */
		LINE_NUMBER,
		/**
		 * The color of function/proc names
		 */
		FUNCTION,
		;
	}
}
