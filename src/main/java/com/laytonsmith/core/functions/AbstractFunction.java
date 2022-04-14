package com.laytonsmith.core.functions;

import com.laytonsmith.PureUtilities.ClassLoading.ClassDiscovery;
import com.laytonsmith.PureUtilities.Common.ReflectionUtils;
import com.laytonsmith.PureUtilities.Common.StreamUtils;
import com.laytonsmith.annotations.DocumentLink;
import com.laytonsmith.annotations.MEnum;
import com.laytonsmith.annotations.core;
import com.laytonsmith.annotations.hide;
import com.laytonsmith.annotations.noprofile;
import com.laytonsmith.annotations.seealso;
import com.laytonsmith.core.Documentation;
import com.laytonsmith.core.LogLevel;
import com.laytonsmith.core.ParseTree;
import com.laytonsmith.core.Script;
import com.laytonsmith.core.SimpleDocumentation;
import com.laytonsmith.core.compiler.FileOptions;
import com.laytonsmith.core.compiler.analysis.Scope;
import com.laytonsmith.core.compiler.analysis.StaticAnalysis;
import com.laytonsmith.core.compiler.signature.FunctionSignatures;
import com.laytonsmith.core.constructs.Auto;
import com.laytonsmith.core.constructs.CArray;
import com.laytonsmith.core.constructs.CClassType;
import com.laytonsmith.core.constructs.CClosure;
import com.laytonsmith.core.constructs.CFunction;
import com.laytonsmith.core.constructs.CString;
import com.laytonsmith.core.constructs.CVoid;
import com.laytonsmith.core.constructs.IVariable;
import com.laytonsmith.core.constructs.IVariableList;
import com.laytonsmith.core.constructs.LeftHandSideType;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.constructs.generics.GenericParameters;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import com.laytonsmith.core.exceptions.ConfigCompileGroupException;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.natives.interfaces.Mixed;
import com.laytonsmith.core.snapins.PackagePermission;
import com.laytonsmith.tools.docgen.DocGenTemplates;
import com.laytonsmith.tools.docgen.DocGenTemplates.Generator.GenerateException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.TreeSet;

/**
 *
 */
public abstract class AbstractFunction implements Function {

	private boolean shouldProfile = true;
	private FunctionSignatures cachedFunctionSignatures = null;

	protected AbstractFunction() {
		//If we have the noprofile annotation, cache that we don't want to profile.
		shouldProfile = !this.getClass().isAnnotationPresent(noprofile.class);
	}

	/**
	 * {@inheritDoc}
	 *
	 * By default, we return CVoid.
	 *
	 * @param t
	 * @param env
	 * @param parent
	 * @param nodes
	 * @return
	 */
	@Override
	public Mixed execs(Target t, Environment env, Script parent, GenericParameters generics, ParseTree... nodes) {
		return CVoid.VOID;
	}

	private Set<Function> nagAlert = new TreeSet<>();

	@Override
	public Mixed exec(Target t, Environment env, GenericParameters generics, Mixed... args) throws ConfigRuntimeException {
		boolean hasOld = ReflectionUtils.hasMethod(this.getClass(), "exec", null,
				new Class[]{Target.class, Environment.class, Mixed[].class});
		if(hasOld) {
			if(!nagAlert.contains(this)) {
				System.err.println("The extension function " + this.getName() + " is implementing an old version of the exec"
						+ " method. This extension WILL break in the future, and needs code changes. Please alert"
						+ " the author.");
				nagAlert.add(this);
			}
			try {
				return (Mixed) ReflectionUtils.invokeMethod(this.getClass(), this, "exec",
						new Class[]{Target.class, Environment.class, Mixed[].class},
						new Object[]{t, env, args});
			} catch(ReflectionUtils.ReflectionException ex) {
				if(ex.getCause() instanceof InvocationTargetException ite) {
					if(ite.getCause() instanceof ConfigRuntimeException cre) {
						// throw this one normally, so the rest of the framework can react accordingly
						throw cre;
					}
				}
				throw ex;
			}
		}
		// It doesn't have the old method, nor does it override this method.
		throw new Error(this.getClass() + " does not properly implement the exec method.");
	}

	/**
	 * {@inheritDoc}
	 * Calling {@link #getCachedSignatures()} where possible is preferred for runtime performance.
	 */
	@Override
	public FunctionSignatures getSignatures() {
		return null;
	}

	/**
	 * Gets the function's signatures from the cache, or through {@link #getSignatures()} if they have not been cached.
	 * The signatures will be cached in this second case.
	 * Do NOT call this method from {@link #getSignatures()}.
	 * @return This function's signatures.
	 */
	public FunctionSignatures getCachedSignatures() {
		if(this.cachedFunctionSignatures == null) {
			this.cachedFunctionSignatures = this.getSignatures();
		}
		return this.cachedFunctionSignatures;
	}

