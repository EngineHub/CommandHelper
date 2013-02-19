
package com.laytonsmith.core.compiler;

import com.laytonsmith.core.CHLog;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.constructs.Token;
import com.laytonsmith.core.constructs.Token.TType;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 *
 * @author Layton
 */
class LexerObject {
	StringBuilder buffer;
	//We have 5 states we need to monitor, multiline, line/block comment, single/double quote.
	//Additionally, we want to have counters for the line number for the applicable ones that
	//can span multiple lines.
	boolean state_in_single_quote = false;
	boolean state_in_double_quote = false;
	boolean state_in_multiline = false;
	boolean state_in_line_comment = false;
	boolean state_in_block_comment = false;
	boolean state_in_smart_block_comment = false;
	boolean state_in_pure_mscript = false;
	boolean state_in_opt_var = false;
	boolean state_in_var = false;
	boolean state_in_ivar = false;
	boolean state_in_fileopts = false;
	StringBuffer fileopts = new StringBuffer();
	Target fileoptsTarget = null;
	int start_single_quote = 1;
	int start_double_quote = 1;
	int start_multiline = 1;
	int start_block_comment = 1;
	int brace_stack = 0;
	int square_brace_stack = 0;
	List<Token> token_list = null;
	String config;
	File file;
	//Code target information
	int line_num = 1;
	int column = 1;
	int lastColumn = 0;
	Target target = Target.UNKNOWN;
	final boolean usingNonPure;
	private static SortedSet<TokenMap> tokenMap = new TreeSet<TokenMap>();

	private static class TokenMap implements Comparable<TokenMap> {

		String token;
		Token.TType type;

		public TokenMap(String token, Token.TType type) {
			this.token = token;
			this.type = type;
		}

		public int compareTo(TokenMap o) {
			if (this.token.length() == o.token.length()) {
				//Zero case
				return this.token.compareTo(o.token);
			} else if (this.token.length() < o.token.length()) {
				//This token is shorter than the other
				return 1;
			} else {
				//This token is larger than the other
				return -1;
			}
		}

		@Override
		public String toString() {
			return token;
		}
	}

