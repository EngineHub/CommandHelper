package com.laytonsmith.core.compiler;

import com.laytonsmith.PureUtilities.Common.StreamUtils;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The alias compiler takes alias definitions and compiles them down into alias definitions.
 */
public class AliasCompiler {

	public static void main(String [] args) throws Exception {
		List<AliasToken> list = new AliasLexer("/c /* = */ = /** hi */ code", null).parse();
		StreamUtils.GetSystemOut().println(list);
	}

	public static List<AliasToken> lex(String script, File file) throws ConfigCompileException{
		return new AliasLexer(script, file).parse();
	}

	private static class AliasLexer {
		private final String script;
		private final File file;
		private List<AliasToken> tokens;

		private StringBuilder buf = new StringBuilder();
		private Target t;

		private boolean pastLabel;
		private boolean pastDefinitionStart;
		private boolean inVariable;
		private boolean inQuote;
		private boolean inSingleString;
		private boolean inDoubleString;
		private boolean inOptionalVariable;
		private boolean inAliasCode;
		private boolean pastMultiline;
		private boolean inMultiline;
		private boolean isNewStyleMultiline;
		private boolean inComment;
		private boolean inLineComment;
		private boolean inMultilineComment;
		private boolean inDocBlockComment;

		public AliasLexer(String script, File file){
			if((int)script.charAt(0) == 65279){
				// Remove the UTF-8 Byte Order Mark, if present.
				script = script.substring(1);
			}
			script = script.replaceAll("\r\n", "\n");
			script = script + "\n";
			this.script = script;
			this.file = file;
		}

		public List<AliasToken> parse() throws ConfigCompileException{
			if(tokens == null){
				doParse();
			}
			return tokens;
		}

