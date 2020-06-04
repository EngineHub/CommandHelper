package com.laytonsmith.core.compiler.analysis;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Stack;

import com.laytonsmith.core.ParseTree;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.constructs.CClassType;
import com.laytonsmith.core.constructs.CFunction;
import com.laytonsmith.core.constructs.CNull;
import com.laytonsmith.core.constructs.CString;
import com.laytonsmith.core.constructs.CVoid;
import com.laytonsmith.core.constructs.IVariable;
import com.laytonsmith.core.constructs.InstanceofUtil;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.constructs.Variable;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import com.laytonsmith.core.exceptions.CRE.CREException;
import com.laytonsmith.core.functions.DataHandling;
import com.laytonsmith.core.functions.Function;
import com.laytonsmith.core.functions.IncludeCache;
import com.laytonsmith.core.natives.interfaces.Mixed;
import java.io.IOException;

/**
 * This class can be used to perform static analysis.
 * @author P.J.S. Kools
 */
public class StaticAnalysis {

	private static final StaticAnalysisConfiguration CONFIGURATION;

	static {
		try {
			CONFIGURATION = StaticAnalysisConfiguration.GetConfiguration();
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}

	private final Scope startScope;
	private final Set<Scope> scopes;
	private final boolean isMainAnalysis;
	private ParseTree astRootNode = null;
	private Scope endScope = null;

	private Scope globalScope = null;

	/**
	 * Contains all {@link StaticAnalysis} objects that have been created for this analysis, including this analysis.
	 * This is one analysis per file, which can be used to traverse each file again with a full scope graph.
	 */
	private Set<StaticAnalysis> staticAnalyses = new HashSet<>();

	/**
	 * Contains the {@link Scope} object belonging to each AST node.
	 * Should only contain AST nodes within the file for which analysis was created (excluding includes).
	 */
	private Map<ParseTree, Scope> astScopeMap = new HashMap<>();

	private static StaticAnalysis autoIncludesAnalysis = null;

	/**
	 * Creates a new {@link StaticAnalysis}.
	 * @param isMainAnalysis - If {@code true}, full analyses will be performed with auto includes if present.
	 * If {@code false}, only the scope graph will be generated and a full analysis is expected to be done externally.
	 */
	public StaticAnalysis(boolean isMainAnalysis) {
		this(null, isMainAnalysis);
	}

	/**
	 * Creates a new {@link StaticAnalysis} with a custom {@link Scope} in which this file's start scope can perform
	 * lookups.
	 * @param parentScope - The {@link Scope} in which this file's start scope can perform lookups.
	 * @param isMainAnalysis - If {@code true}, full analyses will be performed with auto includes if present.
	 * If {@code false}, only the scope graph will be generated and a full analysis is expected to be done externally.
	 */
	public StaticAnalysis(Scope parentScope, boolean isMainAnalysis) {
		this.startScope = (parentScope != null ? parentScope : new Scope());
		this.scopes = new HashSet<>();
		this.scopes.add(this.startScope);
		this.isMainAnalysis = isMainAnalysis;
		this.staticAnalyses.add(this);
	}

	private StaticAnalysis(Scope startScope, Scope endScope, Set<Scope> scopes,
			boolean isMainAnalysis, Scope globalScope, ParseTree astRootNode,
			Set<StaticAnalysis> staticAnalyses, Map<ParseTree, Scope> astScopeMap) {
		this.startScope = startScope;
		this.endScope = endScope;
		this.scopes = scopes;
		this.isMainAnalysis = isMainAnalysis;
		this.globalScope = globalScope;
		this.astRootNode = astRootNode;
		this.staticAnalyses = staticAnalyses;
		this.staticAnalyses.add(this);
		this.astScopeMap = astScopeMap;
	}

	/**
	 * Starts analysis on the given AST. If this is a main analysis, full analysis is performed with auto includes if
	 * available. Otherwise, only the scope graph will be generated and a full analysis is expected to be done
	 * externally.
	 * @param ast - The {@link ParseTree} to analze.
	 * @param env - The {@link Environment}.
	 * @param envs - The set of expected {@link EnvironmentImpl} classes to be available at runtime.
	 * @param exceptions - Any compile exceptions will be added to this set.
	 */
	public void analyze(ParseTree ast, Environment env,
			Set<Class<? extends Environment.EnvironmentImpl>> envs, Set<ConfigCompileException> exceptions) {

		// Don't perform static analysis if it's disabled.
		if(!enabled()) {
			return;
		}

		// Clear scopes from previous analysis.
		this.scopes.clear();
		this.scopes.add(this.startScope);

		// Store new AST.
		this.astRootNode = ast;

		// Handle auto includes if present.
		if(autoIncludesAnalysis != null) {
			if(this.isMainAnalysis) {
				this.startScope.addParent(autoIncludesAnalysis.endScope);
				this.staticAnalyses.addAll(autoIncludesAnalysis.staticAnalyses);
			}
			this.globalScope = autoIncludesAnalysis.globalScope;
		}

		// Pass the start scope to the root node, allowing it to adjust the scope graph.
		this.endScope = linkScope(this.startScope, ast, env, exceptions);

		// Handle include references and analyze the final scope graph if this is the main analysis.
		if(this.isMainAnalysis) {
			this.handleIncludeRefs(env, envs, exceptions);
			this.analyzeFinalScopeGraph(env, exceptions);
		}
	}

	/**
	 * Sets and analyzes the given list of auto includes which will be used when performing main analyses.
	 * This works by creating a scope graph that represents a file with an {@code include()} call for each auto include
	 * in the provided order. Both this 'fake' file and future analyses will be able to lookup procedures in these
	 * auto includes. Main analyses can also use variables that are available at the end of these auto includes.
	 * @param autoIncludes - The list of auto include files.
	 * @param env - The {@link Environment}.
	 * @param envs - The set of expected {@link EnvironmentImpl} classes to be available at runtime.
	 * @param exceptions - Any compile exceptions will be added to this set.
	 */
	public static void setAndAnalyzeAutoIncludes(List<File> autoIncludes, Environment env,
			Set<Class<? extends Environment.EnvironmentImpl>> envs, Set<ConfigCompileException> exceptions) {

		// Don't perform static analysis if it's disabled.
		if(!enabled()) {
			return;
		}

		// Clear previous auto includes analysis and return since there are no auto includes.
		if(autoIncludes == null || autoIncludes.size() == 0) {
			autoIncludesAnalysis = null;
			return;
		}

		// Create scope graph with an include for each auto include.
		// This fakes the scope graph for a script with an include for each auto include file.
		Scope startScope = new Scope();
		StaticAnalysis analysis = new StaticAnalysis(startScope, true);
		analysis.staticAnalyses.remove(analysis); // Remove itself from analyses, as it isn't actually a file analysis.
		Scope inScope = startScope;
		for(File autoInclude : autoIncludes) {
			Scope outScope = analysis.createNewScope();
			inScope.addReference(new IncludeReference(
					autoInclude.getAbsolutePath(), inScope, outScope, Target.UNKNOWN));
			inScope = outScope;
		}
		analysis.endScope = inScope;
		analysis.globalScope = analysis.endScope;

		// Perform static analysis on the created script.
		analysis.handleIncludeRefs(env, envs, exceptions);
		analysis.analyzeFinalScopeGraph(env, exceptions);

		// Store the new analysis.
		autoIncludesAnalysis = analysis;
	}

	private void analyzeFinalScopeGraph(Environment env, Set<ConfigCompileException> exceptions) {

		// Convert ivariable assign declarations into variable references or declarations.
		for(Scope scope : this.scopes) {
			for(Declaration decl : scope.getAllDeclarationsLocal(Namespace.IVARIABLE_ASSIGN)) {

				// Attempt to find ivariable declaration or another yet unclassified ivariable assign (excluding this).
				// TODO - Do this for every code path, since if any path doesn't have a declaration, this should be it.
				boolean declarationFound = !scope.getDeclarations(Namespace.IVARIABLE, decl.getIdentifier()).isEmpty()
						|| scope.getReachableDeclarations(Namespace.IVARIABLE_ASSIGN, decl.getIdentifier()).size() > 1;

				// Create variable declaration or reference for this ivariable assign.
				if(declarationFound) {
					scope.addReference(new Reference(Namespace.IVARIABLE, decl.getIdentifier(), decl.getTarget()));
				} else {
					scope.addDeclaration(new Declaration(
							Namespace.IVARIABLE, decl.getIdentifier(), CClassType.AUTO, decl.getTarget()));
				}
			}
		}

		// Generate compile error for duplicate ivariable declarations.
		for(Scope scope : this.scopes) {
			for(Declaration decl : scope.getAllDeclarationsLocal(Namespace.IVARIABLE)) {
				if(decl instanceof ParamDeclaration) {
					continue; // Allow parameter declarations to shadow previous declarations.
				}
				Set<Declaration> dupDecls = scope.getReachableDeclarations(Namespace.IVARIABLE, decl.getIdentifier());
				if(dupDecls.size() > 1) {
					dupDecls.remove(decl);
					// TODO - Generate only one exception with all targets in them.
					// TODO - Consider getting the earliest declaration only (instead of the last of each code path).
					for(Declaration dupDecl : dupDecls) {
						exceptions.add(new ConfigCompileException("Duplicate variable declaration: Variable "
								+ decl.getIdentifier() + " is already declared at "
								+ dupDecl.getTarget().toString(), decl.getTarget()));
					}
				}
			}
		}

		// TODO - Remove if not useful anymore. MS allows proc overrides.
//		// Generate compile error for duplicate procedure declarations.
//		for(Scope scope : this.scopes) {
//			for(Declaration decl : scope.getAllDeclarationsLocal(Namespace.PROCEDURE)) {
//				if(decl instanceof ProcRootDeclaration) {
//					continue; // These are not actual proc declarations, so skip them.
//				}
//				Set<Declaration> dupDecls = scope.getReachableDeclarations(Namespace.PROCEDURE, decl.getIdentifier());
//				if(dupDecls.size() > 1) {
//					dupDecls.remove(decl);
//					// TODO - Generate only one exception with all targets in them.
//					// TODO - Consider getting the earliest declaration only (instead of the last of each code path).
//					for(Declaration dupDecl : dupDecls) {
//						exceptions.add(new ConfigCompileException("Duplicate procedure declaration: Procedure "
//								+ decl.getIdentifier() + " is already declared at "
//								+ dupDecl.getTarget().toString(), decl.getTarget()));
//					}
//				}
//			}
//		}

		// Resolve procedure references or add them as required-before-usage to the surrounding procedure.
		Map<Scope, Set<Reference>> procReqRefMap = new HashMap<>();
		for(Scope scope : this.scopes) {
			for(Reference ref : scope.getAllReferencesLocal(Namespace.PROCEDURE)) {
				Set<Declaration> decls = scope.getDeclarations(Namespace.PROCEDURE, ref.getIdentifier());
				if(decls.isEmpty() && this.globalScope != null) {
					decls = this.globalScope.getDeclarations(Namespace.PROCEDURE, ref.getIdentifier());
				}
				if(decls.isEmpty()) {

					// Procedure not found. Add reference as requirement to surrounding proc if available.
					Set<Declaration> rootDecls = scope.getDeclarations(
							Namespace.PROCEDURE, ProcRootDeclaration.PROC_ROOT);
					if(!rootDecls.isEmpty()) {
						for(Declaration decl : rootDecls) {
							((ProcRootDeclaration) decl).procDecl.addRequiredReference(ref);
						}
					} else {

						// Procedure cannot be resolved and is not within another procedure.
						exceptions.add(new ConfigCompileException(
								"Procedure cannot be resolved: " + ref.getIdentifier(), ref.getTarget()));
					}
				} else {

					// Store the (still empty) required references set.
					Declaration decl = decls.iterator().next();
					procReqRefMap.put(scope, ((ProcDeclaration) decl).getRequiredRefs());
				}
			}
		}

		// Resolve required procedure references from the place where the proc that requires them is called.
		// There is no need to look up in the global scope here, as then the references would have resolved already.
		for(Entry<Scope, Set<Reference>> entry : procReqRefMap.entrySet()) {
			Scope scope = entry.getKey();
			for(Reference ref : entry.getValue()) {
				if(scope.getDeclarations(Namespace.PROCEDURE, ref.getIdentifier()).isEmpty()) {
					exceptions.add(new ConfigCompileException(
							"Procedure cannot be resolved: " + ref.getIdentifier(), ref.getTarget()));
				}
			}
		}

		// Type check.
		this.typecheck(env, exceptions);
	}

	private void typecheck(Environment env, Set<ConfigCompileException> exceptions) {
		for(StaticAnalysis analysis : this.staticAnalyses) {
			analysis.typecheck(analysis.astRootNode, env, exceptions);
		}
	}

	/**
	 * Traverses the parse tree, type checking functions through their
	 * {@link Function#typecheck(StaticAnalysis, ParseTree, Environment, Set)} methods.
	 * When {@link IVariable}s are traversed, a compile error is added when they do not resolve to a declaration.
	 * @param ast - The parse tree.
	 * @param env - The {@link Environment}, used for instanceof checks on types.
	 * @param exceptions - Any compile exceptions will be added to this set.
	 * @return The return type of the parse tree.
	 */
	public CClassType typecheck(ParseTree ast, Environment env, Set<ConfigCompileException> exceptions) {
		Mixed node = ast.getData();
		if(node instanceof CFunction) {
			CFunction cFunc = (CFunction) node;
			if(cFunc.hasFunction()) {
				Function func = cFunc.getCachedFunction();
				if(func != null) {
					return func.typecheck(this, ast, env, exceptions);
				}
				return CClassType.AUTO; // Unknown return type.
			} else if(cFunc.hasIVariable()) { // The function is a var reference to a closure: '@myClosure(<args>)'.
				return CClassType.AUTO; // TODO - Get actual type (return type of closure, iclosure, rclosure?).
			} else if(cFunc.hasProcedure()) { // The function is a procedure reference.
				return CClassType.AUTO; // TODO - Get actual type.
			} else {
				throw new Error("Unsupported " + CFunction.class.getSimpleName()
						+ " type in type checking for node with value: " + cFunc.val());
			}
		} else if(node instanceof IVariable) {
			IVariable ivar = (IVariable) node;
			Scope scope = this.getTermScope(ast);
			if(scope != null) {
				Set<Declaration> decls = scope.getDeclarations(
						Namespace.IVARIABLE, ivar.getVariableName());
				if(decls.isEmpty()) {
					exceptions.add(new ConfigCompileException(
							"Variable cannot be resolved: " + ivar.getVariableName(), ivar.getTarget()));
					return CClassType.AUTO;
				} else {
					// TODO - Get the most specific type when multiple declarations exist.
					return decls.iterator().next().getType();
				}
			} else {
				// If this runs, then an IVariable reference was created without setting its Scope using setTermScope().
				exceptions.add(new ConfigCompileException("Variable cannot be resolved (missing variable scope, this is"
						+ " an internal error that should never happen): " + ivar.getVariableName(), ivar.getTarget()));
				return CClassType.AUTO;
			}
		} else if(node instanceof Variable) {
			return CString.TYPE; // $vars can only be strings.
		}

		// The node is some other Construct, so return its type.
		try {
			return node.typeof();
		} catch (Throwable t) {
			// Functions that might contain these unsupported objects should make sure that they don't type check them.
			// In case an unsupported object causes an error here, it likely means that we have a syntax error.
			exceptions.add(new ConfigCompileException("Unsupported AST node implementation in type checking: "
					+ node.getClass().getSimpleName(), node.getTarget()));
			return CClassType.AUTO;
		}
	}

	/**
	 * Checks whether the given type is instance of the expected type, adding a compile error to the passed
	 * exceptions set if it isn't. This never generates an error when the given type is {@link CClassType#AUTO}.
	 * @param type - The type to check.
	 * @param expected - The expected {@link CClassType}.
	 * @param t
	 * @param exceptions
	 */
	public static void requireType(CClassType type, CClassType expected,
			Target t, Environment env, Set<ConfigCompileException> exceptions) {

		// Handle types that cause exceptions in the InstanceofUtil isInstanceof check.
		if(type == expected || type == CClassType.AUTO || type == CNull.TYPE) {
			return;
		}
		if(type == CVoid.TYPE || expected == CVoid.TYPE || expected == CNull.TYPE) {
			exceptions.add(new ConfigCompileException("Expected type " + expected.getSimpleName()
					+ ", but received type " + type.getSimpleName() + " instead.", t));
			return;
		}

		// Handle 'normal' types.
		if(!InstanceofUtil.isInstanceof(type, expected, env)) {
			exceptions.add(new ConfigCompileException("Expected type " + expected.getSimpleName()
				+ ", but received type " + type.getSimpleName() + " instead.", t));
		}
	}

	/**
	 * Checks whether the given type is instance of any of the expected types, adding a compile error to the passed
	 * exceptions set if it isn't. This never generates an error when the given type is {@link CClassType#AUTO}.
	 * @param type - The type to check.
	 * @param expected - The expected {@link CClassType}s, which should always be of at least size 1.
	 * @param t
	 * @param exceptions
	 */
	public static void requireAnyType(CClassType type, CClassType[] expected,
			Target t, Environment env, Set<ConfigCompileException> exceptions) {
		assert expected.length > 0 : "You must at least provide one expected type to requireAnyType().";

		// Return if the type is instanceof any expected type.
		for(CClassType exp : expected) {
			if(type == exp || type == CClassType.AUTO || type == CNull.TYPE
					|| (type != CVoid.TYPE && exp != CVoid.TYPE && exp != CNull.TYPE)
					|| InstanceofUtil.isInstanceof(type, exp, env)) {
				return;
			}
		}

		// Add an exception since the type was not compatible.
		if(expected.length == 1) {
			exceptions.add(new ConfigCompileException("Expected type " + expected[0].getSimpleName()
					+ ", but received type " + type.getSimpleName() + " instead.", t));
		} else {
			String types = "";
			for(CClassType exp : expected) {
				types += (types.isEmpty() ? exp.getSimpleName() : ", " + exp.getSimpleName());
			}
			exceptions.add(new ConfigCompileException("Expected any of types {" + types
					+ "}, but received type " + type.getSimpleName() + " instead.", t));
		}
	}

	/**
	 * Checks whether the given AST node is an {@link IVariable}, adding a compile error to the passed
	 * exceptions set if it isn't.
	 * @param node - The AST node to check.
	 * @param t
	 * @param exceptions
	 * @return The {@link IVariable} if it was one, or {@code null} if it wasn't.
	 */
	public static IVariable requireIVariable(Mixed node, Target t, Set<ConfigCompileException> exceptions) {
		if(node instanceof IVariable) {
			return (IVariable) node;
		}
		exceptions.add(new ConfigCompileException("Expected ivariable "
				+ ", but received type " + node.getName() + " instead.", t));
		return null;
	}

	/**
	 * Checks whether the given AST node is an {@link CClassType}, adding a compile error to the passed
	 * exceptions set if it isn't.
	 * @param node - The AST node to check.
	 * @param t
	 * @param exceptions
	 * @return The {@link CClasType} if it was one, or {@code null} if it wasn't.
	 */
	public static CClassType requireClassType(Mixed node, Target t, Set<ConfigCompileException> exceptions) {
		if(node instanceof CClassType) {
			return (CClassType) node;
		}
		exceptions.add(new ConfigCompileException(
				"Expected classtype, but received type " + node.getName() + " instead.", t));
		return null;
	}

	private void handleIncludeRefs(Environment env,
			Set<Class<? extends Environment.EnvironmentImpl>> envs, Set<ConfigCompileException> exceptions) {

		// Compile and get all (in)direct includes.
		Set<IncludeReference> handledRefs = new HashSet<>();
		Set<IncludeReference> linkedRefs = new HashSet<>();
		this.compileIncludesLinkCycles(handledRefs, linkedRefs, new Stack<>(), env, envs, exceptions);

		// Create a set containing only unhandled references.
		Set<IncludeReference> unhandledRefs = new HashSet<>(handledRefs);
		unhandledRefs.removeAll(linkedRefs);

		// TODO - Remove assert and commented-out code when no longer needed. Scope linkage has moved elsewhere.
//		System.out.println("Handled refs size: " + handledRefs.size());
//		System.out.println("Linked refs size: " + linkedRefs.size());
		assert unhandledRefs.size() == 0 : "Unhandled references after compileIncludesLinkCycles() call.";

//		// Link all unhandled references.
//		/*
//		 * TODO - Should be done in order from children to parents.
//		 * This allows for linking of 'leaves' (nodes for which all refs have been handled) before cloning things.
//		 * When first cloning, include gaps are also cloned and still have to be handled.
//		 */
//		for(IncludeReference ref : unhandledRefs) {
//
//			// Get the static analysis of the include.
//			File file = Static.GetFileFromArgument(ref.getIdentifier(), env, ref.getTarget(), null);
//			StaticAnalysis includeAnalysis = IncludeCache.getStaticAnalysis(file, ref.getTarget());
//			if(includeAnalysis == null) {
//
//				// The include did not compile, so ignore the include entirely.
//				ref.getOutScope().setParent(ref.getInScope());
//			} else {
//
//				// Link this include reference to a clone of the scope graph.
//				// TODO - Make sure that this clone is linked first, as it otherwise remains unlinked.
//				// TODO - Actually clone includeAnalysis (or at least its scopes).
//				// TODO - Use addParent to allow multiple parents.
//				includeAnalysis.startScope.setParent(ref.getInScope());
//				ref.getOutScope().setParent(includeAnalysis.endScope);
//			}
//		}
	}

	/**
	 * Compiles and caches all used includes. Links the scope graphs of cyclic includes, including their children,
	 * but not their parents.
	 * @param handledRefs - Supply an empty set. Will contain all handled references.
	 * @param linkedRefs - Supply an empty set. Will contain all linked references
	 * (that are part of an already-linked cycle).
	 * @param path - Supply an empty stack. Will be empty when this method returns.
	 * @param env
	 * @param envs
	 * @param exceptions
	 * @return {@code true} if this depth-first traversal included a cycle, {@code false} otherwise (used internally).
	 */
	private boolean compileIncludesLinkCycles(Set<IncludeReference> handledRefs, Set<IncludeReference> linkedRefs,
			Stack<IncludeReference> path, Environment env, Set<Class<? extends Environment.EnvironmentImpl>> envs,
			Set<ConfigCompileException> exceptions) {
		boolean containsCycle = false; // TODO - Remove cycle-awareness if no longer used.
		for(IncludeReference includeRef : this.getIncludeRefs()) {

			// Directly link scope graphs of cyclic includes.
			if(path.contains(includeRef)) {
				containsCycle = true;
				Set<Scope> cycleScopes = new HashSet<>();
				List<StaticAnalysis> cycleAnalyses = new ArrayList<>();
				for(int i = path.size() - 1; i >= 0; i--) {
					IncludeReference pathRef = path.get(i);

					// Directly link the include if it has not been linked yet.
					if(linkedRefs.add(pathRef)) {

						// Get the static analysis of the include.
						File file = Static.GetFileFromArgument(
								pathRef.getIdentifier(), env, pathRef.getTarget(), null);
						StaticAnalysis includeAnalysis = IncludeCache.getStaticAnalysis(file);
						if(includeAnalysis == null) {

							// The include did not compile, so ignore the include entirely.
							this.addDirectedEdge(pathRef.getOutScope(), pathRef.getInScope());
						} else {
							cycleAnalyses.add(includeAnalysis);
							cycleScopes.addAll(includeAnalysis.scopes);

							// Directly link this include reference.
							this.addDirectedEdge(includeAnalysis.startScope, pathRef.getInScope());
							this.addDirectedEdge(pathRef.getOutScope(), includeAnalysis.endScope);

							// Store the include's analyses for later analysis.
							this.staticAnalyses.addAll(includeAnalysis.staticAnalyses);
						}
					}

					// Break when the cycle is handled.
					if(pathRef.equals(includeRef)) {
						break;
					}
				}

				// Update the scopes of all analyses in the cycle to be their union.
				for(StaticAnalysis analysis : cycleAnalyses) {
					analysis.scopes.addAll(cycleScopes);
				}

				// As this include reference is in a cycle, it has already been handled.
				continue;
			}

			// Skip already handled include references and mark the current reference a handled.
			if(!handledRefs.add(includeRef)) {
				continue;
			}

			// Resolve and compile the include.
			StaticAnalysis includeAnalysis;
			try {
				File file = Static.GetFileFromArgument(includeRef.getIdentifier(), env, includeRef.getTarget(), null);
				includeAnalysis = IncludeCache.getStaticAnalysis(file);
				if(includeAnalysis == null) {
					includeAnalysis = new StaticAnalysis(false);
					IncludeCache.get(file, env, envs, includeAnalysis, includeRef.getTarget());
					assert IncludeCache.getStaticAnalysis(file) != null
							: "Failed to cache include analysis.";
				}
			} catch (CREException e) {

				// Convert CREs into compile errors if there was a problem resolving or compiling a static include.
				// TODO - Split compilation such that we can use syntax-correct faulty includes anyways.
				exceptions.add(new ConfigCompileException(e.getMessage(), e.getTarget()));

				// Link directly and continue as there's no StaticAnalysis to handle.
				this.addDirectedEdge(includeRef.getOutScope(), includeRef.getInScope());
				linkedRefs.add(includeRef);
				continue;
			}

			// Skip the file if the analysis does not contain a complete scope graph.
			if(includeAnalysis.endScope == null) {

				// This case might not always cause an exception to be printed, so inform about the failed include.
				exceptions.add(new ConfigCompileException(
						"An error occurred while analyzing included file", includeRef.getTarget()));

				// Link directly and continue as there's no StaticAnalysis to handle.
				this.addDirectedEdge(includeRef.getOutScope(), includeRef.getInScope());
				linkedRefs.add(includeRef);
				continue;
			}

			// Recurse on the include's analysis.
			path.push(includeRef);
			boolean childContainsCycle = includeAnalysis.compileIncludesLinkCycles(
					handledRefs, linkedRefs, path, env, envs, exceptions);
			path.pop();

			// Clone and link the include analysis if it has not yet been handled, and mark it as handled.
			if(linkedRefs.add(includeRef)) {

				// Clone the include analysis and absorb its scopes into this analysis.
				StaticAnalysis includeAnalysisClone = includeAnalysis.clone();
				this.scopes.addAll(includeAnalysisClone.scopes);

				// Link the cloned include analysis scopes.
				this.addDirectedEdge(includeAnalysisClone.startScope, includeRef.getInScope());
				this.addDirectedEdge(includeRef.getOutScope(), includeAnalysisClone.endScope);

				// Store the include's analyses for later analysis.
				this.staticAnalyses.addAll(includeAnalysisClone.staticAnalyses);
			}

			// TODO - Remove commented-out code if no longer needed.
//			// Directly link the include reference if it does not contain a cycle.
//			// This links all includes from a cycle or non-cycle to non-cycle children.
//			// Includes that have a cycle as child should clone the cycle, as the cycle shouldn't be able to perform
//			// lookups in all parents.
//			// TODO - There are cases where cloning is needed, even when there's no cycle. Always clone?
//			if(childContainsCycle) {
//				containsCycle = true;
//
//				// Link the include reference to a clone if it links to a cycle it is not part of.
//				if(!linkedRefs.contains(includeRef)) {
//					// TODO - Clone includeAnalysis (or at least its scopes).
//					// TODO - Use addParent to allow multiple parents.
//					includeAnalysis.startScope.setParent(includeRef.getInScope());
//					includeRef.getOutScope().setParent(includeAnalysis.endScope);
//				}
//			} else {
//
//				// Directly link the include reference as it does not point to a cycle.
//				linkedRefs.add(includeRef);
//				// TODO - Use addParent to allow multiple parents.
//				includeAnalysis.startScope.setParent(includeRef.getInScope());
//				includeRef.getOutScope().setParent(includeAnalysis.endScope);
//			}
		}
		return containsCycle; // TODO - Remove return value if unused.
	}

	/**
	 * Gets all include references in this analysis.
	 * This traverses the scope graph starting from all root scopes, including the gaps left by include references.
	 * This should not be called on a cyclic scope graph.
	 * @return All include references in this analysis.
	 */
	private Set<IncludeReference> getIncludeRefs() {
		Set<IncludeReference> refs = new HashSet<>();
		for(Scope scope : this.scopes) {
			for(Reference ref : scope.getAllReferencesLocal(Namespace.INCLUDE)) {
				IncludeReference includeRef = (IncludeReference) ref;
				refs.add(includeRef);
			}
		}
		return refs;
	}

	/**
	 * Gets the scope at the end of the analyzed file.
	 * @return The end scope, or {@code null} if no analysis has been done.
	 */
	public Scope getEndScope() {
		return this.endScope;
	}

	/**
	 * If the given AST node is a {@link CFunction} containing a function:
	 * Calls {@link Function#linkScope(Scope, ParseTree, Set)} on the given AST node.
	 * If the given AST node is a {@link CFunction} containing a variable ("@c()" closure execution syntax):
	 * Generates a compile error if the variable cannot be resolved.
	 * If the given AST node is an {@link IVariable} variable reference, the variable's declared type is set to the
	 * type of the variable declaration it resolves to.
	 * If it does not resolve to a declaration, a compile error is generated.
	 * @param parentScope
	 * @param ast
	 * @param env
	 * @param exceptions - Any compile exceptions will be added to this set.
	 * @return The returned scope from {@link Function#linkScope(Scope, ParseTree, Set)} in the first case,
	 * or the parent scope otherwise.
	 */
	public Scope linkScope(Scope parentScope, ParseTree ast,
			Environment env, Set<ConfigCompileException> exceptions) {
		Mixed node = ast.getData();
		if(node instanceof CFunction) {
			CFunction cFunc = (CFunction) node;
			if(cFunc.hasFunction()) {
				Function func = cFunc.getCachedFunction();
				if(func != null) {
					return func.linkScope(this, parentScope, ast, env, exceptions);
				}
				return parentScope;
			} else if(cFunc.hasIVariable()) { // The function is a var reference to a closure: '@myClosure(<args>)'.

				// Add variable reference in a new scope.
				Scope refScope = this.createNewScope(parentScope);
				refScope.addReference(new Reference(Namespace.IVARIABLE, cFunc.val(), cFunc.getTarget()));
				this.setTermScope(ast, refScope);
				return refScope;
			} else if(cFunc.hasProcedure()) { // The function is a procedure reference.

				// Add procedure reference in a new scope.
				Scope refScope = this.createNewScope(parentScope);
				refScope.addReference(new Reference(Namespace.PROCEDURE, cFunc.val(), cFunc.getTarget()));

				// Handle the proc call arguments.
				Scope argScope = refScope;
				for(ParseTree child : ast.getChildren()) {
					argScope = linkScope(argScope, child, env, exceptions);
				}
				return argScope;
			} else {
				throw new Error("Unsupported " + CFunction.class.getSimpleName()
						+ " type in static analysis for node with value: " + cFunc.val());
			}
		} else if(node instanceof IVariable) {
			IVariable ivar = (IVariable) node;

			// Add variable reference in a new scope.
			Scope refScope = this.createNewScope(parentScope);
			refScope.addReference(new Reference(Namespace.IVARIABLE, ivar.getVariableName(), ivar.getTarget()));
			this.setTermScope(ast, refScope);
			return refScope;
		}
		return parentScope;
	}

	/**
	 * Handles parameter AST nodes, namely {@link IVariable}s or the {@code assign()} function (for typed parameters or
	 * parameters with a default value). The parameter is declared in a new scope that is chained to paramScope.
	 * If the parameter is typed and/or has a default value assigned to it,
	 * then that value will be handled in the valScope.
	 * @param paramScope - The scope to which a new scope is linked in which the declaration will be placed.
	 * @param valScope - The scope to which a new scope is linked in which the assigned value will be handled.
	 * @param ast
	 * @param env
	 * @param exceptions
	 * @return The resulting scopes in format {paramScope, valScope}.
	 */
	/*
	 *  TODO - Mark IVAR declarations as parameters, such that they can be shadowed and such that they can override
	 *  variable declarations. Otherwise this will lead to false duplicate declarations.
	 */
	public Scope[] linkParamScope(Scope paramScope, Scope valScope,
			ParseTree ast, Environment env, Set<ConfigCompileException> exceptions) {
		Mixed node = ast.getData();

		// Handle normal untyped parameter.
		if(node instanceof IVariable) { // Normal parameter.
			IVariable iVar = (IVariable) node;
			Scope newParamScope = this.createNewScope(paramScope);
			newParamScope.addDeclaration(new ParamDeclaration(
					iVar.getVariableName(), iVar.getDefinedType(), iVar.getTarget()));
			this.setTermScope(ast, newParamScope);
			return new Scope[] {newParamScope, valScope};
		}

		// Handle assign parameter (typed and/or with default value).
		if(node instanceof CFunction) { // Typed parameter or assign.
			Function func = ((CFunction) node).getCachedFunction();
			if(func != null && func instanceof DataHandling.assign) {
				return ((DataHandling.assign) func).linkParamScope(this, paramScope, valScope, ast, env, exceptions);
			}
		}

		// Handle non-parameter parameter. Fall back to handling the function's arguments.
		// TODO - Does this fallback make sense or should this term just be skipped?
		exceptions.add(new ConfigCompileException("Invalid parameter", node.getTarget()));
		return new Scope[] {paramScope, this.linkScope(valScope, ast, env, exceptions)};
	}

	public Scope createNewScope(Scope parent) {
		Scope scope = this.createNewScope();
		scope.addParent(parent);
		return scope;
	}

	public Scope createNewScope() {
		Scope scope = new Scope();
		this.scopes.add(scope);
		return scope;
	}

	public void addDirectedEdge(Scope child, Scope parent) {
		child.addParent(parent);
	}

	public void removeDirectedEdge(Scope child, Scope parent) {
		child.removeParent(parent);
	}

	public Set<Scope> getRootScopes() {
		Set<Scope> ret = new HashSet<>();
		for(Scope scope : this.scopes) {
			if(scope.getParents().size() == 0) {
				ret.add(scope);
			}
		}
		return ret;
	}

	public void setTermScope(ParseTree term, Scope scope) {
		this.astScopeMap.put(term, scope);
	}

	public Scope getTermScope(ParseTree term) {
		return this.astScopeMap.get(term);
	}

	/**
	 * Clones this {@link StaticAnalysis} including its scopes, but with shared declaration and reference links in
	 * these scopes. This method should only be called after all references and declarations have been added to the
	 * scope graph, and will no longer change. This also means that for example {@link IncludeReference}s still point
	 * to their original start and end scope, and not to the cloned ones.
	 */
	@Override
	public StaticAnalysis clone() {
		return this.clone(new HashMap<>(), true);
	}

	private StaticAnalysis clone(Map<Scope, Scope> cloneMapping, boolean cloneAnalyses) {
		Scope startScope = cloneScope(this.startScope, cloneMapping);
		Scope endScope = cloneScope(this.endScope, cloneMapping);
		Scope globalScope = cloneScope(this.globalScope, cloneMapping);

		Set<Scope> scopesClone = new HashSet<>();
		for(Scope scope : this.scopes) {
			scopesClone.add(cloneScope(scope, cloneMapping));
		}

		Set<StaticAnalysis> analysesClone = new HashSet<>();
		if(cloneAnalyses) {
			for(StaticAnalysis analysis : this.staticAnalyses) {
				if(analysis != this) {
					analysesClone.add(analysis.clone(cloneMapping, false));
				}
			}
		}

		Map<ParseTree, Scope> astScopeMap = new HashMap<>();
		for(Entry<ParseTree, Scope> entry : this.astScopeMap.entrySet()) {
			astScopeMap.put(entry.getKey(), cloneScope(entry.getValue(), cloneMapping));
		}

		return new StaticAnalysis(startScope, endScope, scopesClone,
				this.isMainAnalysis, globalScope, this.astRootNode, analysesClone, astScopeMap);
	}

	private static Scope cloneScope(Scope scope, Map<Scope, Scope> cloneMapping) {

		// Handle null scopes.
		if(scope == null) {
			return null;
		}

		// Return clone from the cache if it has already been cloned.
		Scope scopeClone = cloneMapping.get(scope);
		if(scopeClone != null) {
			return scopeClone;
		}

		// Create unlinked clone, updating the mapping from scopes to their clones.
		scopeClone = scope.shallowUnlinkedClone();
		cloneMapping.put(scope, scopeClone);

		// Clone the parents, using cached clones for already cloned parents.
		for(Scope parent : scope.getParents()) {
			scopeClone.addParent(cloneScope(parent, cloneMapping));
		}
		for(Entry<Namespace, Set<Scope>> entry : scope.getSpecificParents().entrySet()) {
			Namespace namespace = entry.getKey();
			for(Scope parent : entry.getValue()) {
				scopeClone.addSpecificParent(cloneScope(parent, cloneMapping), namespace);
			}
		}
		return scopeClone;
	}

	/**
	 * Returns whether static analysis is enabled or not.
	 * @return
	 * @deprecated This is a temporary method, it should be replaced with proper settings once static analysis is
	 * ready for release.
	 */
	@Deprecated
	public static boolean enabled() {
		return CONFIGURATION.globalEnable();
	}
}
