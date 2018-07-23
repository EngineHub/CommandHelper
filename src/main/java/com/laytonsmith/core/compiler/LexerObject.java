package com.laytonsmith.core.compiler;

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
 *
 */
class LexerObject {

	StringBuilder buffer;
	//We have 5 states we need to monitor, multiline, line/block comment, single/double quote.
	//Additionally, we want to have counters for the line number for the applicable ones that
	//can span multiple lines.
	boolean stateInSingleQuote = false;
	boolean stateInDoubleQuote = false;
	boolean stateInMultiline = false;
	boolean stateInLineComment = false;
	boolean stateInBlockComment = false;
	boolean stateInSmartBlockComment = false;
	boolean stateInPureMscript = false;
	boolean stateInOptVar = false;
	boolean stateInVar = false;
	boolean stateInIvar = false;
	boolean stateInFileopts = false;
	StringBuffer fileopts = new StringBuffer();
	int startSingleQuote = 1;
	int startDoubleQuote = 1;
	int startMultiline = 1;
	int startBlockComment = 1;
	int braceStack = 0;
	int squareBraceStack = 0;
	List<Token> tokenList = null;
	String config;
	File file;
	//Code target information
	int lineNum = 1;
	int column = 1;
	int lastColumn = 0;
	Target target = Target.UNKNOWN;
	final boolean usingNonPure;
	private static final SortedSet<TokenMap> TOKEN_MAP = new TreeSet<TokenMap>();

	private static class TokenMap implements Comparable<TokenMap> {

		String token;
		Token.TType type;

		public TokenMap(String token, Token.TType type) {
			this.token = token;
			this.type = type;
		}

