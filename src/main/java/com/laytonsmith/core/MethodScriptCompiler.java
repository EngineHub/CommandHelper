package com.laytonsmith.core;

import com.laytonsmith.PureUtilities.Common.FileUtil;
import com.laytonsmith.PureUtilities.Common.StringUtils;
import com.laytonsmith.PureUtilities.SimpleVersion;
import com.laytonsmith.PureUtilities.SmartComment;
import com.laytonsmith.annotations.OperatorPreferred;
import com.laytonsmith.annotations.breakable;
import com.laytonsmith.annotations.nolinking;
import com.laytonsmith.annotations.unbreakable;
import com.laytonsmith.core.Optimizable.OptimizationOption;
import com.laytonsmith.core.compiler.BranchStatement;
import com.laytonsmith.core.compiler.CompilerEnvironment;
import com.laytonsmith.core.compiler.CompilerWarning;
import com.laytonsmith.core.compiler.EarlyBindingKeyword;
import com.laytonsmith.core.compiler.FileOptions;
import com.laytonsmith.core.compiler.FileOptions.SuppressWarning;
import com.laytonsmith.core.compiler.Keyword;
import com.laytonsmith.core.compiler.KeywordList;
import com.laytonsmith.core.compiler.LateBindingKeyword;
import com.laytonsmith.core.compiler.TokenStream;
import com.laytonsmith.core.compiler.analysis.StaticAnalysis;
import com.laytonsmith.core.constructs.CBareString;
import com.laytonsmith.core.constructs.CClassType;
import com.laytonsmith.core.constructs.CDecimal;
import com.laytonsmith.core.constructs.CDouble;
import com.laytonsmith.core.constructs.CFunction;
import com.laytonsmith.core.constructs.CInt;
import com.laytonsmith.core.constructs.CKeyword;
import com.laytonsmith.core.constructs.CLabel;
import com.laytonsmith.core.constructs.CNull;
import com.laytonsmith.core.constructs.CPreIdentifier;
import com.laytonsmith.core.constructs.CSemicolon;
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
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.environments.GlobalEnv;
import com.laytonsmith.core.exceptions.CRE.CRECastException;
import com.laytonsmith.core.exceptions.CRE.CRERangeException;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import com.laytonsmith.core.exceptions.ConfigCompileGroupException;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.extensions.ExtensionManager;
import com.laytonsmith.core.extensions.ExtensionTracker;
import com.laytonsmith.core.functions.Compiler;
import com.laytonsmith.core.functions.Compiler.__autoconcat__;
import com.laytonsmith.core.functions.Compiler.__cbrace__;
import com.laytonsmith.core.functions.Compiler.__smart_string__;
import com.laytonsmith.core.functions.Compiler.__statements__;
import com.laytonsmith.core.functions.Compiler.p;
import com.laytonsmith.core.functions.ControlFlow;
import com.laytonsmith.core.functions.DataHandling;
import com.laytonsmith.core.functions.Function;
import com.laytonsmith.core.functions.FunctionBase;
import com.laytonsmith.core.functions.FunctionList;
import com.laytonsmith.core.functions.ArrayHandling.array_get;
import com.laytonsmith.core.functions.Math.neg;
import com.laytonsmith.core.functions.StringHandling;
import com.laytonsmith.core.natives.interfaces.Mixed;
import com.laytonsmith.persistence.DataSourceException;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EmptyStackException;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

/**
 * The MethodScriptCompiler class handles the various stages of compilation and provides helper methods for execution of
 * the compiled trees.
 */
public final class MethodScriptCompiler {

	private static final EnumSet<Optimizable.OptimizationOption> NO_OPTIMIZATIONS = EnumSet.noneOf(Optimizable.OptimizationOption.class);

	private MethodScriptCompiler() {
	}

	private static final Pattern VAR_PATTERN = Pattern.compile("\\$[\\p{L}0-9_]+");
	private static final Pattern IVAR_PATTERN = Pattern.compile(IVariable.VARIABLE_NAME_REGEX);
	private static final Pattern NUM_PATTERN = Pattern.compile("[0-9]+");
	private static final Pattern FUNC_NAME_PATTERN = Pattern.compile("[_a-zA-Z0-9]+");

	/**
	 * Lexes the script, and turns it into a token stream.This looks through the script character by character.
	 *
	 * @param script The script to lex
	 * @param env The environment.
	 * @param file The file this script came from, or potentially null if the code is from a dynamic source
	 * @param inPureMScript If the script is in pure MethodScript, this should be true. Pure MethodScript is defined as
	 * code that doesn't have command alias wrappers.
	 * @return A stream of tokens
	 * @throws ConfigCompileException If compilation fails due to bad syntax
	 */
	public static TokenStream lex(String script, Environment env, File file, boolean inPureMScript)
			throws ConfigCompileException {
		return lex(script, env, file, inPureMScript, false);
	}

	/**
	 * Lexes the script, and turns it into a token stream. This looks through the script character by character.
	 *
	 * @param script The script to lex
	 * @param env
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
	@SuppressWarnings({"null", "UnnecessaryContinue"})
	public static TokenStream lex(String script, Environment env, File file,
			boolean inPureMScript, boolean saveAllTokens) throws ConfigCompileException {
		if(env == null) {
			// We MUST have a CompilerEnvironment, but it doesn't need to be used, but we have to create it at this
			// stage.
			env = Environment.createEnvironment(new CompilerEnvironment());
		}
		if(!env.hasEnv(CompilerEnvironment.class)) {
			env = env.cloneAndAdd(new CompilerEnvironment());
		}
		if(script.isEmpty()) {
			return new TokenStream(new LinkedList<>(), "", new HashMap<>());
		}
		if(script.charAt(0) == 65279) {
			// Remove the UTF-8 Byte Order Mark, if present.
			script = script.substring(1);
		}
		final StringBuilder fileOptions = new StringBuilder();
		/**
		 * May be null if the file options aren't parsed yet, but if they have been, they will be parsed and put in
		 * this variable, to allow the lexer to make use of the file options already.
		 */
		FileOptions builtFileOptions = null;
		script = script.replace("\r\n", "\n");
		script = script + "\n";
		final Set<String> keywords = KeywordList.getKeywordNames();
		final TokenStream tokenList = new TokenStream();

		// Set our state variables.
		boolean stateInQuote = false;
		int quoteLineNumberStart = 1;
		boolean inSmartQuote = false;
		int smartQuoteLineNumberStart = 1;
		boolean inComment = false;
		int commentLineNumberStart = 1;
		boolean commentIsBlock = false;
		boolean inOptVar = false;
		boolean inCommand = (!inPureMScript);
		boolean inMultiline = false;
		boolean inSmartComment = false;
		boolean inFileOptions = false;
		boolean inAnnotation = false;
		int fileOptionsLineNumberStart = 1;

		StringBuilder buf = new StringBuilder();
		int lineNum = 1;
		int column = 1;
		int lastColumn = 0;
		Target target = Target.UNKNOWN;