	/**
	 * {@inheritDoc} By default, {@link CClassType#AUTO} is returned.
	 */
	@Override
	public LeftHandSideType getReturnType(ParseTree node, Target t, List<LeftHandSideType> argTypes,
			List<Target> argTargets, LeftHandSideType inferredReturnType,
			Environment env, Set<ConfigCompileException> exceptions) {

		// Match arguments to function signatures if available.
		FunctionSignatures signatures = this.getCachedSignatures();
		if(signatures != null) {
			return signatures.getReturnType(node, t, argTypes, argTargets, inferredReturnType, env, exceptions);
		}

		// No information is available about the return type.
		return CClassType.AUTO.asLeftHandSideType();
	}

	@Override
	public List<LeftHandSideType> getResolvedParameterTypes(StaticAnalysis analysis,
			Target t, Environment env, GenericParameters generics,
			LeftHandSideType inferredReturnType, List<ParseTree> children) {
//		FunctionSignatures signatures = getCachedSignatures();
		List<LeftHandSideType> ret = new ArrayList<>(children.size());
		if(generics != null) {
			// Explicit parameters were provided, just use those.
			return generics.getLeftHandParameters();
		}
		for(ParseTree child : children) {
			ret.add(child.getDeclaredType(analysis, env, Auto.LHSTYPE));
		}
		return ret;
//		if(signatures == null) {
//			return ret;
//		} else {
//			List<FunctionSignature> matches = new ArrayList<>();
//			for(FunctionSignature s : signatures.getSignatures()) {
//				if(s.matches(ret, generics, env, inferredReturnType, false)) {
//					matches.add(s);
//				}
//			}
//			if(matches.size() != 1) {
//				throw new CRECastException("Cannot infer argument types for " + getName() + ", "
//						+ (matches.isEmpty()
//								? "no signature matched." : "multiple signatures matched.")
//						+ " Provide explicit type parameters, or use an", t);
//			}
//			FunctionSignature s = matches.get(0);
//			for(Param p : s.getParams()) {
//				if(p.getType() == null) {
//					ret.add(null);
//				} else {
//					if(p.getType().isTypeName()) {
//
//					} else {
//						ret.add(p.getType());
//					}
//				}
//			}
//		}
//		return ret;
	}

	/**
	 * {@inheritDoc} By default, this calls {@link StaticAnalysis#typecheck(ParseTree, Set)} on the function's arguments
	 * and passes them to {@link #getReturnType(Target, List, List, Set)} to get this function's return type.
	 */
	@Override
	public LeftHandSideType typecheck(StaticAnalysis analysis,
			ParseTree ast, LeftHandSideType inferredReturnType,
			Environment env, Set<ConfigCompileException> exceptions) {
		try {
			// Get and check the types of the function's arguments.
			List<ParseTree> children = ast.getChildren();
			List<LeftHandSideType> argTypes = new ArrayList<>(children.size());
			List<Target> argTargets = new ArrayList<>(children.size());
			for(ParseTree child : children) {
				LeftHandSideType inferredParameterType = child.getDeclaredType(analysis, env, Auto.LHSTYPE);
				inferredParameterType = LeftHandSideType.resolveTypeFromGenerics(Target.UNKNOWN, env, inferredParameterType, null, null, (Map) null);
				argTypes.add(analysis.typecheck(child, inferredParameterType, env, exceptions));
				argTargets.add(child.getTarget());
			}
			// Return the return type of this function.
			return this.getReturnType(ast, ast.getTarget(),
					argTypes, argTargets, inferredReturnType, env, exceptions);
		} catch(RuntimeException t) {
			// We can't recover from this, but at least we can give a more useful error message
			String e = "While typechecking " + this.getName() + ", the attached Throwable occurred. This was"
					+ " associated with the code at or around " + ast.getTarget() + ": " + t.getMessage();
			throw new RuntimeException(e, t);
		}
	}

