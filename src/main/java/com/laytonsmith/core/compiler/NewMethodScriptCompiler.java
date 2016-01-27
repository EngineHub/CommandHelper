package com.laytonsmith.core.compiler;

import com.laytonsmith.PureUtilities.Common.StreamUtils;
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
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * 
 */
public class NewMethodScriptCompiler {

	/**
	 * Takes a raw string input, and parses it into a TokenStream, which can be
	 * passed to either the preprocessor, or the compiler.
	 *
	 * @param script
	 * @param file
	 * @param startInPureMscript
	 * @return
	 * @throws ConfigCompileException
	 */
	public static TokenStream lex(String script, File file, boolean startInPureMscript) throws ConfigCompileException {
		script = script.replaceAll("\r\n", "\n");
		script = script + "\n";

		LexerObject lo = new LexerObject(script, file, startInPureMscript);

		return lo.lex();
	}

	public static List<NewScript> preprocess(TokenStream tokenStream, Environment compilerEnvironment) throws ConfigCompileException {
		List<NewScript> scripts = new ArrayList<NewScript>();
		//We need to split the command definition and the pure mscript parts. First,
		//we split on newlines, those are each going to be our alias definitions
		List<List<Token>> commands = new ArrayList<List<Token>>();
		List<Token> working = new ArrayList<Token>();
		for (int i = 0; i < tokenStream.size(); i++) {
			Token t = tokenStream.get(i);
			if (t.type == Token.TType.NEWLINE) {
				commands.add(working);
				working = new ArrayList<Token>();
				continue;
			}
			working.add(t);
		}

		//Now they are split into individual aliases
		for (List<Token> stream : commands) {
			//We need to make constructs from the left, and compile the right
			//Compiling the right can be simply passed off to the compile
			//function, but we need to parse the left ourselves
			//We *should* only have (bare) strings, numbers, brackets on the left
			List<Token> left = new ArrayList<Token>();
			TokenStream right = new TokenStream(new ArrayList<Token>(), tokenStream.fileOptions);
			boolean inLeft = true;
			boolean hasLabel = false;
			for (Token t : stream) {
				if (t.type == Token.TType.ALIAS_END) {
					inLeft = false;
					continue;
				}
				if (t.type == TType.LABEL) {
					hasLabel = true;
				}
				if (inLeft) {
					left.add(t);
				} else {
					right.add(t);
				}
			}
			ParseTree cright = compile(right, compilerEnvironment);
			List<Construct> cleft = new ArrayList<Construct>();
			boolean atFinalVar = false;
			boolean atOptionalVars = false;
			boolean pastLabel = false;
			String label = "";
			try {
				for (int i = 0; i < left.size(); i++) {
					Token t = left.get(i);
					if (hasLabel && !pastLabel) {
						if (t.type == TType.LABEL) {
							pastLabel = true;
							continue;
						}
						label += t.val();
						continue;
					}
					if (atFinalVar) {
						throw new ConfigCompileException("The final var must be the last declaration in the alias", t.getTarget());
					}
					if (t.type == TType.LSQUARE_BRACKET) {
						Token tname = left.get(i + 1);
						atOptionalVars = true;
						if (tname.val().equals("$")) {
							atFinalVar = true;
						}
						if (tname.type != TType.VARIABLE && tname.type != TType.FINAL_VAR) {
							throw new ConfigCompileException("Expecting a variable, but found " + tname.val(), tname.getTarget());
						}
						i++;
						Token next = left.get(i + 1);
						if (next.type != TType.OPT_VAR_ASSIGN && next.type != TType.RSQUARE_BRACKET) {
							throw new ConfigCompileException("Expecting either a variable assignment or right square bracket, but found " + next.val(), next.getTarget());
						}
						i++;
						String defaultVal = "";
						if (next.type == TType.OPT_VAR_ASSIGN) {
							//We have an assignment here
							Token val = left.get(i + 1);
							i++;
							defaultVal = val.val();
							next = left.get(i + 1);
						}
						if (next.type != TType.RSQUARE_BRACKET) {
							throw new ConfigCompileException("Expecting a right square bracket, but found " + next.val() + " instead. (Did you forget to quote a multi word string?)", next.getTarget());
						}
						i++;
						Variable v = new Variable(tname.val(), defaultVal, true, (tname.val().equals("$")), tname.getTarget());
						cleft.add(v);
						continue;
					}
					if (t.type == TType.VARIABLE || t.type == TType.FINAL_VAR) {
						//Required variable
						if (atOptionalVars) {
							throw new ConfigCompileException("Only optional variables may come after the first optional variable", t.getTarget());
						}
						if (t.val().equals("$")) {
							atFinalVar = true;
						}
						Variable v = new Variable(t.val(), "", false, t.val().equals("$"), t.getTarget());
						cleft.add(v);
						continue;
					}
					cleft.add(tokenToConstruct(t));
				}
			} catch (IndexOutOfBoundsException e) {
				throw new ConfigCompileException("Expecting more tokens, but reached end of alias signature before tokens were resolved.", left.get(0).getTarget());
			}
			if (!cleft.isEmpty()) {
				link(cright, compilerEnvironment);
				scripts.add(new NewScript(cleft, cright, label));
			}
		}

		return scripts;
	}

	/**
	 * TODO: Need a platform resolver here?
	 * @param tokenStream
	 * @param compilerEnvironment
	 * @return
	 * @throws ConfigCompileException 
	 */
	public static ParseTree compile(TokenStream tokenStream, Environment compilerEnvironment) throws ConfigCompileException {
		ParseTree root = new ParseTree(new CFunction("__autoconcat__", Target.UNKNOWN), tokenStream.getFileOptions());
		new CompilerObject(tokenStream).compile(root, compilerEnvironment);
		link(root, compilerEnvironment);
		return root;
	}

	private static void link(ParseTree root, Environment compilerEnvirontment) throws ConfigCompileException {
		//Before we actually link, we need to optimize our branch functions, that is,
		//currently just if. However, at this point, we also need to optimize __autoconcat__.
		//so we know what the tree actually looks like. Also, we want to first group all our auto includes
		//together, along with our actual tree.
		ParseTree master = new ParseTree(new CFunction("__autoconcat__", Target.UNKNOWN), root.getFileOptions());
		for (ParseTree include : compilerEnvirontment.getEnv(CompilerEnvironment.class).getIncludes()) {
			master.addChild(include);
		}
		master.addChild(root);
		OptimizerObject optimizer = new OptimizerObject(root, compilerEnvirontment);
		optimizer.optimize();
		//root is now optimized
	}

	

	private static Construct tokenToConstruct(Token t) {
		if (t.type == Token.TType.STRING) {
			return new CString(t.val(), t.getTarget());
		}
		if (t.type == Token.TType.BARE_STRING) {
			return new CBareString(t.val(), t.getTarget());
		}
		if (t.type == Token.TType.INTEGER) {
			return new CInt(Long.parseLong(t.val()), t.getTarget());
		}
		if (t.type == Token.TType.DOUBLE) {
			return new CDouble(Double.parseDouble(t.val()), t.getTarget());
		}
		return null;
	}

	public static void main(String[] args) throws ConfigCompileException {
		CompilerEnvironment env = new CompilerEnvironment();
		env.setConstant("v.n", "value");
		TokenStream stream = lex("<! strict; > @var", null, true);
		StreamUtils.GetSystemOut().println(stream + "\n");
//		StreamUtils.GetSystemOut().println(preprocess(stream).toString());
		ParseTree tree = compile(stream, Environment.createEnvironment(env));
		StreamUtils.GetSystemOut().println(tree.toStringVerbose());
	}
}
