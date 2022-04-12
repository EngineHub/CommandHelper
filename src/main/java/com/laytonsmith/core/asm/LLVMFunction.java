package com.laytonsmith.core.asm;

import com.laytonsmith.PureUtilities.ClassLoading.ClassDiscovery;
import com.laytonsmith.annotations.api;
import com.laytonsmith.annotations.noprofile;
import com.laytonsmith.annotations.seealso;
import com.laytonsmith.core.Documentation;
import com.laytonsmith.core.LogLevel;
import com.laytonsmith.core.ParseTree;
import com.laytonsmith.core.Script;
import com.laytonsmith.core.compiler.analysis.Scope;
import com.laytonsmith.core.compiler.analysis.StaticAnalysis;
import com.laytonsmith.core.compiler.signature.FunctionSignatures;
import com.laytonsmith.core.constructs.Auto;
import com.laytonsmith.core.constructs.CClassType;
import com.laytonsmith.core.constructs.CFunction;
import com.laytonsmith.core.constructs.LeftHandSideType;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.constructs.generics.GenericParameters;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.functions.ExampleScript;
import com.laytonsmith.core.functions.Function;
import com.laytonsmith.core.functions.FunctionBase;
import com.laytonsmith.core.functions.FunctionList;
import com.laytonsmith.core.natives.interfaces.Mixed;
import com.laytonsmith.core.snapins.PackagePermission;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 *
 */
public abstract class LLVMFunction implements FunctionBase, Function {

	private boolean shouldProfile = true;

	public LLVMFunction() {
		shouldProfile = !this.getClass().isAnnotationPresent(noprofile.class);
	}

	@Override
	public String docs() {
		return getDefaultDocs();
	}

	private FunctionBase getDefaultFunction() throws ConfigCompileException {
		CFunction f = new CFunction(getName(), Target.UNKNOWN);
		FunctionBase fb = FunctionList.getFunction(f, api.Platforms.COMPILER_LLVM, null);
		return fb;
	}

	private String getDefaultDocs() {
		try {
			FunctionBase fb = getDefaultFunction();
			return fb.docs();
		} catch (ConfigCompileException ex) {
			return "mixed {...} This function is missing documentation. Please report it.";
		}
	}

	@Override
	public boolean appearInDocumentation() {
		return true;
	}

	@Override
	public PackagePermission getPermission() {
		return PackagePermission.NO_PERMISSIONS_NEEDED;
	}

	@Override
	public boolean isCore() {
		// All LLVM functions are core.
		return true;
	}

	@Override
	public boolean isRestricted() {
		return false;
	}

	@Override
	public final boolean preResolveVariables() {
		return true;
	}

	@Override
	public Boolean runAsync() {
		return null;
	}

	@Override
	public final Mixed exec(Target t, Environment environment, GenericParameters generics, Mixed... args) throws ConfigRuntimeException {
		throw new UnsupportedOperationException("Not supported.");
	}

	@Override
	public FunctionSignatures getSignatures() {
		return null;
	}

	@Override
	public List<LeftHandSideType> getResolvedParameterTypes(StaticAnalysis analysis,
			Target t, Environment env, GenericParameters generics,
			LeftHandSideType inferredReturnType, List<ParseTree> children) {
		List<LeftHandSideType> ret = new ArrayList<>();
		if(generics != null) {
			// Explicit parameters were provided, just use those.
			return generics.getLeftHandParameters();
		}
		for(ParseTree child : children) {
			ret.add(child.getDeclaredType(analysis, t, env, Auto.LHSTYPE));
		}
		return ret;
	}

	/**
	 * {@inheritDoc}
	 * By default, null is returned.
	 */
	@Override
	public LeftHandSideType getReturnType(ParseTree node, Target t, List<LeftHandSideType> argTypes, List<Target> argTargets,
			LeftHandSideType inferredType, Environment env, Set<ConfigCompileException> exceptions) {
		return LeftHandSideType.fromCClassType(CClassType.AUTO, t); // No information is available about the return type.
	}