	/**
	 * Adds simple tokens to the auto-identifier list. Order does not
	 * matter, it is sorted appropriately for you.
	 */
	private static void setupTokens() {
		//Even though we handle multiline specially, to force the lookahead to check
		//far enough in advance, we need to add them to this list
		//			tokenMap.add(new TokenMap(">>>", Token.TType.MULTILINE_START));
		//			tokenMap.add(new TokenMap("<<<", Token.TType.MULTILINE_END));
		tokenMap.add(new TokenMap("<=", Token.TType.LTE));
		tokenMap.add(new TokenMap("<", Token.TType.LT));
		tokenMap.add(new TokenMap(">", Token.TType.GT));
		tokenMap.add(new TokenMap(">=", Token.TType.GTE));
		tokenMap.add(new TokenMap("==", Token.TType.EQUALS));
		tokenMap.add(new TokenMap("===", Token.TType.STRICT_EQUALS));
		tokenMap.add(new TokenMap("!=", Token.TType.NOT_EQUALS));
		tokenMap.add(new TokenMap("!==", Token.TType.STRICT_NOT_EQUALS));
		tokenMap.add(new TokenMap("&&", Token.TType.LOGICAL_AND));
		tokenMap.add(new TokenMap("||", Token.TType.LOGICAL_OR));
		tokenMap.add(new TokenMap("!", Token.TType.LOGICAL_NOT));
		tokenMap.add(new TokenMap("+", Token.TType.PLUS));
		tokenMap.add(new TokenMap("-", Token.TType.MINUS));
		tokenMap.add(new TokenMap("*", Token.TType.MULTIPLICATION));
		tokenMap.add(new TokenMap("/", Token.TType.DIVISION));
		tokenMap.add(new TokenMap("++", Token.TType.INCREMENT));
		tokenMap.add(new TokenMap("--", Token.TType.DECREMENT));
		tokenMap.add(new TokenMap("%", Token.TType.MODULO));
		tokenMap.add(new TokenMap("**", Token.TType.EXPONENTIAL));
		tokenMap.add(new TokenMap(".", Token.TType.CONCAT));
		tokenMap.add(new TokenMap("->", Token.TType.DEREFERENCE));
		tokenMap.add(new TokenMap("::", Token.TType.DEREFERENCE));
		tokenMap.add(new TokenMap("${", Token.TType.CONST_START));
		tokenMap.add(new TokenMap("{", Token.TType.LCURLY_BRACKET));
		tokenMap.add(new TokenMap("}", Token.TType.RCURLY_BRACKET));
		tokenMap.add(new TokenMap("[", Token.TType.LSQUARE_BRACKET));
		tokenMap.add(new TokenMap("]", Token.TType.RSQUARE_BRACKET));
		tokenMap.add(new TokenMap("..", Token.TType.SLICE));
		tokenMap.add(new TokenMap("=", Token.TType.ASSIGNMENT));
		tokenMap.add(new TokenMap(":", Token.TType.LABEL));
		tokenMap.add(new TokenMap(",", Token.TType.COMMA));
		tokenMap.add(new TokenMap("(", Token.TType.FUNC_START));
		tokenMap.add(new TokenMap(")", Token.TType.FUNC_END));
		tokenMap.add(new TokenMap("+=", TType.PLUS_ASSIGNMENT));
		tokenMap.add(new TokenMap("-=", TType.MINUS_ASSIGNMENT));
		tokenMap.add(new TokenMap("*=", TType.MULTIPLICATION_ASSIGNMENT));
		tokenMap.add(new TokenMap("/=", TType.DIVISION_ASSIGNMENT));
		tokenMap.add(new TokenMap(".=", TType.CONCAT_ASSIGNMENT));
	}
	static {
		setupTokens();
	}

	LexerObject(String config, File file, boolean startInPureMscript) {
		this.config = config.replaceAll("\r\n", "\n") + "\n";
		this.file = file;
		state_in_pure_mscript = startInPureMscript;
		usingNonPure = !startInPureMscript;
		clearBuffer();
	}

	private void buffer(Object s) {
		buffer.append(s);
	}

	private void parseBuffer() {
		String last = clearBuffer().trim();
		if (!last.isEmpty()) {
			append(identifyToken(last));
		}
	}

	private String clearBuffer() {
		String buf = "";
		if (buffer != null) {
			buf = buffer.toString();
		}
		buffer = new StringBuilder(32);
		return buf;
	}

	private Token identifyToken(String item) {
		try {
			Long.parseLong(item.trim());
			return new Token(Token.TType.INTEGER, item, target);
		} catch (NumberFormatException e) {
			//Not an integer
			//Not an integer
		}
		try {
			Double.parseDouble(item);
			return new Token(Token.TType.DOUBLE, item, target);
		} catch (NumberFormatException e) {
			//Not a double
			//Not a double
		}
		if (item.trim().equals("$")) {
			return new Token(Token.TType.FINAL_VAR, "$", target);
		}
		if (item.matches("\\$[a-zA-Z0-9_]+")) {
			return new Token(Token.TType.VARIABLE, item.trim(), target);
		}
		if (item.matches("@[a-zA-Z0-9_]+")) {
			return new Token(Token.TType.IVARIABLE, item.trim(), target);
		}
		//else it's a bare string
		return new Token(Token.TType.BARE_STRING, item.trim(), target);
	}

	private void append(String value, Token.TType type) {
		append(new Token(type, value, target));
	}

	private void append(Token t) {
		token_list.add(t);
	}

