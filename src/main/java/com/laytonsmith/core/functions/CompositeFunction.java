package com.laytonsmith.core.functions;

import com.laytonsmith.PureUtilities.Common.StreamUtils;
import com.laytonsmith.core.MethodScriptCompiler;
import com.laytonsmith.core.ParseTree;
import com.laytonsmith.core.Script;
import com.laytonsmith.core.constructs.CArray;
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
 *
 * CompositeFunctions benefit from the fact that assuming the underlying functions
 * exist on a given platform, the function can be automatically provided on that platform.
 * This prevents rewrites for straightforward functions.
 */
public abstract class CompositeFunction extends AbstractFunction {

	private static final Map<Class<? extends CompositeFunction>, ParseTree> CACHED_SCRIPTS = new HashMap<>();

	@Override
	public final Mixed exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
		ParseTree tree;
		// TODO: Ultimately, this is not scalable. We need to compile and cache these scripts at Java compile time,
		// not at runtime the first time a function is used. This is an easier first step though.
		if(!CACHED_SCRIPTS.containsKey(this.getClass())) {
			try {

				String script = script();
				tree = MethodScriptCompiler.compile(MethodScriptCompiler.lex(script, env, null, true),
						env, env.getEnvClasses())
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

		GlobalEnv gEnv = env.getEnv(GlobalEnv.class);
		IVariableList oldVariables = gEnv.GetVarList();
		IVariableList newVariables = new IVariableList();
		newVariables.set(new IVariable(CArray.TYPE, "@arguments", new CArray(t, args.length, args), t));
		gEnv.SetVarList(newVariables);
		Mixed ret = CVoid.VOID;
		try {
			if(gEnv.GetScript() != null) {
				gEnv.GetScript().eval(tree, env);
			} else {
				// This can happen when the environment is not fully setup during tests, in addition to optimization
				Script.GenerateScript(null, null).eval(tree, env);
			}
		} catch (FunctionReturnException ex) {
			ret = ex.getReturn();
		} catch (ConfigRuntimeException ex) {
			if(gEnv.GetStackTraceManager().getCurrentStackTrace().isEmpty()) {
				ex.setTarget(t);
				ConfigRuntimeException.StackTraceElement ste = new ConfigRuntimeException
						.StackTraceElement(this.getName(), t);
				gEnv.GetStackTraceManager().addStackTraceElement(ste);
			}
			gEnv.GetStackTraceManager().setCurrentTarget(t);
			throw ex;
		}
		gEnv.SetVarList(oldVariables);

		return ret;
	}

	/**
	 * The script that will be compiled and run when this function is executed. The value array @arguments will be set
	 * with the function inputs. Variables set in this script will not leak to the actual script environment, but in
	 * general, the rest of the environment is identical, and so any other changes to the environment will persist. To
	 * return a value, use return().
	 * <p>
	 * In complex cases, it will be easier to just return {@link #getBundledCode()}.
	 *
	 * @return
	 */
	protected abstract String script();

	/**
	 * For more complex functions, it's probably useful to write the code in an external file. The general contract
	 * is that for functions, they should go in the function_impl resources folder with
	 * {@code function_name.ms}, and then the script method can just {@code return getBundledCode();}
	 * @return
	 */
	protected String getBundledCode() {
		return StreamUtils.GetString(CompositeFunction.class.getResourceAsStream("/function_impl/" + getName() + ".ms"));
	}

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
