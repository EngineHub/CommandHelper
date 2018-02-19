package com.laytonsmith.core;

import com.laytonsmith.abstraction.Implementation;
import com.laytonsmith.annotations.breakable;
import com.laytonsmith.annotations.nolinking;
import com.laytonsmith.annotations.unbreakable;
import com.laytonsmith.commandhelper.CommandHelperPlugin;
import com.laytonsmith.core.Optimizable.OptimizationOption;
import com.laytonsmith.core.compiler.FileOptions;
import com.laytonsmith.core.compiler.KeywordList;
import com.laytonsmith.core.compiler.TokenStream;
import com.laytonsmith.core.constructs.CDecimal;
import com.laytonsmith.core.constructs.CDouble;
import com.laytonsmith.core.constructs.CFunction;
import com.laytonsmith.core.constructs.CIdentifier;
import com.laytonsmith.core.constructs.CInt;
import com.laytonsmith.core.constructs.CKeyword;
import com.laytonsmith.core.constructs.CLabel;
import com.laytonsmith.core.constructs.CNull;
import com.laytonsmith.core.constructs.CPreIdentifier;
import com.laytonsmith.core.constructs.CSlice;
import com.laytonsmith.core.constructs.CString;
import com.laytonsmith.core.constructs.CSymbol;
import com.laytonsmith.core.constructs.CVoid;
import com.laytonsmith.core.constructs.Construct;
import com.laytonsmith.core.constructs.IVariable;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.constructs.Token;
import com.laytonsmith.core.constructs.Token.TType;
import com.laytonsmith.core.constructs.Variable;
import com.laytonsmith.core.environments.CommandHelperEnvironment;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.environments.GlobalEnv;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import com.laytonsmith.core.exceptions.ConfigCompileGroupException;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.exceptions.ProgramFlowManipulationException;
import com.laytonsmith.core.functions.Compiler;
import com.laytonsmith.core.functions.DataHandling;
import com.laytonsmith.core.functions.Function;
import com.laytonsmith.core.functions.FunctionBase;
import com.laytonsmith.core.functions.FunctionList;
import com.laytonsmith.core.functions.IncludeCache;
import com.laytonsmith.core.taskmanager.TaskManager;
import com.laytonsmith.persistence.DataSourceException;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EmptyStackException;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

/**
 * The MethodScriptCompiler class handles the various stages of compilation and provides helper methods for execution of
 * the compiled trees.
 */
public final class MethodScriptCompiler {

    private final static EnumSet<Optimizable.OptimizationOption> NO_OPTIMIZATIONS = EnumSet.noneOf(Optimizable.OptimizationOption.class);

    private MethodScriptCompiler() {
    }

    private static final Pattern VAR_PATTERN = Pattern.compile("\\$[\\p{L}0-9_]+");
    private static final Pattern IVAR_PATTERN = Pattern.compile(IVariable.VARIABLE_NAME_REGEX);

    /**
     * Lexes the script, and turns it into a token stream. This looks through the script character by character.
     *
     * @param script The script to lex
     * @param file The file this script came from, or potentially null if the code is from a dynamic source
     * @param inPureMScript If the script is in pure MethodScript, this should be true. Pure MethodScript is defined as
     * code that doesn't have command alias wrappers.
     * @return A stream of tokens
     * @throws ConfigCompileException If compilation fails due to bad syntax
     */
    public static TokenStream lex(String script, File file, boolean inPureMScript) throws ConfigCompileException {
        return lex(script, file, inPureMScript, false);
    }

