package com.laytonsmith.core.functions;

import com.laytonsmith.core.MethodScriptCompiler;
import com.laytonsmith.core.ParseTree;
import com.laytonsmith.core.Script;
import com.laytonsmith.core.constructs.CArray;
import com.laytonsmith.core.constructs.CClassType;
import com.laytonsmith.core.constructs.CVoid;
import com.laytonsmith.core.constructs.IVariable;
import com.laytonsmith.core.constructs.IVariableList;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.environments.GlobalEnv;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import com.laytonsmith.core.exceptions.ConfigCompileGroupException;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.exceptions.FunctionReturnException;
import com.laytonsmith.core.natives.interfaces.Mixed;

import java.util.HashMap;
import java.util.Map;

/**
 * A CompositeFunction is a function that executes MethodScript ultimately. It is written entirely in MethodScript, and
 * does not directly run any java.
 */
public abstract class CompositeFunction extends AbstractFunction {

	private static final Map<Class<? extends CompositeFunction>, ParseTree> CACHED_SCRIPTS = new HashMap<>();

	@Override
	public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
		ParseTree tree;
		if(!CACHED_SCRIPTS.containsKey(this.getClass())) {
			try {

				String script = script();
				tree = MethodScriptCompiler.compile(MethodScriptCompiler.lex(script, null, true))
						// the root of the tree is null, so go ahead and pull it up
						.getChildAt(0);
			} catch (ConfigCompileException | ConfigCompileGroupException ex) {
				// This is really bad.
				throw new Error(ex);
			}
			if(cacheCompile()) {
				CACHED_SCRIPTS.put(this.getClass(), tree);
			}
		} else {
			tree = CACHED_SCRIPTS.get(this.getClass());
		}
		GlobalEnv env = environment.getEnv(GlobalEnv.class);
		IVariableList oldVariables = env.GetVarList();
		IVariableList newVariables = new IVariableList();
		newVariables.set(new IVariable(CClassType.get("array"), "@arguments", new CArray(t, args.length, args), t));
		env.SetVarList(newVariables);
		Mixed ret = CVoid.VOID;
		try {
			if(env.GetScript() != null) {
				env.GetScript().eval(tree, environment);
			} else {
				// This can happen when the environment is not fully setup during tests.
				Script.GenerateScript(null, null).eval(tree, environment);
			}
		} catch (FunctionReturnException ex) {
			ret = ex.getReturn();
		}
		env.SetVarList(oldVariables);
		return ret;
	}

	/**
	 * The script that will be compiled and run when this function is executed. The value array @arguments will be set
	 * with the function inputs. Variables set in this script will not leak to the actual script environment, but in
	 * general, the rest of the environment is identical, and so any other changes to the environment will persist. To
	 * return a value, use return().
	 *
	 * @return
	 */
	protected abstract String script();

	/**
	 * This method can be overridden to return false if the script should not be compiled and cached.
	 *
	 * @return
	 */
	protected boolean cacheCompile() {
		return true;
	}

	@Override
	public final Mixed execs(Target t, Environment env, Script parent, ParseTree... nodes) {
		throw new Error(this.getClass().toString());
	}

	@Override
	public final boolean useSpecialExec() {
		// This defeats the purpose, so don't allow this.
		return false;
	}

}
