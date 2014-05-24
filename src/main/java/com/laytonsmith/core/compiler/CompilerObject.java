
package com.laytonsmith.core.compiler;

import com.laytonsmith.core.ParseTree;
import com.laytonsmith.core.constructs.CBoolean;
import com.laytonsmith.core.constructs.CDouble;
import com.laytonsmith.core.constructs.CFunction;
import com.laytonsmith.core.constructs.CInt;
import com.laytonsmith.core.constructs.CKeyword;
import com.laytonsmith.core.constructs.CLabel;
import com.laytonsmith.core.constructs.CNull;
import com.laytonsmith.core.constructs.CString;
import com.laytonsmith.core.constructs.CSymbol;
import com.laytonsmith.core.constructs.Construct;
import com.laytonsmith.core.constructs.NewIVariable;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.constructs.Token;
import com.laytonsmith.core.constructs.Token.TType;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import java.util.Arrays;
import java.util.EmptyStackException;
import java.util.List;
import java.util.Stack;

/**
 *
 * 
 */
class CompilerObject {
	TokenStream stream;
	Stack<ParseTree> nodes = new Stack<ParseTree>();
	int autoConcatCounter = 0;
	int bracketCounter = 0;
	Stack<Target> bracketLines = new Stack<Target>();
	int braceCounter = 0;
	Stack<Target> braceLines = new Stack<Target>();
	Stack<Target> functionLines = new Stack<Target>();
	ParseTree pointer;
	ParseTree root;
	CompilerEnvironment env;

	CompilerObject(TokenStream stream) {
		this.stream = stream;
	}

	Token peek() {
		if (stream.isEmpty()) {
			return new Token(TType.UNKNOWN, "", Target.UNKNOWN);
		}
		return stream.get(0);
	}

	Token consume() {
		return stream.remove(0);
	}

	void compile(ParseTree root, Environment compilerEnv) throws ConfigCompileException {
		this.root = root;
		nodes.push(root);
		pointer = root;
		this.env = compilerEnv.getEnv(CompilerEnvironment.class);
		while (!stream.isEmpty()) {
			compile0();
		}
		if (bracketCounter > 0) {
			throw new ConfigCompileException("Unclosed brackets. (Did you forget a right bracket (])?)", bracketLines.peek());
		}
		if (braceCounter > 0) {
			throw new ConfigCompileException("Unclosed braces. (Did you forget a right brace (})?)", braceLines.peek());
		}
		if (!functionLines.isEmpty()) {
			throw new ConfigCompileException("Unclosed left parenthesis. (Did you forget to close a function?)", functionLines.peek());
		}
	}