	/**
	 * Lexes the script, and turns it into a token stream. This looks through the script character by character.
	 *
	 * @param script The script to lex
	 * @param file The file this script came from, or potentially null if the code is from a dynamic source
	 * @param inPureMScript If the script is in pure MethodScript, this should be true. Pure MethodScript is defined as
	 * code that doesn't have command alias wrappers.
	 * @param saveAllTokens If this script is planning to be compiled, then this value should always be false, however,
	 * if the calling code needs all tokens for informational purposes (and doesn't plan on actually compiling the code)
	 * then this can be true. If true, all tokens are saved, including comments and (some) whitespace. Given this lexing
	 * stream, the exact source code could be re-constructed.
	 *
	 * A note on whitespace: The whitespace tokens are not guaranteed to be accurate, however, the column information
	 * is. If you have two tokens t1 and t2, each with a value of length 1, where the columns are 1 and 5, then that
	 * means there are 4 spaces between the two.
	 * @return A stream of tokens
	 * @throws ConfigCompileException If compilation fails due to bad syntax
	 */
	public static TokenStream lex(String script, File file, boolean inPureMScript, boolean saveAllTokens) throws ConfigCompileException {
		if(script.isEmpty()) {
			return new TokenStream(new ArrayList<>(), "");
		}
		if((int) script.charAt(0) == 65279) {
			// Remove the UTF-8 Byte Order Mark, if present.
			script = script.substring(1);
		}
		StringBuilder fileOptions = new StringBuilder();
		StringBuilder fileOptionsBuf = null;
		script = script.replaceAll("\r\n", "\n");
		script = script + "\n";
		Set<String> keywords = KeywordList.getKeywordNames();
		List<Token> token_list = new ArrayList<>();
		
		// Set our state variables.
		boolean state_in_quote = false;
		int quoteLineNumberStart = 1;
		boolean in_smart_quote = false;
		int smartQuoteLineNumberStart = 1;
		boolean in_comment = false;
		int commentLineNumberStart = 1;
		boolean comment_is_block = false;
		boolean in_opt_var = false;
		boolean inCommand = (!inPureMScript);
		boolean inMultiline = false;
		boolean in_hash_comment = false;
		boolean in_smart_comment = false;
		boolean in_file_options = false;
		int fileOptionsLineNumberStart = 1;
		
		StringBuilder buf = new StringBuilder();
		int line_num = 1;
		int column = 1;
		int lastColumn = 0;
		Target target = Target.UNKNOWN;
		
		// Lex the script character by character.
		for(int i = 0; i < script.length(); i++) {
			Character c = script.charAt(i);
			Character c2 = null;
			if(i < script.length() - 1) {
				c2 = script.charAt(i + 1);
			}
			
			column += i - lastColumn;
			lastColumn = i;
			if(c == '\n') {
				line_num++;
				column = 1;
				if(!inMultiline && !inPureMScript) {
					inCommand = true;
				}
			}
			if(buf.length() == 0) {
				target = new Target(line_num, file, column);
			}
			
			// Comment handling. This is bypassed if we are in a string.
			if(!state_in_quote && !in_smart_quote) {
				switch(c) {
					
					// Block comments start (/* and /**) and Double slash line comment start (//).
					case '/': {
						if(!in_comment) {
							if(c2 == '*') { // "/*" or "/**".
								buf.append("/*");
								in_comment = true;
								comment_is_block = true;
								if(i < script.length() - 2 && script.charAt(i + 2) == '*') { // "/**".
									in_smart_comment = true;
									buf.append("*");
									i++;
								}
								commentLineNumberStart = line_num;
								i++;
								
								// Handle file option tokens.
								if(saveAllTokens && fileOptionsBuf != null && fileOptionsBuf.length() > 0) {
									token_list.add(new Token(
											TType.FILE_OPTIONS_STRING, fileOptionsBuf.toString(), target));
									fileOptions.append(fileOptionsBuf);
									fileOptionsBuf = new StringBuilder();
								}
								
								continue;
							} else if(c2 == '/') { // "//".
								buf.append("//");
								in_comment = true;
								comment_is_block = false;
								i++;
								
								// Handle file option tokens.
								if(saveAllTokens && fileOptionsBuf != null && fileOptionsBuf.length() > 0) {
									token_list.add(new Token(
											TType.FILE_OPTIONS_STRING, fileOptionsBuf.toString(), target));
									fileOptions.append(fileOptionsBuf);
									fileOptionsBuf = new StringBuilder();
								}
								
								continue;
							}
						}
						break;
					}
					
					// Line comment start (#).
					case '#': {
						if(!in_comment) { // "#".
							buf.append("#");
							in_comment = true;
							comment_is_block = false;
							in_hash_comment = true;
							
							// Handle file option tokens.
							if(saveAllTokens && fileOptionsBuf != null && fileOptionsBuf.length() > 0) {
								token_list.add(new Token(
										TType.FILE_OPTIONS_STRING, fileOptionsBuf.toString(), target));
								fileOptions.append(fileOptionsBuf);
								fileOptionsBuf = new StringBuilder();
							}
							
							continue;
						}
						break;
					}
					
					// Block comment end (*/).
					case '*': {
						if(in_comment && comment_is_block && c2 == '/') { // "*/".
							if(saveAllTokens || in_smart_comment) {
								buf.append("*/");
								token_list.add(new Token(in_smart_comment ? TType.SMART_COMMENT : TType.COMMENT,
										buf.toString(), target));
							}
							buf = new StringBuilder();
							target = new Target(line_num, file, column);
							in_comment = false;
							comment_is_block = false;
							in_smart_comment = false;
							i++;
							continue;
						}
						break;
					}
					
					// Line comment end (\n).
					case '\n': {
						if(in_comment && !comment_is_block) { // "\n".
							in_comment = false;
							in_hash_comment = false;
							if(saveAllTokens) {
								token_list.add(new Token(TType.COMMENT, buf.toString(), target));
								token_list.add(new Token(TType.NEWLINE, "\n", new Target(line_num + 1, file, 0)));
							}
							buf = new StringBuilder();
							target = new Target(line_num, file, column);
							continue;
						}
						break;
					}
				}
			}
			
			// If we are in a comment, add the character to the buffer.
			if(in_comment) {
				buf.append(c);
				continue;
			}
			
			// If we are in file options, add the character to the buffer if it's not a file options end character.
			if(in_file_options) {
				// For a '>' character outside of a comment, '\>' would have to be used in file options.
				// Other characters than '>'cannot be escaped.
				// If support for more escaped characters would be desired in the future, it could be added here.
				switch(c) {
					case '\\': {
						if(c2 == '>') { // "\>".
							fileOptions.append('>');
							i++;
							continue;
						}
					}
					case '>': {
						if(saveAllTokens) {
							token_list.add(new Token(TType.FILE_OPTIONS_STRING, fileOptionsBuf.toString(), target));
							token_list.add(new Token(TType.FILE_OPTIONS_END, ">", target));
						}
						fileOptions.append(fileOptionsBuf);
						fileOptionsBuf = null;
						in_file_options = false;
						continue;
					}
				}
				fileOptions.append(c);
				continue;
			}
			
			// Handle non-comment non-quoted characters.
			if(!state_in_quote) {
				// We're not in a comment or quoted string, handle: +=, -=, *=, /=, .=, ->, ++, --, %, **, *, +, -, /,
				// >=, <=, <<<, >>>, <, >, ===, !==, ==, !=, &&&, |||, &&, ||, !, {, }, .., ., ::, [, =, ], :, comma,
				// (, ), ;, and whitespace.
				matched: {
					Token token;
					switch(c) {
						case '+': {
							if(c2 == '=') { // "+=".
								token = new Token(TType.PLUS_ASSIGNMENT, "+=", target);
								i++;
							} else if(c2 == '+') { // "++".
								token = new Token(TType.INCREMENT, "++", target);
								i++;
							} else { // "+".
								token = new Token(TType.PLUS, "+", target);
							}
							break;
						}
						case '-': {
							if(c2 == '=') { // "-=".
								token = new Token(TType.MINUS_ASSIGNMENT, "-=", target);
								i++;
							} else if(c2 == '-') { // "--".
								token = new Token(TType.DECREMENT, "--", target);
								i++;
							} else if(c2 == '>') { // "->".
								token = new Token(TType.DEREFERENCE, "->", target);
								i++;
							} else { // "-".
								token = new Token(TType.MINUS, "-", target);
							}
							break;
						}
						case '*': {
							if(c2 == '=') { // "*=".
								token = new Token(TType.MULTIPLICATION_ASSIGNMENT, "*=", target);
								i++;
							} else if(c2 == '*') { // "**".
								token = new Token(TType.EXPONENTIAL, "**", target);
								i++;
							} else { // "*".
								token = new Token(TType.MULTIPLICATION, "*", target);
							}
							break;
						}
						case '/': {
							if(c2 == '=') { // "/=".
								token = new Token(TType.DIVISION_ASSIGNMENT, "/=", target);
								i++;
							} else { // "/".
								// Protect against matching commands.
								if(Character.isLetter(c2)) {
									break matched; // Pretend that division didn't match.
								}
								token = new Token(TType.DIVISION, "/", target);
							}
							break;
						}
						case '.': {
							if(c2 == '=') { // ".=".
								token = new Token(TType.CONCAT_ASSIGNMENT, ".=", target);
								i++;
							} else if(c2 == '.') { // "..".
								token = new Token(TType.SLICE, "..", target);
								i++;
							} else { // ".".
								token = new Token(TType.DOT, ".", target);
							}
							break;
						}
						case '%': {
							token = new Token(TType.MODULO, "%", target);
							break;
						}
						case '>': {
							if(c2 == '=') { // ">=".
								token = new Token(TType.GTE, ">=", target);
								i++;
							} else if(c2 == '>' && i < script.length() - 2 && script.charAt(i + 2) == '>') { // ">>>".
								token = new Token(TType.MULTILINE_START, ">>>", target);
								inMultiline = true;
								i += 2;
							} else { // ">".
								token = new Token(TType.GT, ">", target);
							}
							break;
						}
						case '<': {
							if(c2 == '!') { // "<!".
								if(line_num != 1 || column != 1) {
									throw new ConfigCompileException(
											"File options start is only allowed at the top of a file", target);
								}
								
								// Add previous characters as UNKNOWN token. Note that this will never happen as long
								// as '<!' has to be the first token in a script, but this is more update proof.
								if(buf.length() > 0) {
									token_list.add(new Token(TType.UNKNOWN, buf.toString(), target));
									buf = new StringBuilder();
									target = new Target(line_num, file, column);
								}
								
								if(saveAllTokens) {
									token_list.add(new Token(TType.FILE_OPTIONS_START, "<!", target));
								}
								fileOptionsBuf = new StringBuilder();
								in_file_options = true;
								fileOptionsLineNumberStart = line_num;
								i++;
								continue;
							} else if(c2 == '=') { // "<=".
								token = new Token(TType.LTE, "<=", target);
								i++;
							} else if(c2 == '<' && i < script.length() - 2 && script.charAt(i + 2) == '<') { // "<<<".
								token = new Token(TType.MULTILINE_END, "<<<", target);
								inMultiline = false;
								i += 2;
							} else { // "<".
								token = new Token(TType.LT, "<", target);
							}
							break;
						}
						case '=': {
							if(c2 == '=') {
								if(i < script.length() - 2 && script.charAt(i + 2) == '=') { // "===".
									token = new Token(TType.STRICT_EQUALS, "===", target);
									i += 2;
								} else { // "==".
									token = new Token(TType.EQUALS, "==", target);
									i++;
								}
							} else { // "=".
								if(inCommand) {
									if(in_opt_var) {
										token = new Token(TType.OPT_VAR_ASSIGN, "=", target);
									} else {
										token = new Token(TType.ALIAS_END, "=", target);
										inCommand = false;
									}
								} else {
									token = new Token(TType.ASSIGNMENT, "=", target);
								}
							}
							break;
						}
						case '!': {
							if(c2 == '=') {
								if(i < script.length() - 2 && script.charAt(i + 2) == '=') { // "!==".
									token = new Token(TType.STRICT_NOT_EQUALS, "!==", target);
									i += 2;
								} else { // "!=".
									token = new Token(TType.NOT_EQUALS, "!=", target);
									i++;
								}
							} else { // "!".
								token = new Token(TType.LOGICAL_NOT, "!", target);
							}
							break;
						}
						case '&': {
							if(c2 == '&') {
								if(i < script.length() - 2 && script.charAt(i + 2) == '&') { // "&&&".
									token = new Token(TType.DEFAULT_AND, "&&&", target);
									i += 2;
								} else { // "&&".
									token = new Token(TType.LOGICAL_AND, "&&", target);
									i++;
								}
							} else { // "&".
								// Bitwise symbols are not used yet.
								break matched; // Pretend that bitwise AND didn't match.
//								token = new Token(TType.BIT_AND, "&", target);
							}
							break;
						}
						case '|': {
							if(c2 == '|') {
								if(i < script.length() - 2 && script.charAt(i + 2) == '|') { // "|||".
									token = new Token(TType.DEFAULT_OR, "|||", target);
									i += 2;
								} else { // "||".
									token = new Token(TType.LOGICAL_OR, "||", target);
									i++;
								}
							} else { // "|".
								// Bitwise symbols are not used yet.
								break matched; // Pretend that bitwise OR didn't match.
//								token = new Token(TType.BIT_OR, "|", target);
							}
							break;
						}
						// Bitwise symbols are not used yet.
//						case '^': {
//							token = new Token(TType.BIT_XOR, "^", target);
//							break;
//						}
						case ':': {
							if(c2 == ':') { // "::".
								token = new Token(TType.DEREFERENCE, "::", target);
								i++;
							} else { // ":".
								token = new Token(TType.LABEL, ":", target);
							}
							break;
						}
						case '{': {
							token = new Token(TType.LCURLY_BRACKET, "{", target);
							break;
						}
						case '}': {
							token = new Token(TType.RCURLY_BRACKET, "}", target);
							break;
						}
						case '[': {
							token = new Token(TType.LSQUARE_BRACKET, "[", target);
							in_opt_var = true;
							break;
						}
						case ']': {
							token = new Token(TType.RSQUARE_BRACKET, "]", target);
							in_opt_var = false;
							break;
						}
						case ',': {
							token = new Token(TType.COMMA, ",", target);
							break;
						}
						case ';': {
							token = new Token(TType.SEMICOLON, ";", target);
							break;
						}
						case '(': {
							token = new Token(TType.FUNC_START, "(", target);
							
							// Handle the buffer or previous token, with the knowledge that a FUNC_START follows.
							if(buf.length() > 0) {
								if(saveAllTokens) {
									// In this case, we need to check for keywords first, because we want to go ahead
									// and convert into that stage. In the future, we might want to do this
									// unconditionally, but for now, just go ahead and only do it if saveAllTokens is
									// true, because we know that won't be used by the compiler.
									if(KeywordList.getKeywordByName(buf.toString()) != null) {
										// It's a keyword.
										token_list.add(new Token(TType.KEYWORD, buf.toString(), target));
									} else {
										// It's not a keyword, but a normal function.
										token_list.add(new Token(TType.FUNC_NAME, buf.toString(), target));
									}
								} else {
									token_list.add(new Token(TType.FUNC_NAME, buf.toString(), target));
								}
								buf = new StringBuilder();
								target = new Target(line_num, file, column);
							} else {
								// The previous token, if unknown, should be changed to a FUNC_NAME. If it's not
								// unknown, we may be doing standalone parenthesis, so auto tack on the __autoconcat__
								// function.
								try {
									int count = 1;
									while(token_list.get(token_list.size() - count).type == TType.WHITESPACE) {
										count++;
									}
									if(token_list.get(token_list.size() - count).type == TType.UNKNOWN) {
										token_list.get(token_list.size() - count).type = TType.FUNC_NAME;
										// Go ahead and remove the whitespace here too, they break things.
										count--;
										for(int a = 0; a < count; a++) {
											token_list.remove(token_list.size() - 1);
										}
									} else {
										token_list.add(new Token(TType.FUNC_NAME, "__autoconcat__", target));
									}
								} catch (IndexOutOfBoundsException e) {
									// This is the first element on the list, so, it's another autoconcat.
									token_list.add(new Token(TType.FUNC_NAME, "__autoconcat__", target));
								}
							}
							break;
						}
						case ')': {
							token = new Token(TType.FUNC_END, ")", target);
							break;
						}
						case ' ': { // Whitespace case #1.
							token = new Token(TType.WHITESPACE, " ", target);
							break;
						}
						case '\t': { // Whitespace case #2 (TAB).
							token = new Token(TType.WHITESPACE, "\t", target);
							break;
						}
						default: {
							// No match was found at this point, so continue matching below.
							break matched;
						}
					}
					
					// Add previous characters as UNKNOWN token.
					if(buf.length() > 0) {
						token_list.add(new Token(TType.UNKNOWN, buf.toString(), target));
						buf = new StringBuilder();
						target = new Target(line_num, file, column);
					}
					
					// Add the new token to the token list.
					token_list.add(token);
					
					// Continue lexing.
					continue;
				}
			}
			
			// Handle non-comment characters that might start or stop a quoted string.
			switch(c) {
				case '\'': {
					if(state_in_quote && !in_smart_quote) {
						token_list.add(new Token(TType.STRING, buf.toString(), target));
						buf = new StringBuilder();
						target = new Target(line_num, file, column);
						state_in_quote = false;
						continue;
					} else if(!state_in_quote) {
						state_in_quote = true;
						quoteLineNumberStart = line_num;
						in_smart_quote = false;
						if(buf.length() > 0) {
							token_list.add(new Token(TType.UNKNOWN, buf.toString(), target));
							buf = new StringBuilder();
							target = new Target(line_num, file, column);
						}
						continue;
					} else {
						// We're in a smart quote.
						buf.append("'");
					}
					break;
				}
				case '"': {
					if(state_in_quote && in_smart_quote) {
						token_list.add(new Token(TType.SMART_STRING, buf.toString(), target));
						buf = new StringBuilder();
						target = new Target(line_num, file, column);
						state_in_quote = false;
						in_smart_quote = false;
						continue;
					} else if(!state_in_quote) {
						state_in_quote = true;
						in_smart_quote = true;
						smartQuoteLineNumberStart = line_num;
						if(buf.length() > 0) {
							token_list.add(new Token(TType.UNKNOWN, buf.toString(), target));
							buf = new StringBuilder();
							target = new Target(line_num, file, column);
						}
						continue;
					} else {
						// We're in normal quotes.
						buf.append('"');
					}
					break;
				}
				case '\n': {
					
					// Append a newline to the buffer if it's quoted.
					if(state_in_quote) {
						buf.append(c);
					} else {
						// Newline is not quoted. Move the buffer to an UNKNOWN token and add a NEWLINE token.
						if(buf.length() > 0) {
							token_list.add(new Token(TType.UNKNOWN, buf.toString(), target));
							buf = new StringBuilder();
							target = new Target(line_num, file, column);
						}
						token_list.add(new Token(TType.NEWLINE, "\n", target));
					}
					continue;
				}
				case '\\': {
					// Handle escaped characters in quotes or a single "\" seperator token otherwise.
					
					// Handle backslash character outside of quotes.
					if(!state_in_quote) {
						token_list.add(new Token(TType.SEPERATOR, "\\", target));
						break;
					}
					
					// Handle an escape sign in a quote.
					switch(c2) {
						case '\\':
						case '\'':
						case '"' : buf.append(c2);   break;
						case 'n': buf.append('\n'); break;
						case 'r': buf.append('\r'); break;
						case 't': buf.append('\t'); break;
						case '0' : buf.append('\0'); break;
						case 'f' : buf.append('\f'); break; // Form feed.
						case 'v' : buf.append('\u000B'); break; // Vertical TAB.
						case 'a' : buf.append('\u0007'); break; // Alarm.
						case 'b' : buf.append('\u0008'); break; // Backspace.
						case 'u' : { // Unicode (4 characters).
							// Grab the next 4 characters, and check to see if they are numbers.
							if(i + 5 >= script.length()) {
								throw new ConfigCompileException("Unrecognized unicode escape sequence", target);
							}
							String unicode = script.substring(i + 2, i + 6);
							int unicodeNum;
							try {
								unicodeNum = Integer.parseInt(unicode, 16);
							} catch (NumberFormatException e) {
								throw new ConfigCompileException(
										"Unrecognized unicode escape sequence: \\u" + unicode, target);
							}
							buf.append(Character.toChars(unicodeNum));
							i += 4;
							break;
						}
						case 'U' : { // Unicode (8 characters).
							// Grab the next 8 characters and check to see if they are numbers.
							if(i + 9 >= script.length()) {
								throw new ConfigCompileException("Unrecognized unicode escape sequence", target);
							}
							String unicode = script.substring(i + 2, i + 10);
							int unicodeNum;
							try {
								unicodeNum = Integer.parseInt(unicode, 16);
							} catch (NumberFormatException e) {
								throw new ConfigCompileException(
										"Unrecognized unicode escape sequence: \\u" + unicode, target);
							}
							buf.append(Character.toChars(unicodeNum));
							i += 8;
							break;
						}
						case '@': {
							if(!in_smart_quote) {
								throw new ConfigCompileException("The escape sequence \\@ is not"
										+ " a recognized escape sequence in a non-smart string", target);
							}
							buf.append("\\@");
							break;
						}
						default: {
							// Since we might expand this list later, don't let them use unescaped backslashes.
							throw new ConfigCompileException(
									"The escape sequence \\" + c2 + " is not a recognized escape sequence", target);
						}
					}
					i++;
					continue;
				}
				default: {
					
					// At this point, only non-comment and non-escaped characters that are not part of a
					// quote start/end are left.
					// Disallow Non-Breaking Space Characters.
					if(!state_in_quote && c == '\u00A0'/*nbsp*/) {
						throw new ConfigCompileException("NBSP character in script", target);
					}
					
					// Add the characters that didn't match anything to the buffer.
					buf.append(c);
					continue;
				}
			}
		} // End of lexing.
		
		// Handle unended file options.
		if(in_file_options) {
			throw new ConfigCompileException("Unended file options. You started the the file options on line "
					+ fileOptionsLineNumberStart, target);
		}
		
		// Handle unended string literals.
		if(state_in_quote) {
			if(in_smart_quote) {
				throw new ConfigCompileException("Unended string literal. You started the last double quote on line "
						+ smartQuoteLineNumberStart, target);
			} else {
				throw new ConfigCompileException("Unended string literal. You started the last single quote on line "
						+ quoteLineNumberStart, target);
			}
		}
		
		// Handle unended comment blocks. Since a newline is added to the end of the script, line comments are ended.
		if(in_comment || comment_is_block) {
			throw new ConfigCompileException("Unended block comment. You started the comment on line "
					+ commentLineNumberStart, target);
		}
		
		// Look at the tokens and get meaning from them. Also, look for improper symbol locations
		// and go ahead and absorb unary +- into the token.
		for(int i = 0; i < token_list.size(); i++) {
			Token t = token_list.get(i);
			Token prev2 = i - 2 >= 0 ? token_list.get(i - 2) : new Token(TType.UNKNOWN, "", t.target);
			Token prev1 = i - 1 >= 0 ? token_list.get(i - 1) : new Token(TType.UNKNOWN, "", t.target);
			Token next = i + 1 < token_list.size() ? token_list.get(i + 1) : new Token(TType.UNKNOWN, "", t.target);

			// Combine whitespace tokens into one.
			if(t.type == TType.WHITESPACE && next.type == TType.WHITESPACE) {
				t.value += next.val();
				token_list.remove(i + 1);
				i--; // rescan this token
				continue;
			}
			
			// Convert "-" + number to -number if allowed.
			if(t.type == TType.UNKNOWN && prev1.type.isPlusMinus() // Convert "± UNKNOWN".
					&& !prev2.type.isIdentifier() // Don't convert "number/string/var ± ...".
					&& !prev2.type.equals(TType.FUNC_END) // Don't convert "func() ± ...".
					&& !IVAR_PATTERN.matcher(t.val()).matches() // Don't convert "± @var".
					&& !VAR_PATTERN.matcher(t.val()).matches()) { // Don't convert "± $var".
				// It is a negative/positive number: Absorb the sign.
				t.value = prev1.value + t.value;
				token_list.remove(i - 1);
				i--;
			}
			
			// Assign a type to all UNKNOWN tokens.
			if(t.type == TType.UNKNOWN) {
				if(t.val().charAt(0) == '/' && t.val().length() > 1) {
					t.type = TType.COMMAND;
				} else if(t.val().equals("$")) {
					t.type = TType.FINAL_VAR;
				} else if(VAR_PATTERN.matcher(t.val()).matches()) {
					t.type = TType.VARIABLE;
				} else if(IVAR_PATTERN.matcher(t.val()).matches()) {
					t.type = TType.IVARIABLE;
				} else if(t.val().charAt(0) == '@') {
					throw new ConfigCompileException("IVariables must match the regex: " + IVAR_PATTERN, target);
				} else if(keywords.contains(t.val())) {
					t.type = TType.KEYWORD;
				} else if(t.val().matches("[\t ]*")) {
					t.type = TType.WHITESPACE;
				} else {
					t.type = TType.LIT;
				}
			}
			
			// Skip this check if we're not in pure mscript.
			if(inPureMScript) {
				if(t.type.isSymbol() && !t.type.isUnary() && !next.type.isUnary()) {
					if(prev1.type.equals(TType.FUNC_START) || prev1.type.equals(TType.COMMA)
							|| next.type.equals(TType.FUNC_END) || next.type.equals(TType.COMMA)
							|| prev1.type.isSymbol() || next.type.isSymbol()) {
						throw new ConfigCompileException("Unexpected symbol (" + t.val() + ")", t.getTarget());
					}
				}
			}
			
		}
		
		// Return the result.
		return new TokenStream(token_list, fileOptions.toString());
	}