	/**
	 * {@inheritDoc}
	 * By default, this calls {@link StaticAnalysis#typecheck(ParseTree, Environment, Set)} on the function's arguments and passes
	 * them to {@link #getReturnType(Target, List, List, Environment, Set)} (Target, List, List, Set)} to get this function's return type.
	 */
	@Override
	public LeftHandSideType typecheck(StaticAnalysis analysis,
			ParseTree ast, LeftHandSideType inferredType, Environment env, Set<ConfigCompileException> exceptions) {

		// Get and check the types of the function's arguments.
		List<ParseTree> children = ast.getChildren();
		List<LeftHandSideType> argTypes = new ArrayList<>(children.size());
		List<Target> argTargets = new ArrayList<>(children.size());
		for(ParseTree child : children) {
			argTypes.add(analysis.typecheck(child, inferredType, env, exceptions));
			argTargets.add(child.getTarget());
		}

		// Return the return type of this function.
		return this.getReturnType(ast, ast.getTarget(),
				argTypes, argTargets, inferredType, env, exceptions);
	}

	/**
	 * {@inheritDoc}
	 * By default, the parent scope is passed to the first child, the result is passed to the second child, etc.
	 * This method returns the scope as returned by the last child, or the parent scope if it does not have children.
	 */
	@Override
	public Scope linkScope(StaticAnalysis analysis, Scope parentScope,
			ParseTree ast, Environment env, Set<ConfigCompileException> exceptions) {
		Scope scope = parentScope;
		for(ParseTree child : ast.getChildren()) {
			scope = analysis.linkScope(scope, child, env, exceptions);
		}
		return scope;
	}

	@Override
	public ParseTree postParseRewrite(ParseTree ast, Environment env,
			Set<Class<? extends Environment.EnvironmentImpl>> envs, Set<ConfigCompileException> exceptions) {
		return null;
	}

	@Override
	public ExampleScript[] examples() throws ConfigCompileException {
		try {
			FunctionBase fb = getDefaultFunction();
			if(fb instanceof Function) {
				return ((Function) fb).examples();
			}

		} catch (ConfigCompileException ex) {
			//
		}
		return new ExampleScript[0];
	}

	@Override
	public boolean shouldProfile() {
		return shouldProfile;
	}

	@Override
	public LogLevel profileAt() {
		return LogLevel.VERBOSE;
	}

	@Override
	public String profileMessage(Environment env, Mixed... args) {
		throw new UnsupportedOperationException("Not supported.");
	}

	@Override
	public String profileMessageS(List<ParseTree> args) {
		throw new UnsupportedOperationException("Not supported.");
	}

	@Override
	public URL getSourceJar() {
		return ClassDiscovery.GetClassContainer(this.getClass());
	}

	private static final Class[] EMPTY_CLASS = new Class[0];
	@Override
	public Class<? extends Documentation>[] seeAlso() {
		seealso see = this.getClass().getAnnotation(seealso.class);
		if(see == null) {
			return EMPTY_CLASS;
		} else {
			return see.value();
		}
	}

	@Override
	public int compareTo(Function o) {
		return this.getName().compareTo(o.getName());
	}

	/**
	 * Appends the IR code for this function.
	 * @param builder The ongoing builder. Functions should append to this as needed.
	 * @param t The code target this node comes from.
	 * @param env The Environment
	 * @param nodes The children passed to this function, could be empty array.
	 * @return Information on the returned value, including things like type (if known) and how to reference the
	 * output value.
	 * @throws com.laytonsmith.core.exceptions.ConfigCompileException If there is a compilation error.
	 */
	public abstract IRData buildIR(IRBuilder builder, Target t, Environment env, ParseTree... nodes) throws ConfigCompileException;

	@Override
	public final boolean useSpecialExec() {
		throw new UnsupportedOperationException("Not supported.");
	}

	@Override
	public final Mixed execs(Target t, Environment env, Script parent, GenericParameters generics, ParseTree... nodes) {
		throw new UnsupportedOperationException("Not supported.");
	}

	/**
	 * If this function is used, and it needs to do startup configuration, that configuration goes here.
	 *
	 * There may be other circumstances where this code is called, for instance if the user has indicated
	 * that it may be called via reflection, so functions should keep this in mind, and configure the environment
	 * appropriately.
	 * @param builder
	 * @param startupEnv
	 * @param t The synthesized code target for this function
	 */
	public void addStartupCode(IRBuilder builder, Environment startupEnv, Target t) {
		//
	}
}