		private void doParse() throws ConfigCompileException {
			tokens = new ArrayList<>();
			int line = 1;
			int col = 0;
			Target singleQuoteStart = Target.UNKNOWN;
			Target doubleQuoteStart = Target.UNKNOWN;
			Target multilineCommentStart = Target.UNKNOWN;
			Target optionalVariableStart = Target.UNKNOWN;
			for(int i = 0; i < script.length(); i++){
				// Need up to a 3 lookahead
				col++;
				Character c = script.charAt(i);
				Character c2 = null;
				Character c3 = null;
				Character c4 = null;
				if (i < script.length() - 1) {
					c2 = script.charAt(i + 1);
				}
				if (i < script.length() - 2) {
					c3 = script.charAt(i + 2);
				}
				if (i < script.length() - 3) {
					c4 = script.charAt(i + 3);
				}
				t = new Target(line, file, col);

				if(!pastDefinitionStart && !inComment && !inAliasCode){
					if(c == '\n' || Character.isWhitespace(c)){
						continue;
					}
					pastLabel = c == '/' || c == '\'' || c == '"';
					pastDefinitionStart = true;
				}
				
				/* Strings and comments override basically everything */
				if(inLineComment){
					if(c == '\n'){
						inLineComment = false;
						inComment = false;
					}
					if(!inAliasCode){
						continue;
					}
				}
				if(inMultilineComment){
					if(c == '*' && c2 == '/'){
						boolean wasDocBlock = inDocBlockComment;
						inMultilineComment = inDocBlockComment = inComment = false;
						if(!inAliasCode){
							if(wasDocBlock){
								buffer(AliasTokenType.DOCBLOCK, t);
							}
							i++;
							continue;
						}
					}
					if(inDocBlockComment || inAliasCode){
						buf.append(c);
						continue;
					} else {
						continue;
					}
				}
				if(inQuote){
					if(c == '\\'){
						if(c2 == '\'' || c2 == '"'){
							buf.append(c2);
							i++;
							continue;
						}
					}
				}
				boolean skipProcessing = false;
				if(!inComment){
					if(c == '\'' && inSingleString){
						inQuote = false;
						inSingleString = false;
						if(!inAliasCode){
							if(!inOptionalVariable){
								validateLit(buf.toString());
							}
							buffer(AliasTokenType.LIT, t);
							continue;
						}
						skipProcessing = true;
					}
					if(c == '"' && inDoubleString){
						inQuote = false;
						inDoubleString = false;
						if(!inAliasCode){
							if(!inOptionalVariable){
								validateLit(buf.toString());
							}
							buffer(AliasTokenType.LIT, t);
							continue;
						}
						skipProcessing = true;
					}
					if(!skipProcessing){
						if(c == '\'' && !inQuote){
							// Start single string
							inQuote = true;
							inSingleString = true;
							singleQuoteStart = t;
							if(!inAliasCode){
								continue;
							}
						}
						if(c == '"' && !inQuote){
							inQuote = true;
							inDoubleString = true;
							doubleQuoteStart = t;
							if(!inAliasCode){
								continue;
							}
						}
					}
					if(inQuote && !inAliasCode){
						buf.append(c);
						continue;
					}
				}
				if(c == '#' || (c == '/' && c2 == '/') && !inQuote){
					inComment = true;
					inLineComment = true;
					if(!inAliasCode){
						endToken();
						if(c == '/'){
							i++;
						}
						continue;
					}
				}
				if(c == '/' && c2 == '*' && !inQuote){
					int skip = 1;
					inComment = true;
					inMultilineComment = true;
					multilineCommentStart = t;
					if(c3 == '*'){
						skip++;
						inDocBlockComment = true;
					}
					if(!inAliasCode){
						endToken();
						i += skip;
						continue;
					}
				}
				/* End string/comment handling */



				if(c == ':'){
					if(pastLabel){
						throw new ConfigCompileException("Unexpected symbol \":\", this may only be used as the command label.", t);
					}
					pastLabel = true;
					buffer(AliasTokenType.LABEL, t);
					continue;
				}

				if(!pastLabel){
					if(Character.isWhitespace(c)){
						continue;
					}
					buf.append(c);
					continue;
				}


				if(inAliasCode){
					// Once in the alias code, we can be in either two states, looking for the multiline start, or any
					// other code. Whitespace is not taken care of here, so we need to handle it. (i.e. ignore it)
					if(!pastMultiline && Character.isWhitespace(c) && c != '\n'){
						continue;
					}
					if(pastMultiline){
						if(!inMultiline){
							if(c == '\n'){
								// This is the end of the alias code. We need to append the code then reset all the variables.
								buffer(AliasTokenType.CODE, t);
								buffer(AliasTokenType.ALIAS_END, t);
								resetStates();
								continue;
							}
						} else if(!inQuote && !inComment){
							// We're done with the multiline possibly.
							if(c == '<' && c2 == '<' && c3 == '<'){
								if(isNewStyleMultiline){
									throw new ConfigCompileException("Unexpected multiline end symbol type, expected \"?>\" but found \"<<<\"", t);
								}
								i += 2;
								buffer(AliasTokenType.CODE, t);
								buffer(AliasTokenType.ALIAS_END, t);
								resetStates();
								continue;
							}
							if(c == '?' && c2 == '>'){
								if(!isNewStyleMultiline){
									throw new ConfigCompileException("Unexpected multiline end symbol type, expected \"<<<\" but found \"?>\"", t);
								}
								i++;
								buffer(AliasTokenType.CODE, t);
								buffer(AliasTokenType.ALIAS_END, t);
								resetStates();
								continue;
							}
						}
						buf.append(c);
						continue;
					}
					if(!pastMultiline){
						if(c == '>' && c2 == '>' && c3 == '>'){
							isNewStyleMultiline = false;
							inMultiline = true;
							i += 2;
							pastMultiline = true;
							continue;
						} else if(c == '<' && c2 == '?' && c3 == 'm' && c4 == 's'){
							isNewStyleMultiline = true;
							inMultiline = true;
							i += 3;
							pastMultiline = true;
							continue;
						}
						if((c == '<' && c2 == '<' && c3 == '<')
								|| (c == '?' && c2 == '>')){
							throw new ConfigCompileException("Unexpected multiline end symbol", t);
						}
						buf.append(c);
						pastMultiline = true;
						continue;
					}
				}

				if(Character.isWhitespace(c) && !inQuote && c != '\n'){
					// Ignore this, but end the previous token
					endToken();
					continue;
				}

				if(c == '$' && buf.length() == 0){
					inVariable = true;
				}

				if(c == '['){
					if(inOptionalVariable){
						throw new ConfigCompileException("Unexpected left bracket", t);
					}
					endToken();
					inOptionalVariable = true;
					optionalVariableStart = t;
					buf.append('[');
					buffer(AliasTokenType.OPTIONAL_START, t);
					continue;
				}
				if(c == ']'){
					if(!inOptionalVariable){
						throw new ConfigCompileException("Unexpected right bracket", t);
					}
					endToken();
					buf.append(']');
					inOptionalVariable = false;
					buffer(AliasTokenType.OPTIONAL_END, t);
					continue;
				}
				if(c == '='){
					endToken();
					buf.append('=');
					if(inOptionalVariable){
						buffer(AliasTokenType.ASSIGN, t);
					} else {
						buffer(AliasTokenType.DEFINITION_END, t);
						inAliasCode = true;
						pastMultiline = false;
					}
					continue;
				}

				if(!inQuote && c == '\n'){
					endToken();
					buffer(AliasTokenType.ALIAS_END, t);
					continue;
				}

				buf.append(c);
			}
			if(inSingleString){
				throw new ConfigCompileException("Unended string", singleQuoteStart);
			}
			if(inDoubleString){
				throw new ConfigCompileException("Unended string", doubleQuoteStart);
			}
			if(inMultilineComment){
				throw new ConfigCompileException("Unended block comment", multilineCommentStart);
			}
			if(inOptionalVariable){
				throw new ConfigCompileException("Unended optional variable", optionalVariableStart);
			}

			endToken();
		}