	/**
	 * {@inheritDoc} By default, the parent scope is passed to the first child, the result is passed to the second
	 * child, etc. This method returns the scope as returned by the last child, or the parent scope if it does not have
	 * children.
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

	/**
	 * Functions that use lazy evaluation where the first argument is always evaluated, and later arguments might not be
	 * evaluated depending on the outcome of previous arguments.
	 *
	 * @param analysis - The {@link StaticAnalysis}.
	 * @param parentScope - The current scope.
	 * @param ast - The abstract syntax tree representing this function.
	 * @param env - The environment.
	 * @param exceptions - A set to put compile errors in.
	 * @return The new (linked) scope from the first argument, or the parent scope if no arguments are available or if
	 * this function does not require a new scope.
	 */
	protected Scope linkScopeLazy(StaticAnalysis analysis, Scope parentScope,
			ParseTree ast, Environment env, Set<ConfigCompileException> exceptions) {
		if(ast.numberOfChildren() >= 1) {

			// Get this lazy function's name.
			String funcName = ((CFunction) ast.getData()).val();

			// Create a stack with the function's arguments.
			Stack<ParseTree> argsStack = new Stack<>();
			for(int i = ast.numberOfChildren() - 1; i >= 0; i--) {
				argsStack.push(ast.getChildAt(i));
			}

			// Handle each argument, unfolding children that represent the same lazy function.
			// This essentially mimics optimization and(and(a, b), c) -> and(a, b, c).
			// Omitting this optimization causes 'c' not to be able to resolve in 'b' its scope.
			Scope firstArgScope = null;
			Scope lastArgScope = null;
			while(!argsStack.empty()) {
				ParseTree arg = argsStack.pop();

				// Unfold children that represent the same lazy function.
				if(arg.getData() instanceof CFunction && ((CFunction) arg.getData()).val().equals(funcName)) {
					for(int i = arg.numberOfChildren() - 1; i >= 0; i--) {
						argsStack.push(arg.getChildAt(i));
					}
					continue;
				}

				// Handle 'normal' argument. Order: arg1 -> ((arg2? -> arg3?) -> ...) (lazy evaluation).
				if(firstArgScope == null) {
					firstArgScope = analysis.linkScope(parentScope, arg, env, exceptions);
					lastArgScope = firstArgScope;
				} else {
					lastArgScope = analysis.linkScope(lastArgScope, arg, env, exceptions);
				}
			}
			return firstArgScope;
		}
		return parentScope;
	}

	/**
	 * By default, we return false, because most functions do not need this
	 *
	 * @return
	 */
	@Override
	public boolean useSpecialExec() {
		return false;
	}

	/**
	 * Most functions should show up in the normal documentation. However, if this function shouldn't show up in the
	 * documentation, it should mark itself with the @hide annotation.
	 *
	 * @return
	 */
	@Override
	public final boolean appearInDocumentation() {
		return this.getClass().getAnnotation(hide.class) == null;
	}

	@Override
	public ParseTree postParseRewrite(ParseTree ast, Environment env,
			Set<Class<? extends Environment.EnvironmentImpl>> envs, Set<ConfigCompileException> exceptions) {
		return null;
	}

	/**
	 * Just return null by default. Most functions won't get to this anyways, since canOptimize is returning false.
	 *
	 * @param t
	 * @param env
	 * @param args
	 * @return
	 * @throws com.laytonsmith.core.exceptions.ConfigCompileException
	 */
	public Mixed optimize(Target t, Environment env, Mixed... args) throws ConfigCompileException {
		return null;
	}

	/**
	 * It may be that a function can simply check for compile errors, but not optimize.In this case, it is appropriate
	 * to use this definition of optimizeDynamic, to return a value that will essentially make no changes, or in the
	 * case where it can optimize anyways, even if some values are undetermined at the moment.
	 *
	 * @param t
	 * @param env
	 * @param envs
	 * @param children
	 * @param fileOptions
	 * @return
	 * @throws com.laytonsmith.core.exceptions.ConfigCompileException
	 * @throws com.laytonsmith.core.exceptions.ConfigCompileGroupException
	 */
	public ParseTree optimizeDynamic(Target t, Environment env,
			Set<Class<? extends Environment.EnvironmentImpl>> envs, List<ParseTree> children, FileOptions fileOptions)
			throws ConfigCompileException, ConfigRuntimeException, ConfigCompileGroupException {
		return null;
	}

	/**
	 * Most functions don't need the varlist.
	 *
	 * @param varList
	 */
	public void varList(IVariableList varList) {
	}

	/**
	 * Most functions want the atomic values, not the variable itself.
	 *
	 * @return
	 */
	@Override
	public boolean preResolveVariables() {
		return true;
	}

	@Override
	public ExampleScript[] examples() throws ConfigCompileException {
		return null;
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
		StringBuilder b = new StringBuilder();
		boolean first = true;
		for(Mixed ccc : args) {
			if(!first) {
				b.append(", ");
			}
			first = false;
			if(ccc instanceof CArray || ccc.isInstanceOf(CArray.TYPE, null, env)) {
				//Arrays take too long to toString, so we don't want to actually toString them here if
				//we don't need to.
				b.append("<arrayNotShown size:").append(((CArray) ccc).size(env)).append(">");
			} else if(ccc instanceof CClosure || ccc.isInstanceOf(CClosure.TYPE, null, env)) {
				//The toString of a closure is too long, so let's not output them either.
				b.append("<closureNotShown>");
			} else if(ccc instanceof CString || ccc.isInstanceOf(CString.TYPE, null, env)) {
				String val = ccc.val().replace("\\", "\\\\").replace("'", "\\'");
				int max = 1000;
				if(val.length() > max) {
					val = val.substring(0, max) + "... (" + (val.length() - max) + " more characters hidden)";
				}
				b.append("'").append(val).append("'");
			} else if(ccc instanceof IVariable) {
				b.append(((IVariable) ccc).getVariableName());
			} else {
				b.append(ccc.val());
			}
		}
		return "Executing function: " + this.getName() + "(" + b.toString() + ")";
	}