    /**
     * This function breaks the token stream into parts, separating the aliases/MethodScript from the command triggers
     *
     * @param tokenStream
     * @return
     * @throws ConfigCompileException
     */
    public static List<Script> preprocess(TokenStream tokenStream) throws ConfigCompileException {
        if (tokenStream == null || tokenStream.isEmpty()) {
            return new ArrayList<>();
        }
        //First, pull out the duplicate newlines
        ArrayList<Token> temp = new ArrayList<>();
        for (int i = 0; i < tokenStream.size(); i++) {
            try {
                if (tokenStream.get(i).type.equals(TType.NEWLINE)) {
                    temp.add(new Token(TType.NEWLINE, "\n", tokenStream.get(i).target));
                    while (tokenStream.get(++i).type.equals(TType.NEWLINE)) {
                    }
                }
                if (tokenStream.get(i).type != TType.WHITESPACE) {
                    temp.add(tokenStream.get(i));
                }
            } catch (IndexOutOfBoundsException e) {
            }
        }

        if (temp.size() > 0 && temp.get(0).type.equals(TType.NEWLINE)) {
            temp.remove(0);
        }

        tokenStream.clear();
        tokenStream.addAll(temp);

        //Handle multiline constructs
        ArrayList<Token> tokens1_1 = new ArrayList<>();
        boolean inside_multiline = false;
        Token thisToken = null;
        for (int i = 0; i < tokenStream.size(); i++) {
            Token prevToken = i - 1 >= tokenStream.size() ? tokenStream.get(i - 1) : new Token(TType.UNKNOWN, "", Target.UNKNOWN);
            thisToken = tokenStream.get(i);
            Token nextToken = i + 1 < tokenStream.size() ? tokenStream.get(i + 1) : new Token(TType.UNKNOWN, "", Target.UNKNOWN);
            //take out newlines between the = >>> and <<< tokens (also the tokens)
            if (thisToken.type.equals(TType.ALIAS_END) && nextToken.val().equals(">>>")) {
                inside_multiline = true;
                tokens1_1.add(thisToken);
                i++;
                continue;
            }
            if (thisToken.val().equals("<<<")) {
                if (!inside_multiline) {
                    throw new ConfigCompileException("Found multiline end symbol, and no multiline start found",
                            thisToken.target);
                }
                inside_multiline = false;
                continue;
            }
            if (thisToken.val().equals(">>>") && inside_multiline) {
                throw new ConfigCompileException("Did not expect a multiline start symbol here, are you missing a multiline end symbol above this line?", thisToken.target);
            }
            if (thisToken.val().equals(">>>") && !prevToken.type.equals(TType.ALIAS_END)) {
                throw new ConfigCompileException("Multiline symbol must follow the alias_end (=) symbol", thisToken.target);
            }

            //If we're not in a multiline construct, or we are in it and it's not a newline, add
            //it
            if (!inside_multiline || !thisToken.type.equals(TType.NEWLINE)) {
                tokens1_1.add(thisToken);
            }
        }

        assert thisToken != null;

        if (inside_multiline) {
            throw new ConfigCompileException("Expecting a multiline end symbol, but your last multiline alias appears to be missing one.", thisToken.target);
        }

        //take out newlines that are behind a \
        ArrayList<Token> tokens2 = new ArrayList<>();
        for (int i = 0; i < tokens1_1.size(); i++) {
            // For now, just remove comments
            if (tokens1_1.get(i).type.isComment()) {
                tokens1_1.remove(i);
                i--;
                continue;
            }
            if (!tokens1_1.get(i).type.equals(TType.STRING) && tokens1_1.get(i).val().equals("\\") && tokens1_1.size() > i
                    && tokens1_1.get(i + 1).type.equals(TType.NEWLINE)) {
                tokens2.add(tokens1_1.get(i));
                i++;
                continue;
            }
            tokens2.add(tokens1_1.get(i));
        }

        //Now that we have all lines minified, we should be able to split
        //on newlines, and easily find the left and right sides
        List<Token> left = new ArrayList<>();
        List<Token> right = new ArrayList<>();
        List<Script> scripts = new ArrayList<>();
        boolean inLeft = true;
        for (Token t : tokens2) {
            if (inLeft) {
                if (t.type == TType.ALIAS_END) {
                    inLeft = false;
                } else {
                    left.add(t);
                }
            } else if (t.type == TType.NEWLINE) {
                inLeft = true;
                // Check for spurious symbols, which indicate an issue with the
                // script, but ignore any whitespace.
                for (int j = left.size() - 1; j >= 0; j--) {
                    if (left.get(j).type == TType.NEWLINE) {
                        if (j > 0 && left.get(j - 1).type != TType.WHITESPACE) {
                            throw new ConfigCompileException("Unexpected token: " + left.get(j - 1).val(), left.get(j - 1).getTarget());
                        }
                    }
                }
                Script s = new Script(left, right, null, tokenStream.getFileOptions());
                scripts.add(s);
                left = new ArrayList<>();
                right = new ArrayList<>();
            } else {
                right.add(t);
            }
        }
        return scripts;
    }