		private void resetStates(){
			inAliasCode = inDoubleString = inMultiline =
					inOptionalVariable = inSingleString = inVariable =
					isNewStyleMultiline = pastDefinitionStart = pastLabel =
					pastMultiline = inQuote = inComment = inLineComment =
					inMultilineComment = inDocBlockComment = false;
		}

		private void validateLit(String lit) throws ConfigCompileException{
			if(lit.contains(" ") || lit.contains("\t") || lit.contains("\n")){
				throw new ConfigCompileException("Alias literals cannot contain whitespace", t);
			}
		}

		private final Pattern LIT_PATTERN = Pattern.compile("([\\[\\]=:\\$])");

		private void endToken() throws ConfigCompileException {
			if(buf.length() == 0){
				return;
			}
			if(inVariable){
				String var = buf.toString();
				if(!var.matches("^\\$[a-zA-Z0-9_]*$")){
					throw new ConfigCompileException("Invalid variable name", t);
				}
				buffer(AliasTokenType.VARIABLE, t);
				inVariable = false;
				return;
			}
			//Otherwise it's a lit
			Matcher m = LIT_PATTERN.matcher(buf.toString());
			if(m.find()){
				throw new ConfigCompileException("Unexpected symbol \"" + m.group(1) + "\" in alias definition (you may quote strings in the definition)", t);
			}
			buffer(AliasTokenType.LIT, t);
		}

		private void buffer(AliasTokenType type, Target t){
			// Re-work the column so that it starts at the beginning of the token, not the end
			Target myTarget = new Target(t.line(), t.file(), t.col() - buf.toString().length());
			tokens.add(new AliasToken(buf.toString(), type, myTarget));
			buf = new StringBuilder();
		}
	}

	public static class Alias {

	}

	public static class AliasToken {
		private String token;
		private AliasTokenType type;
		private Target t;

		public AliasToken(String token, AliasTokenType type, Target t){
			this.token = token;
			this.type = type;
			this.t = t;
		}

		@Override
		public String toString() {
			return "(" + type.name() + ") " + token;
		}
	}

	public static enum AliasTokenType {
		UNKNOWN,
		VARIABLE,
		OPTIONAL_START,
		OPTIONAL_END,
		ASSIGN,
		LABEL,
		LIT,
		DEFINITION_END,
		ALIAS_END,
		CODE,
		DOCBLOCK
	}
}
