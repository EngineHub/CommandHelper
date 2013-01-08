
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
import com.laytonsmith.core.constructs.IVariable;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.constructs.Token;
import com.laytonsmith.core.constructs.Token.TType;
import com.laytonsmith.core.constructs.Variable;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EmptyStackException;
import java.util.List;
import java.util.Stack;

/**
 *
 * @author Layton
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
		
		//Go through and __autoconcat__ the cbrace children
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
			consume(); //Consume the left parenthesis
			CFunction f = new CFunction(t.val(), t.getTarget());
			functionLines.add(peek().getTarget());
			pushNode(f);
			return;
		}
		
		if(t.type == TType.FUNC_START){
			//It's a loose parenthetical. Push an autoconcat onto the stack.
			CFunction f = new CFunction("__autoconcat__", Target.UNKNOWN);
			functionLines.add(peek().getTarget());
			pushNode(f);
			autoConcatCounter++;
			return;
		}
		if (t.type == TType.FUNC_END || t.type == TType.COMMA) {
			if (autoConcatCounter > 0) {
				autoConcatCounter--;
				popNode(t.getTarget());
			}
		}
		if (t.type == TType.COMMA) {
			if(peek().type == TType.COMMA){
				//This is a compile error, but extra commas at the end are ok
				throw new ConfigCompileException("Unexpected comma found. (Two commas in a row were found. Are you missing an argument?)", t.getTarget());
			}
			return;
		}
		if (t.type == TType.FUNC_END) {
			//We're done with this child, so push it up
			popNode(t.getTarget());
			try{
				functionLines.pop();
			} catch(EmptyStackException e){
				//They have too many right parenthesis
				throw new ConfigCompileException("Unexpected right parenthesis. (You have too many closing parenthesis)", t.getTarget());
			}
			return;
		}
		if (t.type == TType.LSQUARE_BRACKET) {
			//If the pointer has children, we actually want to turn this into an array_get,
			//with the last child being the first argument. If there aren't any children,
			//this is a generic array creation instead, so we want to change it into
			//an array function.
			if(pointer.hasChildren()){
				ParseTree lhs = pointer.getChildAt(pointer.numberOfChildren() - 1);
				pointer.removeChildAt(pointer.numberOfChildren() - 1);
				pushNode(new CFunction("array_get", Target.UNKNOWN));
				pointer.addChild(lhs);
				pushNode(new CFunction("__autoconcat__", Target.UNKNOWN));
			} else {
				//For now, a compile error
				throw new ConfigCompileException("Unexpected left square bracket", t.getTarget());
			}
//			CFunction f = new CFunction("__autoconcat__", Target.UNKNOWN);
//			CBracket c = new CBracket(new ParseTree(f, stream.getFileOptions()));
//			pushNode(c);
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
			popNode(t.getTarget());//pops the autoconcat
			popNode(t.getTarget());//pops the array get
			return;
		}
		if (t.type == TType.LCURLY_BRACKET) {
			CFunction f = new CFunction("__cbrace__", t.getTarget());
			pushNode(f);
			braceCounter++;
			braceLines.push(t.getTarget());
			return;
		}
		if (t.type == TType.RCURLY_BRACKET) {
			if (braceCounter == 0) {
				throw new ConfigCompileException("Unexpected right brace. (Did you have too many right braces (}) in your code?)", t.getTarget());
			}
			//If our pointer has multiple children, we need to throw them all in an autoconcat
			if(pointer.numberOfChildren() > 1){
				ParseTree ac = new ParseTree(new CFunction("__autoconcat__", Target.UNKNOWN), stream.getFileOptions());
				ac.setChildren(new ArrayList<ParseTree>(pointer.getChildren()));
				pointer.removeChildren();
				pointer.addChild(ac);
			}
			braceCounter--;
			braceLines.pop();
			popNode(t.getTarget());
			return;
		}
		//If the next token ISN'T a ) , } ] we need to autoconcat this
		if (peek().type != TType.FUNC_END && peek().type != TType.COMMA 
				&& peek().type != TType.RCURLY_BRACKET 
				&& peek().type != TType.RSQUARE_BRACKET
				&& peek().type != TType.LCURLY_BRACKET) {
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
				return new IVariable(t.val(), t.getTarget());
			case BARE_STRING:
				if (t.val().equals("true")) {
					return new CBoolean(true, t.getTarget());
				} else if (t.val().equals("false")) {
					return new CBoolean(false, t.getTarget());
				} else if (t.val().equals("null")) {
					return new CNull(t.getTarget());
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
			case VARIABLE:
				return new Variable(t.val(), null, t.getTarget());
			default:
				throw new ConfigCompileException("Unexpected identifier? Found '" + t.val() + "' but was not any expected value.", t.getTarget());
		}
	}
    
}
