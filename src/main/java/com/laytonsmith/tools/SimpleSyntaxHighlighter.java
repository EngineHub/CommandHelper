package com.laytonsmith.tools;

import com.laytonsmith.PureUtilities.ClassLoading.ClassDiscovery;
import com.laytonsmith.PureUtilities.Color;
import com.laytonsmith.PureUtilities.Common.StreamUtils;
import com.laytonsmith.core.FullyQualifiedClassName;
import com.laytonsmith.core.MethodScriptCompiler;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.compiler.KeywordList;
import com.laytonsmith.core.compiler.TokenStream;
import com.laytonsmith.core.constructs.NativeTypeList;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.constructs.Token;
import com.laytonsmith.core.constructs.Token.TType;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.exceptions.CRE.CRECastException;
import com.laytonsmith.tools.docgen.DocGenTemplates;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * The SimpleSyntaxHighlighter class contains a method to do HTML syntax highlighting on a given block of plain text
 * code.
 */
public final class SimpleSyntaxHighlighter {

	public static void main(String[] args) throws Exception {
		String script = "<!\nstrict: on;\n>";
		List<Token> ts = MethodScriptCompiler.lex(script, null, null, true, true);
		for(Token t : ts) {
			System.out.println(t.type);
			System.out.println(t.value);
			System.out.println(t.target);
			System.out.println("----------------");
		}
		StreamUtils.GetSystemOut().println(SimpleSyntaxHighlighter.Highlight(null, script, true));
	}

	/**
	 * A list of keywords in the MethodScript language.
	 */
	public static final Set<String> KEYWORDS;

	static {
		ClassDiscovery.getDefaultInstance().addDiscoveryLocation(ClassDiscovery.GetClassContainer(SimpleSyntaxHighlighter.class));
		KEYWORDS = Collections.unmodifiableSet(KeywordList.getKeywordNames());
	}

	private static final EnumMap<ElementTypes, Color> CLASSES = new EnumMap<>(ElementTypes.class);

	static {
		CLASSES.put(ElementTypes.COMMENT, new Color(0x88, 0x88, 0x88));
		CLASSES.put(ElementTypes.SMART_COMMENT, new Color(0x88, 0x88, 0x88));
		CLASSES.put(ElementTypes.SINGLE_STRING, new Color(0xFF, 0x99, 0x00));
		CLASSES.put(ElementTypes.DOUBLE_STRING, new Color(0xCC, 0x99, 0x00));
		CLASSES.put(ElementTypes.VAR, new Color(0x00, 0x99, 0x33));
		CLASSES.put(ElementTypes.DVAR, new Color(0x00, 0xCC, 0xFF));
		CLASSES.put(ElementTypes.BACKGROUND_COLOR, new Color(0xF9, 0xF9, 0xF9));
		CLASSES.put(ElementTypes.BORDER_COLOR, new Color(0xA7, 0xD7, 0xF9));
		CLASSES.put(ElementTypes.KEYWORD, Color.BLUE);
		CLASSES.put(ElementTypes.LINE_NUMBER, new Color(0xBD, 0xC4, 0xB1));
		CLASSES.put(ElementTypes.FUNCTION, new Color(0x00, 0x00, 0x00));
		CLASSES.put(ElementTypes.OBJECT_TYPE, Color.GRAY);
		CLASSES.put(ElementTypes.COMMAND, Color.MAGENTA);
		CLASSES.put(ElementTypes.FILE_OPTIONS_BLOCK, Color.DARK_GRAY);
		CLASSES.put(ElementTypes.FILE_OPTIONS_KEY, Color.BLUE);
		CLASSES.put(ElementTypes.FILE_OPTIONS_VALUE, Color.GRAY);
	}

	private final EnumMap<ElementTypes, Color> classes;
	private final String code;
	private final boolean inPureMscript;

	private SimpleSyntaxHighlighter(EnumMap<ElementTypes, Color> classes, String code, boolean inPureMscript) {
		this.classes = classes;
		this.code = code;
		this.inPureMscript = inPureMscript;
	}

	private String getColor(ElementTypes type) {
		if(classes == null) {
			return null;
		}
		Color c = classes.get(type);
		if(c == null) {
			c = CLASSES.get(type);
		}
		return "color: #" + getRGB(c) + ";";
	}

	private String getRGB(Color c) {
		return String.format("%02X%02X%02X", c.getRed(), c.getGreen(), c.getBlue());
	}

