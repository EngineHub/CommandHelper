package com.laytonsmith.core.functions;

import com.laytonsmith.PureUtilities.Common.StreamUtils;
import com.laytonsmith.core.FlowFunction;
import com.laytonsmith.core.MethodScriptCompiler;
import com.laytonsmith.core.NodeModifiers;
import com.laytonsmith.core.ParseTree;
import com.laytonsmith.core.Prefs;
import com.laytonsmith.core.Script;
import com.laytonsmith.core.StepAction;
import com.laytonsmith.core.StepAction.StepResult;
import com.laytonsmith.core.compiler.analysis.ParamDeclaration;
import com.laytonsmith.core.compiler.analysis.ReturnableDeclaration;
import com.laytonsmith.core.compiler.analysis.Scope;
import com.laytonsmith.core.compiler.analysis.StaticAnalysis;
import com.laytonsmith.core.constructs.CArray;
import com.laytonsmith.core.constructs.CVoid;
import com.laytonsmith.core.constructs.IVariable;
import com.laytonsmith.core.constructs.IVariableList;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.constructs.generics.GenericParameters;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.environments.GlobalEnv;
import com.laytonsmith.core.exceptions.CRE.CREFormatException;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import com.laytonsmith.core.exceptions.ConfigCompileGroupException;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.natives.interfaces.Mixed;

import java.io.File;
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
public abstract class CompositeFunction extends AbstractFunction
		implements FlowFunction<CompositeFunction.CompositeState> {

	private static final Map<Class<? extends CompositeFunction>, ParseTree> CACHED_SCRIPTS = new HashMap<>();

	static class CompositeState {
		enum Phase { EVAL_ARGS, EVAL_BODY }
		Phase phase = Phase.EVAL_ARGS;
		ParseTree[] children;
		Mixed[] evaluatedArgs;
		int argIndex = 0;
		IVariableList oldVariables;
	}

	@Override
	public final Mixed exec(Target t, Environment env, GenericParameters generics, Mixed... args) throws ConfigRuntimeException {
		// Sync fallback for compile-time optimization (CONSTANT_OFFLINE).
		// The FlowFunction path is used during normal interpretation.
		ParseTree tree = getOrCompileTree(env);

		GlobalEnv gEnv = env.getEnv(GlobalEnv.class);
		IVariableList oldVariables = gEnv.GetVarList();
		IVariableList newVariables = new IVariableList(oldVariables);
		try {
			newVariables.set(new IVariable(CArray.TYPE, "@arguments", new CArray(t, args.length,
					null, env, args), t));
		} catch (ConfigCompileException cce) {
			throw new CREFormatException(cce.getMessage(), t);
		}
		gEnv.SetVarList(newVariables);
		Mixed ret = CVoid.VOID;
		try {
			if(gEnv.GetScript() != null) {
				ret = gEnv.GetScript().eval(tree, env);
			} else {
				ret = Script.GenerateScript(null, null, null).eval(tree, env);
			}
		} finally {
			gEnv.SetVarList(oldVariables);
		}

		return ret;
	}

	@Override
	public StepResult<CompositeState> begin(Target t, ParseTree[] children, Environment env) {
		CompositeState state = new CompositeState();
		state.children = children;
		state.evaluatedArgs = new Mixed[children.length];
		if(children.length > 0) {
			return new StepResult<>(new StepAction.Evaluate(children[0]), state);
		} else {
			return evalBody(t, state, env);
		}
	}

	@Override
	public StepResult<CompositeState> childCompleted(Target t, CompositeState state, Mixed result, Environment env) {
		if(state.phase == CompositeState.Phase.EVAL_ARGS) {
			state.evaluatedArgs[state.argIndex] = result;
			state.argIndex++;
			if(state.argIndex < state.children.length) {
				return new StepResult<>(new StepAction.Evaluate(state.children[state.argIndex]), state);
			}
			return evalBody(t, state, env);
		} else {
			// Body evaluation complete
			return new StepResult<>(new StepAction.Complete(result), state);
		}
	}

	@Override
	public StepResult<CompositeState> childInterrupted(Target t, CompositeState state,
			StepAction.FlowControl action, Environment env) {
		if(state.phase == CompositeState.Phase.EVAL_BODY
				&& action.getAction() instanceof ControlFlow.ReturnAction ret) {
			return new StepResult<>(new StepAction.Complete(ret.getValue()), state);
		}
		return null;
	}

	@Override
	public void cleanup(Target t, CompositeState state, Environment env) {
		if(state != null && state.oldVariables != null) {
			env.getEnv(GlobalEnv.class).SetVarList(state.oldVariables);
		}
	}

	private StepResult<CompositeState> evalBody(Target t, CompositeState state, Environment env) {
		state.phase = CompositeState.Phase.EVAL_BODY;
		ParseTree tree = getOrCompileTree(env);

		GlobalEnv gEnv = env.getEnv(GlobalEnv.class);
		state.oldVariables = gEnv.GetVarList();
		IVariableList newVariables = new IVariableList(state.oldVariables);
		try {
			newVariables.set(new IVariable(CArray.TYPE, "@arguments",
					new CArray(t, state.evaluatedArgs.length, state.evaluatedArgs), t));
		} catch(ConfigCompileException cce) {
			throw new CREFormatException(cce.getMessage(), t);
		}
		gEnv.SetVarList(newVariables);

		return new StepResult<>(new StepAction.Evaluate(tree), state);
	}

	private ParseTree getOrCompileTree(Environment env) {
		if(CACHED_SCRIPTS.containsKey(this.getClass())) {
			return CACHED_SCRIPTS.get(this.getClass());
		}
		// TODO: Ultimately, this is not scalable. We need to compile and cache these scripts at Java compile time,
		// not at runtime the first time a function is used. This is an easier first step though.
		File debugFile = null;
		if(Prefs.DebugMode()) {
			debugFile = new File("/NATIVE-MSCRIPT/" + getName());
		}
		ParseTree tree;
		try {
			String script = script();
			Scope rootScope = new Scope();
			rootScope.addDeclaration(new ParamDeclaration("@arguments", CArray.TYPE.asLeftHandSideType(), null,
					new NodeModifiers(),
					Target.UNKNOWN));
			rootScope.addDeclaration(new ReturnableDeclaration(null, new NodeModifiers(), Target.UNKNOWN));
			tree = MethodScriptCompiler.compile(MethodScriptCompiler.lex(script, env, debugFile, true),
					env, env.getEnvClasses(), new StaticAnalysis(rootScope, true))
					// the root of the tree is null, so go ahead and pull it up
					.getChildAt(0);
		} catch(ConfigCompileException | ConfigCompileGroupException ex) {
			// This is really bad.
			throw new Error(ex);
		}
		if(cacheCompile()) {
			CACHED_SCRIPTS.put(this.getClass(), tree);
		}
		return tree;
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


}