		// Lex the script character by character.
		for(int i = 0; i < script.length(); i++) {
			char c = script.charAt(i);
			char c2 = '\0';
			char c3 = '\0';
			if(i < script.length() - 1) {
				c2 = script.charAt(i + 1);
			}
			if(i < script.length() - 2) {
				c3 = script.charAt(i + 2);
			}

			column += i - lastColumn;
			lastColumn = i;
			if(c == '\n') {
				lineNum++;
				column = 0;
				if(!inMultiline && !inPureMScript) {
					inCommand = true;
				}
			}
			if(buf.length() == 0) {
				target = new Target(lineNum, file, column);
			}

			// If we are in file options, add the character to the buffer if it's not a file options end character.
			if(inFileOptions) {
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
						break;
					}
					case '>': {
						if(saveAllTokens) {
							tokenList.add(new Token(TType.FILE_OPTIONS_STRING,
									fileOptions.toString(), target));
							tokenList.add(new Token(TType.FILE_OPTIONS_END, ">", target));
						}
						inFileOptions = false;
						builtFileOptions = TokenStream.parseFileOptions(fileOptions.toString(), new HashMap<>());
						continue;
					}
				}
				fileOptions.append(c);
				continue;
			}

			// Comment handling. This is bypassed if we are in a string.
			if(!stateInQuote && !inSmartQuote) {
				switch(c) {

					// Block comments start (/* and /**) and Double slash line comment start (//).
					case '/': {
						if(c2 == '*') { // "/*" or "/**".
							if(inComment && commentIsBlock) {
								// This compiler warning can be removed and the nested comment blocks implemented in 3.3.6
								// or later.
								CompilerWarning warning = new CompilerWarning("Nested comment blocks are being"
										+ " added to a future version, where this code will suddenly cause an unclosed"
										+ " comment block. You can remove this block comment open symbol now, or add a "
										+ " new block comment close when this feature is implemented (it will likely"
										+ " cause an obvious compile error), and optionally suppress this warning.",
										target, SuppressWarning.FutureNestedCommentChange);
								env.getEnv(CompilerEnvironment.class).addCompilerWarning(builtFileOptions, warning);
							}
							if(!inComment) {
								buf.append("/*");
								inComment = true;
								commentIsBlock = true;
								if(i + 2 < script.length() && script.charAt(i + 2) == '*'
										&& (i + 3 >= script.length() || script.charAt(i + 3) != '/')) { // "/**".
									inSmartComment = true;
									buf.append("*");
									i++;
								}
								commentLineNumberStart = lineNum;
								i++;
								continue;
							}
						} else if(c2 == '/') { // "//".
							if(!inComment) {
								buf.append("//");
								inComment = true;
								i++;
								continue;
							}
						}
						break;
					}

					// Line comment start (#).
					case '#': {
						if(!inComment) { // "#".
							buf.append("#");
							inComment = true;
							continue;
						}
						break;
					}

					// Block comment end (*/).
					case '*': {
						if(inComment && commentIsBlock && c2 == '/') { // "*/".
							if(saveAllTokens || inSmartComment) {
								buf.append("*/");
								validateTerminatedBidiSequence(buf.toString(), target);
								tokenList.add(new Token(inSmartComment ? TType.SMART_COMMENT : TType.COMMENT,
										buf.toString(), target));
							}
							buf = new StringBuilder();
							target = new Target(lineNum, file, column);
							inComment = false;
							commentIsBlock = false;
							inSmartComment = false;
							i++;
							continue;
						}
						break;
					}

					// Line comment end (\n).
					case '\n': {
						if(inComment && !commentIsBlock) { // "\n".
							inComment = false;
							if(saveAllTokens) {
								validateTerminatedBidiSequence(buf.toString(), target);
								tokenList.add(new Token(TType.COMMENT, buf.toString(), target));
								tokenList.add(new Token(TType.NEWLINE, "\n", new Target(lineNum + 1, file, 0)));
							}
							buf = new StringBuilder();
							target = new Target(lineNum, file, column);
							continue;
						}
						break;
					}
				}
			}

			// If we are in a comment, add the character to the buffer.
			if(inComment || (inAnnotation && c != '}')) {
				buf.append(c);
				continue;
			}

			// Handle non-comment non-quoted characters.
			if(!stateInQuote) {
				// We're not in a comment or quoted string, handle: +=, -=, *=, /=, .=, ->, ++, --, %, **, *, +, -, /,
				// >=, <=, <<<, >>>, <, >, ===, !==, ==, !=, &&&, |||, &&, ||, !, {, }, .., ., ::, [, =, ], :, comma,
				// (, ), ;, and whitespace.
				matched:
				{
					Token token;
					switch(c) {
						case '+': {
							if(c2 == '=') { // "+=".
								token = new Token(TType.PLUS_ASSIGNMENT, "+=", target.copy());
								i++;
							} else if(c2 == '+') { // "++".
								token = new Token(TType.INCREMENT, "++", target.copy());
								i++;
							} else { // "+".
								token = new Token(TType.PLUS, "+", target.copy());
							}
							break;
						}
						case '-': {
							if(c2 == '=') { // "-=".
								token = new Token(TType.MINUS_ASSIGNMENT, "-=", target.copy());
								i++;
							} else if(c2 == '-') { // "--".
								token = new Token(TType.DECREMENT, "--", target.copy());
								i++;
							} else if(c2 == '>') { // "->".
								token = new Token(TType.DEREFERENCE, "->", target.copy());
								i++;
							} else { // "-".
								token = new Token(TType.MINUS, "-", target.copy());
							}
							break;
						}
						case '*': {
							if(c2 == '=') { // "*=".
								token = new Token(TType.MULTIPLICATION_ASSIGNMENT, "*=", target.copy());
								i++;
							} else if(c2 == '*') { // "**".
								token = new Token(TType.EXPONENTIAL, "**", target.copy());
								i++;
							} else { // "*".
								token = new Token(TType.MULTIPLICATION, "*", target.copy());
							}
							break;
						}
						case '/': {
							if(c2 == '=') { // "/=".
								token = new Token(TType.DIVISION_ASSIGNMENT, "/=", target.copy());
								i++;
							} else { // "/".
								// Protect against matching commands.
								if(Character.isLetter(c2)) {
									break matched; // Pretend that division didn't match.
								}
								token = new Token(TType.DIVISION, "/", target.copy());
							}
							break;
						}
						case '.': {
							if(c2 == '=') { // ".=".
								token = new Token(TType.CONCAT_ASSIGNMENT, ".=", target.copy());
								i++;
							} else if(c2 == '.' && c3 == '.') {
								token = new Token(TType.VARARGS, "...", target.copy());
								i += 2;
							} else if(c2 == '.') { // "..".
								token = new Token(TType.SLICE, "..", target.copy());
								i++;
							} else { // ".".
								token = new Token(TType.DOT, ".", target.copy());
							}
							break;
						}
						case '%': {
							token = new Token(TType.MODULO, "%", target.copy());
							break;
						}
						case '>': {
							if(c2 == '=') { // ">=".
								token = new Token(TType.GTE, ">=", target.copy());
								i++;
							} else if(c2 == '>' && i < script.length() - 2 && script.charAt(i + 2) == '>') { // ">>>".
								token = new Token(TType.MULTILINE_START, ">>>", target.copy());
								inMultiline = true;
								i += 2;
							} else { // ">".
								token = new Token(TType.GT, ">", target.copy());
							}
							break;
						}
						case '<': {
							if(c2 == '!') { // "<!".
								if(buf.length() > 0) {
									tokenList.add(new Token(TType.UNKNOWN, buf.toString(), target));
									buf = new StringBuilder();
									target = new Target(lineNum, file, column);
								}

								if(saveAllTokens) {
									tokenList.add(new Token(TType.FILE_OPTIONS_START, "<!", target.copy()));
								}
								inFileOptions = true;
								fileOptionsLineNumberStart = lineNum;
								i++;
								continue;
							} else if(c2 == '=') { // "<=".
								token = new Token(TType.LTE, "<=", target.copy());
								i++;
							} else if(c2 == '<' && i < script.length() - 2 && script.charAt(i + 2) == '<') { // "<<<".
								token = new Token(TType.MULTILINE_END, "<<<", target.copy());
								inMultiline = false;
								i += 2;
							} else { // "<".
								token = new Token(TType.LT, "<", target.copy());
							}
							break;
						}
						case '=': {
							if(c2 == '=') {
								if(i < script.length() - 2 && script.charAt(i + 2) == '=') { // "===".
									token = new Token(TType.STRICT_EQUALS, "===", target.copy());
									i += 2;
								} else { // "==".
									token = new Token(TType.EQUALS, "==", target.copy());
									i++;
								}
							} else { // "=".
								if(inCommand) {
									if(inOptVar) {
										token = new Token(TType.OPT_VAR_ASSIGN, "=", target.copy());
									} else {
										token = new Token(TType.ALIAS_END, "=", target.copy());
										inCommand = false;
									}
								} else {
									token = new Token(TType.ASSIGNMENT, "=", target.copy());
								}
							}
							break;
						}
						case '!': {
							if(c2 == '=') {
								if(i < script.length() - 2 && script.charAt(i + 2) == '=') { // "!==".
									token = new Token(TType.STRICT_NOT_EQUALS, "!==", target.copy());
									i += 2;
								} else { // "!=".
									token = new Token(TType.NOT_EQUALS, "!=", target.copy());
									i++;
								}
							} else { // "!".
								token = new Token(TType.LOGICAL_NOT, "!", target.copy());
							}
							break;
						}
						case '&': {
							if(c2 == '&') {
								if(i < script.length() - 2 && script.charAt(i + 2) == '&') { // "&&&".
									token = new Token(TType.DEFAULT_AND, "&&&", target.copy());
									i += 2;
								} else { // "&&".
									token = new Token(TType.LOGICAL_AND, "&&", target.copy());
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
									token = new Token(TType.DEFAULT_OR, "|||", target.copy());
									i += 2;
								} else { // "||".
									token = new Token(TType.LOGICAL_OR, "||", target.copy());
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
								token = new Token(TType.DEREFERENCE, "::", target.copy());
								i++;
							} else { // ":".
								token = new Token(TType.LABEL, ":", target.copy());
							}
							break;
						}
						case '{': {
							token = new Token(TType.LCURLY_BRACKET, "{", target.copy());
							break;
						}
						case '}': {
							if(inAnnotation) {
								// Eventually, this will no longer be a comment type, but for now, we just want
								// to totally ignore annotations, as if they were comments.
								inAnnotation = false;
								token = new Token(/*TType.ANNOTATION*/TType.COMMENT, "@{" + buf.toString() + "}", target);
								buf = new StringBuilder();
								break;
							}
							token = new Token(TType.RCURLY_BRACKET, "}", target.copy());
							break;
						}
						case '[': {
							token = new Token(TType.LSQUARE_BRACKET, "[", target.copy());
							inOptVar = true;
							break;
						}
						case ']': {
							token = new Token(TType.RSQUARE_BRACKET, "]", target.copy());
							inOptVar = false;
							break;
						}
						case ',': {
							token = new Token(TType.COMMA, ",", target.copy());
							break;
						}
						case ';': {
							token = new Token(TType.SEMICOLON, ";", target.copy());
							break;
						}
						case '(': {
							token = new Token(TType.FUNC_START, "(", target.copy());

							// Handle the buffer or previous token, with the knowledge that a FUNC_START follows.
							if(buf.length() > 0) {
								// In this case, we need to check for keywords first, because we want to go ahead
								// and convert into that stage. In the future, we might want to do this
								// unconditionally, but for now, just go ahead and only do it if saveAllTokens is
								// true, because we know that won't be used by the compiler.
								if(saveAllTokens && KeywordList.getKeywordByName(buf.toString()) != null) {
									// It's a keyword.
									tokenList.add(new Token(TType.KEYWORD, buf.toString(), target));
								} else {
									// It's not a keyword, but a normal function.
									String funcName = buf.toString();
									if(FUNC_NAME_PATTERN.matcher(funcName).matches()) {
										tokenList.add(new Token(TType.FUNC_NAME, funcName, target));
									} else {
										tokenList.add(new Token(TType.UNKNOWN, funcName, target));
									}
								}
								buf = new StringBuilder();
								target = new Target(lineNum, file, column);
							} else {
								// The previous token, if unknown, should be changed to a FUNC_NAME. If it's not
								// unknown, we may be doing standalone parenthesis, so auto tack on the __autoconcat__
								// function.
								try {
									int count = 0;
									Iterator<Token> it = tokenList.descendingIterator();
									Token t;
									while((t = it.next()).type == TType.WHITESPACE) {
										count++;
									}
									if(t.type == TType.UNKNOWN) {
										t.type = TType.FUNC_NAME;
										// Go ahead and remove the whitespace here too, they break things.
										count--;
										for(int a = 0; a < count; a++) {
											tokenList.removeLast();
										}
									}
								} catch (NoSuchElementException e) {
									// This is the first element on the list, so, it's another autoconcat.
								}
							}
							break;
						}
						case ')': {
							token = new Token(TType.FUNC_END, ")", target.copy());
							break;
						}
						case ' ': { // Whitespace case #1.
							token = new Token(TType.WHITESPACE, " ", target.copy());
							break;
						}
						case '\t': { // Whitespace case #2 (TAB).
							token = new Token(TType.WHITESPACE, "\t", target.copy());
							break;
						}
						case '@': {
							if(c2 == '{') {
								inAnnotation = true;
								i++;
								continue;
							}
							break matched;
						}
						default: {
							// No match was found at this point, so continue matching below.
							break matched;
						}
					}

					// Add previous characters as UNKNOWN token.
					if(buf.length() > 0) {
						tokenList.add(new Token(TType.UNKNOWN, buf.toString(), target));
						buf = new StringBuilder();
						target = new Target(lineNum, file, column);
					}

					// Add the new token to the token list.
					tokenList.add(token);

					// Continue lexing.
					continue;
				}
			}

			// Handle non-comment characters that might start or stop a quoted string.
			switch(c) {
				case '\'': {
					if(stateInQuote && !inSmartQuote) {
						validateTerminatedBidiSequence(buf.toString(), target);
						tokenList.add(new Token(TType.STRING, buf.toString(), target));
						buf = new StringBuilder();
						target = new Target(lineNum, file, column);
						stateInQuote = false;
						continue;
					} else if(!stateInQuote) {
						stateInQuote = true;
						quoteLineNumberStart = lineNum;
						inSmartQuote = false;
						if(buf.length() > 0) {
							tokenList.add(new Token(TType.UNKNOWN, buf.toString(), target));
							buf = new StringBuilder();
							target = new Target(lineNum, file, column);
						}
						continue;
					} else {
						// We're in a smart quote.
						buf.append("'");
					}
					break;
				}
				case '"': {
					if(stateInQuote && inSmartQuote) {
						validateTerminatedBidiSequence(buf.toString(), target);
						tokenList.add(new Token(TType.SMART_STRING, buf.toString(), target));
						buf = new StringBuilder();
						target = new Target(lineNum, file, column);
						stateInQuote = false;
						inSmartQuote = false;
						continue;
					} else if(!stateInQuote) {
						stateInQuote = true;
						inSmartQuote = true;
						smartQuoteLineNumberStart = lineNum;
						if(buf.length() > 0) {
							tokenList.add(new Token(TType.UNKNOWN, buf.toString(), target));
							buf = new StringBuilder();
							target = new Target(lineNum, file, column);
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
					if(stateInQuote) {
						buf.append(c);
					} else {
						// Newline is not quoted. Move the buffer to an UNKNOWN token and add a NEWLINE token.
						if(buf.length() > 0) {
							tokenList.add(new Token(TType.UNKNOWN, buf.toString(), target));
							buf = new StringBuilder();
							target = new Target(lineNum, file, column);
						}
						tokenList.add(new Token(TType.NEWLINE, "\n", target));
					}
					continue;
				}
				case '\\': {
					// Handle escaped characters in quotes or a single "\" seperator token otherwise.

					// Handle backslash character outside of quotes.
					if(!stateInQuote) {
						tokenList.add(new Token(TType.SEPERATOR, "\\", target));
						break;
					}

					// Handle an escape sign in a quote.
					switch(c2) {
						case '\\':
							if(inSmartQuote) {
								// Escaping of '@' and '\' is handled within __smart_string__.
								buf.append('\\');
							}
							buf.append('\\');
							break;
						case '\'':
						case '"':
							buf.append(c2);
							break;
						case 'n':
							buf.append('\n');
							break;
						case 'r':
							buf.append('\r');
							break;
						case 't':
							buf.append('\t');
							break;
						case '0':
							buf.append('\0');
							break;
						case 'f':
							buf.append('\f');
							break; // Form feed.
						case 'v':
							buf.append('\u000B');
							break; // Vertical TAB.
						case 'a':
							buf.append('\u0007');
							break; // Alarm.
						case 'b':
							buf.append('\u0008');
							break; // Backspace.
						case 'u': { // Unicode (4 characters).
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
						case 'U': { // Unicode (8 characters).
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
							if(!inSmartQuote) {
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
					if(!stateInQuote && c == '\u00A0'/*nbsp*/) {
						throw new ConfigCompileException("NBSP character in script", target);
					}

					// Add the characters that didn't match anything to the buffer.
					buf.append(c);
					continue;
				}
			}
		} // End of lexing.

		// Handle unended file options.
		if(inFileOptions) {
			throw new ConfigCompileException("Unended file options. You started the the file options on line "
					+ fileOptionsLineNumberStart, target);
		}

		// Handle unended string literals.
		if(stateInQuote) {
			if(inSmartQuote) {
				throw new ConfigCompileException("Unended string literal. You started the last double quote on line "
						+ smartQuoteLineNumberStart, target);
			} else {
				throw new ConfigCompileException("Unended string literal. You started the last single quote on line "
						+ quoteLineNumberStart, target);
			}
		}

		// Handle unended comment blocks. Since a newline is added to the end of the script, line comments are ended.
		if(inComment || commentIsBlock) {
			throw new ConfigCompileException("Unended block comment. You started the comment on line "
					+ commentLineNumberStart, target);
		}

		// Look at the tokens and get meaning from them. Also, look for improper symbol locations
		// and go ahead and absorb unary +- into the token.
		ListIterator<Token> it = tokenList.listIterator(0);
		while(it.hasNext()) {
			Token t = it.next();

			// Combine whitespace tokens into one.
			if(t.type == TType.WHITESPACE && it.hasNext()) {
				Token next;
				if((next = it.next()).type == TType.WHITESPACE) {
					t.value += next.val();
					it.remove(); // Remove 'next'.
				} else {
					it.previous(); // Select 'next' <--.
				}
				it.previous(); // Select 't' <--.
				it.next(); // Select 't' -->.
			}

			// Convert "-" + number to -number if allowed.
			it.previous(); // Select 't' <--.
			if(it.hasPrevious() && t.type == TType.UNKNOWN) {
				Token prev1 = it.previous(); // Select 'prev1' <--.
				if(prev1.type.isPlusMinus()) {

					// Find the first non-whitespace token before the '-'.
					Token prevNonWhitespace = null;
					while(it.hasPrevious()) {
						if(it.previous().type != TType.WHITESPACE) {
							prevNonWhitespace = it.next();
							break;
						}
					}
					while(it.next() != prev1) { // Skip until selection is at 'prev1 -->'.
					}

					if(prevNonWhitespace != null) {
						// Convert "±UNKNOWN" if the '±' is used as a sign (and not an add/subtract operation).
						if(!prevNonWhitespace.type.isIdentifier() // Don't convert "number/string/var ± ...".
								&& prevNonWhitespace.type != TType.FUNC_END // Don't convert "func() ± ...".
								&& prevNonWhitespace.type != TType.RSQUARE_BRACKET // Don't convert "] ± ..." (arrays).
								&& NUM_PATTERN.matcher(t.val()).matches()) { // Only convert numbers
							// It is a negative/positive number: Absorb the sign.
							t.value = prev1.value + t.value;
							it.remove(); // Remove 'prev1'.
						}
					}
				} else {
					it.next(); // Select 'prev1' -->.
				}
			}
			it.next(); // Select 't' -->.

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
					throw new ConfigCompileException("IVariables must match the regex: " + IVAR_PATTERN, t.getTarget());
				} else if(keywords.contains(t.val())) {
					t.type = TType.KEYWORD;
				} else if(t.val().matches("[\t ]*")) {
					t.type = TType.WHITESPACE;
				} else {
					t.type = TType.LIT;
				}
			}

			if(it.hasNext()) {
				Token next = it.next(); // Select 'next' -->.
				it.previous(); // Select 'next' <--.
				it.previous(); // Select 't' <--.
				if(t.type.isSymbol() && !t.type.isUnary() && !next.type.isUnary()) {
					if(it.hasPrevious()) {
						Token prev1 = it.previous(); // Select 'prev1' <--.
						if(prev1.type.equals(TType.FUNC_START) || prev1.type.equals(TType.COMMA)
								|| next.type.equals(TType.FUNC_END) || next.type.equals(TType.COMMA)
								|| prev1.type.isSymbol() || next.type.isSymbol()) {
							throw new ConfigCompileException("Unexpected symbol token (" + t.val() + ")", t.getTarget());
						}
						it.next(); // Select 'prev1' -->.
					}
				}
				it.next(); // Select 't' -->.
			}
		}

		// Set file options
		{
			Map<String, String> defaults = new HashMap<>();
			List<File> dirs = new ArrayList<>();
			if(file != null) {
				File f = file.getParentFile();
				while(true) {
					if(f == null) {
						break;
					}
					File fileOptionDefaults = new File(f, ".msfileoptions");
					if(fileOptionDefaults.exists()) {
						dirs.add(fileOptionDefaults);
					}
					f = f.getParentFile();
				}
			}
			Collections.reverse(dirs);
			for(File d : dirs) {
				try {
					defaults.putAll(TokenStream.parseFileOptions(FileUtil.read(d), defaults).getRawOptions());
				} catch (IOException ex) {
					throw new ConfigCompileException("Cannot read " + d.getAbsolutePath(), Target.UNKNOWN, ex);
				}
			}
			tokenList.setFileOptions(fileOptions.toString(), defaults);
		}
		// Make sure that the file options are the first non-comment code in the file
		{
			boolean foundCode = false;
			for(Token t : tokenList) {
				if(t.type.isFileOption()) {
					if(foundCode) {
						throw new ConfigCompileException("File options must be the first non-comment section in the"
								+ " code", t.target);
					}
					break;
				}
				if(!t.type.isComment() && !t.type.isWhitespace()) {
					foundCode = true;
				}
			}
		}

		{
			// Filename check
			String fileName = tokenList.getFileOptions().getName();
			if(!fileName.isEmpty()) {
				if(!file.getAbsolutePath().replace('\\', '/').endsWith(fileName.replace('\\', '/'))) {
					CompilerWarning warning = new CompilerWarning(file + " has the wrong file name in the file options ("
							+ fileName + ")", new Target(0, file, 0), null);
					env.getEnv(CompilerEnvironment.class).addCompilerWarning(null, warning);
				}
			}
		}
		{
			// Required extension check
			// TODO: Add support for specifying required versions
			Collection<ExtensionTracker> exts = ExtensionManager.getTrackers().values();
			Set<String> notFound = new HashSet<>();
			for(String extension : tokenList.getFileOptions().getRequiredExtensions()) {
				boolean found = false;
				for(ExtensionTracker t : exts) {
					if(t.getIdentifier().equalsIgnoreCase(extension)) {
						found = true;
						break;
					}
				}
				if(!found) {
					notFound.add(extension);
				}
			}
			if(!notFound.isEmpty()) {
				throw new ConfigCompileException("Could not compile file, because one or more required"
						+ " extensions are not loaded: " + StringUtils.Join(notFound, ", ")
						+ ". These extensions must be provided before compilation can continue.",
						new Target(0, file, 0));
			}
		}

		return tokenList;
	}

	/**
	 * This function breaks the token stream into parts, separating the aliases/MethodScript from the command triggers
	 *
	 * @param tokenStream
	 * @param envs
	 * @return
	 * @throws ConfigCompileException
	 */
	@SuppressWarnings("UnnecessaryLabelOnContinueStatement")
	public static List<Script> preprocess(TokenStream tokenStream,
			Set<Class<? extends Environment.EnvironmentImpl>> envs) throws ConfigCompileException {
		if(tokenStream == null || tokenStream.isEmpty()) {
			return new ArrayList<>();
		}

		// Remove leading newlines.
		while(!tokenStream.isEmpty() && tokenStream.getFirst().type == TType.NEWLINE) {
			tokenStream.removeFirst(); // Remove leading newlines.
		}

		// Return an empty list if there were only newlines.
		if(tokenStream.isEmpty()) {
			return new ArrayList<>();
		}

		// Remove whitespaces and duplicate newlines.
		{
			ListIterator<Token> it = tokenStream.listIterator(0);
			Token token = it.next();
			outerLoop:
			while(true) {
				switch(token.type) {
					case WHITESPACE: {
						it.remove(); // Remove whitespaces.
						if(!it.hasNext()) {
							break outerLoop;
						}
						token = it.next();
						continue outerLoop;
					}
					case NEWLINE: {
						while(it.hasNext()) {
							if((token = it.next()).type == TType.NEWLINE) {
								it.remove(); // Remove duplicate newlines.
							} else {
								continue outerLoop;
							}
						}
						break outerLoop;
					}
					default: {
						if(!it.hasNext()) {
							break outerLoop;
						}
						token = it.next();
						continue outerLoop;
					}
				}
			}
		}

		// Handle multiline constructs.
		// Take out newlines between the '= >>>' and '<<<' tokens (also removing the '>>>' and '<<<' tokens).
		// Also remove comments and also remove newlines that are behind a '\'.
		boolean insideMultiline = false;
		ListIterator<Token> it = tokenStream.listIterator(0);
		Token token = null;
		while(it.hasNext()) {
			token = it.next();

			switch(token.type) {
				case ALIAS_END: { // "=".
					if(it.hasNext()) {
						if(it.next().type == TType.MULTILINE_START) { // "= >>>".
							insideMultiline = true;
							it.remove(); // Remove multiline start (>>>).
							it.previous(); // Select 'token' <---.
							it.next(); // Select 'token' -->.
						} else {
							it.previous(); // Select 'next' <---.
						}
					}
					continue;
				}
				case MULTILINE_END: { // "<<<".

					// Handle multiline end token (<<<) without start.
					if(!insideMultiline) {
						throw new ConfigCompileException(
								"Found multiline end symbol, and no multiline start found", token.target);
					}

					insideMultiline = false;
					it.remove(); // Remove multiline end (<<<).
					continue;
				}
				case MULTILINE_START: { // ">>>".

					// Handle multiline start token (>>>) while already in multiline.
					if(insideMultiline) {
						throw new ConfigCompileException("Did not expect a multiline start symbol here,"
								+ " are you missing a multiline end symbol above this line?", token.target);
					}

					// Handle multiline start token (>>>) without alias end (=) in front.
					it.previous(); // Select 'token' <--.
					if(!it.hasPrevious() || it.previous().type != TType.ALIAS_END) {
						throw new ConfigCompileException(
								"Multiline symbol must follow the alias_end (=) symbol", token.target);
					}
					it.next(); // Select 'prev' -->.
					it.next(); // Select 'token' -->.
					continue;
				}
				case NEWLINE: { // "\n".

					// Skip newlines that are inside a multiline construct.
					if(insideMultiline) {
						it.remove(); // Remove newline.
					}
					continue;
				}

				// Remove comments.
				case COMMENT: {
					it.remove(); // Remove comment.
					continue;
				}
				default: {

					// Remove newlines that are behind a '\'.
					if(token.type != TType.STRING && token.val().equals("\\") && it.hasNext()) {
						if(it.next().type == TType.NEWLINE) {
							it.remove(); // Remove newline.
							it.previous(); // Select 'token' <--.
							it.next(); // Select 'token' -->.
						} else {
							it.previous(); // Select 'next' <--.
						}
					}
				}
			}
		}

		assert token != null;

		// Handle missing multiline end token.
		if(insideMultiline) {
			throw new ConfigCompileException("Expecting a multiline end symbol, but your last multiline alias appears to be missing one.", token.target);
		}

		// Now that we have all lines minified, we should be able to split on newlines
		// and easily find the left and right sides.
		List<Token> left = new ArrayList<>();
		List<Token> right = new ArrayList<>();
		List<Script> scripts = new ArrayList<>();
		SmartComment comment = null;
		tokenLoop:
		for(it = tokenStream.listIterator(0); it.hasNext();) {
			Token t = it.next();

			if(t.type == TType.SMART_COMMENT) {
				if(comment != null) {
					// TODO: Double smart comment, this should be an error case
				}
				comment = new SmartComment(t.val());
				t = it.next();
			}

			// Add all tokens until ALIAS_END (=) or end of stream.
			while(t.type != TType.ALIAS_END) {
				if(!it.hasNext()) {
					break tokenLoop; // End of stream.
				}
				left.add(t);
				t = it.next();
			}

			// Add all tokens until NEWLINE (\n).
			while(t.type != TType.NEWLINE) {
				assert it.hasNext(); // All files end with a newline, so end of stream should be impossible here.
				right.add(t);
				t = it.next();
			}

			// Create a new script for the obtained left and right if end of stream has not been reached.
			if(t.type == TType.NEWLINE) {

				// Check for spurious symbols, which indicate an issue with the script, but ignore any whitespace.
				for(int j = left.size() - 1; j >= 0; j--) {
					if(left.get(j).type == TType.NEWLINE) {
						if(j > 0 && left.get(j - 1).type != TType.WHITESPACE) {
							throw new ConfigCompileException(
									"Unexpected token: " + left.get(j - 1).val(), left.get(j - 1).getTarget());
						}
					}
				}

				// Create a new script from the command descriptor (left) and code (right) and add it to the list.
				Script s = new Script(left, right, null, envs, tokenStream.getFileOptions(), comment);
				scripts.add(s);

				// Create new left and right array for the next script.
				left = new ArrayList<>();
				right = new ArrayList<>();
				comment = null;
			}
		}

		// Return the scripts.
		return scripts;
	}

	/**
	 * Compiles the token stream into a valid ParseTree. This also includes optimization and reduction.
	 *
	 * @param stream The token stream, as generated by {@link #lex(String, Environment, File, boolean) lex}
	 * @param environment If an environment is already set up, it can be passed in here. The code will tolerate a null
	 * value, but if present, should be passed in. If the value is null, a standalone environment will be generated
	 * and used.
	 * @param envs The environments that are going to be present at runtime. Even if the {@code environment} parameter
	 * is null, this still must be non-null and populated with one or more values.
	 * @return A fully compiled, optimized, and reduced parse tree. If {@code stream} is null or empty, null is
	 * returned.
	 * @throws ConfigCompileException If the script contains syntax errors. Additionally, during optimization, certain
	 * methods may cause compile errors. Any function that can optimize static occurrences and throws a
	 * {@link ConfigRuntimeException} will have that exception converted to a ConfigCompileException.
	 * @throws com.laytonsmith.core.exceptions.ConfigCompileGroupException A ConfigCompileGroupException is just
	 * a collection of single {@link ConfigCompileException}s.
	 */
	public static ParseTree compile(TokenStream stream, Environment environment,
			Set<Class<? extends Environment.EnvironmentImpl>> envs) throws ConfigCompileException,
			ConfigCompileGroupException {
		return compile(stream, environment, envs, new StaticAnalysis(true));
	}

	/**
	 * Compiles the token stream into a valid ParseTree. This also includes optimization and reduction.
	 *
	 * @param stream The token stream, as generated by {@link #lex(String, Environment, File, boolean) lex}
	 * @param environment If an environment is already set up, it can be passed in here. The code will tolerate a null
	 * value, but if present, should be passed in. If the value is null, a standalone environment will be generated
	 * and used.
	 * @param envs The environments that are going to be present at runtime. Even if the {@code environment} parameter
	 * is null, this still must be non-null and populated with one or more values.
	 * @param staticAnalysis The static analysis object, or {@code null} to not perform static analysis. This object
	 * is used to perform static analysis on the AST that results from parsing, before any AST optimizations.
	 * this method has finished execution.
	 * @return A fully compiled, optimized, and reduced parse tree. If {@code stream} is null or empty, null is
	 * returned.
	 * @throws ConfigCompileException If the script contains syntax errors. Additionally, during optimization, certain
	 * methods may cause compile errors. Any function that can optimize static occurrences and throws a
	 * {@link ConfigRuntimeException} will have that exception converted to a ConfigCompileException.
	 * @throws com.laytonsmith.core.exceptions.ConfigCompileGroupException A ConfigCompileGroupException is just
	 * a collection of single {@link ConfigCompileException}s.
	 */
	public static ParseTree compile(TokenStream stream, Environment environment,
			Set<Class<? extends Environment.EnvironmentImpl>> envs, StaticAnalysis staticAnalysis)
			throws ConfigCompileException, ConfigCompileGroupException {
		Objects.requireNonNull(envs, "envs parameter must not be null");
		Objects.requireNonNull(staticAnalysis, "Static Analysis may be disabled, but cannot be null.");
		try {
			if(environment == null) {
					// We MUST have a CompilerEnvironment. It doesn't need to be used, but we have to create it at
					// this stage.
					environment = Static.GenerateStandaloneEnvironment(false);
			}
			if(!environment.hasEnv(CompilerEnvironment.class)) {
				Environment e = Static.GenerateStandaloneEnvironment(false);
				environment = environment.cloneAndAdd(e.getEnv(CompilerEnvironment.class));
			}
			environment.getEnv(CompilerEnvironment.class).setStaticAnalysis(staticAnalysis);
		} catch (IOException | DataSourceException | URISyntaxException | Profiles.InvalidProfileException ex) {
			throw new RuntimeException(ex);
		}
		Set<ConfigCompileException> compilerErrors = new HashSet<>();

		// Return a null AST when the program is empty.
		// Do run static analysis to allow for including this empty file in another file.
		if(stream == null || stream.isEmpty()) {
			staticAnalysis.analyze(null, environment, envs, compilerErrors);
			return null;
		}

		Target unknown;
		try {
			//Instead of using Target.UNKNOWN, we can at least set the file.
			unknown = new Target(0, stream.get(0).target.file(), 0);
		} catch (Exception e) {
			unknown = Target.UNKNOWN;
		}

		// Remove all newlines and whitespaces.
		ListIterator<Token> it = stream.listIterator(0);
		while(it.hasNext()) {
			if(it.next().type.isWhitespace()) {
				it.remove();
			}
		}

		processEarlyKeywords(stream, environment, compilerErrors);
		// All early keyword errors are handled, but then we halt compilation at this stage, since most
		// further errors are likley meaningless.
		if(!compilerErrors.isEmpty()) {
			throw new ConfigCompileGroupException(compilerErrors);
		}

		// Get the file options.
		final FileOptions fileOptions = stream.getFileOptions();

		final ParseTree rootNode = new ParseTree(fileOptions);
		rootNode.setData(CNull.NULL);
		ParseTree tree = rootNode;
		Stack<ParseTree> parents = new Stack<>();
		/**
		 * constructCount is used to determine if we need to use autoconcat when reaching a FUNC_END. The previous
		 * constructs, if the count is greater than 1, will be moved down into an autoconcat.
		 */
		Stack<AtomicInteger> constructCount = new Stack<>();
		constructCount.push(new AtomicInteger(0));
		parents.push(tree);

		tree.addChild(new ParseTree(new CFunction(__autoconcat__.NAME, unknown), fileOptions, true));
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

		int braceCount = 0;

		SmartComment lastSmartComment = null;

		// Create a Token array to iterate over, rather than using the LinkedList's O(n) get() method.
		Token[] tokenArray = stream.toArray(Token[]::new);
		for(int i = 0; i < tokenArray.length; i++) {
			t = tokenArray[i];
			Token prev1 = i - 1 >= 0 ? tokenArray[i - 1] : new Token(TType.UNKNOWN, "", t.target.copy());
			Token next1 = i + 1 < stream.size() ? tokenArray[i + 1] : new Token(TType.UNKNOWN, "", t.target.copy());
			Token next2 = i + 2 < stream.size() ? tokenArray[i + 2] : new Token(TType.UNKNOWN, "", t.target.copy());
			Token next3 = i + 3 < stream.size() ? tokenArray[i + 3] : new Token(TType.UNKNOWN, "", t.target.copy());

			// Brace handling
			if(t.type == TType.LCURLY_BRACKET) {
				ParseTree b = new ParseTree(new CFunction(__cbrace__.NAME, t.getTarget()), fileOptions, true);
				tree.addChild(b);
				tree = b;
				parents.push(b);
				braceCount++;
				constructCount.push(new AtomicInteger(0));
				continue;
			} else if(t.type == TType.RCURLY_BRACKET) {
				if(braceCount == 0) {
					throw new ConfigCompileException("Unexpected end curly brace", t.target);
				}
				braceCount--;
				if(constructCount.peek().get() > 1) {
					//We need to autoconcat some stuff
					int stacks = constructCount.peek().get();
					int replaceAt = tree.getChildren().size() - stacks;
					ParseTree c = new ParseTree(new CFunction(__autoconcat__.NAME, tree.getTarget()), fileOptions, true);
					List<ParseTree> subChildren = new ArrayList<>();
					for(int b = replaceAt; b < tree.numberOfChildren(); b++) {
						subChildren.add(tree.getChildAt(b));
					}
					c.setChildren(subChildren);
					if(replaceAt > 0) {
						List<ParseTree> firstChildren = new ArrayList<>();
						for(int d = 0; d < replaceAt; d++) {
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
			} else if(t.type == TType.LABEL && !tree.getChildren().isEmpty()) {
				//Associative array/label handling
				//If it's not an atomic identifier it's an error.
				if(!prev1.type.isAtomicLit() && prev1.type != TType.IVARIABLE && prev1.type != TType.KEYWORD) {
					ConfigCompileException error = new ConfigCompileException("Invalid label specified", t.getTarget());
					if(prev1.type == TType.FUNC_END) {
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
			} else if(t.type.equals(TType.LSQUARE_BRACKET)) {
				//Array notation handling
				//tree.addChild(new ParseTree(new CFunction("__cbracket__", t.getTarget()), fileOptions));
				arrayStack.push(new AtomicInteger(tree.getChildren().size() - 1));
				continue;
			} else if(t.type.equals(TType.RSQUARE_BRACKET)) {
				boolean emptyArray = false;
				if(prev1.type.equals(TType.LSQUARE_BRACKET)) {
					emptyArray = true;
				}
				if(arrayStack.size() == 1) {
					throw new ConfigCompileException("Mismatched square bracket", t.target);
				}
				//array is the location of the array
				int array = arrayStack.pop().get();
				//index is the location of the first node with the index
				int index = array + 1;
				if(array == -1 || array >= tree.numberOfChildren()) {
					throw new ConfigCompileException("Brackets are illegal here", t.target);
				}

				ParseTree myArray = tree.getChildAt(array);
				ParseTree myIndex;
				if(!emptyArray) {
					myIndex = new ParseTree(new CFunction(__autoconcat__.NAME, myArray.getTarget()), fileOptions, true);

					for(int j = index; j < tree.numberOfChildren(); j++) {
						myIndex.addChild(tree.getChildAt(j));
					}
				} else {
					myIndex = new ParseTree(new CSlice("0..-1", t.target), fileOptions, true);
				}
				tree.setChildren(tree.getChildren().subList(0, array));
				ParseTree arrayGet = new ParseTree(new CFunction(array_get.NAME, t.target), fileOptions, true);
				arrayGet.addChild(myArray);
				arrayGet.addChild(myIndex);

				// Check if the @var[...] had a negating "-" in front. If so, add a neg().
				if(!minusArrayStack.isEmpty() && arrayStack.size() + 1 == minusArrayStack.peek().get()) {
					if(!next1.type.equals(TType.LSQUARE_BRACKET)) { // Wait if there are more array_get's coming.
						ParseTree negTree = new ParseTree(new CFunction(neg.NAME, unknown), fileOptions, true);
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
			} else if(t.type == TType.SMART_STRING) {
				//Smart strings
				if(t.val().replace("\\\\", "").replace("\\@", "").contains("@")) {
					ParseTree function = new ParseTree(fileOptions);
					function.setData(new CFunction(__smart_string__.NAME, t.target));
					ParseTree string = new ParseTree(fileOptions);
					string.setData(new CString(t.value, t.target));
					function.addChild(string);
					tree.addChild(function);
				} else {
					// To convert a smart string to a regular string, we need to un-escape the '\' and '@' chars.
					String str = t.val().replace("\\\\", "\\").replace("\\@", "@");
					tree.addChild(new ParseTree(new CString(str, t.target), fileOptions, true));
				}
				constructCount.peek().incrementAndGet();
				continue;
			} else if(t.type == TType.DEREFERENCE) {
				//Currently unimplemented, but going ahead and making it strict
				compilerErrors.add(new ConfigCompileException("The '" + t.val() + "' symbol is not currently allowed in raw strings. You must quote all"
						+ " symbols.", t.target));
			} else if(t.type.equals(TType.FUNC_NAME)) {
				CFunction func = new CFunction(t.val(), t.target);
				{
					// Check for code upgrade warning
					try {
						OperatorPreferred opPref = func.getFunction().getClass().getAnnotation(OperatorPreferred.class);
						if(opPref != null) {
							String msg = "The operator \"" + opPref.value() + "\" is preferred over the functional"
									+ " usage.";
							CompilerWarning warning = new CompilerWarning(msg, t.target,
									FileOptions.SuppressWarning.CodeUpgradeNotices);
							environment.getEnv(CompilerEnvironment.class).addCodeUpgradeNotice(fileOptions, warning);
						}
					} catch (ConfigCompileException ex) {
						// The function doesn't exist. It may be a compile error later (or maybe not, if it's
						// preprocessed out) but we don't want to handle that at this point either way. In any
						// case, we can't find it, so don't report it.
					}
				}
				ParseTree f = new ParseTree(func, fileOptions);
				tree.addChild(f);
				constructCount.push(new AtomicInteger(0));
				tree = f;
				parents.push(f);
			} else if(t.type.equals(TType.FUNC_START)) {
				if(!prev1.type.equals(TType.FUNC_NAME)) {
					ParseTree f;
					if(prev1.type != TType.SEMICOLON
							&& prev1.type != TType.COMMA
							&& prev1.type != TType.FUNC_START
							&& !prev1.type.isSymbol()
							&& prev1.target.line() != t.target.line()
							&& MSVersion.LATEST.lt(new SimpleVersion(3, 3, 7))) {
						// Remove this in 3.3.7, and just always do the rewrite.
						CompilerWarning warning = new CompilerWarning("This will attempt to execute the previous"
								+ " statement in version 3.3.7 and above."
								+ " If this is not intended, place a semicolon at the end of the above line, and"
								+ " this warning will go away. If it is intended, move this parenthesis up to the same"
								+ " line to actually execute it.", t.target,
								SuppressWarning.PossibleUnexpectedExecution);
						environment.getEnv(CompilerEnvironment.class).addCompilerWarning(fileOptions, warning);
						f = new ParseTree(new CFunction(Compiler.__autoconcat__.NAME, unknown), fileOptions);
					} else {
						f = new ParseTree(new CFunction(Compiler.p.NAME, unknown), fileOptions);
					}
					constructCount.push(new AtomicInteger(0));
					tree.addChild(f);
					tree = f;
					parents.push(f);
				}
				parens++;
			} else if(t.type.equals(TType.FUNC_END)) {
				if(parens <= 0) {
					throw new ConfigCompileException("Unexpected parenthesis", t.target);
				}
				parens--;
				parents.pop(); // Pop function.
				if(constructCount.peek().get() > 1) {
					//We need to autoconcat some stuff
					int stacks = constructCount.peek().get();
					int replaceAt = tree.getChildren().size() - stacks;
					ParseTree c = new ParseTree(new CFunction(__autoconcat__.NAME, tree.getTarget()), fileOptions, true);
					List<ParseTree> subChildren = new ArrayList<>();
					for(int b = replaceAt; b < tree.numberOfChildren(); b++) {
						subChildren.add(tree.getChildAt(b));
					}
					c.setChildren(subChildren);
					if(replaceAt > 0) {
						List<ParseTree> firstChildren = new ArrayList<>();
						for(int d = 0; d < replaceAt; d++) {
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
				if(!minusFuncStack.isEmpty() && minusFuncStack.peek().get() == parens + 1) {
					if(next1.type.equals(TType.LSQUARE_BRACKET)) {
						// Move the negation to the array_get which contains this function.
						minusArrayStack.push(new AtomicInteger(arrayStack.size() + 1)); // +1 because the bracket isn't counted yet.
					} else {
						// Negate this function.
						ParseTree negTree = new ParseTree(new CFunction(neg.NAME, unknown), fileOptions);
						negTree.addChild(tree.getChildAt(tree.numberOfChildren() - 1));
						tree.removeChildAt(tree.numberOfChildren() - 1);
						tree.addChildAt(tree.numberOfChildren(), negTree);
					}
					minusFuncStack.pop();
				}

			} else if(t.type.equals(TType.COMMA)) {
				if(constructCount.peek().get() > 1) {
					int stacks = constructCount.peek().get();
					int replaceAt = tree.getChildren().size() - stacks;
					ParseTree c = new ParseTree(new CFunction(__autoconcat__.NAME, unknown), fileOptions);
					List<ParseTree> subChildren = new ArrayList<>();
					for(int b = replaceAt; b < tree.numberOfChildren(); b++) {
						subChildren.add(tree.getChildAt(b));
					}
					c.setChildren(subChildren);
					if(replaceAt > 0) {
						List<ParseTree> firstChildren = new ArrayList<>();
						for(int d = 0; d < replaceAt; d++) {
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
			} else if(t.type == TType.SLICE || next1.type == TType.SLICE) {
				if(t.type == TType.SLICE) {
					//We got here because the previous token isn't being ignored, because it's
					//actually a control character, instead of whitespace, but this is a
					//"empty first" slice notation. Compare this to the code below.
					try {
						CSlice slice;
						String value = next1.val();
						if(next1.type == TType.MINUS || next1.type == TType.PLUS) {
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
				if(next1.type.equals(TType.SLICE)) {
					//Slice notation handling
					try {
						CSlice slice;
						if(t.type.isSeparator() || (t.type.isWhitespace() && prev1.type.isSeparator()) || t.type.isKeyword()) {
							//empty first
							String value = next2.val();
							i++;
							if(next2.type == TType.MINUS || next2.type == TType.PLUS) {
								value = next2.val() + next3.val();
								i++;
							}
							slice = new CSlice(".." + value, next1.getTarget());
							if(t.type.isKeyword()) {
								tree.addChild(new ParseTree(new CKeyword(t.val(), t.getTarget()), fileOptions));
								constructCount.peek().incrementAndGet();
							}
						} else if(next2.type.isSeparator() || next2.type.isKeyword()) {
							//empty last
							String modifier = "";
							if(prev1.type == TType.MINUS || prev1.type == TType.PLUS) {
								//The negative would have already been inserted into the tree
								modifier = prev1.val();
								tree.removeChildAt(tree.getChildren().size() - 1);
							}
							slice = new CSlice(modifier + t.value + "..", t.target);
						} else {
							//both are provided
							String modifier1 = "";
							if(prev1.type == TType.MINUS || prev1.type == TType.PLUS) {
								//It's a negative, incorporate that here, and remove the
								//minus from the tree
								modifier1 = prev1.val();
								tree.removeChildAt(tree.getChildren().size() - 1);
							}
							Token first = t;
							if(first.type.isWhitespace()) {
								first = prev1;
							}
							Token second = next2;
							i++;
							String modifier2 = "";
							if(next2.type == TType.MINUS || next2.type == TType.PLUS) {
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
				}
			} else if(t.type == TType.VARARGS) {
				if(tree.getChildren().isEmpty()) {
					throw new ConfigCompileException("Unexpected varargs token (\"...\")", t.target);
				}
				ParseTree previous = tree.getChildAt(tree.getChildren().size() - 1);
				// TODO: Add LHSType as well, though this will not work as is with user objects. It may need
				// to be moved into a node modifier or something.
				if(!(previous.getData() instanceof CClassType)) {
					throw new ConfigCompileException("Unexpected varargs token (\"...\"). This can only be used with types.", t.target);
				}
				if(previous.getData() instanceof CClassType c) {
					previous.setData(c.asVarargs());
				}
				continue;
			} else if(t.type == TType.LIT) {
				Construct c;
				try {
					c = Static.resolveConstruct(t.val(), t.target, true);
				} catch (ConfigRuntimeException ex) {
					throw new ConfigCompileException(ex);
				}
				// make CDouble/CDecimal here because otherwise Long.parseLong() will remove
				// minus zero before decimals and leading zeroes after decimals
				if(c instanceof CInt && next1.type == TType.DOT && next2.type == TType.LIT) {
					try {
						c = new CDouble(Double.parseDouble(t.val() + '.' + next2.val()), t.target);
						i += 2;
					} catch (NumberFormatException e) {
						// Not a double
					}
					constructCount.peek().incrementAndGet();
				} else if(c instanceof CDecimal) {
					String neg = "";
					if(prev1.value.equals("-")) {
						// Absorb sign unless neg() becomes compatible with CDecimal
						neg = "-";
						tree.removeChildAt(tree.getChildren().size() - 1);
					} else {
						constructCount.peek().incrementAndGet();
					}
					if(next1.type == TType.DOT && next2.type == TType.LIT) {
						c = new CDecimal(neg + t.value.substring(2) + '.' + next2.value, t.target);
						i += 2;
					} else {
						c = new CDecimal(neg + t.value.substring(2), t.target);
					}
				} else {
					constructCount.peek().incrementAndGet();
				}
				tree.addChild(new ParseTree(c, fileOptions));
			} else if(t.type.equals(TType.STRING) || t.type.equals(TType.COMMAND)) {
				tree.addChild(new ParseTree(new CString(t.val(), t.target), fileOptions));
				constructCount.peek().incrementAndGet();
			} else if(t.type.equals(TType.IDENTIFIER)) {
				tree.addChild(new ParseTree(new CPreIdentifier(t.val(), t.target), fileOptions));
				constructCount.peek().incrementAndGet();
			} else if(t.type.isKeyword()) {
				tree.addChild(new ParseTree(new CKeyword(t.val(), t.getTarget()), fileOptions));
				constructCount.peek().incrementAndGet();
			} else if(t.type.equals(TType.IVARIABLE)) {
				tree.addChild(new ParseTree(new IVariable(t.val(), t.target), fileOptions));
				constructCount.peek().incrementAndGet();
			} else if(t.type.equals(TType.UNKNOWN)) {
				tree.addChild(new ParseTree(Static.resolveConstruct(t.val(), t.target), fileOptions));
				constructCount.peek().incrementAndGet();
			} else if(t.type.isSymbol()) { //Logic and math symbols

				// Attempt to find "-@var" and change it to "neg(@var)" if it's not @a - @b. Else just add the symbol.
				// Also handles "-function()" and "-@var[index]".
				if(t.type.equals(TType.MINUS) && !prev1.type.isAtomicLit() && !prev1.type.equals(TType.IVARIABLE)
						&& !prev1.type.equals(TType.VARIABLE) && !prev1.type.equals(TType.RCURLY_BRACKET)
						&& !prev1.type.equals(TType.RSQUARE_BRACKET) && !prev1.type.equals(TType.FUNC_END)
						&& (next1.type.equals(TType.IVARIABLE) || next1.type.equals(TType.VARIABLE) || next1.type.equals(TType.FUNC_NAME))) {

					// Check if we are negating a value from an array, function or variable.
					if(next2.type.equals(TType.LSQUARE_BRACKET)) {
						minusArrayStack.push(new AtomicInteger(arrayStack.size() + 1)); // +1 because the bracket isn't counted yet.
					} else if(next1.type.equals(TType.FUNC_NAME)) {
						minusFuncStack.push(new AtomicInteger(parens + 1)); // +1 because the function isn't counted yet.
					} else {
						ParseTree negTree = new ParseTree(new CFunction(neg.NAME, unknown), fileOptions);
						negTree.addChild(new ParseTree(new IVariable(next1.value, next1.target), fileOptions));
						tree.addChild(negTree);
						constructCount.peek().incrementAndGet();
						i++; // Skip the next variable as we've just handled it.
					}
				} else {
					tree.addChild(new ParseTree(new CSymbol(t.val(), t.type, t.target), fileOptions));
					constructCount.peek().incrementAndGet();
				}

			} else if(t.type == TType.DOT) {
				// Check for doubles that start with a decimal, otherwise concat
				Construct c = null;
				if(next1.type == TType.LIT && prev1.type != TType.STRING && prev1.type != TType.SMART_STRING) {
					try {
						c = new CDouble(Double.parseDouble('.' + next1.val()), t.target);
						i++;
					} catch (NumberFormatException e) {
						// Not a double
					}
				}
				if(c == null) {
					c = new CSymbol(".", TType.CONCAT, t.target);
				}
				tree.addChild(new ParseTree(c, fileOptions));
				constructCount.peek().incrementAndGet();
			} else if(t.type.equals(TType.VARIABLE) || t.type.equals(TType.FINAL_VAR)) {
				tree.addChild(new ParseTree(new Variable(t.val(), null, false, t.type.equals(TType.FINAL_VAR), t.target), fileOptions));
				constructCount.peek().incrementAndGet();
				//right_vars.add(new Variable(t.val(), null, t.line_num));
			} else if(t.type.equals(TType.SMART_COMMENT)) {
				lastSmartComment = new SmartComment(t.val());
				continue;
			} else if(t.type.equals(TType.SEMICOLON)) {
				tree.addChild(new ParseTree(new CSemicolon(t.target), fileOptions));
				constructCount.peek().incrementAndGet();
			}
			if(lastSmartComment != null) {
				if(tree.getChildren().isEmpty()) {
					tree.getNodeModifiers().setComment(lastSmartComment);
				} else {
					tree.getChildren().get(tree.getChildren().size() - 1).getNodeModifiers().setComment(lastSmartComment);
				}
				lastSmartComment = null;
			}
		}

		assert t != null || stream.isEmpty();

		// Handle mismatching square brackets "[]".
		assert !arrayStack.isEmpty() : "The last element of arrayStack should be present, but it was popped.";
		if(arrayStack.size() != 1) {

			// Some starting square bracket '[' was not closed at the end of the script.
			// Find the last '[' that was not closed and use that as target instead of the last line of the script.
			Target target = traceMismatchedOpenToken(stream, TType.LSQUARE_BRACKET, TType.RSQUARE_BRACKET);
			assert target != null : "Mismatched bracket was detected, but target-finding code could not find it.";

			// Throw a CRE.
			throw new ConfigCompileException("Mismatched square brackets", target);
		}

		// Handle mismatching parentheses "()".
		if(parens != 0) {

			// Some starting parentheses '(' was not closed at the end of the script.
			// Find the last '(' that was not closed and use that as target instead of the last line of the script.
			Target target = traceMismatchedOpenToken(stream, TType.FUNC_START, TType.FUNC_END);
			assert target != null : "Mismatched parentheses was detected, but target-finding code could not find it.";

			// Throw a CRE.
			throw new ConfigCompileException("Mismatched parentheses", target);
		}

		// Handle mismatching curly braces "{}".
		if(braceCount != 0) {

			// Some starting curly brace '{' was not closed at the end of the script.
			// Find the last '{' that was not closed and use that as target instead of the last line of the script.
			Target target = traceMismatchedOpenToken(stream, TType.LCURLY_BRACKET, TType.RCURLY_BRACKET);
			assert target != null : "Mismatched curly brace was detected, but target-finding code could not find it.";

			// Throw a CRE.
			throw new ConfigCompileException("Mismatched curly braces", target);
		}

		// Assert that the parents stack does not have unexpected unhandled elements remaining.
		assert parents.size() == 2 : "Expected exactly the root and autoconcat nodes on parents stack.";
		assert parents.pop() == tree : "Mismatching stack element.";
		assert parents.pop() == rootNode : "Expected the last element of the stack to be the root node.";
		assert rootNode.getChildAt(0) == tree : "Expected tree to be the first child of the root node.";

		// Process the AST.
		Stack<List<Procedure>> procs = new Stack<>();
		procs.add(new ArrayList<>());
		processKeywords(tree, environment, compilerErrors);
		addSelfStatements(tree, environment, envs, compilerErrors);
		rewriteAutoconcats(tree, environment, envs, compilerErrors, true);
		processLateKeywords(tree, environment, compilerErrors);
		checkLinearComponents(tree, environment, compilerErrors);
		postParseRewrite(rootNode, environment, envs, compilerErrors, true); // Pass rootNode since this might rewrite 'tree'.
		tree = rootNode.getChildAt(0);
		moveNodeModifiersOffSyntheticNodes(tree);
		staticAnalysis.analyze(tree, environment, envs, compilerErrors);
		optimize(tree, environment, envs, procs, compilerErrors);
		link(tree, compilerErrors);
		checkFunctionsExist(tree, compilerErrors, envs);
		checkBreaks(tree, compilerErrors);
		if(!staticAnalysis.isLocalEnabled()) {
			checkUnhandledCompilerConstructs(tree, environment, compilerErrors);
		}
		if(!compilerErrors.isEmpty()) {
			if(compilerErrors.size() == 1) {
				// Just throw the one CCE
				throw compilerErrors.iterator().next();
			} else {
				throw new ConfigCompileGroupException(compilerErrors);
			}
		}
		eliminateDeadCode(tree, environment, envs);
		return rootNode;
	}

	private static void checkLinearComponents(ParseTree tree, Environment env,
			Set<ConfigCompileException> compilerErrors) {
		for(ParseTree m : tree.getAllNodes()) {
			if(m.getData() instanceof CBareString && !(m.getData() instanceof CKeyword)) {
				if(m.getFileOptions().isStrict()) {
					compilerErrors.add(new ConfigCompileException("Use of bare (unquoted) string: "
							+ m.getData().val(), m.getTarget()));
				} else {
					env.getEnv(CompilerEnvironment.class).addCompilerWarning(m.getFileOptions(),
							new CompilerWarning("Use of bare (unquoted) string: "
									+ m.getData().val(), m.getTarget(),
									FileOptions.SuppressWarning.UseBareStrings));
				}
			}
		}
	}

	/**
	 * Trace target of mismatching open tokens such as '(' in '()' or '{' in '{}'. This should be used when it is
	 * known that there are more start than close tokens, but no target is known for the extra start token.
	 * @param stream - The token stream to scan.
	 * @param openType - The open type, which would be {@link TType#FUNC_START (} for a parentheses check.
	 * @param closeType - The close type, which would be {@link TType#FUNC_END )} for a parentheses check.
	 * @return The target of the last occurrence of the opening type that did not have a matching closing type.
	 * Returns null of no target was found.
	 */
	private static Target traceMismatchedOpenToken(TokenStream stream, TType openType, TType closeType) {
		// Some starting parentheses '(' was not closed at the end of the script.
		// Find the last '(' that was not closed and use that as target instead of the last line of the script.
		Iterator<Token> iterator = stream.descendingIterator();
		int closingCount = 0;
		while(iterator.hasNext()) {
			Token token = iterator.next();
			if(token.type == closeType) {
				closingCount++;
			} else if(token.type == openType) {
				if(closingCount <= 0) {
					return token.target;
				}
				closingCount--;
			}
		}
		return null;
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
		if(!(tree.getData() instanceof CFunction)) {
			//Don't care about these
			return;
		}
		if(!((CFunction) tree.getData()).hasFunction()) {
			//We need to recurse, but this is not expected to be a function
			for(ParseTree child : tree.getChildren()) {
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
		if(func.getClass().getAnnotation(nolinking.class) != null) {
			// Don't link here
			return;
		}
		// We have special handling for procs and closures, and of course break and the loops.
		// If any of these are here, we kick into special handling mode. Otherwise, we recurse.
		if(func instanceof ControlFlow._break) {
			// First grab the counter in the break function. If the break function doesn't
			// have any children, then 1 is implied. break() requires the argument to be
			// a CInt, so if it weren't, there should be a compile error.
			long breakCounter = 1;
			if(tree.getChildren().size() == 1) {
				try {
					breakCounter = ArgumentValidation.getInt32(tree.getChildAt(0).getData(), tree.getChildAt(0).getTarget());
				} catch (CRECastException | CRERangeException e) {
					compilerErrors.add(new ConfigCompileException(e));
					return;
				}
			}
			if(breakCounter > currentLoops) {
				// Throw an exception, as this would break above a loop. Different error messages
				// are applied to different cases
				if(currentLoops == 0) {
					compilerErrors.add(new ConfigCompileException("The break() function can only break out of loops" + (lastUnbreakable == null ? "."
							: ", but an attempt to break out of a " + lastUnbreakable + " was detected."), tree.getTarget()));
				} else {
					compilerErrors.add(new ConfigCompileException("Too many breaks"
							+ " detected. Check your loop nesting, and set the break count to an appropriate value.", tree.getTarget()));
				}
			}
			return;
		}
		if(func.getClass().getAnnotation(unbreakable.class) != null) {
			// Parse the children like normal, but reset the counter to 0.
			for(ParseTree child : tree.getChildren()) {
				checkBreaks0(child, 0, func.getName(), compilerErrors);
			}
			return;
		}
		if(func.getClass().getAnnotation(breakable.class) != null) {
			// Don't break yet, still recurse, but up our current loops counter.
			currentLoops++;
		}
		for(ParseTree child : tree.getChildren()) {
			checkBreaks0(child, currentLoops, lastUnbreakable, compilerErrors);
		}
	}

	/**
	 * In some steps, smart comments and other modifiers might need to be placed on synthetic nodes. These should
	 * be moved up into the top level synthetic node. This should be one of the last transformations
	 * called after tree rewrites are done.
	 */
	private static void moveNodeModifiersOffSyntheticNodes(ParseTree node) {
		if(node.isSyntheticNode() && node.hasChildren() && node.getNodeModifiers() != null) {
			node.getNodeModifiers().merge(node.getChildAt(0).getNodeModifiers());
		}
		for(ParseTree child : node.getChildren()) {
			moveNodeModifiersOffSyntheticNodes(child);
		}
	}

	private static void addSelfStatements(ParseTree root, Environment env,
			Set<Class<? extends Environment.EnvironmentImpl>> envs, Set<ConfigCompileException> compilerErrors) {
		for(int i = 0; i < root.numberOfChildren(); i++) {
			ParseTree node = root.getChildAt(i);
			boolean isSelfStatement = false;
			if(node.getData() instanceof CFunction cf && cf.hasFunction()) {
				Function function = null;
				try {
					function = cf.getFunction();
				} catch(ConfigCompileException ex) {
					// Functions should be validated later, in case they're removed.
				}
				try {
					isSelfStatement = function != null
							&& function.isSelfStatement(node.getTarget(), env, node.getChildren(), envs);
				} catch(ConfigCompileException ex) {
					compilerErrors.add(ex);
					return;
				}
			}
			if(isSelfStatement) {
				int offset = i + 1;
				if(!(root.getData() instanceof CFunction cf && cf.val().equals(Compiler.__autoconcat__.NAME))) {
					// We need to create an autoconcat node first, and put this and the semicolon in that
					ParseTree newNode = new ParseTree(new CFunction(Compiler.__autoconcat__.NAME, Target.UNKNOWN), root.getFileOptions(), true);
					newNode.addChild(root);
					root = newNode;
					offset = 1;
				}
				root.getChildren().add(offset, new ParseTree(new CSemicolon(Target.UNKNOWN),
						node.getFileOptions(), true));
			}
			addSelfStatements(node, env, envs, compilerErrors);
		}
	}

	/**
	 * Rewrites __autoconcat__ AST nodes to executable AST nodes. This should be called before AST optimization, static
	 * analysis and anything else that requires a fully executable AST. When this method returns, any __autoconcat__
	 * that did not contain compile errors has been rewritten.
	 *
	 * @param root
	 * @param env
	 * @param envs
	 * @param compilerExceptions
	 * @param rewriteKeywords
	 */
	public static void rewriteAutoconcats(ParseTree root, Environment env,
			Set<Class<? extends Environment.EnvironmentImpl>> envs, Set<ConfigCompileException> compilerExceptions,
			boolean rewriteKeywords) {
		if(!root.hasChildren()) {
			if(root.getData() instanceof CFunction && root.getData().val().equals(__autoconcat__.NAME)) {
				ParseTree tree = new ParseTree(new CFunction(__statements__.NAME, root.getTarget()),
						root.getFileOptions(), true);
				tree.setOptimized(true);
				root.replace(tree);
			}
			return;
		}
		List<List<ParseTree>> children = new ArrayList<>();
		List<ParseTree> ongoingChildren = new ArrayList<>();
		for(int i = 0; i < root.numberOfChildren(); i++) {
			ParseTree child = root.getChildAt(i);
			if(child.getData() instanceof CSemicolon) {
				if(ongoingChildren.isEmpty()) {
					env.getEnv(CompilerEnvironment.class).addCompilerWarning(child.getFileOptions(),
							new CompilerWarning("Empty statement.", child.getTarget(),
									SuppressWarning.UselessCode));
					continue;
				}
				children.add(ongoingChildren);
				ongoingChildren = new ArrayList<>();
			} else {
				ongoingChildren.add(child);
			}
		}

		List<ParseTree> newChildren = new ArrayList<>();
		ParseTree statements = new ParseTree(new CFunction(Compiler.__statements__.NAME,
				root.getTarget()), root.getFileOptions(), true);
		for(int k = 0; k < children.size(); k++) {
			List<ParseTree> subChild = children.get(k);
			ParseTree autoconcat = new ParseTree(new CFunction(Compiler.__autoconcat__.NAME,
					root.getTarget()), root.getFileOptions(), true);
			autoconcat.setChildren(subChild);
			rewriteAutoconcats(autoconcat, env, envs, compilerExceptions, false);
			if(rewriteKeywords) {
				if(autoconcat.getChildren().isEmpty()) {
					// Uh oh, the rewrite pulled it up, but we still need it to be in a sub-function, so put
					// it back in an autoconcat. We'll pull it back up again in the next step if we need.
					ParseTree autoconcat2 = new ParseTree(new CFunction(Compiler.__autoconcat__.NAME,
							root.getTarget()), root.getFileOptions(), true);
					autoconcat2.addChild(autoconcat);
					autoconcat = autoconcat2;
				}
				processLateKeywords(autoconcat, env, compilerExceptions);
			}
			// Between the rewrite and the keywords, we've removed the need for autoconcat, so pull
			// up the value here.
			if(autoconcat.getChildren().size() == 1 && autoconcat.getChildAt(0).getData() instanceof CFunction cf
					&& cf.val().equals(Compiler.__statements__.NAME)
					&& autoconcat.isSyntheticNode()) {
				autoconcat.replace(autoconcat.getChildAt(0));
			}
			if(autoconcat.getData() instanceof CFunction cf
					&& cf.val().equals(Compiler.__statements__.NAME)) {
				// Pull up any sub-statements, instead of nesting.
				for(ParseTree statementChild : autoconcat.getChildren()) {
					statements.addChild(statementChild);
				}
			} else {
				statements.addChild(autoconcat);
			}
			if(rewriteKeywords
					&& autoconcat.getData() instanceof CFunction cf
					&& autoconcat.getData().val().equals(Compiler.__statements__.NAME)
					&& autoconcat.getChildren().size() > 1
					&& root.getFileOptions().isStrict()) {
				doMissingSemicolonError(autoconcat.getChildAt(0).getTarget(), compilerExceptions);
			}
		}
		if(statements.getChildren().size() == 1 && statements.getChildAt(0).getData() instanceof CFunction cf) {
			if(statements.getChildAt(0).isSyntheticNode()
					&& (
						cf.val().equals(Compiler.__statements__.NAME)
						|| cf.val().equals(Compiler.__autoconcat__.NAME)
						|| cf.val().equals(StringHandling.sconcat.NAME)
						|| cf.val().equals(DataHandling._string.NAME)
					)) {
				// Pull it up, this was a synthetic node that was generated from autoconcat
				statements.setChildren(statements.getChildAt(0).getChildren());
			}
		}
		if(!statements.getChildren().isEmpty()) {
			newChildren.add(statements);
		}

		root.setChildren(newChildren);

		if(!ongoingChildren.isEmpty()) {
			for(ParseTree child : ongoingChildren) {
				root.addChild(child);
			}
		}


		for(int j = 0; j < root.getChildren().size(); j++) {
			rewriteAutoconcats(root.getChildAt(j), env, envs, compilerExceptions, true);
		}
		if(root.getData() instanceof CFunction && root.getData().val().equals(__autoconcat__.NAME)) {

			// In non-strict mode, let __autoconcat__ glue arguments together with sconcat.
			boolean returnSConcat = !root.getFileOptions().isStrict();

			try {
				ParseTree ret = __autoconcat__.rewrite(root.getChildren(), returnSConcat, envs);
				root.replace(ret);
				// TODO: Remove this. This is a stopgap measure, because some keyword handlers
				// don't remove the keywords before this step, and they are handled in a postParseRewrite
				// override. This goes against the idea of the keyword handlers, but in the meantime,
				// it means that we can't always detect if it's supposed to be a statement or not,
				// so just bypass the check in this case.
				boolean hasKeyword = false;
				for(ParseTree node : root.getChildren()) {
					if(node.getData() instanceof CKeyword) {
						hasKeyword = true;
						break;
					}
				}
				if(rewriteKeywords && !hasKeyword && root.getFileOptions().isStrict()
						&& root.getData() instanceof CFunction cf
						&& cf.val().equals(Compiler.__statements__.NAME)
						&& ret.numberOfChildren() > 1
						&& !ongoingChildren.isEmpty()) {
					// The last statement was expected to have a semicolon, but didn't.
					doMissingSemicolonError(root.getChildAt(root.numberOfChildren() - 1)
							.getTarget(), compilerExceptions);
				}
			} catch (ConfigCompileException ex) {
				compilerExceptions.add(ex);
			}
		}
	}

	private static void doMissingSemicolonError(Target target, Set<ConfigCompileException> exceptions) {
//		String message = "Semicolon ';' expected.";
//		if(MSVersion.LATEST.lte(new SimpleVersion(3, 3, 6))) {
//			// Warning
//			message += " This will be an error in the next version.";
//			env.getEnv(CompilerEnvironment.class).addFutureErrorCompilerWarning(message, target);
//		} else {
//			// Error
//			exceptions.add(new ConfigCompileException(message, target));
//		}
	}

	/**
	 * Allows functions to perform a rewrite step to rewrite the AST as received from the parser to a valid
	 * executable AST. Optimizations should not yet be performed in this rewrite step.
	 * Additionally, this step traverses all {@link CFunction} nodes and ensures that they either have their represented
	 * function cached or are unknown by the compiler.
	 * Traversal is pre-order depth-first.
	 * @param ast The abstract syntax tree representing this function.
	 * @param env The environment.
	 * @param envs The set of expected environment classes at runtime.
	 * @param exceptions A set to put compile errors in.
	 * @param topLevel True if this is the top level of the parse tree. External code should always pass in true here.
	 * @return The rewritten AST node that should completely replace the AST node representing this function, or
	 * {@code null} to not replace this AST node. Note that the rewrite will be called on this newly returned AST node
	 * if it is different from the passed node.
	 */
	private static ParseTree postParseRewrite(ParseTree ast, Environment env,
			Set<Class<? extends Environment.EnvironmentImpl>> envs, Set<ConfigCompileException> exceptions, boolean topLevel) {
		Mixed node = ast.getData();
		if(node instanceof CFunction cFunc) {
			if(cFunc.hasFunction()) {
				try {
					Function func = cFunc.getFunction();
					ParseTree newAst = func.postParseRewrite(ast, env, envs, exceptions);
					if(newAst != null) {
						ast = newAst;
					}
				} catch (ConfigCompileException ex) {
					// Unknown function. This will be handled later.
				}
			}
		}
		boolean isStrict = ast.getFileOptions().isStrict();
		for(int i = 0; i < ast.numberOfChildren(); i++) {
			ParseTree child = ast.getChildAt(i);
			ParseTree newChild = postParseRewrite(child, env, envs, exceptions, false);
			if(newChild != null && child != newChild) {
				ast.getChildren().set(i, newChild);
				i--; // Allow the new child to do a rewrite step as well.
				continue;
			}
			// Add compile exceptions when encountering child statements in function arguments where
			// statements are not acceptable because void would be an invalid argument type. This would otherwise be
			// a runtime error in strict mode where auto-concat is not allowed and statements are used instead.
			// This can be updated once a more comprehensive void return type check is done.
			if(child.getData() instanceof CFunction
					&& child.getData().val().equals(Compiler.__statements__.NAME)
					&& ast.getData() instanceof CFunction cFunction) {

				Function function = cFunction.getCachedFunction();
				if(function == null) {
					continue;
				}

				boolean statementsAllowed = false;
				if(function instanceof BranchStatement branchStatement) {
					List<Boolean> branches = branchStatement.statementsAllowed(ast.getChildren());
					if(branches.get(i)) {
						statementsAllowed = true;
					}
				}

				if(statementsAllowed) {
					continue;
				}

				String unexpectedStatement = "Unexpected statement; ";
				try {
					if(function.isSelfStatement(ast.getTarget(), env, ast.getChildren(), envs)) {
						// We can give a better error message here, because the semicolon wasn't actually added
						// by the user (probably) it's the self statement function that's the problem here.
						unexpectedStatement += cFunction.val() + " not allowed in this context.";
					} else if(ast.getFileOptions().isStrict() && cFunction.getTarget() != child.getTarget()) {
						unexpectedStatement += "auto concatenation not allowed in Strict mode. (or invalid semi-colon)";
					} else {
						unexpectedStatement += "semicolon (;) not allowed in this context.";
					}
				} catch(ConfigCompileException ex) {
					exceptions.add(ex);
				}

				if(ast.getFileOptions().isStrict()) {
					exceptions.add(new ConfigCompileException(unexpectedStatement, child.getTarget()));
				} else {
					// Statements aren't allowed here, but we aren't in strict mode, so
					// pull up the value in the statement to here. sconcat is an exception to this rule, since
					// it's entirely too special.
					if(!cFunction.val().equals(StringHandling.sconcat.NAME)) {
						if(child.getChildren().size() != 1) {
							exceptions.add(new ConfigCompileException(unexpectedStatement, child.getTarget()));
						} else {
							CompilerWarning warning = new CompilerWarning(unexpectedStatement,
									child.getTarget(), SuppressWarning.UnexpectedStatement);
							env.getEnv(CompilerEnvironment.class).addCompilerWarning(ast.getFileOptions(), warning);
							child.replace(child.getChildren().get(0));
						}
					}
				}
			}
		}

		return ast;
	}

	/**
	 * Recurses down the tree and
	 * <ul>
	 *     <li>Links functions</li>
	 *     <li>Validates function argument size</li>
	 * </ul>
	 * This should be called after {@link #optimize(ParseTree, Environment, Set, Stack, Set)} so that functions with
	 * custom linkage only get linked when they are not removed during optimization.
	 *
	 *
	 * @param tree
	 * @param compilerErrors
	 */
	private static void link(ParseTree tree, Set<ConfigCompileException> compilerErrors) {
		if(tree.getData() instanceof CFunction cFunction) {
			Function function = cFunction.getCachedFunction();

			// Check the argument count, and do any custom linking the function may have.
			if(function != null) {
				if(function.getClass().getAnnotation(nolinking.class) != null) {
					// Don't link children of a nolinking function.
					return;
				}

				if(!isValidNumArgs(function, tree.getChildren().size())) {
					compilerErrors.add(new ConfigCompileException("Incorrect number of arguments passed to "
							+ tree.getData().val(), tree.getData().getTarget()));
				}
				if(function instanceof Optimizable op) {
					if(op.optimizationOptions().contains(OptimizationOption.CUSTOM_LINK)) {
						try {
							op.link(tree.getData().getTarget(), tree.getChildren());
						} catch (ConfigRuntimeException ex) {
							compilerErrors.add(new ConfigCompileException(ex));
						} catch (ConfigCompileException ex) {
							compilerErrors.add(ex);
						}
					}
				}
			}
		}

		// Walk the children.
		for(ParseTree child : tree.getChildren()) {
			if(child.getData() instanceof CFunction) {
				link(child, compilerErrors);
			}
		}
	}

	/**
	 * Recurses down the tree and checks whether functions exist in the given environments, generating compile errors
	 * if they don't. This should be called after optimization, since it's okay to use undefined functions as long as
	 * static optimization can determine that they are never called in the current environment.
	 * This check ignores child nodes of functions with the {@link nolinking} annotation.
	 * @param tree
	 */
	private static void checkFunctionsExist(ParseTree tree, Set<ConfigCompileException> compilerErrors,
			Set<Class<? extends Environment.EnvironmentImpl>> envs) {

		// Ignore non-CFunction nodes.
		if(tree.getData() instanceof CFunction cFunc) {

			// Check current node, returning if it is a 'nolinking' function.
			if(cFunc.hasFunction()) {
				FunctionBase func = cFunc.getCachedFunction(envs);
				lookup: {
					if(func == null) {

						// Technically, we could be dealing with a FunctionBase that isn't cached. So do another lookup.
						try {
							func = FunctionList.getFunction(cFunc, envs);
						} catch (ConfigCompileException ex) {
							compilerErrors.add(ex);
							break lookup;
						}
					}
					if(func.getClass().getAnnotation(nolinking.class) != null) {
						return; // Don't check children of 'nolinking' functions.
					}
				}
			}

			// Recursively check children.
			for(ParseTree child : tree.getChildren()) {
				checkFunctionsExist(child, compilerErrors, envs);
			}
		}
	}

	/**
	 * Recurses down into the tree, attempting to optimize where possible. A few things have strong coupling, for
	 * information on these items, see the documentation included in the source.
	 *
	 * @param tree
	 * @return
	 */
	private static void optimize(ParseTree tree, Environment env,
			Set<Class<? extends Environment.EnvironmentImpl>> envs, Stack<List<Procedure>> procs,
			Set<ConfigCompileException> compilerErrors) {
		if(tree.isOptimized()) {
			return; //Don't need to re-run this
		}

		if(!(tree.getData() instanceof CFunction)) {
			//There's no way to optimize something that's not a function
			return;
		}
		//If it is a proc definition, we need to go ahead and see if we can add it to the const proc stack
		if(tree.getData().val().equals(DataHandling.proc.NAME)) {
			procs.push(new ArrayList<>());
		}
		CFunction cFunction = (CFunction) tree.getData();
		Function func = cFunction.getCachedFunction(envs);
		if(func != null) {
			if(func.getClass().getAnnotation(nolinking.class) != null) {
				//It's an unlinking function, so we need to stop at this point
				return;
			}
		}

		List<ParseTree> children = tree.getChildren();
		if(func instanceof Optimizable && ((Optimizable) func).optimizationOptions()
				.contains(OptimizationOption.PRIORITY_OPTIMIZATION) && isValidNumArgs(func, children.size())) {
			// This is a priority optimization function, meaning it needs to be optimized before its children are.
			// This is required when optimization of the children could cause different internal behavior, for
			// instance if this function is expecting the precense of soem code element, but the child gets
			// optimized out, this would cause an error, even though the user did in fact provide code in that
			// section.
			try {
				((Optimizable) func).optimizeDynamic(tree.getTarget(), env, envs, children, tree.getFileOptions());
			} catch (ConfigCompileException ex) {
				// If an error occurs, we will skip the rest of this element
				compilerErrors.add(ex);
				return;
			} catch (ConfigCompileGroupException ex) {
				compilerErrors.addAll(ex.getList());
				return;
			} catch (ConfigRuntimeException ex) {
				compilerErrors.add(new ConfigCompileException(ex));
				return;
			}
		}

		boolean fullyStatic = true;
		boolean hasIVars = false;
		for(ParseTree node : children) {
			if(node.getData() instanceof CFunction) {
				optimize(node, env, envs, procs, compilerErrors);
			}

			if(node.getData() instanceof Construct d) {
				if(d.isDynamic() || (d instanceof IVariable)) {
					fullyStatic = false;
				}
			}
			if(node.getData() instanceof IVariable) {
				hasIVars = true;
			}
		}

		//In all cases, at this point, we are either unable to optimize, or we will
		//optimize, so set our optimized variable at this point.
		tree.setOptimized(true);

		if(func == null) {
			//It's a proc call. Let's see if we can optimize it
			Procedure p = null;
			loop:
			for(List<Procedure> proc : procs) {
				for(Procedure pp : proc) {
					if(pp.getName().equals(cFunction.val())) {
						p = pp;
						break loop;
					}
				}
			}
			if(p != null) {
				try {
					Mixed c = DataHandling.proc.optimizeProcedure(p.getTarget(), p, children);
					if(c != null) {
						tree.setData(c);
						tree.removeChildren();
						return;
					} //else Nope, couldn't optimize.
				} catch (ConfigRuntimeException ex) {
					//Cool. Caught a runtime error at compile time :D
					compilerErrors.add(new ConfigCompileException(ex));
				}
			}
			//else this procedure isn't listed yet. Maybe a compiler error, maybe not, depends,
			//so we can't for sure say, but we do know we can't optimize this
			return;
		}
		if(tree.getData().val().equals(DataHandling.proc.NAME)) {
			//Check for too few arguments
			if(children.size() < 2) {
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
				ParseTree root = new ParseTree(
						new CFunction(__autoconcat__.NAME, Target.UNKNOWN), tree.getFileOptions());
				Script fakeScript = Script.GenerateScript(root, "*", null);

				if(env.hasEnv(GlobalEnv.class)) {
					// For testing, we frequently set this to null, so check this first.
					env.getEnv(GlobalEnv.class).SetFlag("no-check-undefined", true);
				}
				Procedure myProc = DataHandling.proc.getProcedure(tree.getTarget(), env, fakeScript, children.toArray(ParseTree[]::new));
				tree.getNodeModifiers().merge(children.get(0).getNodeModifiers());
				if(env.hasEnv(GlobalEnv.class)) {
					env.getEnv(GlobalEnv.class).ClearFlag("no-check-undefined");
				}
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
		if(func instanceof Optimizable optimizable) {
			options = optimizable.optimizationOptions();
		}
		if(options.contains(OptimizationOption.OPTIMIZE_DYNAMIC) && isValidNumArgs(func, tree.numberOfChildren())) {
			try {
				ParseTree tempNode;
				try {
					for(ParseTree child : tree.getChildren()) {
						if(child.getData() instanceof CSymbol) {
							throw new ConfigCompileException("Unexpected symbol", tree.getData().getTarget());
						}
					}
					tempNode = ((Optimizable) func).optimizeDynamic(tree.getData().getTarget(), env, envs,
							tree.getChildren(), tree.getFileOptions());
				} catch (ConfigRuntimeException e) {
					//Turn it into a compile exception, then rethrow
					throw new ConfigCompileException(e);
				} catch (Error t) {
					throw new Error("The linked Error had a code target on or around " + tree.getData().getTarget(), t);
				}
				if(tempNode == Optimizable.PULL_ME_UP) {
					if(tree.hasChildren()) {
						tempNode = tree.getChildAt(0);
					} else {
						tempNode = null;
					}
				}
				if(tempNode == Optimizable.REMOVE_ME) {
					tree.setData(new CFunction(p.NAME, Target.UNKNOWN));
					tree.removeChildren();
				} else if(tempNode != null) {
					tree.setData(tempNode.getData());
					tree.setOptimized(tempNode.isOptimized());
					tree.setChildren(tempNode.getChildren());
					Construct.SetWasIdentifierHelper(tree.getData(), tempNode.getData(), false);
					optimize(tree, env, envs, procs, compilerErrors);
					tree.setOptimized(true);
					//Some functions can actually make static the arguments, for instance, by pulling up a hardcoded
					//array, so if they have reversed this, make note of that now
					if(tempNode.hasBeenMadeStatic()) {
						fullyStatic = true;
					}
				} //else it wasn't an optimization, but a compile check
			} catch (ConfigCompileException ex) {
				compilerErrors.add(ex);
				// Also turn off optimizations for the rest of this flow, so we don't try the other optimization
				// mechanisms, which are also bound to fail.
				options = NO_OPTIMIZATIONS;
			} catch (ConfigCompileGroupException ex) {
				compilerErrors.addAll(ex.getList());
				options = NO_OPTIMIZATIONS;
			}
		}
		if(!fullyStatic) {
			return;
		}
		//Otherwise, everything is static, or an IVariable and we can proceed.
		//Note since we could still have IVariables, we have to handle those
		//specially from here forward
		if(func.preResolveVariables() && hasIVars) {
			//Well, this function isn't equipped to deal with IVariables.
			return;
		}
		//It could have optimized by changing the name, in that case, we
		//don't want to run this now
		if(tree.getData().val().equals(oldFunctionName)
				&& (options.contains(OptimizationOption.OPTIMIZE_CONSTANT) || options.contains(OptimizationOption.CONSTANT_OFFLINE))) {
			Mixed[] constructs = new Mixed[tree.getChildren().size()];
			for(int i = 0; i < tree.getChildren().size(); i++) {
				constructs[i] = tree.getChildAt(i).getData();
			}
			try {
				try {
					Mixed result;
					if(options.contains(OptimizationOption.CONSTANT_OFFLINE)) {
						if(!isValidNumArgs(func, tree.getChildren().size())) {
							compilerErrors.add(new ConfigCompileException("Incorrect number of arguments passed to "
									+ tree.getData().val(), tree.getData().getTarget()));
							result = null;
						} else {
							result = func.exec(tree.getData().getTarget(), env, constructs);
						}
					} else if(isValidNumArgs(func, constructs.length)) {
						result = ((Optimizable) func).optimize(tree.getData().getTarget(), env, constructs);
					} else {
						result = null;
					}

					//If the result is null, it was just a check, it can't optimize further.
					if(result != null) {
						Construct.SetWasIdentifierHelper(tree.getData(), result, false);
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

	private static boolean eliminateDeadCode(ParseTree tree, Environment env, Set<Class<? extends Environment.EnvironmentImpl>> envs) {
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
		if(tree.getData() instanceof CFunction && ((CFunction) tree.getData()).hasFunction()) {
			Function f = ((CFunction) tree.getData()).getCachedFunction(envs);
			if(f == null) {
				return false;
			}
			List<ParseTree> children = tree.getChildren();
			List<Boolean> branches;
			if(f instanceof BranchStatement branchStatement) {
				branches = branchStatement.isBranch(children);
				if(branches.size() != children.size()) {
					if(!isValidNumArgs(f, children.size())) {
						// Incorrect number of arguments passed to the function, not a branch implementation error.
						return false;
					}
					throw new Error(f.getName() + " does not properly implement isBranch. It does not return a value"
							+ " with the same count as the actual children. Children: " + children.size() + ";"
							+ " Branches: " + branches.size() + ";"
							+ " Code target causing this: "
							+ tree.getTarget());
				}
			} else {
				branches = children.stream().map(child -> false).toList();
			}
			boolean doDeletion = false;
			for(int m = 0; m < children.size(); m++) {
				boolean isBranch = branches.get(m);
				if(doDeletion) {
					if(isBranch) {
						doDeletion = false;
					} else {
						env.getEnv(CompilerEnvironment.class)
								.addCompilerWarning(tree.getFileOptions(), new CompilerWarning("Unreachable code. Consider"
									+ " removing this code.", children.get(m).getTarget(),
										FileOptions.SuppressWarning.UnreachableCode));
						children.remove(m);
						m--;
						continue;
					}
				}
				ParseTree child = children.get(m);
				if(child.getData() instanceof CFunction cFunction) {
					if(!cFunction.hasFunction()) {
						continue;
					}
					Function c = cFunction.getCachedFunction(envs);
					if(c == null) {
						continue;
					}
					Set<OptimizationOption> options = NO_OPTIMIZATIONS;
					if(c instanceof Optimizable optimizable) {
						options = optimizable.optimizationOptions();
					}
					doDeletion = options.contains(OptimizationOption.TERMINAL);
					boolean subDoDelete = eliminateDeadCode(child, env, envs);
					if(subDoDelete) {
						doDeletion = true;
					}
				}
				if(isBranch) {
					doDeletion = false;
				}
			}
			return doDeletion;
		}
		return false;
	}

	/**
	 * Runs keyword processing on the tree. Note that this is run before optimization, and is a depth first process.
	 *
	 * @param tree
	 */
	@SuppressWarnings("ThrowableResultIgnored")
	private static void processKeywords(ParseTree tree, Environment env, Set<ConfigCompileException> compileErrors) {
		List<ParseTree> children = tree.getChildren();
		boolean processSubChildren = true;
		for(int i = 0; i < children.size(); i++) {
			ParseTree node = children.get(i);
			if(processSubChildren) {
				processKeywords(node, env, compileErrors);
			}
			// Keywords can be standalone, or a function can double as a keyword. So we have to check for both
			// conditions.
			Mixed m = node.getData();
			if(!(m instanceof CKeyword
					|| (m instanceof CLabel && ((CLabel) m).cVal() instanceof CKeyword)
					|| (m instanceof CFunction))) {
				continue;
			}
			Keyword keyword = KeywordList.getKeywordByName(m.val());
			if(keyword == null) {
				continue;
			}
			// This looks a bit confusing, but is fairly straightforward. We want to process the child elements of all
			// remaining nodes, so that subchildren that need processing will be finished, and our current tree level will
			// be able to independently process it. We don't want to process THIS level though, just the children of this level.
			if(processSubChildren) {
				for(int j = i + 1; j < children.size(); j++) {
					processKeywords(children.get(j), env, compileErrors);
				}
				processSubChildren = false;
			}
			// Now that all the children of the rest of the chain are processed, we can do the processing of this level.
			try {
				i = keyword.process(children, i);
			} catch (ConfigCompileException ex) {
				// Keyword processing failed, but the keyword might be part of some other syntax where it's valid.
				// Store the compile error so that it can be thrown after all if the keyword won't be handled.
				env.getEnv(CompilerEnvironment.class).potentialKeywordCompileErrors.put(m.getTarget(), ex);
			}
		}

	}

	@SuppressWarnings("ThrowableResultIgnored")
	private static void processLateKeywords(ParseTree tree, Environment env, Set<ConfigCompileException> compileErrors) {
		List<ParseTree> children = tree.getChildren();
		for(int i = 0; i < children.size(); i++) {
			ParseTree node = children.get(i);
			processLateKeywords(node, env, compileErrors);
			// Keywords can be standalone, or a function can double as a keyword. So we have to check for both
			// conditions.
			Mixed m = node.getData();
			if(!(m instanceof CKeyword)) {
				continue;
			}
			LateBindingKeyword keyword = KeywordList.getLateBindingKeywordByName(m.val());
			if(keyword == null) {
				continue;
			}

			// Now that all the children of the rest of the chain are processed, we can do the processing of this level.
			try {
				ParseTree lhs = null;
				if(i != 0) {
					lhs = children.get(i - 1);
				}
				ParseTree rhs = null;
				if(i + 1 < children.size()) {
					rhs = children.get(i + 1);
				}
				switch(keyword.getAssociativity()) {
					case LEFT ->  {
						if(lhs == null && !keyword.allowEmptyValue()) {
							throw new ConfigCompileException("Unexpected keyword " + keyword.getName(), node.getTarget());
						}
						ParseTree replacement = keyword.processLeftAssociative(node.getTarget(), node.getFileOptions(), lhs);
						children.set(i, replacement);
						if(lhs != null) {
							children.remove(i - 1);
							i--;
						}
					}
					case RIGHT ->  {
						if(rhs == null && !keyword.allowEmptyValue()) {
							throw new ConfigCompileException("Unexpected keyword " + keyword.getName(), node.getTarget());
						}
						ParseTree replacement = keyword.processRightAssociative(node.getTarget(), node.getFileOptions(), rhs);
						children.set(i, replacement);
						if(rhs != null) {
							children.remove(i + 1);
						}
					}
					case BOTH ->  {
						if(!keyword.allowEmptyValue() && (i == 0 || i + 1 >= children.size())) {
							throw new ConfigCompileException("Unexpected keyword " + keyword.getName(), node.getTarget());
						}
						ParseTree replacement = keyword.processBothAssociative(node.getTarget(), node.getFileOptions(),
								lhs, rhs);
						children.set(i, replacement);
						if(rhs != null) {
							children.remove(i + 1);
							i--;
						}
						if(lhs != null) {
							children.remove(i - 1);
						}
					}
				}
			} catch (ConfigCompileException ex) {
				// Keyword processing failed, but the keyword might be part of some other syntax where it's valid.
				// Store the compile error so that it can be thrown after all if the keyword won't be handled.
				env.getEnv(CompilerEnvironment.class).potentialKeywordCompileErrors.put(m.getTarget(), ex);
			}
		}
	}

	@SuppressWarnings("ThrowableResultIgnored")
	private static void processEarlyKeywords(TokenStream stream, Environment env, Set<ConfigCompileException> compileErrors) {
		Token token;
		for(ListIterator<Token> it = stream.listIterator(); it.hasNext(); ) {
			int ind = it.nextIndex();
			token = it.next();

			// Some keywords look like function names, we need those too.
			if(token.type != TType.KEYWORD && token.type != TType.FUNC_NAME) {
				continue;
			}

			EarlyBindingKeyword keyword = KeywordList.getEarlyBindingKeywordByName(token.val());
			if(keyword == null) {
				continue;
			}

			// Now that all the children of the rest of the chain are processed, we can do the processing of this level.
			int newInd;
			try {
				newInd = keyword.process(stream, env, ind);
			} catch (ConfigCompileException ex) {
				compileErrors.add(ex);
				continue;
			}

			// Create iterator at new index. Required in all cases to prevent ConcurrentModificationExceptions.
			if(newInd >= stream.size()) {
				break;
			}
			it = stream.listIterator(newInd + 1); // +1 to let the next .next() call select that index.
		}
	}

	/**
	 * Generates compile errors for unhandled compiler constructs that should not be present in the final AST,
	 * such as {@link CKeyword}.
	 * This is purely validation and should be called on the final AST.
	 * @param tree - The final abstract syntax tree.
	 * @param env - The environment.
	 * @param compilerErrors - A set to put compile errors in.
	 * @deprecated This is handled in {@link StaticAnalysis} and will no longer be useful when static analysis is
	 * permanently enabled.
	 */
	@Deprecated
	private static void checkUnhandledCompilerConstructs(ParseTree tree,
			Environment env, Set<ConfigCompileException> compilerErrors) {
		for(ParseTree node : tree.getAllNodes()) {
			Mixed m = node.getData();

			// Create compile error for unexpected keywords.
			if(m instanceof CKeyword) {
				ConfigCompileException ex =
						env.getEnv(CompilerEnvironment.class).potentialKeywordCompileErrors.get(m.getTarget());
				compilerErrors.add(ex != null ? ex
						: new ConfigCompileException("Unexpected keyword: " + m.val(), m.getTarget()));
			}
		}
	}

	private static boolean isValidNumArgs(Function function, int numArgs) {
		for(int allowedNumArgs : function.numArgs()) {
			if(allowedNumArgs == Integer.MAX_VALUE || allowedNumArgs == numArgs) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Shorthand for lexing, compiling, and executing a script.
	 *
	 * @param script The textual script to execute
	 * @param file The file it was located in
	 * @param inPureMScript If it is pure MScript, or aliases
	 * @param env The execution environment
	 * @param envs The environments that will be available at runtime
	 * @param done The MethodScriptComplete callback (may be null)
	 * @param s A script object (may be null)
	 * @param vars Any $vars (may be null)
	 * @return
	 * @throws ConfigCompileException
	 * @throws com.laytonsmith.core.exceptions.ConfigCompileGroupException This indicates that a group of compile errors
	 * occurred.
	 */
	public static Mixed execute(String script, File file, boolean inPureMScript, Environment env,
			Set<Class<? extends Environment.EnvironmentImpl>> envs,
			MethodScriptComplete done, Script s, List<Variable> vars)
			throws ConfigCompileException, ConfigCompileGroupException {
		return execute(compile(lex(script, env, file, inPureMScript), env, envs), env, done, s, vars);
	}

	/**
	 * Executes a pre-compiled MethodScript, given the specified Script environment. Both done and script may be null,
	 * and if so, reasonable defaults will be provided. The value sent to done will also be returned, as a Mixed, so
	 * this one function may be used synchronously also.
	 *
	 * @param root
	 * @param env
	 * @param done
	 * @param script
	 * @return
	 */
	public static Mixed execute(ParseTree root, Environment env, MethodScriptComplete done, Script script) {
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
	public static Mixed execute(ParseTree root, Environment env, MethodScriptComplete done, Script script, List<Variable> vars) {
		if(root == null) {
			return CVoid.VOID;
		}
		if(script == null) {
			script = new Script(null, null, env.getEnv(GlobalEnv.class).GetLabel(), env.getEnvClasses(),
					root.getFileOptions(), null);
		}
		if(vars != null) {
			Map<String, Variable> varMap = new HashMap<>();
			for(Variable v : vars) {
				varMap.put(v.getVariableName(), v);
			}
			for(Mixed tempNode : root.getAllData()) {
				if(tempNode instanceof Variable variable) {
					Variable vv = varMap.get(variable.getVariableName());
					if(vv != null) {
						variable.setVal(vv.getDefault());
					} else {
						//The variable is unset. I'm not quite sure what cases would cause this
						variable.setVal("");
					}
				}
			}
		}
		StringBuilder b = new StringBuilder();
		Mixed returnable = null;
		for(ParseTree gg : root.getChildren()) {
			Mixed retc = script.eval(gg, env);
			if(root.numberOfChildren() == 1) {
				returnable = retc;
				if(done == null) {
					// string builder is not needed, so return immediately
					return returnable;
				}
			}
			String ret = retc.val();
			if(!ret.trim().isEmpty()) {
				b.append(ret).append(" ");
			}
		}
		if(done != null) {
			done.done(b.toString().trim());
		}
		if(returnable != null) {
			return returnable;
		}
		return Static.resolveConstruct(b.toString().trim(), Target.UNKNOWN);
	}

	private static final List<Character> PDF_STACK = Arrays.asList(
			'\u202A', // LRE
			'\u202B', // RLE
			'\u202D', // LRO
			'\u202E'  // RLO
	);
	private static final List<Character> PDI_STACK = Arrays.asList(
			'\u2066', // LRI
			'\u2067'  // RLI
	);
	private static final char PDF = '\u202C';
	private static final char PDI = '\u2069';

	/**
	 * A bidirectional control character (bidi character) is a unicode character which is used in legitimate
	 * circumstances to force right to left languages such as Arabic and Hebrew to format correctly, when used
	 * within the same encoding stream. There are a number of these characters, but in particular, there are two
	 * which can be used to hide the functionality, because they cause the rendering of the text to appear one way,
	 * but the compiler will read the code differently. In particular, if we insert "right to left isolation override"
	 * in the middle of a comment, it will cause the rest of the line to be reversed.
	 * <p>
	 * Say we have the following code, which appears like this in a text editor, where RLI is the text direction override.
	 * <pre>
	 *     /* This is a comment RLI &#42;/ if(!@admin) {exit()}
	 *     codeOnlyForAdmins();
	 * </pre>
	 *
	 * The editor will render that code as shown above, but the compiler will view the code as if it were
	 * written as such:
	 * <pre>
	 *     /* This is a comment if(!@admin) {exit()} &#42;/
	 *     codeOnlyForAdmins();
	 * </pre>
	 *
	 * Thus bypassing the apparently correct check for admin access. The key here is that the RLI indicator is not
	 * visible in the editor, and so cannot be checked for through simple code review without compiler or editor
	 * support. Some editors may add this in the future, but here, we simply disallow the attack at the compiler
	 * level, making such code uncompilable whether or not it is visible in the editor.
	 * <p>
	 * It's also worth noting that the end of a line implicitely terminates unbalanced flow modifiers.
	 * <p>
	 * The solution then is to disallow unterminated flow modifiers from being used in comments and strings, where
	 * the effects can flow across string or comment end markers. This allows for right-to-left languages to be used
	 * within strings and comments anyways, but prevents them from being used maliciously. Thus, this function should
	 * be called against the full string of the completed token.
	 * <p>
	 * For full details, see
	 * <a href="https://trojansource.codes/trojan-source.pdf">https://trojansource.codes/trojan-source.pdf</a>
	 * @param s The string to check
	 * @param t The code target of the token
	 * @throws ConfigCompileException If an unexpected sequence is detected
	 */
	private static void validateTerminatedBidiSequence(String s, Target t) throws ConfigCompileException {
		int pdfStack = 0;
		int pdiStack = 0;
		for(Character c : s.toCharArray()) {
			if(PDF_STACK.contains(c)) {
				pdfStack++;
			}
			if(c == PDF) {
				pdfStack--;
			}
			if(PDI_STACK.contains(c)) {
				pdiStack++;
			}
			if(c == PDI) {
				pdiStack--;
			}
		}
		if(pdfStack != 0 || pdiStack != 0) {
			throw new ConfigCompileException("Incorrectly formatted unicode sequence", t);
		}
	}
}