		@Override
		public int compareTo(TokenMap o) {
			if(this.token.length() == o.token.length()) {
				//Zero case
				return this.token.compareTo(o.token);
			} else if(this.token.length() < o.token.length()) {
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
	 * Adds simple tokens to the auto-identifier list. Order does not matter, it is sorted appropriately for you.
	 */
	private static void setupTokens() {
		//Even though we handle multiline specially, to force the lookahead to check
		//far enough in advance, we need to add them to this list
		//			tokenMap.add(new TokenMap(">>>", Token.TType.MULTILINE_START));
		//			tokenMap.add(new TokenMap("<<<", Token.TType.MULTILINE_END));
		TOKEN_MAP.add(new TokenMap("<=", Token.TType.LTE));
		TOKEN_MAP.add(new TokenMap("<", Token.TType.LT));
		TOKEN_MAP.add(new TokenMap(">", Token.TType.GT));
		TOKEN_MAP.add(new TokenMap(">=", Token.TType.GTE));
		TOKEN_MAP.add(new TokenMap("==", Token.TType.EQUALS));
		TOKEN_MAP.add(new TokenMap("===", Token.TType.STRICT_EQUALS));
		TOKEN_MAP.add(new TokenMap("!=", Token.TType.NOT_EQUALS));
		TOKEN_MAP.add(new TokenMap("!==", Token.TType.STRICT_NOT_EQUALS));
		TOKEN_MAP.add(new TokenMap("&&", Token.TType.LOGICAL_AND));
		TOKEN_MAP.add(new TokenMap("||", Token.TType.LOGICAL_OR));
		TOKEN_MAP.add(new TokenMap("!", Token.TType.LOGICAL_NOT));
		TOKEN_MAP.add(new TokenMap("+", Token.TType.PLUS));
		TOKEN_MAP.add(new TokenMap("-", Token.TType.MINUS));
		TOKEN_MAP.add(new TokenMap("*", Token.TType.MULTIPLICATION));
		TOKEN_MAP.add(new TokenMap("/", Token.TType.DIVISION));
		TOKEN_MAP.add(new TokenMap("++", Token.TType.INCREMENT));
		TOKEN_MAP.add(new TokenMap("--", Token.TType.DECREMENT));
		TOKEN_MAP.add(new TokenMap("%", Token.TType.MODULO));
		TOKEN_MAP.add(new TokenMap("**", Token.TType.EXPONENTIAL));
		TOKEN_MAP.add(new TokenMap(".", Token.TType.CONCAT));
		TOKEN_MAP.add(new TokenMap("->", Token.TType.DEREFERENCE));
		TOKEN_MAP.add(new TokenMap("::", Token.TType.DEREFERENCE));
		TOKEN_MAP.add(new TokenMap("${", Token.TType.CONST_START));
		TOKEN_MAP.add(new TokenMap("{", Token.TType.LCURLY_BRACKET));
		TOKEN_MAP.add(new TokenMap("}", Token.TType.RCURLY_BRACKET));
		TOKEN_MAP.add(new TokenMap("[", Token.TType.LSQUARE_BRACKET));
		TOKEN_MAP.add(new TokenMap("]", Token.TType.RSQUARE_BRACKET));
		TOKEN_MAP.add(new TokenMap("..", Token.TType.SLICE));
		TOKEN_MAP.add(new TokenMap("=", Token.TType.ASSIGNMENT));
		TOKEN_MAP.add(new TokenMap(":", Token.TType.LABEL));
		TOKEN_MAP.add(new TokenMap(",", Token.TType.COMMA));
		TOKEN_MAP.add(new TokenMap("(", Token.TType.FUNC_START));
		TOKEN_MAP.add(new TokenMap(")", Token.TType.FUNC_END));
	}

	static {
		setupTokens();
	}

	LexerObject(String config, File file, boolean startInPureMscript) {
		this.config = config.replaceAll("\r\n", "\n") + "\n";
		this.file = file;
		stateInPureMscript = startInPureMscript;
		usingNonPure = !startInPureMscript;
		clearBuffer();
	}

	private void buffer(Object s) {
		buffer.append(s);
	}

	private void parseBuffer() {
		String last = clearBuffer().trim();
		if(!last.isEmpty()) {
			append(identifyToken(last));
		}
	}

	private String clearBuffer() {
		String buf = "";
		if(buffer != null) {
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
		if(item.trim().equals("$")) {
			return new Token(Token.TType.FINAL_VAR, "$", target);
		}
		if(item.matches("\\$[a-zA-Z0-9]+")) {
			return new Token(Token.TType.VARIABLE, item.trim(), target);
		}
		if(item.matches("@[a-zA-Z0-9]+")) {
			return new Token(Token.TType.IVARIABLE, item.trim(), target);
		}
		//else it's a bare string
		return new Token(Token.TType.BARE_STRING, item.trim(), target);
	}

	private void append(String value, Token.TType type) {
		append(new Token(type, value, target));
	}

	private void append(Token t) {
		tokenList.add(t);
	}

	public TokenStream lex() throws ConfigCompileException {
		if(tokenList != null) {
			return new TokenStream(new ArrayList<Token>(tokenList), "");
		} else {
			tokenList = new ArrayList<Token>();
		}
		for(int i = 0; i < config.length(); i++) {
			Character c = config.charAt(i);
			Character c2 = null;
			Character c3 = null;
			if(i < config.length() - 1) {
				c2 = config.charAt(i + 1);
			}
			if(i < config.length() - 2) {
				c3 = config.charAt(i + 2);
			}
			column += i - lastColumn;
			lastColumn = i;
			if(c == '\n') {
				lineNum++;
				column = 1;
			}
			target = new Target(lineNum, file, column);
			//First, lets identify our stateful parameters
			//File Options
			if(stateInFileopts) {
				if(c == '\\' && c2 == '>') {
					//literal >
					fileopts.append('>');
					i++;
					continue;
				} else if(c == '>') {
					stateInFileopts = false;
					continue;
				} else {
					fileopts.append(c);
					continue;
				}
			}
			//Comments are only applicable if we are not inside a string
			if(!stateInDoubleQuote && !stateInSingleQuote) {
				//If we aren't already in a comment, we might be starting one here
				if(!stateInBlockComment && !stateInLineComment) {
					if(c == '/' && c2 == '*') {
						//Start of block comment
						parseBuffer();
						stateInBlockComment = true;
						startBlockComment = lineNum;
						if(c3 == '*') {
							//It's also a smart block comment
							stateInSmartBlockComment = true;
							i++;
						} else {
							stateInSmartBlockComment = false;
						}
						i++;
						continue;
					}
					if(c == '#') {
						parseBuffer();
						//Start of line comment
						stateInLineComment = true;
						continue;
					}
				} else if(stateInBlockComment) {
					//We might be ending the block comment
					if(c == '*' && c2 == '/') {
						stateInBlockComment = false;
						i++;
//						if(state_in_smart_block_comment) {
//							//We need to process the block comment here
//							//TODO:
//							//We need to process the block comment here
//							//TODO:
//						}
						clearBuffer();
						continue;
					}
				} else if(stateInLineComment) {
					if(c == '\n') {
						stateInLineComment = false;
						clearBuffer();
						continue;
					}
				}
			}
			//Ok, now if we are in a comment, we should just continue
			if(stateInBlockComment || stateInLineComment) {
				if(stateInSmartBlockComment) {
					buffer(c);
				}
			}
			//Now we need to check for strings
			if(!stateInDoubleQuote) {
				if(c == '"') {
					parseBuffer();
					//Start of smart string
					stateInDoubleQuote = true;
					startDoubleQuote = lineNum;
					continue;
				}
			}
			if(!stateInSingleQuote) {
				if(c == '\'') {
					//Start of string
					parseBuffer();
					stateInSingleQuote = true;
					startSingleQuote = lineNum;
					continue;
				}
			}
			if(stateInDoubleQuote || stateInSingleQuote) {
				if(c == '\\') {
					//It's an escaped something or another
					switch(c2) {
						case 'n':
							buffer("\n");
							i++;
							break;
						case 't':
							buffer("\t");
							i++;
							break;
						case 'u':
							StringBuilder unicode = new StringBuilder();
							for(int m = 0; m < 4; m++) {
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
							if(stateInDoubleQuote) {
								//It's an error if we're in double quotes to escape a single quote
								error("Invalid escape found. It is an error to escape single quotes inside a double quote.");
							} else {
								buffer("'");
								i++;
							}
							break;
						case '"':
							if(stateInSingleQuote) {
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
			if(stateInDoubleQuote) {
				if(c == '"') {
					stateInDoubleQuote = false;
					append(clearBuffer(), Token.TType.SMART_STRING);
					//This is currently an error, but won't be forever
					error("Double quotes are currently unsupported");
					continue;
				} else {
					buffer(c);
					continue;
				}
			}
			if(stateInSingleQuote) {
				if(c == '\'') {
					stateInSingleQuote = false;
					append(clearBuffer(), Token.TType.STRING);
					continue;
				} else {
					buffer(c);
					continue;
				}
			}
			//Now deal with multiline states
			if(c == '>' && c2 == '>' && c3 == '>') {
				//Multiline start
				if(stateInMultiline) {
					error("Found multiline start symbol while already in multiline!");
				}
				stateInMultiline = true;
				startMultiline = lineNum;
				i += 2;
				continue;
			}
			if(c == '<' && c2 == '<' && c3 == '<') {
				if(!stateInMultiline) {
					error("Found multiline end symbol while not in multiline!");
				}
				stateInMultiline = false;
				i += 2;
				continue;
			}
			//Newlines don't count
			if(Character.isWhitespace(c) && c != '\n') {
				//We need to parse the buffer
				parseBuffer();
				continue;
			}
			if(c == '<' && c2 == '!') {
				if(!tokenList.isEmpty()) {
					throw new ConfigCompileException("File options must come first in the file.", target);
				}
				stateInFileopts = true;
				i++;
				continue;
			}
			//To simplify token processing later, we will go ahead and do special handling if we're
			//not in pure mscript. Therefore, = will
			//get special handling up here, as well as square brackets
			if(!stateInPureMscript) {
				if(c == '[') {
					if(stateInOptVar) {
						error("Found [ symbol, but a previous optional variable had already been started");
					}
					stateInOptVar = true;
					parseBuffer();
					append("[", Token.TType.LSQUARE_BRACKET);
					continue;
				}
				if(c == ']') {
					if(!stateInOptVar) {
						error("Found ] symbol, but no optional variable had been started");
					}
					stateInOptVar = false;
					parseBuffer();
					append("]", Token.TType.RSQUARE_BRACKET);
					continue;
				}
				if(stateInOptVar) {
					if(c == '=') {
						//This is an optional variable declaration
						parseBuffer();
						append("=", Token.TType.OPT_VAR_ASSIGN);
						continue;
					}
				}
				if(c == '=') {
					stateInPureMscript = true;
					parseBuffer();
					append("=", Token.TType.ALIAS_END);
					continue;
				}
				if(c == ':') {
					parseBuffer();
					append(":", Token.TType.LABEL);
					continue;
				}
				if(c == '\n') {
					parseBuffer();
					if(tokenList.isEmpty() || tokenList.get(tokenList.size() - 1).type != TType.NEWLINE) {
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
			if(c == '\n') {
				if(stateInMultiline) {
					continue;
				} else {
					if(!tokenList.isEmpty() && tokenList.get(tokenList.size() - 1).type != Token.TType.NEWLINE) {
						parseBuffer();
						if(usingNonPure) {
							if(tokenList.get(tokenList.size() - 1).type != TType.NEWLINE) {
								//Don't add duplicates
								append("\n", Token.TType.NEWLINE);
							}
							//This also signals the end of pure mscript
							stateInPureMscript = false;
							continue;
						} else if(stateInPureMscript) {
							continue;
						}
					} else {
						continue;
					}
				}
			}
			//Handle decimal place vs concat
			if(c == '.' && Character.isDigit(c2)) {
				//It'll get identified automatically in a bit
				buffer(c);
				continue;
			}
			//We need to handle /cmd vs division
			if(c == '/' && (c2 == '/' || Character.isLetter(c2))) {
				//It'll be registered as a bare string later
				buffer(c);
				continue;
			}
			//Now we are in pure mscript mode
			//Loop through our token
			int skip;
			if((skip = identifySymbol(i)) != -1) {
				//Cool, it found one. Jump ahead.
				i += skip;
				continue;
			}
			buffer(c);
		}
		parseBuffer();
		return new TokenStream(new ArrayList<Token>(tokenList), fileopts.toString());
	}

	/**
	 * If a symbol token is the next thing in the stream, it will be identified, pushed onto the token_list, and the
	 * number of characters to advance the stream is returned. If this method returns 0, no token was identified, and no
	 * changes will have been made.
	 *
	 * @param startAt
	 * @return
	 */
	private int identifySymbol(int startAt) {
		//We need as much of a lookahead as our largest token
		char[] lookahead = new char[TOKEN_MAP.first().token.length()];
		//Fill in our lookahead buffer
		for(int i = 0; i < lookahead.length; i++) {
			if(i + startAt < config.length() - 1) {
				lookahead[i] = config.charAt(i + startAt);
			} else {
				lookahead[i] = ' ';
			}
		}
		//Now walk through our token list, and if we find a match, use it.
		for(TokenMap tm : TOKEN_MAP) {
			boolean found = true;
			for(int i = 0; i < tm.token.length(); i++) {
				if(tm.token.charAt(i) != lookahead[i]) {
					found = false;
					break;
				}
			}
			if(found) {
				//Found it
				String last = clearBuffer();
				if(!last.isEmpty()) {
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