    /**
     * Compiles the token stream into a valid ParseTree. This also includes optimization and reduction.
     *
     * @param stream The token stream, as generated by {@link #lex(String, File, boolean) lex}
     * @return A fully compiled, optimized, and reduced parse tree. If {@code stream} is null or empty, null is
     * returned.
     * @throws ConfigCompileException If the script contains syntax errors. Additionally, during optimization, certain
     * methods may cause compile errors. Any function that can optimize static occurrences and throws a
     * {@link ConfigRuntimeException} will have that exception converted to a ConfigCompileException.
     */
    @SuppressWarnings("UnnecessaryContinue")
    public static ParseTree compile(TokenStream stream) throws ConfigCompileException, ConfigCompileGroupException {
        Set<ConfigCompileException> compilerErrors = new HashSet<>();
        if (stream == null || stream.isEmpty()) {
            return null;
        }
        Target unknown;
        try {
            //Instead of using Target.UNKNOWN, we can at least set the file.
            unknown = new Target(0, stream.get(0).target.file(), 0);
        } catch (Exception e) {
            unknown = Target.UNKNOWN;
        }

        List<Token> tempStream = new ArrayList<>(stream.size());
        for (Token t : stream) {
            if (!t.type.isWhitespace()) {
                tempStream.add(t);
            }
        }
        stream.clear();
        stream.addAll(tempStream);
        FileOptions fileOptions = stream.getFileOptions();

        ParseTree tree = new ParseTree(fileOptions);
        tree.setData(CNull.NULL);
        Stack<ParseTree> parents = new Stack<>();
        /**
         * constructCount is used to determine if we need to use autoconcat when reaching a FUNC_END. The previous
         * constructs, if the count is greater than 1, will be moved down into an autoconcat.
         */
        Stack<AtomicInteger> constructCount = new Stack<>();
        constructCount.push(new AtomicInteger(0));
        parents.push(tree);

        tree.addChild(new ParseTree(new CFunction("__autoconcat__", unknown), fileOptions));
        parents.push(tree.getChildAt(0));
        tree = tree.getChildAt(0);
        constructCount.push(new AtomicInteger(0));

        /**
         * The array stack is used to keep track of the number of square braces in use.
         */
        Stack<AtomicInteger> arrayStack = new Stack<>();
        arrayStack.add(new AtomicInteger(-1));

        Stack<AtomicInteger> minusArrayStack = new Stack<>();
        Stack<AtomicInteger> minusFuncStack = new Stack<>();

        int parens = 0;
        Token t = null;

        int bracketCount = 0;

        for (int i = 0; i < stream.size(); i++) {
            t = stream.get(i);
            //Token prev2 = i - 2 >= 0 ? stream.get(i - 2) : new Token(TType.UNKNOWN, "", t.target);
            Token prev1 = i - 1 >= 0 ? stream.get(i - 1) : new Token(TType.UNKNOWN, "", t.target);
            Token next1 = i + 1 < stream.size() ? stream.get(i + 1) : new Token(TType.UNKNOWN, "", t.target);
            Token next2 = i + 2 < stream.size() ? stream.get(i + 2) : new Token(TType.UNKNOWN, "", t.target);
            Token next3 = i + 3 < stream.size() ? stream.get(i + 3) : new Token(TType.UNKNOWN, "", t.target);

            // Brace handling
            if (t.type == TType.LCURLY_BRACKET) {
                ParseTree b = new ParseTree(new CFunction("__cbrace__", t.getTarget()), fileOptions);
                tree.addChild(b);
                tree = b;
                parents.push(b);
                bracketCount++;
                constructCount.push(new AtomicInteger(0));
                continue;
            }

            if (t.type == TType.RCURLY_BRACKET) {
                bracketCount--;
                if (constructCount.peek().get() > 1) {
                    //We need to autoconcat some stuff
                    int stacks = constructCount.peek().get();
                    int replaceAt = tree.getChildren().size() - stacks;
                    ParseTree c = new ParseTree(new CFunction("__autoconcat__", tree.getTarget()), fileOptions);
                    List<ParseTree> subChildren = new ArrayList<>();
                    for (int b = replaceAt; b < tree.numberOfChildren(); b++) {
                        subChildren.add(tree.getChildAt(b));
                    }
                    c.setChildren(subChildren);
                    if (replaceAt > 0) {
                        List<ParseTree> firstChildren = new ArrayList<>();
                        for (int d = 0; d < replaceAt; d++) {
                            firstChildren.add(tree.getChildAt(d));
                        }
                        tree.setChildren(firstChildren);
                    } else {
                        tree.removeChildren();
                    }
                    tree.addChild(c);
                }
                parents.pop();
                tree = parents.peek();
                constructCount.pop();
                try {
                    constructCount.peek().incrementAndGet();
                } catch (EmptyStackException e) {
                    throw new ConfigCompileException("Unexpected end curly brace", t.target);
                }
                continue;
            }

            //Associative array/label handling
            if (t.type == TType.LABEL && tree.getChildren().size() > 0) {
                //If it's not an atomic identifier it's an error.
                if (!prev1.type.isAtomicLit() && prev1.type != TType.IVARIABLE && prev1.type != TType.KEYWORD) {
                    ConfigCompileException error = new ConfigCompileException("Invalid label specified", t.getTarget());
                    if (prev1.type == TType.FUNC_END) {
                        // This is a fairly common mistake, so we have special handling for this,
                        // because otherwise we would get a "Mismatched parenthesis" warning (which doesn't make sense),
                        // and potentially lots of other invalid errors down the line, so we go ahead
                        // and stop compilation at this point.
                        throw error;
                    }
                    compilerErrors.add(error);
                }
                // Wrap previous construct in a CLabel
                ParseTree cc = tree.getChildren().get(tree.getChildren().size() - 1);
                tree.removeChildAt(tree.getChildren().size() - 1);
                tree.addChild(new ParseTree(new CLabel(cc.getData()), fileOptions));
                continue;
            }

            //Array notation handling
            if (t.type.equals(TType.LSQUARE_BRACKET)) {
                arrayStack.push(new AtomicInteger(tree.getChildren().size() - 1));
                continue;
            } else if (t.type.equals(TType.RSQUARE_BRACKET)) {
                boolean emptyArray = false;
                if (prev1.type.equals(TType.LSQUARE_BRACKET)) {
                    emptyArray = true;
                }
                if (arrayStack.size() == 1) {
                    throw new ConfigCompileException("Mismatched square bracket", t.target);
                }
                //array is the location of the array
                int array = arrayStack.pop().get();
                //index is the location of the first node with the index
                int index = array + 1;
                if (!tree.hasChildren() || array == -1) {
                    throw new ConfigCompileException("Brackets are illegal here", t.target);
                }
                ParseTree myArray = tree.getChildAt(array);
                ParseTree myIndex;
                if (!emptyArray) {
                    myIndex = new ParseTree(new CFunction("__autoconcat__", myArray.getTarget()), fileOptions);

                    for (int j = index; j < tree.numberOfChildren(); j++) {
                        myIndex.addChild(tree.getChildAt(j));
                    }
                } else {
                    myIndex = new ParseTree(new CSlice("0..-1", t.target), fileOptions);
                }
                tree.setChildren(tree.getChildren().subList(0, array));
                ParseTree arrayGet = new ParseTree(new CFunction("array_get", t.target), fileOptions);
                arrayGet.addChild(myArray);
                arrayGet.addChild(myIndex);

                // Check if the @var[...] had a negating "-" in front. If so, add a neg().
                if (minusArrayStack.size() != 0 && arrayStack.size() + 1 == minusArrayStack.peek().get()) {
                    if (!next1.type.equals(TType.LSQUARE_BRACKET)) { // Wait if there are more array_get's comming.
                        ParseTree negTree = new ParseTree(new CFunction("neg", unknown), fileOptions);
                        negTree.addChild(arrayGet);
                        tree.addChild(negTree);
                        minusArrayStack.pop();
                    } else {
                        // Negate the next array_get instead, so just add this one to the tree.
                        tree.addChild(arrayGet);
                    }
                } else {
                    tree.addChild(arrayGet);
                }
                constructCount.peek().set(constructCount.peek().get() - myIndex.numberOfChildren());
                continue;
            }

            //Smart strings
            if (t.type == TType.SMART_STRING) {
                if (t.val().contains("@")) {
                    ParseTree function = new ParseTree(fileOptions);
                    function.setData(new CFunction(new Compiler.smart_string().getName(), t.target));
                    ParseTree string = new ParseTree(fileOptions);
                    string.setData(new CString(t.value, t.target));
                    function.addChild(string);
                    tree.addChild(function);
                } else {
                    tree.addChild(new ParseTree(new CString(t.val(), t.target), fileOptions));
                }
                constructCount.peek().incrementAndGet();
                continue;
            }

            if (t.type == TType.DEREFERENCE) {
                //Currently unimplemented, but going ahead and making it strict
                compilerErrors.add(new ConfigCompileException("The '" + t.val() + "' symbol is not currently allowed in raw strings. You must quote all"
                        + " symbols.", t.target));
            }

            if (t.type.equals(TType.FUNC_NAME)) {
                CFunction func = new CFunction(t.val(), t.target);
                ParseTree f = new ParseTree(func, fileOptions);
                tree.addChild(f);
                constructCount.push(new AtomicInteger(0));
                tree = f;
                parents.push(f);
            } else if (t.type.equals(TType.FUNC_START)) {
                if (!prev1.type.equals(TType.FUNC_NAME)) {
                    throw new ConfigCompileException("Unexpected parenthesis", t.target);
                }
                parens++;
            } else if (t.type.equals(TType.FUNC_END)) {
                if (parens <= 0) {
                    throw new ConfigCompileException("Unexpected parenthesis", t.target);
                }
                parens--;
                ParseTree function = parents.pop();
                if (constructCount.peek().get() > 1) {
                    //We need to autoconcat some stuff
                    int stacks = constructCount.peek().get();
                    int replaceAt = tree.getChildren().size() - stacks;
                    ParseTree c = new ParseTree(new CFunction("__autoconcat__", tree.getTarget()), fileOptions);
                    List<ParseTree> subChildren = new ArrayList<>();
                    for (int b = replaceAt; b < tree.numberOfChildren(); b++) {
                        subChildren.add(tree.getChildAt(b));
                    }
                    c.setChildren(subChildren);
                    if (replaceAt > 0) {
                        List<ParseTree> firstChildren = new ArrayList<>();
                        for (int d = 0; d < replaceAt; d++) {
                            firstChildren.add(tree.getChildAt(d));
                        }
                        tree.setChildren(firstChildren);
                    } else {
                        tree.removeChildren();
                    }
                    tree.addChild(c);
                }
                constructCount.pop();
                try {
                    constructCount.peek().incrementAndGet();
                } catch (EmptyStackException e) {
                    throw new ConfigCompileException("Unexpected end parenthesis", t.target);
                }
                try {
                    tree = parents.peek();
                } catch (EmptyStackException e) {
                    throw new ConfigCompileException("Unexpected end parenthesis", t.target);
                }

                // Handle "-func(args)" and "-func(args)[index]".
                if (minusFuncStack.size() != 0 && minusFuncStack.peek().get() == parens + 1) {
                    if (next1.type.equals(TType.LSQUARE_BRACKET)) {
                        // Move the negation to the array_get which contains this function.
                        minusArrayStack.push(new AtomicInteger(arrayStack.size() + 1)); // +1 because the bracket isn't counted yet.
                    } else {
                        // Negate this function.
                        ParseTree negTree = new ParseTree(new CFunction("neg", unknown), fileOptions);
                        negTree.addChild(tree.getChildAt(tree.numberOfChildren() - 1));
                        tree.removeChildAt(tree.numberOfChildren() - 1);
                        tree.addChildAt(tree.numberOfChildren(), negTree);
                    }
                    minusFuncStack.pop();
                }

            } else if (t.type.equals(TType.COMMA)) {
                if (constructCount.peek().get() > 1) {
                    int stacks = constructCount.peek().get();
                    int replaceAt = tree.getChildren().size() - stacks;
                    ParseTree c = new ParseTree(new CFunction("__autoconcat__", unknown), fileOptions);
                    List<ParseTree> subChildren = new ArrayList<>();
                    for (int b = replaceAt; b < tree.numberOfChildren(); b++) {
                        subChildren.add(tree.getChildAt(b));
                    }
                    c.setChildren(subChildren);
                    if (replaceAt > 0) {
                        List<ParseTree> firstChildren = new ArrayList<>();
                        for (int d = 0; d < replaceAt; d++) {
                            firstChildren.add(tree.getChildAt(d));
                        }
                        tree.setChildren(firstChildren);
                    } else {
                        tree.removeChildren();
                    }
                    tree.addChild(c);
                }
                constructCount.peek().set(0);
                continue;
            }
            if (t.type == TType.SLICE) {
                //We got here because the previous token isn't being ignored, because it's
                //actually a control character, instead of whitespace, but this is a
                //"empty first" slice notation. Compare this to the code below.
                try {
                    CSlice slice;
                    String value = next1.val();
                    if (next1.type == TType.MINUS || next1.type == TType.PLUS) {
                        value = next1.val() + next2.val();
                        i++;
                    }
                    slice = new CSlice(".." + value, t.getTarget());
                    i++;
                    tree.addChild(new ParseTree(slice, fileOptions));
                    constructCount.peek().incrementAndGet();
                    continue;
                } catch (ConfigRuntimeException ex) {
                    //CSlice can throw CREs, but at this stage, we have to
                    //turn them into a CCE.
                    throw new ConfigCompileException(ex);
                }
            }
            if (next1.type.equals(TType.SLICE)) {
                //Slice notation handling
                try {
                    CSlice slice;
                    if (t.type.isSeparator() || (t.type.isWhitespace() && prev1.type.isSeparator()) || t.type.isKeyword()) {
                        //empty first
                        String value = next2.val();
                        i++;
                        if (next2.type == TType.MINUS || next2.type == TType.PLUS) {
                            value = next2.val() + next3.val();
                            i++;
                        }
                        slice = new CSlice(".." + value, next1.getTarget());
                        if (t.type.isKeyword()) {
                            tree.addChild(new ParseTree(new CKeyword(t.val(), t.getTarget()), fileOptions));
                            constructCount.peek().incrementAndGet();
                        }
                    } else if (next2.type.isSeparator() || next2.type.isKeyword()) {
                        //empty last
                        String modifier = "";
                        if (prev1.type == TType.MINUS || prev1.type == TType.PLUS) {
                            //The negative would have already been inserted into the tree
                            modifier = prev1.val();
                            tree.removeChildAt(tree.getChildren().size() - 1);
                        }
                        slice = new CSlice(modifier + t.value + "..", t.target);
                    } else {
                        //both are provided
                        String modifier1 = "";
                        if (prev1.type == TType.MINUS || prev1.type == TType.PLUS) {
                            //It's a negative, incorporate that here, and remove the
                            //minus from the tree
                            modifier1 = prev1.val();
                            tree.removeChildAt(tree.getChildren().size() - 1);
                        }
                        Token first = t;
                        if (first.type.isWhitespace()) {
                            first = prev1;
                        }
                        Token second = next2;
                        i++;
                        String modifier2 = "";
                        if (next2.type == TType.MINUS || next2.type == TType.PLUS) {
                            modifier2 = next2.val();
                            second = next3;
                            i++;
                        }
                        slice = new CSlice(modifier1 + first.value + ".." + modifier2 + second.value, t.target);
                    }
                    i++;
                    tree.addChild(new ParseTree(slice, fileOptions));
                    constructCount.peek().incrementAndGet();
                    continue;
                } catch (ConfigRuntimeException ex) {
                    //CSlice can throw CREs, but at this stage, we have to
                    //turn them into a CCE.
                    throw new ConfigCompileException(ex);
                }
            } else if (t.type == TType.LIT) {
                Construct c = Static.resolveConstruct(t.val(), t.target);
                if (c instanceof CString && fileOptions.isStrict()) {
                    compilerErrors.add(new ConfigCompileException("Bare strings are not allowed in strict mode", t.target));
                } else if ((c instanceof CInt || c instanceof CDecimal) && next1.type == TType.DOT && next2.type == TType.LIT) {
                    // make CDouble/CDecimal here because otherwise Long.parseLong() will remove
                    // minus zero before decimals and leading zeroes after decimals
                    try {
			if(t.value.startsWith("0m")) {
			    // CDecimal
			    String neg = "";
			    if(prev1.value.equals('-')) {
				neg = "-";
			    }
			    c = new CDecimal(neg + t.value.substring(2) + '.' + next2.value, t.target);
			} else {
			    // CDouble
			    c = new CDouble(Double.parseDouble(t.val() + '.' + next2.val()), t.target);
			}
                        i += 2;
                    } catch (NumberFormatException e) {
                        // Not a double
                    }
                }
                tree.addChild(new ParseTree(c, fileOptions));
                constructCount.peek().incrementAndGet();
            } else if (t.type.equals(TType.STRING) || t.type.equals(TType.COMMAND)) {
                tree.addChild(new ParseTree(new CString(t.val(), t.target), fileOptions));
                constructCount.peek().incrementAndGet();
            } else if (t.type.equals(TType.IDENTIFIER)) {
                tree.addChild(new ParseTree(new CPreIdentifier(t.val(), t.target), fileOptions));
                constructCount.peek().incrementAndGet();
            } else if (t.type.isKeyword()) {
                tree.addChild(new ParseTree(new CKeyword(t.val(), t.getTarget()), fileOptions));
                constructCount.peek().incrementAndGet();
            } else if (t.type.equals(TType.IVARIABLE)) {
                tree.addChild(new ParseTree(new IVariable(t.val(), t.target), fileOptions));
                constructCount.peek().incrementAndGet();
            } else if (t.type.equals(TType.UNKNOWN)) {
                tree.addChild(new ParseTree(Static.resolveConstruct(t.val(), t.target), fileOptions));
                constructCount.peek().incrementAndGet();
            } else if (t.type.isSymbol()) { //Logic and math symbols

                // Attempt to find "-@var" and change it to "neg(@var)" if it's not @a - @b. Else just add the symbol.
                // Also handles "-function()" and "-@var[index]".
                if (t.type.equals(TType.MINUS) && !prev1.type.isAtomicLit() && !prev1.type.equals(TType.IVARIABLE)
                        && !prev1.type.equals(TType.VARIABLE) && !prev1.type.equals(TType.RCURLY_BRACKET)
                        && !prev1.type.equals(TType.RSQUARE_BRACKET) && !prev1.type.equals(TType.FUNC_END)
                        && (next1.type.equals(TType.IVARIABLE) || next1.type.equals(TType.VARIABLE) || next1.type.equals(TType.FUNC_NAME))) {

                    // Check if we are negating a value from an array, function or variable.
                    if (next2.type.equals(TType.LSQUARE_BRACKET)) {
                        minusArrayStack.push(new AtomicInteger(arrayStack.size() + 1)); // +1 because the bracket isn't counted yet.
                    } else if (next1.type.equals(TType.FUNC_NAME)) {
                        minusFuncStack.push(new AtomicInteger(parens + 1)); // +1 because the function isn't counted yet.
                    } else {
                        ParseTree negTree = new ParseTree(new CFunction("neg", unknown), fileOptions);
                        negTree.addChild(new ParseTree(new IVariable(next1.value, next1.target), fileOptions));
                        tree.addChild(negTree);
                        constructCount.peek().incrementAndGet();
                        i++; // Skip the next variable as we've just handled it.
                    }
                } else {
                    tree.addChild(new ParseTree(new CSymbol(t.val(), t.type, t.target), fileOptions));
                    constructCount.peek().incrementAndGet();
                }

            } else if (t.type == TType.DOT) {
                // Check for doubles that start with a decimal, otherwise concat
                Construct c = null;
                if (next1.type == TType.LIT && prev1.type != TType.STRING && prev1.type != TType.SMART_STRING) {
                    try {
                        c = new CDouble(Double.parseDouble('.' + next1.val()), t.target);
                        i++;
                    } catch (NumberFormatException e) {
                        // Not a double
                    }
                }
                if (c == null) {
                    c = new CSymbol(".", TType.CONCAT, t.target);
                }
                tree.addChild(new ParseTree(c, fileOptions));
                constructCount.peek().incrementAndGet();
            } else if (t.type.equals(TType.VARIABLE) || t.type.equals(TType.FINAL_VAR)) {
                tree.addChild(new ParseTree(new Variable(t.val(), null, false, t.type.equals(TType.FINAL_VAR), t.target), fileOptions));
                constructCount.peek().incrementAndGet();
                //right_vars.add(new Variable(t.val(), null, t.line_num));
            }

        }

        assert t != null;

        if (arrayStack.size() != 1) {
            throw new ConfigCompileException("Mismatched square brackets", t.target);
        }
        if (parens != 0) {
            throw new ConfigCompileException("Mismatched parenthesis", t.target);
        }
        if (bracketCount != 0) {
            throw new ConfigCompileException("Mismatched curly braces", t.target);
        }

        Stack<List<Procedure>> procs = new Stack<>();
        procs.add(new ArrayList<Procedure>());
        processKeywords(tree);
        optimizeAutoconcats(tree, compilerErrors);
        optimize(tree, procs, compilerErrors);
        link(tree, compilerErrors);
        checkLabels(tree, compilerErrors);
        checkBreaks(tree, compilerErrors);
        if (!compilerErrors.isEmpty()) {
            if (compilerErrors.size() == 1) {
                // Just throw the one CCE
                for (ConfigCompileException e : compilerErrors) {
                    throw e;
                }
            } else {
                throw new ConfigCompileGroupException(compilerErrors);
            }
        }
        parents.pop();
        tree = parents.pop();
        return tree;
    }

