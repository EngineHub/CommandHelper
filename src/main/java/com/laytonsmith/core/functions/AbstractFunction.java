package com.laytonsmith.core.functions;

import com.laytonsmith.PureUtilities.ClassLoading.ClassDiscovery;
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
import com.laytonsmith.core.constructs.CArray;
import com.laytonsmith.core.constructs.CClassType;
import com.laytonsmith.core.constructs.CClosure;
import com.laytonsmith.core.constructs.CFunction;
import com.laytonsmith.core.constructs.CString;
import com.laytonsmith.core.constructs.CVoid;
import com.laytonsmith.core.constructs.IVariable;
import com.laytonsmith.core.constructs.IVariableList;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.natives.interfaces.Mixed;
import com.laytonsmith.core.snapins.PackagePermission;
import com.laytonsmith.tools.docgen.DocGenTemplates;
import com.laytonsmith.tools.docgen.DocGenTemplates.Generator.GenerateException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

/**
 *
 */
public abstract class AbstractFunction implements Function {

	private boolean shouldProfile = true;

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
	public Mixed execs(Target t, Environment env, Script parent, ParseTree... nodes) {
		return CVoid.VOID;
	}

	/**
	 * {@inheritDoc}
	 * By default, {@link CClassType#AUTO} is returned.
	 */
	@Override
	public CClassType getReturnType(Target t, List<CClassType> argTypes,
			List<Target> argTargets, Environment env, Set<ConfigCompileException> exceptions) {
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

	/**
	 * Functions that use lazy evaluation where the first argument is always evaluated, and later arguments might not
	 * be evaluated depending on the outcome of previous arguments.
	 * @param analysis - The {@link StaticAnalysis}.
	 * @param parentScope - The current scope.
	 * @param ast - The abstract syntax tree representing this function.
	 * @param env - The environment.
	 * @param exceptions - A set to put compile errors in.
	 * @return The new (linked) scope from the first argument, or the parent scope if no arguments are available or
	 * if this function does not require a new scope.
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
	 * It may be that a function can simply check for compile errors, but not optimize. In this case, it is appropriate
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
	 */
	public ParseTree optimizeDynamic(Target t, Environment env,
			Set<Class<? extends Environment.EnvironmentImpl>> envs, List<ParseTree> children, FileOptions fileOptions)
			throws ConfigCompileException, ConfigRuntimeException {
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
	public String profileMessage(Mixed... args) {
		StringBuilder b = new StringBuilder();
		boolean first = true;
		for(Mixed ccc : args) {
			if(!first) {
				b.append(", ");
			}
			first = false;
			if(ccc.isInstanceOf(CArray.TYPE)) {
				//Arrays take too long to toString, so we don't want to actually toString them here if
				//we don't need to.
				b.append("<arrayNotShown size:").append(((CArray) ccc).size()).append(">");
			} else if(ccc.isInstanceOf(CClosure.TYPE)) {
				//The toString of a closure is too long, so let's not output them either.
				b.append("<closureNotShown>");
			} else if(ccc.isInstanceOf(CString.TYPE)) {
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