	private String escapeLit(String c) {
		StringBuilder b = new StringBuilder();
		c = c.replaceAll("\t", "   ");
		if(c.length() == 1) {
			b.append(DocGenTemplates.escapeWiki(c));
		} else {
			boolean inMultispace = false;
			for(int i = 0; i < c.length() - 1; i++) {
				char c1 = c.charAt(i);
				if(!Character.isWhitespace(c1)) {
					inMultispace = false;
					b.append(DocGenTemplates.escapeWiki(Character.toString(c1)));
					if(i == c.length() - 2) {
						// last run, also need to append the last character
						b.append(DocGenTemplates.escapeWiki(Character.toString(c.charAt(i + 1))));
					}
					continue;
				} else if(inMultispace) {
					if(Character.isWhitespace(c1)) {
						b.append("&nbsp;");
						continue;
					}
				}
				char c2 = c.charAt(i + 1);
				if(Character.isWhitespace(c1) && Character.isWhitespace(c2)) {
					inMultispace = true;
					b.append("&nbsp;&nbsp;");
					i++;
				} else {
					b.append(c1);
					if(i == c.length() - 2) {
						// last run, also need to append the last character
						b.append(DocGenTemplates.escapeWiki(Character.toString(c2)));
					}
				}
			}
		}
		String ret = b.toString();
		// Oops. Undo this. This is probably not ideal, we should just fix the
		// docgen class, but even if we do, this fix can stay in place.
		ret = ret.replaceAll("&amp;percnt", "&percnt");
		return ret;
	}

	private String escapeLit(char c) {
		return escapeLit(Character.toString(c));
	}

	private String getOpenSpan(ElementTypes t, String extraStyles) {
		return "<span class=\"" + t.name().toLowerCase() + "\" "
				+ (getColor(t) == null ? "" : "style=\"" + getColor(t) + "; ") + extraStyles + "\">";
	}

	private String getOpenSpan(ElementTypes t) {
		return "<span class=\"" + t.name().toLowerCase() + "\" "
				+ (getColor(t) == null ? "" : "style=\"" + getColor(t) + "\"") + ">";
	}

	private String getCloseSpan() {
		return "</span>";
	}