	public TokenStream lex() throws ConfigCompileException {
		if (token_list != null) {
			return new TokenStream(new ArrayList<Token>(token_list), "", Target.UNKNOWN);
		} else {
			token_list = new ArrayList<Token>();
		}
		for (int i = 0; i < config.length(); i++) {
			Character c = config.charAt(i);
			Character c2 = null;
			Character c3 = null;
			if (i < config.length() - 1) {
				c2 = config.charAt(i + 1);
			}
			if (i < config.length() - 2) {
				c3 = config.charAt(i + 2);
			}
			column += i - lastColumn;
			lastColumn = i;
			if (c == '\n') {
				line_num++;
				column = 1;
			}
			target = new Target(line_num, file, column);
			//First, lets identify our stateful parameters
			//File Options
			if (state_in_fileopts) {
				if (c == '\\' && c2 == '>') {
					//literal >
					fileopts.append('>');
					i++;
					continue;
				} else if (c == '>') {
					state_in_fileopts = false;
					continue;
				} else {
					fileopts.append(c);
					continue;
				}
			}
			//Comments are only applicable if we are not inside a string
			if (!state_in_double_quote && !state_in_single_quote) {
				//If we aren't already in a comment, we might be starting one here
				if (state_in_block_comment) {
					//We might be ending the block comment
					if (c == '*' && c2 == '/') {
						state_in_block_comment = false;
						state_in_smart_block_comment = false;
						i++;
						clearBuffer();
					} else if (state_in_smart_block_comment) {
						//We need to process the block comment here
						//TODO:
					}
					continue;
				} else if (state_in_line_comment) {
					if (c == '\n') {
						state_in_line_comment = false;
						clearBuffer();
					}
					continue;
				} else if (!state_in_block_comment && !state_in_line_comment) {
					if (c == '/' && c2 == '*') {
						//Start of block comment
						parseBuffer();
						state_in_block_comment = true;
						start_block_comment = line_num;
						if (c3 == '*') {
							//It's also a smart block comment
							state_in_smart_block_comment = true;
							i++;
						} else {
							state_in_smart_block_comment = false;
						}
						i++;
						continue;
					}
					if (c == '#') {
						parseBuffer();
						//Start of line comment
						state_in_line_comment = true;
						continue;
					}
				}
			}
			//Ok, now if we are in a comment, we should just continue
			if (state_in_block_comment || state_in_line_comment) {
				if (state_in_smart_block_comment) {
					buffer(c);
				}
			}
			//Now we need to check for strings
			if (!state_in_double_quote) {
				if (c == '"') {
					parseBuffer();
					//Start of smart string
					state_in_double_quote = true;
					start_double_quote = line_num;
					continue;
				}
			}
			if (!state_in_single_quote) {
				if (c == '\'') {
					//Start of string
					parseBuffer();
					state_in_single_quote = true;
					start_single_quote = line_num;
					continue;
				}
			}
			if (state_in_double_quote || state_in_single_quote) {
				if (c == '\\') {
					//It's an escaped something or another
					switch (c2) {
						case 'n':
							buffer("\n");
							i++;
							break;
						case 't':
							buffer("\t");
							i++;
							break;
						case '0':
							buffer('\0');
							i++;
							break;
						case 'u':
							StringBuilder unicode = new StringBuilder();
							for (int m = 0; m < 4; m++) {
								try {
									unicode.append(config.charAt(i + 2 + m));
								} catch (IndexOutOfBoundsException e) {
									//If this fails, they didn't put enough characters in the stream
									error("Incomplete unicode escape");
								}
							}
							try {
								Integer.parseInt(unicode.toString(), 16);
							} catch (NumberFormatException e) {
								error("Unrecognized unicode escape sequence");
							}
							buffer(Character.toChars(Integer.parseInt(unicode.toString(), 16)));
							i += 4;
							break;
						case '\'':
							if (state_in_double_quote) {
								//It's an error if we're in double quotes to escape a single quote
								error("Invalid escape found. It is an error to escape single quotes inside a double quote.");
							} else {
								buffer("'");
								i++;
							}
							break;
						case '"':
							if (state_in_single_quote) {
								//It's an error if we're in single quotes to escape a double quote
								error("Invalid escape found. It is an error to escape double quotes inside a single quote.");
							} else {
								buffer('"');
								i++;
							}
							break;
						default:
							//It's invalid, so throw an exception
							error("The escape sequence \\" + c2 + " is not a recognized escape sequence");
							break;
					}
					continue;
				}
			}
			//Now deal with ending a quote
			if (state_in_double_quote) {
				if (c == '"') {
					state_in_double_quote = false;
					append(clearBuffer(), Token.TType.SMART_STRING);
					//This is currently an error, but won't be forever
					error("Double quotes are currently unsupported");
					continue;
				} else {
					buffer(c);
					continue;
				}
			}
			if (state_in_single_quote) {
				if (c == '\'') {
					state_in_single_quote = false;
					append(clearBuffer(), Token.TType.STRING);
					continue;
				} else {
					buffer(c);
					continue;
				}
			}
			//Now deal with multiline states
			if (c == '>' && c2 == '>' && c3 == '>') {
				//Multiline start
				if (state_in_multiline) {
					error("Found multiline start symbol while already in multiline!");
				}
				state_in_multiline = true;
				start_multiline = line_num;
				i += 2;
				continue;
			}
			if (c == '<' && c2 == '<' && c3 == '<') {
				if (!state_in_multiline) {
					error("Found multiline end symbol while not in multiline!");
				}
				state_in_multiline = false;
				i += 2;
				continue;
			}
			//Newlines don't count
			if (Character.isWhitespace(c) && c != '\n') {
				//We need to parse the buffer
				parseBuffer();
				continue;
			}
			if (c == '<' && c2 == '!') {
				if (!token_list.isEmpty()) {
					throw new ConfigCompileException("File options must come first in the file.", target);
				}
				state_in_fileopts = true;
				fileoptsTarget = target;
				i++;
				continue;
			}
			//To simplify token processing later, we will go ahead and do special handling if we're
			//not in pure mscript. Therefore, = will
			//get special handling up here, as well as square brackets
			if (!state_in_pure_mscript) {
				if (c == '[') {
					if (state_in_opt_var) {
						error("Found [ symbol, but a previous optional variable had already been started");
					}
					state_in_opt_var = true;
					parseBuffer();
					append("[", Token.TType.LSQUARE_BRACKET);
					continue;
				}
				if (c == ']') {
					if (!state_in_opt_var) {
						error("Found ] symbol, but no optional variable had been started");
					}
					state_in_opt_var = false;
					parseBuffer();
					append("]", Token.TType.RSQUARE_BRACKET);
					continue;
				}
				if (state_in_opt_var) {
					if (c == '=') {
						//This is an optional variable declaration
						parseBuffer();
						append("=", Token.TType.OPT_VAR_ASSIGN);
						continue;
					}
				}
				if (c == '=') {
					state_in_pure_mscript = true;
					parseBuffer();
					append("=", Token.TType.ALIAS_END);
					continue;
				}
				if (c == ':') {
					parseBuffer();
					append(":", Token.TType.LABEL);
					continue;
				}
				if (c == '\n') {
					parseBuffer();
					if (token_list.isEmpty() || token_list.get(token_list.size() - 1).type != TType.NEWLINE) {
						append("\n", TType.NEWLINE);
					}
					continue;
				}
				//At this point, all other tokens are to be taken literally
				buffer(c);
				continue;
			}
			//Newlines are handled differently if it's in multiline or not.
			//Remember, if we are in multiline mode (or pure mscript), newlines are simply removed, otherwise they are
			//kept (except duplicate ones)
			if (c == '\n') {
				if (state_in_multiline) {
					continue;
				} else {
					if (!token_list.isEmpty() && token_list.get(token_list.size() - 1).type != Token.TType.NEWLINE) {
						parseBuffer();
						if (usingNonPure) {
							if (token_list.get(token_list.size() - 1).type != TType.NEWLINE) {
								//Don't add duplicates
								append("\n", Token.TType.NEWLINE);
							}
							//This also signals the end of pure mscript
							state_in_pure_mscript = false;
							continue;
						} else if (state_in_pure_mscript) {
							continue;
						}
					} else {
						continue;
					}
				}
			}
			//Handle decimal place vs concat
			if (c == '.' && Character.isDigit(c2)) {
				//It'll get identified automatically in a bit
				buffer(c);
				continue;
			}
			//We need to handle /cmd vs division
			if (c == '/' && (c2 == '/' || Character.isLetter(c2))) {
				//It'll be registered as a bare string later
				buffer(c);
				continue;
			}
			//Now we are in pure mscript mode
			//Loop through our token
			int skip;
			if ((skip = identifySymbol(i)) != -1) {
				//Cool, it found one. Jump ahead.
				i += skip;
				continue;
			}
			buffer(c);
		}
		parseBuffer();
		int left = 0;
		int right = 0;
		for(Token t : token_list){
			if(t.val().equals("(")){
				left++;
			}
			if(t.val().equals(")")){
				right++;
			}
		}
		//Check for unclosed things
		if(state_in_multiline){
			throw new ConfigCompileException("Unclosed multiline construct (You have a >>> without a matching <<<). The last multiline construct"
					+ " was started on line " + start_multiline, target);
		}
		if(state_in_block_comment){
			throw new ConfigCompileException("Unclosed block comment (You have a /* without a matching */). The last block comment was started"
					+ " on line " + start_block_comment, target);
		}
		if(state_in_single_quote){
			throw new ConfigCompileException("Unclosed single quote (You have a single quote without a matching end single quote). The last single quote"
					+ " was started on line " + start_single_quote + " ('strings that don't escape conjunctions' (like that) are a common cause of this)", target);
		}
		if(state_in_double_quote){
			throw new ConfigCompileException("Unclosed double quote (You have a double quote without a matching end double quote). The last double quote"
					+ " was started on line " + start_double_quote, target);
		}
		TokenStream ts = new TokenStream(new ArrayList<Token>(token_list), fileopts.toString(), fileoptsTarget);
		//Check for lack of strict mode, and trigger the warning here
		if(!ts.getFileOptions().isStrict()){
			CHLog.GetLogger().CompilerWarning(CompilerWarning.StrictModeOff, "Strict mode is turned off in this file. Strict mode is HIGHLY recommended.", fileoptsTarget, ts.getFileOptions());
		}
		return ts;
	}