	/**
	 * Returns the documentation for this function that is provided as an external resource. This is useful for
	 * functions that have especially long or complex documentation, and adding it as a string directly in code would be
	 * cumbersome.
	 *
	 * @return
	 */
	protected String getBundledDocs() {
		try {
			return getBundledDocs(null);
		} catch (GenerateException ex) {
			// This condition is impossible, so we just ignore this case.
			return "";
		}
	}

	/**
	 * Returns the documentation for this function that is provided as an external resource. This is useful for
	 * functions that have especially long or complex documentation, and adding it as a string directly in code would be
	 * cumbersome. To facilitate dynamic docs, templates can be provided, which will be replaced for you.
	 *
	 * @param map
	 * @throws GenerateException If the templates cannot be properly parsed
	 * @return
	 */
	protected String getBundledDocs(Map<String, DocGenTemplates.Generator> map) throws GenerateException {
		String template = StreamUtils.GetString(AbstractFunction.class.getResourceAsStream("/functionDocs/"
				+ getName()));
		if(map == null) {
			map = new HashMap<>();
		}
		return DocGenTemplates.DoTemplateReplacement(template, map);
	}

	protected <T extends Enum<?> & SimpleDocumentation> String createEnumTable(Class<T> c) {
		StringBuilder b = new StringBuilder();
		MEnum me = c.getAnnotation(MEnum.class);
		String title;
		if(me == null) {
			title = c.getSimpleName();
		} else {
			title = me.value();
		}
		b.append("<br>'''").append(title).append("'''<br>\n");
		b.append("{|\n");
		b.append("|-\n! Name\n! Docs\n! Since\n");
		Enum[] elist = c.getEnumConstants();
		for(Enum e : elist) {
			SimpleDocumentation d = (SimpleDocumentation) e;
			b.append("|-\n")
					.append("| ").append(d.getName()).append("\n")
					.append("| ").append(d.docs()).append("\n")
					.append("| ").append(d.since()).append("\n");
		}
		b.append("|}\n");
		return b.toString();
	}

	@Override
	public String profileMessageS(List<ParseTree> args) {
		return "Executing function: " + this.getName() + "(<" + args.size() + " child"
				+ (args.size() == 1 ? "" : "ren") + " not shown>)";
	}

	@Override
	public PackagePermission getPermission() {
		return PackagePermission.NO_PERMISSIONS_NEEDED;
	}

	@Override
	public URL getSourceJar() {
		return ClassDiscovery.GetClassContainer(this.getClass());
	}

	private static final Class[] EMPTY_CLASS = new Class[0];

	/**
	 * Checks for the &#64;seealso annotation on this class, and returns the value listed there. This is to prevent
	 * subclasses from inheriting the list from super classes.
	 *
	 * @return
	 */
	@Override
	public final Class<? extends Documentation>[] seeAlso() {
		seealso see = this.getClass().getAnnotation(seealso.class);
		if(see == null) {
			return EMPTY_CLASS;
		} else {
			return see.value();
		}
	}

	@Override
	public final boolean isCore() {
		Class c = this.getClass();
		do {
			if(c.getAnnotation(core.class) != null) {
				return true;
			}
			c = c.getDeclaringClass();
		} while(c != null);
		return false;
	}

	public void link(Target t, List<ParseTree> children) throws ConfigCompileException {
		// Do nothing, as a default
	}

	@Override
	public int compareTo(Function o) {
		return this.getName().compareTo(o.getName());
	}

	public Set<ParseTree> getDocumentLinks(List<ParseTree> children) {
		Set<ParseTree> files = new HashSet<>();
		DocumentLink documentLink = this.getClass().getAnnotation(DocumentLink.class);
		if(documentLink != null && this instanceof DocumentLinkProvider) {
			for(int i : documentLink.value()) {
				if(children.size() >= i) {
					files.add(children.get(i));
				}
			}
		} else {
			throw new Error(this.getClass() + " is not tagged with the DocumentLink annotation, or does not"
					+ " implement DocumentLinkProvider, and this method cannot be called on it.");
		}
		return files;
	}

}