	private String highlight() throws Exception {
		Environment env = Static.GenerateStandaloneEnvironment(false);
		TokenStream tokens = MethodScriptCompiler.lex(code, env, null, inPureMscript, true);
		// take out the last token, which is always a newline
		tokens.remove(tokens.size() - 1);
		// if the first token is a newline, also take that out.
		if(tokens.get(0).type == TType.NEWLINE) {
			tokens.remove(0);
		}
		String newlineString = "<div><span style=\"font-style: italic; " + getColor(ElementTypes.LINE_NUMBER) + "\">"
				+ "%0" + Integer.toString(tokens.get(tokens.size() - 1).lineNum - 1).length() + "d</span>&nbsp;&nbsp;&nbsp;";
		StringBuilder out = new StringBuilder();
		AtomicInteger lineNum = new AtomicInteger(1);
		out.append(String.format(newlineString, lineNum.get()));
		for(Token t : tokens) {
			if(null != t.type) {
				switch(t.type) {
					case SMART_COMMENT:
					case COMMENT:
						for(String line : t.val().split("\n")) {
							out.append(getOpenSpan(t.type == TType.SMART_COMMENT
									? ElementTypes.SMART_COMMENT : ElementTypes.COMMENT))
									.append(escapeLit(line))
									.append(getCloseSpan());
							// If this is the last token, don't do this
							// Note that this is a rare instance where reference comparison using == is valid,
							// this is not a bug.
							if(t != tokens.get(tokens.size() - 1)) {
								out.append("</div>").append(String.format(newlineString, lineNum.addAndGet(1)));
							}
						}
						break;
					case SMART_STRING:
						out.append(getOpenSpan(ElementTypes.DOUBLE_STRING)).append("&quot;");
						out.append(processDoubleString(t.toOutputString()));
						out.append("&quot;").append(getCloseSpan());
						break;
					case VARIABLE:
						out.append(getOpenSpan(ElementTypes.DVAR));
						out.append(escapeLit(t.val()));
						out.append(getCloseSpan());
						break;
					case FUNC_NAME:
						if(t.val().equals("__autoconcat__")) {
							// This (currently) only happens when there are loose parenthesis. In this
							// case, we just want to get rid of this token, so we skip it.
							break;
						}
						out.append(getOpenSpan(ElementTypes.FUNCTION, "font-style: italic"));
						out.append("{{function|").append(escapeLit(t.val())).append("}}");
						out.append(getCloseSpan());
						break;
					case KEYWORD:
						out.append(getOpenSpan(ElementTypes.KEYWORD));
						out.append("{{keyword|").append(escapeLit(t.val())).append("}}");
						out.append(getCloseSpan());
						break;
					case STRING:
						out.append(getOpenSpan(ElementTypes.SINGLE_STRING));
						out.append("&apos;").append(escapeLit(t.toOutputString())).append("&apos;");
						out.append(getCloseSpan());
						break;
					case IVARIABLE:
						out.append(getOpenSpan(ElementTypes.VAR));
						out.append(escapeLit(t.val()));
						out.append(getCloseSpan());
						break;
					case LIT:
						String lit = t.val();
						try {
							FullyQualifiedClassName fqcn = FullyQualifiedClassName.forName(lit, Target.UNKNOWN, env);
							if(NativeTypeList.getNativeTypeList().contains(fqcn)) {
								out.append(getOpenSpan(ElementTypes.OBJECT_TYPE));
								out.append("{{object|").append(t.val()).append("}}");
								out.append(getCloseSpan());
							} else {
								out.append(escapeLit(t.val()));
							}
						} catch (CRECastException e) {
							out.append(escapeLit(t.val()));
						}
						break;
					case COMMAND:
						out.append(getOpenSpan(ElementTypes.COMMAND));
						out.append(t.val());
						out.append(getCloseSpan());
						break;
					case NEWLINE:
						out.append("</div>").append(String.format(newlineString, lineNum.addAndGet(1)));
						break;
					case WHITESPACE:
						out.append(escapeLit(t.val()));
						break;
					case FILE_OPTIONS_START:
						out.append(getOpenSpan(ElementTypes.FILE_OPTIONS_BLOCK));
						out.append(escapeLit(t.val()));
						out.append(getCloseSpan());
						break;
					case FILE_OPTIONS_END:
						out.append(getOpenSpan(ElementTypes.FILE_OPTIONS_BLOCK));
						out.append(escapeLit(t.val()));
						out.append(getCloseSpan());
						break;
					case FILE_OPTIONS_STRING:
						out.append(processFileOptionsString(newlineString, lineNum, t.val()));
						break;
					default:
						out.append(escapeLit(t.val()));
						break;
				}
			}
		}
		out.append("</div>");
		String totalOutput = "<div style=\"font-family: 'Consolas','DejaVu Sans','Lucida Console',monospace; "
				+ (classes == null ? "" : "background-color: #"
						+ getRGB(classes.get(ElementTypes.BACKGROUND_COLOR)) + ";"
						+ " border-color: #" + getRGB(classes.get(ElementTypes.BORDER_COLOR))
						+ "; ")
				+ " border-style: solid; border-width: 1px 0px 1px 0px; margin: 1em 2em;"
				+ " padding: 12px 2px 1em 1em;\" class=\"methodscript_code\">" + out.toString() + "</div>";
		return totalOutput;
	}

	private String processFileOptionsString(String newLineString, AtomicInteger lineNum, String value) {
		boolean inKey = true;
		StringBuilder builder = new StringBuilder();
		builder.append(getOpenSpan(ElementTypes.FILE_OPTIONS_KEY));
		for(int i = 0; i < value.length(); i++) {
			char c = value.charAt(i);
			char c2 = '\0';
			if(i + 1 < value.length()) {
				c2 = value.charAt(i + 1);
			}
			if(c == '\n') {
				builder.append(getCloseSpan())
						.append("</div>")
						.append(String.format(newLineString, lineNum.addAndGet(1)))
						.append(getOpenSpan(inKey ? ElementTypes.FILE_OPTIONS_KEY : ElementTypes.FILE_OPTIONS_VALUE));
				continue;
			}
			if(inKey) {
				if(c == ':') {
					inKey = false;
					builder.append(getCloseSpan())
							.append(getOpenSpan(ElementTypes.FILE_OPTIONS_BLOCK))
							.append(':')
							.append(getCloseSpan())
							.append(getOpenSpan(ElementTypes.FILE_OPTIONS_VALUE));
					continue;
				}
				if(c == ';') {
					builder.append(getCloseSpan())
							.append(getOpenSpan(ElementTypes.FILE_OPTIONS_BLOCK))
							.append(';')
							.append(getCloseSpan())
							.append(getOpenSpan(ElementTypes.FILE_OPTIONS_KEY));
					continue;
				}
				builder.append(c);
			} else {
				if(c == '\\' && c2 == ';') {
					builder.append("\\;");
					i++;
					continue;
				}
				if(c == ';') {
					builder.append(getCloseSpan())
							.append(getOpenSpan(ElementTypes.FILE_OPTIONS_BLOCK))
							.append(';')
							.append(getCloseSpan());
					inKey = true;
					builder.append(getOpenSpan(ElementTypes.FILE_OPTIONS_KEY));
					continue;
				}
				builder.append(c);
			}
		}
		builder.append(getCloseSpan());

		return builder.toString();
	}

