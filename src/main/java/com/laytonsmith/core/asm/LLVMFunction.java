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
import com.laytonsmith.core.constructs.CClassType;
import com.laytonsmith.core.constructs.CFunction;
import com.laytonsmith.core.constructs.Target;
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
	public boolean preResolveVariables() {
		return true;
	}

	@Override
	public Boolean runAsync() {
		return null;
	}

	@Override
	public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
		throw new UnsupportedOperationException("Not supported.");
	}

	/**
	 * {@inheritDoc}
	 * By default, null is returned.
	 */
	@Override
	public CClassType getReturnType(Target t, List<CClassType> argTypes, List<Target> argTargets,
			Environment env, Set<ConfigCompileException> exceptions) {
		return CClassType.AUTO; // No information is available about the return type.
	}

	/**
	 * {@inheritDoc}
	 * By default, this calls {@link StaticAnalysis#typecheck(ParseTree, Set)} on the function's arguments and passes
	 * them to {@link #getReturnType(Target, List, List, Set)} to get this function's return type.
	 */
	@Override
	public CClassType typecheck(StaticAnalysis analysis,
			ParseTree ast, Environment env, Set<ConfigCompileException> exceptions) {

		// Get and check the types of the function's arguments.
		List<ParseTree> children = ast.getChildren();
		List<CClassType> argTypes = new ArrayList<>(children.size());
		List<Target> argTargets = new ArrayList<>(children.size());
		for(ParseTree child : children) {
			argTypes.add(analysis.typecheck(child, env, exceptions));
			argTargets.add(child.getTarget());
		}

		// Return the return type of this function.
		return this.getReturnType(ast.getTarget(), argTypes, argTargets, env, exceptions);
	}

	/**
	 * {@inheritDoc}
	 * By default, {@code true} is returned.
	 */
	@Override
	public boolean hasStaticSideEffects() {
		return true; // Assuming that a function does 'something' is safe in terms of not optimizing it away.
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
	public String profileMessage(Mixed... args) {
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
	 * Returns the IR code for this function.
	 * @param t
	 * @param env
	 * @param parent
	 * @param nodes
	 * @return
	 */
	public abstract String getIR(Target t, Environment env, Script parent, ParseTree... nodes);

	@Override
	public boolean useSpecialExec() {
		throw new UnsupportedOperationException("Not supported.");
	}

	@Override
	public Mixed execs(Target t, Environment env, Script parent, ParseTree... nodes) {
		throw new UnsupportedOperationException("Not supported.");
	}

}
