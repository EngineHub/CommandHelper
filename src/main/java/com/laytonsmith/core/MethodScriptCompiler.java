package com.laytonsmith.core;

import com.laytonsmith.core.compiler.Optimizable;
import com.laytonsmith.core.compiler.Optimizable.OptimizationOption;
import com.laytonsmith.core.compiler.FileOptions;
import com.laytonsmith.core.compiler.NewMethodScriptCompiler;
import com.laytonsmith.core.compiler.TokenStream;
import com.laytonsmith.core.constructs.*;
import com.laytonsmith.core.constructs.Token.TType;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.environments.GlobalEnv;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.compiler.CompilerFunctions;
import com.laytonsmith.core.compiler.CompilerWarning;
import com.laytonsmith.core.functions.DataHandling;
import com.laytonsmith.core.functions.Function;
import com.laytonsmith.core.functions.FunctionList;
import com.laytonsmith.core.functions.IncludeCache;
import com.laytonsmith.core.natives.interfaces.Mixed;
import java.io.File;
import java.util.*;

/**
 *
 * @author Layton
 */
public final class MethodScriptCompiler {

	private final static EnumSet<Optimizable.OptimizationOption> NO_OPTIMIZATIONS = EnumSet.noneOf(Optimizable.OptimizationOption.class);
	private final static FileOptions fileOptions;
	static {
		FileOptions temp = null;
		try {
			temp = new FileOptions(new HashMap<FileOptions.Directive, String>(), Target.UNKNOWN);
		} catch (ConfigCompileException ex) {
			//Can't happen, but sure.
		}
		fileOptions = temp;
	}

	private MethodScriptCompiler() {
	}

	public static TokenStream lex(String config, File file, boolean inPureMScript) throws ConfigCompileException {
		return NewMethodScriptCompiler.lex(config, file, inPureMScript);
	}

	/**
	 * This function breaks the token stream into parts, separating the
	 * aliases/MethodScript from the command triggers
	 *
	 * @param tokenStream
	 * @return
	 * @throws ConfigCompileException
	 */
	public static List<Script> preprocess(List<Token> tokenStream) throws ConfigCompileException {
		//First, pull out the duplicate newlines
		ArrayList<Token> temp = new ArrayList<Token>();
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

		tokenStream = temp;

		//Handle multiline constructs
		ArrayList<Token> tokens1_1 = new ArrayList<Token>();
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
				throw new ConfigCompileException("Multiline symbol must follow the alias_end token", thisToken.target);
			}

			//If we're not in a multiline construct, or we are in it and it's not a newline, add
			//it
			if (!inside_multiline || (inside_multiline && !thisToken.type.equals(TType.NEWLINE))) {
				tokens1_1.add(thisToken);
			}
		}

		if (inside_multiline) {
			throw new ConfigCompileException("Expecting a multiline end symbol, but your last multiline alias appears to be missing one.", thisToken.target);
		}

		//take out newlines that are behind a \
		ArrayList<Token> tokens2 = new ArrayList<Token>();
		for (int i = 0; i < tokens1_1.size(); i++) {
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

		List<Token> left = new ArrayList<Token>();
		List<Token> right = new ArrayList<Token>();
		List<Script> scripts = new ArrayList<Script>();
		boolean inLeft = true;
		for (Token t : tokens2) {
			if (inLeft) {
				if (t.type == TType.ALIAS_END) {
					inLeft = false;
				} else {
					left.add(t);
				}
			} else {
				if (t.type == TType.NEWLINE) {
					inLeft = true;
					//Env newEnv = new Env();//env;
//                    try{
//                        newEnv = env.clone();
//                    } catch(Exception e){}
					Script s = new Script(left, right, fileOptions);
					scripts.add(s);
					left = new ArrayList();
					right = new ArrayList();
				} else {
					right.add(t);
				}
			}
		}
		return scripts;
	}

	public static ParseTree compile(TokenStream stream, Environment env) throws ConfigCompileException {
		ParseTree tree = NewMethodScriptCompiler.compile(stream, env);
		return tree;
	}

	/**
	 * Executes a pre-compiled MethodScript, given the specified Script
	 * environment. Both done and script may be null, and if so, reasonable
	 * defaults will be provided. The value sent to done will also be returned,
	 * as a Construct, so this one function may be used synchronously also.
	 *
	 * @param root
	 * @param done
	 * @param script
	 */
	public static Mixed execute(ParseTree root, Environment env, MethodScriptComplete done, Script script) {
		return execute(root, env, done, script, null);
	}

	/**
	 * Executes a pre-compiled MethodScript, given the specified Script
	 * environment, but also provides a method to set the constants in the
	 * script.
	 *
	 * @param root
	 * @param env
	 * @param done
	 * @param script
	 * @param vars
	 * @return
	 */
	public static Mixed execute(ParseTree root, Environment env, MethodScriptComplete done, Script script, List<Variable> vars) {
		if (script == null) {
			script = new Script(null, null, fileOptions);
		}
		if (vars != null) {
			Map<String, Variable> varMap = new HashMap<String, Variable>();
			for (Variable v : vars) {
				varMap.put(v.getName(), v);
			}
			for (Mixed tempNode : root.getAllData()) {
				if (tempNode instanceof Variable) {
					((Variable) tempNode).setVal(
							varMap.get(((Variable) tempNode).getName()).getDefault());
				}
			}
		}
		StringBuilder b = new StringBuilder();
		Mixed returnable = null;
		//for (ParseTree gg : root.getChildren()) {
		script.setLabel(env.getEnv(GlobalEnv.class).GetLabel());
		Mixed retc = script.eval(root, env);
		if (root.numberOfChildren() == 1) {
			returnable = retc;
		}
		String ret = retc.isNull() ? "null" : retc.val();
		if (ret != null && !ret.trim().isEmpty()) {
			b.append(ret).append(" ");
		}
		//}
		if (done != null) {
			done.done(b.toString().trim());
		}
		if (returnable != null) {
			return returnable;
		}
		return Static.resolveConstruct(b.toString().trim(), Target.UNKNOWN);
	}

	public static void registerAutoIncludes(Environment env, Script s) {
		File root = env.getEnv(GlobalEnv.class).GetRootFolder();
		File auto_include = new File(root, "auto_include.ms");
		if (auto_include.exists()) {
			MethodScriptCompiler.execute(IncludeCache.get(auto_include, new Target(0, auto_include, 0), env), env, null, s);
		}

		for (File f : Static.getAliasCore().autoIncludes) {
			MethodScriptCompiler.execute(IncludeCache.get(f, new Target(0, f, 0), env), env, null, s);
		}
	}
}