	void compile0() throws ConfigCompileException {
		Token t = consume();
		if (t.type == TType.NEWLINE) {
			return;
		}
		if (t.type == TType.CONST_START) {
			StringBuilder constName = new StringBuilder();
			while ((t = consume()).type != TType.RCURLY_BRACKET) {
				if (t.type != TType.BARE_STRING && t.type != TType.CONCAT) {
					throw new ConfigCompileException("Constant names may only contain names and dots.", t.getTarget());
				}
				constName.append(t.val());
			}
			Construct constant = env.getConstant(constName.toString());
			if (constant == null) {
				throw new ConfigCompileException("Expected the constant ${" + constName.toString() + "} to be provided in the compilation options, but it wasn't.", t.getTarget());
			}
			t = new Token(TType.STRING, constant.val(), constant.getTarget());
		}							
		if (t.type == TType.BARE_STRING && peek().type == TType.FUNC_START) {
			consume();
			CFunction f = new CFunction(t.val(), t.getTarget());
			functionLines.add(peek().getTarget());
			pushNode(f);
			return;
		}
		if (t.type == TType.FUNC_END || t.type == TType.COMMA) {
			if (autoConcatCounter > 0) {
				autoConcatCounter--;
				popNode(t.getTarget());
			}
		}
		if (t.type == TType.COMMA) {
			return;
		}
		if (t.type == TType.FUNC_END) {
			//We're done with this child, so push it up
			popNode(t.getTarget());
			functionLines.pop();
			return;
		}
		if (t.type == TType.LSQUARE_BRACKET) {
			CFunction f = new CFunction("__cbracket__", Target.UNKNOWN);
			pushNode(f);
			bracketCounter++;
			bracketLines.push(t.getTarget());
			return;
		}
		if (t.type == TType.RSQUARE_BRACKET) {
			if (bracketCounter == 0) {
				throw new ConfigCompileException("Unexpected right bracket. (Did you have too many right square brackets (]) in your code?)", t.getTarget());
			}
			bracketCounter--;
			bracketLines.pop();
			popNode(t.getTarget());
			return;
		}
		if (t.type == TType.LCURLY_BRACKET) {
			CFunction f = new CFunction("__cbrace__", Target.UNKNOWN);
			pushNode(f);
			braceCounter++;
			braceLines.push(t.getTarget());
			return;
		}
		if (t.type == TType.RCURLY_BRACKET) {
			if (braceCounter == 0) {
				throw new ConfigCompileException("Unexpected right brace. (Did you have too many right braces (}) in your code?)", t.getTarget());
			}
			braceCounter--;
			braceLines.pop();
			popNode(t.getTarget());
			return;
		}
		//If the next token ISN'T a ) , } ] we need to autoconcat this
		if (peek().type != TType.FUNC_END && peek().type != TType.COMMA && peek().type != TType.RCURLY_BRACKET && peek().type != TType.RSQUARE_BRACKET) {
			//... unless we're already in an autoconcat
			if (!(pointer.getData() instanceof CFunction && ((CFunction) pointer.getData()).val().equals("__autoconcat__"))) {
				CFunction f = new CFunction("__autoconcat__", Target.UNKNOWN);
				pushNode(f);
				autoConcatCounter++;
			}
		}
		if (t.type == TType.BARE_STRING && peek().type == TType.LABEL) {
			consume();
			pointer.addChild(new ParseTree(new CLabel(new CString(t.val(), t.getTarget())), stream.getFileOptions()));
			return;
		}
		if (t.type.isIdentifier()) {
			//If it's an atomic, put it in a construct and parse tree, then add it
			pointer.addChild(new ParseTree(resolveIdentifier(t), stream.getFileOptions()));
			return;
		}
		if (t.type.isSymbol()) {
			pointer.addChild(new ParseTree(new CSymbol(t.val(), t.type, t.getTarget()), stream.getFileOptions()));
			return;
		}
		//Now we have to check ahead for commas and other division parameters.
	}

	private void pushNode(CFunction node) {
		ParseTree n = new ParseTree(node, stream.getFileOptions());
		pointer.addChild(n);
		nodes.push(n);
		pointer = n;
	}

	private void popNode(Target t) throws ConfigCompileException {
		try {
			nodes.pop();
			pointer = nodes.peek();
		} catch (EmptyStackException e) {
			throw new ConfigCompileException("Unmatched closing parenthesis. (Did you put too many right parenthesis?)", t);
		}
	}
	private static List<String> keywords = Arrays.asList(new String[]{"else", "bind", "proc"});

	private Construct resolveIdentifier(Token t) throws ConfigCompileException {
		switch (t.type) {
			case STRING:
				return new CString(t.val(), t.getTarget());
				//				case SMART_STRING:
				//
				//				case VARIABLE:
				//
				//				case FINAL_VAR:
			case IVARIABLE:
				return new NewIVariable(t.val(), t.getTarget());
			case BARE_STRING:
				if (t.val().equals("true")) {
					return CBoolean.GenerateCBoolean(true, t.getTarget());
				} else if (t.val().equals("false")) {
					return CBoolean.GenerateCBoolean(false, t.getTarget());
				} else if (t.val().equals("null")) {
					return CNull.GenerateCNull(t.getTarget());
				} else if (keywords.contains(t.val())) {
					return new CKeyword(t.val(), t.getTarget());
				} else {
					if (stream.fileOptions.isStrict()) {
						throw new ConfigCompileException("Bare strings not allowed in strict mode. (" + t.val() + ")", t.getTarget());
					} else {
						return new CString(t.val(), t.getTarget());
					}
				}
			case DOUBLE:
				return new CDouble(t.val(), t.getTarget());
			case INTEGER:
				return new CInt(t.val(), t.getTarget());
			default:
				throw new ConfigCompileException("Unexpected identifier? Found '" + t.val() + "' but was not any expected value.", t.getTarget());
		}
	}
    
}