	/**
	 * If a symbol token is the next thing in the stream, it will be
	 * identified, pushed onto the token_list, and the number of characters
	 * to advance the stream is returned. If this method returns -1, no token
	 * was identified, and no changes will have been made.
	 * @param startAt
	 * @return
	 */
	private int identifySymbol(int startAt) {
		//We need as much of a lookahead as our largest token
		char[] lookahead = new char[tokenMap.first().token.length()];
		//Fill in our lookahead buffer
		for (int i = 0; i < lookahead.length; i++) {
			if (i + startAt < config.length() - 1) {
				lookahead[i] = config.charAt(i + startAt);
			} else {
				lookahead[i] = ' ';
			}
		}
		//Now walk through our token list, and if we find a match, use it.
		for (TokenMap tm : tokenMap) {
			boolean found = true;
			for (int i = 0; i < tm.token.length(); i++) {
				if (tm.token.charAt(i) != lookahead[i]) {
					found = false;
					break;
				}
			}
			if (found) {
				//Found it
				String last = clearBuffer();
				if (!last.isEmpty()) {
					append(identifyToken(last));
				}
				append(tm.token, tm.type);
				return tm.token.length() - 1;
			}
		}
		return -1;
	}

	private void error(String message) throws ConfigCompileException {
		throw new ConfigCompileException(message, target);
	}
    
}
