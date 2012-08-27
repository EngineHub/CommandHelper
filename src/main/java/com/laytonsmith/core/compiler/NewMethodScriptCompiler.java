package com.laytonsmith.core.compiler;

import com.laytonsmith.PureUtilities.LogicUtils;
import com.laytonsmith.core.Env;
import com.laytonsmith.core.NewScript;
import com.laytonsmith.core.ParseTree;
import com.laytonsmith.core.constructs.CBareString;
import com.laytonsmith.core.constructs.CDouble;
import com.laytonsmith.core.constructs.CFunction;
import com.laytonsmith.core.constructs.CInt;
import com.laytonsmith.core.constructs.CString;
import com.laytonsmith.core.constructs.Construct;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.constructs.Token;
import com.laytonsmith.core.constructs.Token.TType;
import com.laytonsmith.core.constructs.Variable;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

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
	
	public static List<NewScript> preprocess(List<Token> tokenStream, Env env) throws ConfigCompileException{
		List<NewScript> scripts = new ArrayList<NewScript>();
		//We need to split the command definition and the pure mscript parts. First,
		//we split on newlines, those are each going to be our alias definitions
		List<List<Token>> commands = new ArrayList<List<Token>>();
		List<Token> working = new ArrayList<Token>();
		for(int i = 0; i < tokenStream.size(); i++){
			Token t = tokenStream.get(i);
			if(t.type == Token.TType.NEWLINE){
				commands.add(working);
				working = new ArrayList<Token>();
				continue;
			}
			working.add(t);
		}
		
		//Now they are split into individual aliases
		for(List<Token> stream : commands){
			//We need to make constructs from the left, and compile the right
			//Compiling the right can be simply passed off to the compile
			//function, but we need to parse the left ourselves
			//We *should* only have (bare) strings, numbers, brackets on the left
			List<Token> left = new ArrayList<Token>();
			List<Token> right = new ArrayList<Token>();
			boolean inLeft = true;
			boolean hasLabel = false;
			for(Token t : stream){
				if(t.type == Token.TType.ALIAS_END){
					inLeft = false;
					continue;
				}
				if(t.type == TType.LABEL){
					hasLabel = true;
				}
				if(inLeft){
					left.add(t);
				} else {
					right.add(t);
				}
			}
			ParseTree cright = compile(right);
			List<Construct> cleft = new ArrayList<Construct>();
			boolean atFinalVar = false;
			boolean atOptionalVars = false;
			boolean pastLabel = false;
			String label = "";
			try{
				for(int i = 0; i < left.size(); i++){
					Token t = left.get(i);
					if(hasLabel && !pastLabel){
						if(t.type == TType.LABEL){
							pastLabel = true;
							continue;
						}
						label += t.val();
						continue;
					}
					if(atFinalVar){
						throw new ConfigCompileException("The final var must be the last declaration in the alias", t.getTarget());
					}
					if(t.type == TType.LSQUARE_BRACKET){	
						Token tname = left.get(i + 1);
						atOptionalVars = true;
						if(tname.val().equals("$")){
							atFinalVar = true;
						}
						if(tname.type != TType.VARIABLE && tname.type != TType.FINAL_VAR){
							throw new ConfigCompileException("Expecting a variable, but found " + tname.val(), tname.getTarget());
						}
						i++;
						Token next = left.get(i + 1);
						if(next.type != TType.OPT_VAR_ASSIGN && next.type != TType.RSQUARE_BRACKET){
							throw new ConfigCompileException("Expecting either a variable assignment or right square bracket, but found " + next.val(), next.getTarget());
						}
						i++;
						String defaultVal = "";
						if(next.type == TType.OPT_VAR_ASSIGN){
							//We have an assignment here
							Token val = left.get(i + 1);
							i++;
							defaultVal = val.val();
							next = left.get(i + 1);
						}
						if(next.type != TType.RSQUARE_BRACKET){
							throw new ConfigCompileException("Expecting a right square bracket, but found " + next.val() + " instead.", next.getTarget());
						}
						i++;
						Variable v = new Variable(tname.val(), defaultVal, true, (tname.val().equals("$")), tname.getTarget());
						cleft.add(v);
						continue;
					}
					if(t.type == TType.VARIABLE || t.type == TType.FINAL_VAR){
						//Required variable
						if(atOptionalVars){
							throw new ConfigCompileException("Only optional variables may come after the first optional variable", t.getTarget());
						}
						if(t.val().equals("$")){
							atFinalVar = true;
						}
						Variable v = new Variable(t.val(), "", false, t.val().equals("$"), t.getTarget());
						cleft.add(v);
						continue;
					}
					cleft.add(tokenToConstruct(t));
				}
			} catch(IndexOutOfBoundsException e){
				throw new ConfigCompileException("Expecting more tokens, but reached end of alias signature before tokens were resolved.", left.get(0).getTarget());
			}
			if(!cleft.isEmpty()){
				scripts.add(new NewScript(cleft, cright, label));
			}
		}
		
		return scripts;
	}
	
	public static ParseTree compile(List<Token> tokenStream){
		ParseTree root = new ParseTree(new CFunction("sconcat", Target.UNKNOWN));
		new CompilerObject(tokenStream).compile(root);
		return root;
	}
	
	private static Construct tokenToConstruct(Token t){
		if(t.type == Token.TType.STRING){
			return new CString(t.val(), t.getTarget());
		}
		if(t.type == Token.TType.BARE_STRING){
			return new CBareString(t.val(), t.getTarget());
		}
		if(t.type == Token.TType.INTEGER){
			return new CInt(Long.parseLong(t.val()), t.getTarget());
		}
		if(t.type == Token.TType.DOUBLE){
			return new CDouble(Double.parseDouble(t.val()), t.getTarget());
		}
		return null;
	}
	
	private static class CompilerObject{
		List<Token> stream;
		int counter = 0;
		
		private CompilerObject(List<Token> stream){
			this.stream = stream;
		}
		
		Token peek(){
			return stream.get(counter);
		}
		
		Token consume(){
			return stream.get(counter++);
		}
		
		void compile(ParseTree root){
			
		}
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
				if(this.token.length() == o.token.length()){
					//Zero case
					return this.token.compareTo(o.token);
				} else if(this.token.length() < o.token.length()){
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
		private static void setupTokens(){
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
			
		}
		static{
			setupTokens();
		}

		private LexerObject(String config, File file, boolean startInPureMscript) {
			this.config = config.replaceAll("\r\n", "\n") + "\n";
			this.file = file;
			state_in_pure_mscript = startInPureMscript;
			usingNonPure = !startInPureMscript;
			clearBuffer();
		}

		private void buffer(Object s) {
			buffer.append(s);
		}

		private void parseBuffer(){
			String last = clearBuffer().trim();
			if(!last.isEmpty()){
				append(identifyToken(last));
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
			try{
				Long.parseLong(item.trim());
				return new Token(Token.TType.INTEGER, item, target);
			} catch(NumberFormatException e){
				//Not an integer
			}
			try{
				Double.parseDouble(item);
				return new Token(Token.TType.DOUBLE, item, target);
			} catch(NumberFormatException e){
				//Not a double
			}
			if(item.trim().equals("$")){
				return new Token(Token.TType.FINAL_VAR, "$", target);
			}
			if(item.matches("\\$[a-zA-Z0-9]+")){
				return new Token(Token.TType.VARIABLE, item.trim(), target);
			}
			if(item.matches("@[a-zA-Z0-9]+")){
				return new Token(Token.TType.IVARIABLE, item.trim(), target);
			}
			//else it's a bare string
			return new Token(Token.TType.BARE_STRING, item.trim(), target);
		}
		
		private void append(String value, Token.TType type){
			append(new Token(type, value, target));
		}
		
		private void append(Token t){
			token_list.add(t);
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
					} else if (state_in_block_comment) {
						//We might be ending the block comment
						if (c == '*' && c2 == '/') {
							state_in_block_comment = false;
							i++;
							if (state_in_smart_block_comment) {
								//We need to process the block comment here
								//TODO:
							}
							clearBuffer();
							continue;
						}
					} else if (state_in_line_comment) {
						if (c == '\n') {
							state_in_line_comment = false;
							clearBuffer();
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
				
				//Newlines don't count
				if(Character.isWhitespace(c) && c != '\n'){
					//We need to parse the buffer
					parseBuffer();
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
						parseBuffer();
						append("[", Token.TType.LSQUARE_BRACKET);
						continue;
					}
					if(c == ']'){
						if(!state_in_opt_var){
							error("Found ] symbol, but no optional variable had been started");
						}
						
						state_in_opt_var = false;
						parseBuffer();
						append("]", Token.TType.RSQUARE_BRACKET);
						continue;
					}
					if(state_in_opt_var){
						if(c == '='){
							//This is an optional variable declaration
							parseBuffer();
							append("=", Token.TType.OPT_VAR_ASSIGN);
							continue;
						}
					}
					if(c == '='){
						state_in_pure_mscript = true;	
						parseBuffer();
						append("=", Token.TType.ALIAS_END);
						continue;
					}
					if(c == ':'){
						parseBuffer();
						append(":", Token.TType.LABEL);
						continue;
					}
					if(c == '\n'){
						parseBuffer();
						append("\n", TType.NEWLINE);
						continue;
					}

					//At this point, all other tokens are to be taken literally
					buffer(c);
					continue;
				}
				
				//Newlines are handled differently if it's in multiline or not.
				//Remember, if we are in multiline mode, newlines are simply removed, otherwise they are
				//kept (except duplicate ones)
				if(c == '\n'){
					if(state_in_multiline){
						continue;
					} else {
						if(!token_list.isEmpty() && token_list.get(token_list.size() - 1).type != Token.TType.NEWLINE){
							parseBuffer();
							append("\n", Token.TType.NEWLINE);
							if(usingNonPure){
								//This also signals the end of pure mscript
								state_in_pure_mscript = false;
								continue;
							}
						} else {
							continue;
						}
					}
				}
				
				//Handle decimal place vs concat
				if(c == '.' && Character.isDigit(c2)){
					//It'll get identified automatically in a bit
					buffer(c);
					continue;
				}				
				
				//We need to handle /cmd vs division
				if(c == '/' && (c2 == '/' || Character.isLetter(c2))){
					//It'll be registered as a bare string later
					buffer(c);
					continue;
				}
				
				//Now we are in pure mscript mode
				//Loop through our token
				int skip;
				if((skip = identifySymbol(i)) != -1){
					//Cool, it found one. Jump ahead.
					i += skip;
					continue;
				}								
				
				buffer(c);

			}
			return new ArrayList<Token>(token_list);
		}
		
		/**
		 * If a symbol token is the next thing in the stream, it will be
		 * identified, pushed onto the token_list, and the number of characters
		 * to advance the stream is returned. If this method returns 0, no token
		 * was identified, and no changes will have been made.
		 * @param startAt
		 * @return 
		 */
		private int identifySymbol(int startAt){
			//We need as much of a lookahead as our largest token
			char lookahead[] = new char[tokenMap.first().token.length()];
			//Fill in our lookahead buffer
			for(int i = 0; i < lookahead.length; i++){
				if(i + startAt < config.length() - 1){
					lookahead[i] = config.charAt(i + startAt);
				} else {
					lookahead[i] = ' ';
				}
			}
			//Now walk through our token list, and if we find a match, use it.
			for(TokenMap tm : tokenMap){
				boolean found = true;
				for(int i = 0; i < tm.token.length(); i++){
					if(tm.token.charAt(i) != lookahead[i]){
						found = false;
						break;
					}
				}
				if(found){
					//Found it
					String last = clearBuffer();
					if(!last.isEmpty()){
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
	
	public static void main(String [] args) throws ConfigCompileException{
		List<Token> stream = lex("~var * . * / label:/cmd lit [$]= /blah \n /cmd2 = /blah", null, false);
		System.out.println(stream + "\n");
		Env env = new Env();
		List<NewScript> scripts = preprocess(stream, env);
		System.out.println(scripts);
	}
}
