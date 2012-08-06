package com.laytonsmith.core;

import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.constructs.Token;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author lsmith
 */
public class NewMethodScriptCompiler {

	public static List<Token> lex(String script, File file, boolean startInPureMscript) throws ConfigCompileException {
		script = script.replaceAll("\r\n", "\n");
		script = script + "\n";

		LexerObject lo = new LexerObject(script, file, startInPureMscript);


		return lo.lex();
	}

	private static class LexerObject {

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

		private LexerObject(String config, File file, boolean startInPureMscript) {
			this.config = config;
			this.file = file;
			state_in_pure_mscript = true;
			clearBuffer();
		}

		private void buffer(Object s) {
			buffer.append(s);
		}

		private void processBuffer() {
			if (buffer.length() != 0) {
				token_list.add(identifyToken(clearBuffer()));
			}
		}

		private String clearBuffer() {
			String buf = "";
			if(buffer != null){
				buf = buffer.toString();
			}
			buffer = new StringBuilder(32);
			return buf;
		}

		private Token identifyToken(String item) {
			//TODO:
			return null;
		}
		
		private void append(String value, Token.TType type){
			token_list.add(new Token(type, value, target));
		}

		public List<Token> lex() throws ConfigCompileException {
			if (token_list != null) {
				return new ArrayList<Token>(token_list);
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
				//Comments are only applicable if we are not inside a string
				if (!state_in_double_quote && !state_in_single_quote) {
					//If we aren't already in a comment, we might be starting one here
					if (!state_in_block_comment && !state_in_line_comment) {
						if (c == '/' && c2 == '*') {
							//Start of block comment
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
							//Start of line comment
							state_in_line_comment = true;
							continue;
						}
					} else if (state_in_block_comment) {
						//We might be ending the block comment
						if (c == '*' && c2 == '/') {
							state_in_block_comment = false;
							i++;
							if (state_in_smart_block_comment) {
								//We need to process the block comment here
								//TODO:
								clearBuffer();
							}
						}
					} else if (state_in_line_comment) {
						if (c == '\n') {
							state_in_line_comment = false;
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
						//Start of smart string
						state_in_double_quote = true;
						start_double_quote = line_num;
						continue;
					}
				}
				if (!state_in_single_quote) {
					if (c == '\'') {
						//Start of string
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
								if(state_in_double_quote){
									//It's an error if we're in double quotes to escape a single quote
									error("Invalid escape found. It is an error to escape single quotes inside a double quote.");
								} else {
									buffer("'");
									i++;
								}
								break;
							case '"':
								if(state_in_single_quote){
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
				if(state_in_double_quote){
					if(c == '"'){
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
				
				if(state_in_single_quote){
					if(c == '\''){
						state_in_single_quote = false;
						append(clearBuffer(), Token.TType.STRING);
						continue;
					} else {
						buffer(c);
						continue;
					}
				}
				
				//Now deal with multiline states
				if(c == '>' && c2 == '>' && c3 == '>'){
					//Multiline start
					if(state_in_multiline){
						error("Found multiline start symbol while already in multiline!");
					}
					state_in_multiline = true;
					start_multiline = line_num;
					i+=2;
					continue;
				}
				
				if(c == '<' && c2 == '<' && c3 == '<'){
					if(!state_in_multiline){
						error("Found multiline end symbol while not in multiline!");
					}
					state_in_multiline = false;
					i+=2;
					continue;
				}
				
				//To simplify token processing later, we will go ahead and do special handling if we're
				//not in pure mscript. Therefore, = will
				//get special handling up here, as well as square brackets
				if(!state_in_pure_mscript){
					if(c == '['){
						if(state_in_opt_var){
							error("Found [ symbol, but a previous optional variable had already been started");
						}
						state_in_opt_var = true;
						append("[", Token.TType.LSQUARE_BRACKET);
						continue;
					}
					if(c == ']'){
						if(!state_in_opt_var){
							error("Found ] symbol, but no optional variable had been started");
						}
						
						state_in_opt_var = false;
						append("]", Token.TType.RSQUARE_BRACKET);
						continue;
					}
					if(state_in_opt_var){
						if(c == '='){
							//This is an optional variable declaration
							append("=", Token.TType.OPT_VAR_ASSIGN);
							continue;
						}
					}
					if(c == '='){
						append("=", Token.TType.ALIAS_END);
						continue;
					}

					//At this point, all other tokens are to be taken literally
					buffer(c);
					continue;
				}
				
				//TODO: Pure mscript mode
				

			}
			return new ArrayList<Token>(token_list);
		}

		private void error(String message) throws ConfigCompileException {
			throw new ConfigCompileException(message, target);
		}
	}
	
	public static void main(String [] args) throws ConfigCompileException{
		System.out.println(
		lex("\'A \\\'string\\\'\' >>> 'string' <<<", null, true));
	}
}