	/**
	 * Processes and highlights double strings
	 *
	 * @param buffer
	 * @return
	 */
	private String processDoubleString(String value) {
		StringBuilder b = new StringBuilder();
		StringBuilder brace = new StringBuilder();
		boolean inSimpleVar = false;
		boolean inBrace = false;
		for(int i = 0; i < value.length(); i++) {
			char c = value.charAt(i);
			char c2 = (i + 1 < value.length() ? value.charAt(i + 1) : '\0');
			if(c == '\\' && c2 == '@') {
				b.append("\\@");
				i++;
				continue;
			}
			if(c == '@') {
				if(Character.isLetterOrDigit(c2) || c2 == '_') {
					inSimpleVar = true;
					b.append("<span style=\"").append(getColor(ElementTypes.VAR)).append("\">@");
					continue;
				} else if(c2 == '{') {
					inBrace = true;
					b.append("<span style=\"").append(getColor(ElementTypes.VAR)).append("\">@{</span>");
					i++;
					continue;
				}
			}
			if(inSimpleVar && !(Character.isLetterOrDigit(c) || c == '_')) {
				inSimpleVar = false;
				b.append("</span>");
			}
			if(inBrace && c == '}') {
				inBrace = false;
				b.append(processBraceString(brace.toString()));
				brace = new StringBuilder();
				b.append("<span style=\"").append(getColor(ElementTypes.VAR)).append("\">}</span>");
				continue;
			}
			if(!inBrace) {
				b.append(escapeLit(c));
			} else {
				brace.append(escapeLit(c));
			}
		}
		if(inSimpleVar || inBrace) {
			b.append("</span>");
		}
		return b.toString();
	}

	/**
	 * Brace strings are more complicated, so do this processing separately.
	 *
	 * @param v
	 * @return
	 */
	private String processBraceString(String value) {
		StringBuilder b = new StringBuilder();
		boolean inVarName = true;
		boolean inString = false;
		b.append("<span style=\"").append(getColor(ElementTypes.VAR)).append("\">");
		for(int i = 0; i < value.length(); i++) {
			char c = value.charAt(i);
			char c2 = (i + 1 < value.length() ? value.charAt(i + 1) : '\0');
			if(c == '[' && inVarName) {
				inVarName = false;
				b.append("</span>");
			}
			if(c == '[' && !inString) {
				b.append("<span style=\"").append(getColor(ElementTypes.VAR)).append("\">[</span>");
				continue;
			}
			if(c == ']' && !inString) {
				b.append("<span style=\"").append(getColor(ElementTypes.VAR)).append("\">]</span>");
				continue;
			}
			if(c == '\\' && c2 == '\'' && inString) {
				b.append("\\'");
				continue;
			}
			if(c == '\'') {
				inString = !inString;
			}
			b.append(escapeLit(c));
		}
		if(inVarName) {
			b.append("</span>");
		}
		return b.toString();
	}

	/**
	 * Highlights the given code, using the default color scheme.
	 *
	 * @param code The plain text code
	 * @return The HTML highlighted code
	 */
	public static String Highlight(String code, boolean inPureMscript) throws Exception {
		return new SimpleSyntaxHighlighter(CLASSES, code, inPureMscript).highlight();
	}

	/**
	 * Highlights the given code, using the specified color scheme. If any elements are missing from the EnumMap, the
	 * default color is used.
	 *
	 * @param colors A list of colors for each element type
	 * @param code The plain text code
	 * @return The HTML highlighted code
	 */
	public static String Highlight(EnumMap<ElementTypes, Color> colors, String code, boolean inPureMscript) throws Exception {
		return new SimpleSyntaxHighlighter(colors, code, inPureMscript).highlight();
	}

	public enum ElementTypes {
		/**
		 * Denotes a comment, either line or block
		 */
		COMMENT,
		/**
		 *
		 */
		SMART_COMMENT,
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
		/**
		 * The color of (known) object types
		 */
		OBJECT_TYPE,
		/**
		 * The color of commands in msa files
		 */
		COMMAND,
		/**
		 * The color of a file options block
		 */
		FILE_OPTIONS_BLOCK,
		/**
		 * The color of a key in the file options
		 */
		FILE_OPTIONS_KEY,
		/**
		 * The color of a value in the file options
		 */
		FILE_OPTIONS_VALUE,;
	}
}