    /**
     * Recurses down the tree and ensures that breaks don't bubble up past procedures or the root code tree.
     *
     * @param tree
     * @throws ConfigCompileException
     */
    private static void checkBreaks(ParseTree tree, Set<ConfigCompileException> compilerExceptions) {
        checkBreaks0(tree, 0, null, compilerExceptions);
    }

    private static void checkBreaks0(ParseTree tree, long currentLoops, String lastUnbreakable, Set<ConfigCompileException> compilerErrors) {
        if (!(tree.getData() instanceof CFunction)) {
            //Don't care about these
            return;
        }
        if (tree.getData().val().startsWith("_")) {
            //It's a proc. We need to recurse, but not check this "function"
            for (ParseTree child : tree.getChildren()) {
                checkBreaks0(child, currentLoops, lastUnbreakable, compilerErrors);
            }
            return;
        }
        Function func;
        try {
            func = ((CFunction) tree.getData()).getFunction();
        } catch (ConfigCompileException ex) {
            compilerErrors.add(ex);
            return;
        }
        if (func.getClass().getAnnotation(nolinking.class) != null) {
            // Don't link here
            return;
        }
        // We have special handling for procs and closures, and of course break and the loops.
        // If any of these are here, we kick into special handling mode. Otherwise, we recurse.
        if (func instanceof DataHandling._break) {
            // First grab the counter in the break function. If the break function doesn't
            // have any children, then 1 is implied. break() requires the argument to be
            // a CInt, so if it weren't, there would already have been a compile error, so
            // we can assume it will be a CInt.
            long breakCounter = 1;
            if (tree.getChildren().size() == 1) {
                breakCounter = ((CInt) tree.getChildAt(0).getData()).getInt();
            }
            if (breakCounter > currentLoops) {
                // Throw an exception, as this would break above a loop. Different error messages
                // are applied to different cases
                if (currentLoops == 0) {
                    compilerErrors.add(new ConfigCompileException("The break() function can only break out of loops" + (lastUnbreakable == null ? "."
                            : ", but an attempt to break out of a " + lastUnbreakable + " was detected."), tree.getTarget()));
                } else {
                    compilerErrors.add(new ConfigCompileException("Too many breaks"
                            + " detected. Check your loop nesting, and set the break count to an appropriate value.", tree.getTarget()));
                }
            }
            return;
        }
        if (func.getClass().getAnnotation(unbreakable.class) != null) {
            // Parse the children like normal, but reset the counter to 0.
            for (ParseTree child : tree.getChildren()) {
                checkBreaks0(child, 0, func.getName(), compilerErrors);
            }
            return;
        }
        if (func.getClass().getAnnotation(breakable.class) != null) {
            // Don't break yet, still recurse, but up our current loops counter.
            currentLoops++;
        }
        for (ParseTree child : tree.getChildren()) {
            checkBreaks0(child, currentLoops, lastUnbreakable, compilerErrors);
        }
    }

    /**
     * Optimizing __autoconcat__ out should happen early, and should happen regardless of whether or not optimizations
     * are on or off. So this is broken off into a separate optimization procedure, so that the intricacies of the
     * normal optimizations don't apply to __autoconcat__.
     *
     * @param root
     * @param compilerExceptions
     */
    private static void optimizeAutoconcats(ParseTree root, Set<ConfigCompileException> compilerExceptions) {
        for (ParseTree child : root.getChildren()) {
            if (child.hasChildren()) {
                optimizeAutoconcats(child, compilerExceptions);
            }
        }
        if (root.getData() instanceof CFunction && root.getData().val().equals(__autoconcat__)) {
            try {
                ParseTree ret = ((Compiler.__autoconcat__) ((CFunction) root.getData()).getFunction()).optimizeDynamic(root.getTarget(), root.getChildren(), root.getFileOptions());
                root.setData(ret.getData());
                root.setChildren(ret.getChildren());
            } catch (ConfigCompileException ex) {
                compilerExceptions.add(ex);
            }
        }
    }

    /**
     * Recurses down the tree and ensures that there are no dynamic labels. This has to finish completely after
     * optimization, because the optimizer has no good hook to know when optimization for a unit is fully completed,
     * until ALL units are fully complete, so this happens separately after optimization, but as apart of the normal
     * compile process.
     *
     * @param tree
     * @throws ConfigCompileException
     */
    private static void checkLabels(ParseTree tree, Set<ConfigCompileException> compilerErrors) throws ConfigCompileException {
//		for(ParseTree t : tree.getChildren()){
//			if(t.getData() instanceof CLabel){
//				if(((CLabel)t.getData()).cVal() instanceof IVariable){
//					throw new ConfigCompileException("Variables may not be used as labels", t.getTarget());
//				}
//			}
//			checkLabels(t);
//		}
    }

    /**
     * Recurses down the tree and
     * <ul><li>Links functions</li>
     * <li>Checks function arguments</li></ul>
     * This is a separate process from optimization, because optimization ignores any missing functions.
     *
     * @param tree
     */
    private static void link(ParseTree tree, Set<ConfigCompileException> compilerErrors) {
        FunctionBase treeFunction = null;
        try {
            treeFunction = FunctionList.getFunction(tree.getData());
            if (treeFunction.getClass().getAnnotation(nolinking.class) != null) {
                //Don't link children of a nolinking function.
                return;
            }
        } catch (ConfigCompileException ex) {
            //This can happen if the treeFunction isn't a function, is a proc, etc,
            //but we don't care, we just want to continue.
        }
        // Check the argument count, and do any custom linking the function may have
        if (treeFunction != null) {
            Integer[] numArgs = treeFunction.numArgs();
            if (!Arrays.asList(numArgs).contains(Integer.MAX_VALUE)
                    && !Arrays.asList(numArgs).contains(tree.getChildren().size())) {
                compilerErrors.add(new ConfigCompileException("Incorrect number of arguments passed to "
                        + tree.getData().val(), tree.getData().getTarget()));
            }
            if (treeFunction instanceof Optimizable) {
                Optimizable op = (Optimizable) treeFunction;
                if (op.optimizationOptions().contains(OptimizationOption.CUSTOM_LINK)) {
                    try {
                        op.link(tree.getData().getTarget(), tree.getChildren());
                    } catch (ConfigCompileException ex) {
                        compilerErrors.add(ex);
                    }
                }
            }
        }
        // Walk the children
        for (ParseTree child : tree.getChildren()) {
            if (child.getData() instanceof CFunction) {
                FunctionBase f = null;
                if (child.getData().val().charAt(0) != '_' || child.getData().val().charAt(1) == '_') {
                    // This will throw an exception if the function doesn't exist.
                    try {
                        f = FunctionList.getFunction(child.getData());
                    } catch (ConfigCompileException ex) {
                        compilerErrors.add(ex);
                    }
                }
                link(child, compilerErrors);
            }
        }
    }

    private static final String __autoconcat__ = new Compiler.__autoconcat__().getName();

    /**
     * Recurses down into the tree, attempting to optimize where possible. A few things have strong coupling, for
     * information on these items, see the documentation included in the source.
     *
     * @param tree
     * @return
     */
    private static void optimize(ParseTree tree, Stack<List<Procedure>> procs, Set<ConfigCompileException> compilerErrors) {
        if (tree.isOptimized()) {
            return; //Don't need to re-run this
        }
//		if (tree.getData() instanceof CIdentifier) {
//			optimize(((CIdentifier) tree.getData()).contained(), procs);
//			return;
//		}
        if (!(tree.getData() instanceof CFunction)) {
            //There's no way to optimize something that's not a function
            return;
        }
        //If it is a proc definition, we need to go ahead and see if we can add it to the const proc stack
        if (tree.getData().val().equals("proc")) {
            procs.push(new ArrayList<Procedure>());
        }
        CFunction cFunction = (CFunction) tree.getData();
        Function func;
        try {
            func = (Function) FunctionList.getFunction(cFunction);
        } catch (ConfigCompileException e) {
            func = null;
        }
        if (func != null) {
            if (func.getClass().getAnnotation(nolinking.class) != null) {
                //It's an unlinking function, so we need to stop at this point
                return;
            }
        }
        if (cFunction instanceof CIdentifier) {
            //Add the child to the identifier
            ParseTree c = ((CIdentifier) cFunction).contained();
            tree.addChild(c);
            c.getData().setWasIdentifier(true);
        }

        List<ParseTree> children = tree.getChildren();
        if (func instanceof Optimizable && ((Optimizable) func).optimizationOptions().contains(OptimizationOption.PRIORITY_OPTIMIZATION)) {
            // This is a priority optimization function, meaning it needs to be optimized before its children are.
            // This is required when optimization of the children could cause different internal behavior, for instance
            // if this function is expecting the precense of soem code element, but the child gets optimized out, this
            // would cause an error, even though the user did in fact provide code in that section.
            try {
                ((Optimizable) func).optimizeDynamic(tree.getTarget(), children, tree.getFileOptions());
            } catch (ConfigCompileException ex) {
                // If an error occurs, we will skip the rest of this element
                compilerErrors.add(ex);
                return;
            } catch (ConfigRuntimeException ex) {
                compilerErrors.add(new ConfigCompileException(ex));
                return;
            }
        }
        //Loop through the children, and if any of them are functions that are terminal, truncate.
        //To explain this further, consider the following:
        //For the code: concat(die(), msg('')), this diagram shows the abstract syntax tree:
        //         (concat)
        //        /        \
        //       /          \
        //     (die)       (msg)
        //By looking at the code, we can tell that msg() will never be called, because die() will run first,
        //and since it is a "terminal" function, any code after it will NEVER run. However, consider a more complex condition:
        // if(@input){ die() msg('1') } else { msg('2') msg('3') }
        //              if(@input)
        //        [true]/         \[false]
        //             /           \
        //         (sconcat)     (sconcat)
        //           /   \         /    \
        //          /     \       /      \
        //       (die) (msg[1])(msg[2]) (msg[3])
        //In this case, only msg('1') is guaranteed not to run, msg('2') and msg('3') will still run in some cases.
        //So, we can optimize out msg('1') in this case, which would cause the tree to become much simpler, therefore a worthwile optimization:
        //              if(@input)
        //        [true]/        \[false]
        //             /          \
        //          (die)      (sconcat)
        //                      /    \
        //                     /      \
        //                 (msg[2]) (msg[3])
        //We do have to be careful though, because of functions like if, which actually work like this:
        //if(@var){ die() } else { msg('') }
        //                (if)
        //              /  |  \
        //             /   |   \
        //          @var (die) (msg)
        //We can't git rid of the msg() here, because it is actually in another branch.
        //For the time being, we will simply say that if a function uses execs, it
        //is a branch (branches always use execs, though using execs doesn't strictly
        //mean you are a branch type function).

        for (int i = 0; i < children.size(); i++) {
            ParseTree t = children.get(i);
            if (t.getData() instanceof CFunction) {
                if (t.getData().val().startsWith("_") || (func != null && func.useSpecialExec())) {
                    continue;
                }
                Function f;
                try {
                    f = (Function) FunctionList.getFunction(t.getData());
                } catch (ConfigCompileException ex) {
                    compilerErrors.add(ex);
                    return;
                }
                Set<OptimizationOption> options = NO_OPTIMIZATIONS;
                if (f instanceof Optimizable) {
                    options = ((Optimizable) f).optimizationOptions();
                }
                if (options.contains(OptimizationOption.TERMINAL)) {
                    if (children.size() > i + 1) {
                        //First, a compiler warning
                        CHLog.GetLogger().Log(CHLog.Tags.COMPILER, LogLevel.WARNING, "Unreachable code. Consider removing this code.", children.get(i + 1).getTarget());
                        //Now, truncate the children
                        for (int j = children.size() - 1; j > i; j--) {
                            children.remove(j);
                        }
                        break;
                    }
                }
            }
        }
        boolean fullyStatic = true;
        boolean hasIVars = false;
        for (ParseTree node : children) {
            if (node.getData() instanceof CFunction) {
                optimize(node, procs, compilerErrors);
            }

            if (node.getData().isDynamic() && !(node.getData() instanceof IVariable)) {
                fullyStatic = false;
            }
            if (node.getData() instanceof IVariable) {
                hasIVars = true;
            }
        }

        //In all cases, at this point, we are either unable to optimize, or we will
        //optimize, so set our optimized variable at this point.
        tree.setOptimized(true);

        if (func == null) {
            //It's a proc call. Let's see if we can optimize it
            Procedure p = null;
            loop:
            for (List<Procedure> proc : procs) {
                for (Procedure pp : proc) {
                    if (pp.getName().equals(cFunction.val())) {
                        p = pp;
                        break loop;
                    }
                }
            }
            if (p != null) {
                try {
                    Construct c = DataHandling.proc.optimizeProcedure(p.getTarget(), p, children);
                    if (c != null) {
                        tree.setData(c);
                        tree.removeChildren();
                        return;
                    }//else Nope, couldn't optimize.
                } catch (ConfigRuntimeException ex) {
                    //Cool. Caught a runtime error at compile time :D
                    compilerErrors.add(new ConfigCompileException(ex));
                }
            }
            //else this procedure isn't listed yet. Maybe a compiler error, maybe not, depends,
            //so we can't for sure say, but we do know we can't optimize this
            return;
        }
        if (tree.getData().val().equals("proc")) {
            //Check for too few arguments
            if (children.size() < 2) {
                compilerErrors.add(new ConfigCompileException("Incorrect number of arguments passed to proc",
                        tree.getData().getTarget()));
                return;
            }
            //We just went out of scope, so we need to pop the layer of Procedures that
            //are internal to us
            procs.pop();
            //However, as a special function, we *might* be able to get a const proc out of this
            //Let's see.
            try {
                ParseTree root = new ParseTree(new CFunction(__autoconcat__, Target.UNKNOWN), tree.getFileOptions());
                Script fakeScript = Script.GenerateScript(root, "*");
                Environment env = null;
                try {
                    if (Implementation.GetServerType().equals(Implementation.Type.BUKKIT)) {
                        CommandHelperPlugin plugin = CommandHelperPlugin.self;
                        GlobalEnv gEnv = new GlobalEnv(plugin.executionQueue, plugin.profiler, plugin.persistenceNetwork,
                                MethodScriptFileLocations.getDefault().getConfigDirectory(), plugin.profiles, new TaskManager());
                        env = Environment.createEnvironment(gEnv, new CommandHelperEnvironment());
                    } else {
                        env = Static.GenerateStandaloneEnvironment(false);
                    }
                } catch (IOException | DataSourceException | URISyntaxException | Profiles.InvalidProfileException e) {
                    //
                }
                Procedure myProc = DataHandling.proc.getProcedure(tree.getTarget(), env, fakeScript, children.toArray(new ParseTree[children.size()]));
                procs.peek().add(myProc); //Yep. So, we can move on with our lives now, and if it's used later, it could possibly be static.
            } catch (ConfigRuntimeException e) {
                //Well, they have an error in there somewhere
                compilerErrors.add(new ConfigCompileException(e));
            } catch (NullPointerException e) {
                //Nope, can't optimize.
                return;
            }
        }

        //the compiler trick functions know how to deal with it specially, even if everything isn't
        //static, so do this first.
        String oldFunctionName = func.getName();
        Set<OptimizationOption> options = NO_OPTIMIZATIONS;
        if (func instanceof Optimizable) {
            options = ((Optimizable) func).optimizationOptions();
        }
        if (options.contains(OptimizationOption.OPTIMIZE_DYNAMIC)) {
            try {
                ParseTree tempNode;
                try {
                    tempNode = ((Optimizable) func).optimizeDynamic(tree.getData().getTarget(), tree.getChildren(), tree.getFileOptions());
                } catch (ConfigRuntimeException e) {
                    //Turn it into a compile exception, then rethrow
                    throw new ConfigCompileException(e);
                }
                if (tempNode == Optimizable.PULL_ME_UP) {
                    if (tree.hasChildren()) {
                        tempNode = tree.getChildAt(0);
                    } else {
                        tempNode = null;
                    }
                }
                if (tempNode == Optimizable.REMOVE_ME) {
                    tree.setData(new CFunction("p", Target.UNKNOWN));
                    tree.removeChildren();
                } else if (tempNode != null) {
                    tree.setData(tempNode.getData());
                    tree.setOptimized(tempNode.isOptimized());
                    tree.setChildren(tempNode.getChildren());
                    tree.getData().setWasIdentifier(tempNode.getData().wasIdentifier());
                    optimize(tree, procs, compilerErrors);
                    tree.setOptimized(true);
                    //Some functions can actually make static the arguments, for instance, by pulling up a hardcoded
                    //array, so if they have reversed this, make note of that now
                    if (tempNode.hasBeenMadeStatic()) {
                        fullyStatic = true;
                    }
                } //else it wasn't an optimization, but a compile check
            } catch (ConfigCompileException ex) {
                compilerErrors.add(ex);
            }
        }
        if (!fullyStatic) {
            return;
        }
        //Otherwise, everything is static, or an IVariable and we can proceed.
        //Note since we could still have IVariables, we have to handle those
        //specially from here forward
        if (func.preResolveVariables() && hasIVars) {
            //Well, this function isn't equipped to deal with IVariables.
            return;
        }
        //It could have optimized by changing the name, in that case, we
        //don't want to run this now
        if (tree.getData().getValue().equals(oldFunctionName)
                && (options.contains(OptimizationOption.OPTIMIZE_CONSTANT) || options.contains(OptimizationOption.CONSTANT_OFFLINE))) {
            Construct[] constructs = new Construct[tree.getChildren().size()];
            for (int i = 0; i < tree.getChildren().size(); i++) {
                constructs[i] = tree.getChildAt(i).getData();
            }
            try {
                try {
                    Construct result;
                    if (options.contains(OptimizationOption.CONSTANT_OFFLINE)) {
                        List<Integer> numArgsList = Arrays.asList(func.numArgs());
                        if (!numArgsList.contains(Integer.MAX_VALUE)
                                && !numArgsList.contains(tree.getChildren().size())) {
                            compilerErrors.add(new ConfigCompileException("Incorrect number of arguments passed to "
                                    + tree.getData().val(), tree.getData().getTarget()));
                            result = null;
                        } else {
                            result = func.exec(tree.getData().getTarget(), null, constructs);
                        }
                    } else {
                        result = ((Optimizable) func).optimize(tree.getData().getTarget(), constructs);
                    }

                    //If the result is null, it was just a check, it can't optimize further.
                    if (result != null) {
                        result.setWasIdentifier(tree.getData().wasIdentifier());
                        tree.setData(result);
                        tree.removeChildren();
                    }
                } catch (ConfigRuntimeException e) {
                    //Turn this into a ConfigCompileException, then rethrow
                    throw new ConfigCompileException(e);
                }
            } catch (ConfigCompileException ex) {
                compilerErrors.add(ex);
            }
        }

        //It doesn't know how to optimize. Oh well.
    }

    /**
     * Runs keyword processing on the tree. Note that this is run before optimization, and is a depth first process.
     *
     * @param tree
     */
    private static void processKeywords(ParseTree tree) throws ConfigCompileException {
        // Keyword processing
        List<ParseTree> children = tree.getChildren();
        for (int i = 0; i < children.size(); i++) {
            ParseTree node = children.get(i);
            // Keywords can be standalone, or a function can double as a keyword. So we have to check for both
            // conditions.
            processKeywords(node);
            if (node.getData() instanceof CKeyword
                    || (node.getData() instanceof CLabel && ((CLabel) node.getData()).cVal() instanceof CKeyword)
                    || (node.getData() instanceof CFunction && KeywordList.getKeywordByName(node.getData().val()) != null)) {
                // This looks a bit confusing, but is fairly straightforward. We want to process the child elements of all
                // remaining nodes, so that subchildren that need processing will be finished, and our current tree level will
                // be able to independently process it. We don't want to process THIS level though, just the children of this level.
                for (int j = i + 1; j < children.size(); j++) {
                    processKeywords(children.get(j));
                }
                // Now that all the children of the rest of the chain are processed, we can do the processing of this level.
                i = KeywordList.getKeywordByName(node.getData().val()).process(children, i);
            }
        }

    }

    /**
     * Shorthand for lexing, compiling, and executing a script.
     *
     * @param script The textual script to execute
     * @param file The file it was located in
     * @param inPureMScript If it is pure MScript, or aliases
     * @param env The execution environment
     * @param done The MethodScriptComplete callback (may be null)
     * @param s A script object (may be null)
     * @param vars Any $vars (may be null)
     * @return
     * @throws ConfigCompileException
     * @throws com.laytonsmith.core.exceptions.ConfigCompileGroupException This indicates that a group of compile errors
     * occurred.
     */
    public static Construct execute(String script, File file, boolean inPureMScript, Environment env, MethodScriptComplete done, Script s, List<Variable> vars) throws ConfigCompileException, ConfigCompileGroupException {
        return execute(compile(lex(script, file, inPureMScript)), env, done, s, vars);
    }

    /**
     * Executes a pre-compiled MethodScript, given the specified Script environment. Both done and script may be null,
     * and if so, reasonable defaults will be provided. The value sent to done will also be returned, as a Construct, so
     * this one function may be used synchronously also.
     *
     * @param root
     * @param env
     * @param done
     * @param script
     * @return
     */
    public static Construct execute(ParseTree root, Environment env, MethodScriptComplete done, Script script) {
        return execute(root, env, done, script, null);
    }

    /**
     * Executes a pre-compiled MethodScript, given the specified Script environment, but also provides a method to set
     * the constants in the script.
     *
     * @param root
     * @param env
     * @param done
     * @param script
     * @param vars
     * @return
     */
    public static Construct execute(ParseTree root, Environment env, MethodScriptComplete done, Script script, List<Variable> vars) {
        if (root == null) {
            return CVoid.VOID;
        }
        if (script == null) {
            script = new Script(null, null, env.getEnv(GlobalEnv.class).GetLabel(), new FileOptions(new HashMap<>()));
        }
        if (vars != null) {
            Map<String, Variable> varMap = new HashMap<>();
            for (Variable v : vars) {
                varMap.put(v.getVariableName(), v);
            }
            for (Construct tempNode : root.getAllData()) {
                if (tempNode instanceof Variable) {
                    Variable vv = varMap.get(((Variable) tempNode).getVariableName());
                    if (vv != null) {
                        ((Variable) tempNode).setVal(vv.getDefault());
                    } else {
                        //The variable is unset. I'm not quite sure what cases would cause this
                        ((Variable) tempNode).setVal("");
                    }
                }
            }
        }
        StringBuilder b = new StringBuilder();
        Construct returnable = null;
        for (ParseTree gg : root.getChildren()) {
            Construct retc = script.eval(gg, env);
            if (root.numberOfChildren() == 1) {
                returnable = retc;
            }
            String ret = retc instanceof CNull ? "null" : retc.val();
            if (ret != null && !ret.trim().isEmpty()) {
                b.append(ret).append(" ");
            }
        }
        if (done != null) {
            done.done(b.toString().trim());
        }
        if (returnable != null) {
            return returnable;
        }
        return Static.resolveConstruct(b.toString().trim(), Target.UNKNOWN);
    }

    public static void registerAutoIncludes(Environment env, Script s) {
        for (File f : Static.getAliasCore().autoIncludes) {
            try {
                MethodScriptCompiler.execute(IncludeCache.get(f, new Target(0, f, 0)), env, null, s);
            } catch (ProgramFlowManipulationException e) {
                ConfigRuntimeException.HandleUncaughtException(ConfigRuntimeException.CreateUncatchableException("Cannot break program flow in auto include files.", e.getTarget()), env);
            } catch (ConfigRuntimeException e) {
                e.setEnv(env);
                ConfigRuntimeException.HandleUncaughtException(e, env);
            }
        }
    }
}
