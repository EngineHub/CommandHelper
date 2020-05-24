package com.laytonsmith.core.compiler.analysis;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import com.laytonsmith.core.ParseTree;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.constructs.CClassType;
import com.laytonsmith.core.constructs.CFunction;
import com.laytonsmith.core.constructs.IVariable;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import com.laytonsmith.core.exceptions.CRE.CREException;
import com.laytonsmith.core.functions.DataHandling;
import com.laytonsmith.core.functions.Function;
import com.laytonsmith.core.functions.FunctionBase;
import com.laytonsmith.core.functions.FunctionList;
import com.laytonsmith.core.functions.IncludeCache;
import com.laytonsmith.core.natives.interfaces.Mixed;

/**
 * This class can be used to perform static analysis.
 * @author P.J.S. Kools
 */
public class StaticAnalysis {

	private final Scope startScope;
	private final Set<Scope> scopes;
	private final boolean isMainAnalysis;
	private Scope endScope = null;

	private List<File> autoIncludes = null;
	private Scope globalScope = null;

	public StaticAnalysis(boolean isMainAnalysis) {
		this(null, isMainAnalysis);
	}

	public StaticAnalysis(Scope parentScope, boolean isMainAnalysis) {
		this.startScope = (parentScope != null ? parentScope : new Scope());
		this.scopes = new HashSet<>();
		this.scopes.add(this.startScope);
		this.isMainAnalysis = isMainAnalysis;
	}

	private StaticAnalysis(Scope startScope, Scope endScope, Set<Scope> scopes, boolean isMainAnalysis) {
		this.startScope = startScope;
		this.endScope = endScope;
		this.scopes = scopes;
		this.isMainAnalysis = isMainAnalysis;
	}

	public void setAutoIncludes(List<File> autoIncludes) {
		this.autoIncludes = autoIncludes;
	}

	public void analyze(ParseTree ast, Environment env, Set<ConfigCompileException> exceptions) {

		// Clear scopes from previous analysis.
		this.scopes.clear();
		this.scopes.add(this.startScope);

		// Handle auto includes if present.
		Scope midScope = this.analyzeAutoIncludes(this.startScope, env, exceptions);

		// Pass the start scope to the root node, allowing it to adjust the scope graph.
		this.endScope = linkScope(midScope, ast, env, exceptions);

		// Handle include references and analyze the final scope graph if this is the main analysis.
		if(this.isMainAnalysis) {
			this.handleIncludeRefs(env, exceptions);
			this.analyzeFinalScopeGraph(exceptions);
		}
	}

	private Scope analyzeAutoIncludes(Scope parentScope, Environment env, Set<ConfigCompileException> exceptions) {

		// Clear global scope and return if no auto includes are set.
		if(this.autoIncludes == null || this.autoIncludes.size() == 0) {
			this.globalScope = null;
			return parentScope;
		}

		// Create scope graph with an include for each auto include.
		// This fakes the scope graph for a script with an include for each auto include file.
		// The hidden scope and the analysis below are not added to 'scopes' to prevent re-analysis.
		Scope hiddenStartScope = new Scope();
		hiddenStartScope.addParent(parentScope);
		StaticAnalysis analysis = new StaticAnalysis(hiddenStartScope, true);
		Scope inScope = hiddenStartScope;
		for(File autoInclude : this.autoIncludes) {
			Scope outScope = analysis.createNewScope();
			inScope.addReference(new IncludeReference(
					autoInclude.getAbsolutePath(), inScope, outScope, Target.UNKNOWN));
			inScope = outScope;
		}
		analysis.endScope = inScope;
		analysis.globalScope = analysis.endScope;

		// Perform static analysis on the created script.
		analysis.handleIncludeRefs(env, exceptions);
		analysis.analyzeFinalScopeGraph(exceptions);

		// Set the global scope.
		this.globalScope = analysis.endScope;

		// Return the end scope of the auto includes analysis.
		return analysis.endScope;
	}

	private void analyzeFinalScopeGraph(Set<ConfigCompileException> exceptions) {
		/*
		 *  TODO - Implement checks:
		 *  - Duplicate variable declarations.
		 *  - Duplicate proc declarations.
		 *  - Missing proc declaration.
		 *  Future: Add typechecking / set declared variable types for type checking.
		 */

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
//				System.out.println("[DEBUG] IVARIABLE decl: " + decl.getIdentifier() + " @ " + decl.getTarget()); // TODO - Remove debug.
				Set<Declaration> dupDecls = scope.getReachableDeclarations(Namespace.IVARIABLE, decl.getIdentifier());
//				System.out.println("[DEBUG] IVARIABLE numDecls: " + dupDecls.size()); // TODO - Remove debug.
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

		// Resolve variable references.
		for(Scope scope : this.scopes) {
			for(Reference ref : scope.getAllReferencesLocal(Namespace.IVARIABLE)) {
				if(scope.getDeclarations(Namespace.IVARIABLE, ref.getIdentifier()).isEmpty()) {
					exceptions.add(new ConfigCompileException(
							"Variable cannot be resolved: " + ref.getIdentifier(), ref.getTarget()));
				}
			}
		}

		// Generate compile error for duplicate procedure declarations.
		for(Scope scope : this.scopes) {
			for(Declaration decl : scope.getAllDeclarationsLocal(Namespace.PROCEDURE)) {
				Set<Declaration> dupDecls = scope.getReachableDeclarations(Namespace.PROCEDURE, decl.getIdentifier());
				if(dupDecls.size() > 1) {
					dupDecls.remove(decl);
					// TODO - Generate only one exception with all targets in them.
					// TODO - Consider getting the earliest declaration only (instead of the last of each code path).
					for(Declaration dupDecl : dupDecls) {
						exceptions.add(new ConfigCompileException("Duplicate procedure declaration: Procedure "
								+ decl.getIdentifier() + " is already declared at "
								+ dupDecl.getTarget().toString(), decl.getTarget()));
					}
				}
			}
		}

		// Resolve procedure references.
		for(Scope scope : this.scopes) {
			for(Reference ref : scope.getAllReferencesLocal(Namespace.PROCEDURE)) {
				if(scope.getDeclarations(Namespace.PROCEDURE, ref.getIdentifier()).isEmpty()
						&& (this.globalScope == null
						|| this.globalScope.getDeclarations(Namespace.PROCEDURE, ref.getIdentifier()).isEmpty())) {
					exceptions.add(new ConfigCompileException(
							"Procedure cannot be resolved: " + ref.getIdentifier(), ref.getTarget()));
				}
			}
		}
	}

	private void handleIncludeRefs(Environment env, Set<ConfigCompileException> exceptions) {

		// Compile and get all (in)direct includes.
		Set<IncludeReference> handledRefs = new HashSet<>();
		Set<IncludeReference> linkedRefs = new HashSet<>();
		this.compileIncludesLinkCycles(handledRefs, linkedRefs, new Stack<>(), env, exceptions);

		// Create a set containing only unhandled references.
		Set<IncludeReference> unhandledRefs = new HashSet<>(handledRefs);
		unhandledRefs.removeAll(linkedRefs);

		// TODO - Remove assert and commented-out code when no longer needed. Scope linkage has moved elsewhere.
		System.out.println("Handled refs size: " + handledRefs.size());
		System.out.println("Linked refs size: " + linkedRefs.size());
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
	 * @param exceptions
	 * @return {@code true} if this depth-first traversal included a cycle, {@code false} otherwise (used internally).
	 */
	private boolean compileIncludesLinkCycles(Set<IncludeReference> handledRefs, Set<IncludeReference> linkedRefs,
			Stack<IncludeReference> path, Environment env, Set<ConfigCompileException> exceptions) {
		boolean containsCycle = false; // TODO - Remove cycle-awareness if no longer used.
		System.out.println("[DEBUG] numIncludeRefs: " + this.getIncludeRefs().size()); // TODO - Remove debug.
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
						StaticAnalysis includeAnalysis = IncludeCache.getStaticAnalysis(file, pathRef.getTarget());
						if(includeAnalysis == null) {

							// The include did not compile, so ignore the include entirely.
							this.addDirectedEdge(includeRef.getOutScope(), includeRef.getInScope());
						} else {
							cycleAnalyses.add(includeAnalysis);
							cycleScopes.addAll(includeAnalysis.scopes);

							// Directly link this include reference.
							this.addDirectedEdge(includeAnalysis.startScope, includeRef.getInScope());
							this.addDirectedEdge(includeRef.getOutScope(), includeAnalysis.endScope);
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
				System.out.println("[DEBUG] Loading include: "
						+ includeRef.getIdentifier() + " - " + includeRef.getTarget()); // TODO - Remove debug.
				includeAnalysis = IncludeCache.getStaticAnalysis(file, includeRef.getTarget());
				if(includeAnalysis == null) {
					System.out.println("[DEBUG] Include analysis not cached, generating.");
					includeAnalysis = new StaticAnalysis(false);
					IncludeCache.get(file, env, includeAnalysis, includeRef.getTarget());
					System.out.println("[DEBUG] Include analysis generated and cached.");
					assert IncludeCache.getStaticAnalysis(file, includeRef.getTarget()) != null : "Failed to cache include analysis.";
					assert IncludeCache.getStaticAnalysis(Static.GetFileFromArgument(includeRef.getIdentifier(), env, includeRef.getTarget(), null),
							includeRef.getTarget()) != null : "Failed to cache include analysis (2).";
				}
			} catch (CREException e) {

				// Convert CREs into compile errors if there was a problem resolving or compiling a static include.
				// TODO - Split compilation such that we can use syntax-correct faulty includes anyways.
				exceptions.add(new ConfigCompileException(e.getMessage(), e.getTarget()));

				// Link directly and continue as there's no StaticAnalysis to handle.
				System.out.println("[DEBUG] Failed to compile include. Linking directly."); // TODO - Remove debug.
				this.addDirectedEdge(includeRef.getOutScope(), includeRef.getInScope());
				linkedRefs.add(includeRef);
				continue;
			}

			// Recurse on the include's analysis.
			path.push(includeRef);
			boolean childContainsCycle = includeAnalysis.compileIncludesLinkCycles(
					handledRefs, linkedRefs, path, env, exceptions);
			path.pop();

			// Clone and link the include analysis if it has not yet been handled, and mark it as handled.
			if(linkedRefs.add(includeRef)) {

				// Clone the include analysis and absorb its scopes into this analysis.
				StaticAnalysis includeAnalysisClone = includeAnalysis.clone();
				this.scopes.addAll(includeAnalysisClone.scopes);

				// Link the cloned include analysis scopes.
				this.addDirectedEdge(includeAnalysisClone.startScope, includeRef.getInScope());
				this.addDirectedEdge(includeRef.getOutScope(), includeAnalysisClone.endScope);
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
		// TODO - Remove commented-out code when no longer needed.
//		Set<IncludeReference> refs = new HashSet<>();
//		Stack<Scope> scopeStack = new Stack<>();
//		scopeStack.addAll(this.rootScopes); // TODO - Root scopes cannot look up in child scopes, so this won't work.
//		while(!scopeStack.empty()) {
//			for(Reference ref : scopeStack.pop().getAllReferences(Namespace.INCLUDE)) {
//				IncludeReference includeRef = (IncludeReference) ref;
//				refs.add(includeRef);
//				scopeStack.push(includeRef.getOutScope());
//			}
//		}
//		return refs;

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

	// TODO - Remove if no longer needed.
//	/**
//	 * Creates a new {@link StaticAnalysis} with the current end scope as its root scope. Used for performing static
//	 * analysis on code that runs directly after the code analyzed by this {@link StaticAnalysis}.
//	 * @return
//	 */
//	public StaticAnalysis createAnalysisFromEndScope() {
//		return new StaticAnalysis(this.endScope, false); // TODO - Review whether it is okay to not set 'scopes' here.
//	}

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
	 * @param exceptions
	 * @return The returned scope from {@link Function#linkScope(Scope, ParseTree, Set)} in the first case,
	 * or the parent scope otherwise.
	 */
	public Scope linkScope(Scope parentScope, ParseTree ast,
			Environment env, Set<ConfigCompileException> exceptions) {
		Mixed node = ast.getData();
		if(node instanceof CFunction) {
			CFunction cFunc = (CFunction) node;
			if(cFunc.hasFunction()) {
				try {
					FunctionBase f = FunctionList.getFunction(cFunc, null);
					if(f instanceof Function) {
						Function func = (Function) f;
						return func.linkScope(this, parentScope, ast, env, exceptions);
					}
				} catch (ConfigCompileException ex) {
					// Ignore node. This should cause a compile error in a later stage.
					// TODO - Or the compile error could be generated here, check what's more convenient.
				}
			} else if(cFunc.hasIVariable()) { // The function is a var reference to a closure: '@myClosure(<args>)'.

				// Add variable reference in a new scope.
				Scope refScope = this.createNewScope(parentScope);
				refScope.addReference(new Reference(Namespace.IVARIABLE, cFunc.val(), cFunc.getTarget()));
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
			return new Scope[] {newParamScope, valScope};
		}

		// Handle assign parameter (typed and/or with default value).
		if(node instanceof CFunction) { // Typed parameter or assign.
			CFunction cFunc = (CFunction) node;
			if(cFunc.hasFunction()) {
				try {
					FunctionBase f = FunctionList.getFunction(cFunc, null);
					if(f != null && f.getClass().equals(DataHandling.assign.class)) {
						return ((DataHandling.assign) f).linkParamScope(
								this, paramScope, valScope, ast, env, exceptions);
					}
				} catch (ConfigCompileException ex) {
					// Ignore and handle as non-parameter parameter.
				}
			}
		}

		// Handle non-parameter parameter. Fall back to handling the function's arguments.
		// TODO - Decide whether to generate this type of error here, or during typechecking (saves repeating code).
//		exceptions.add(new ConfigCompileException("Invalid parameter", node.getTarget()));
		return new Scope[] {paramScope, this.linkScope(valScope, ast, env, exceptions)};
	}

//	public Scope linkIsolatedParamScope(Scope parentOuterScope, Scope paramDeclScope,
//			ParseTree ast, Environment env, Set<ConfigCompileException> exceptions) {
//		Mixed node = ast.getData();
//		if(node instanceof IVariable) { // Normal parameter.
//			IVariable iVar = (IVariable) node;
//			Scope newScope = this.createNewScope(parentOuterScope);
//			newScope.addDeclaration(new Declaration(
//					Namespace.IVARIABLE, iVar.getVariableName(), iVar.getDefinedType(), iVar.getTarget()));
//			paramDeclScope.addDeclaration(new Declaration(
//					Namespace.IVARIABLE, iVar.getVariableName(), iVar.getDefinedType(), iVar.getTarget()));
//			return newScope;
//		} else if(node instanceof CFunction && ((CFunction) node).hasFunction()) { // Typed parameter or assign.
//			try {
//				FunctionBase f = FunctionList.getFunction((CFunction) node, null);
//				if(f instanceof DataHandling.assign) {
//					Function func = (Function) f;
//					return func.linkScope(this, parentScope, ast, env, exceptions);
//				}
//			} catch (ConfigCompileException ex) {
//				// Ignore node. This should cause a compile error in a later stage.
//			}
//		}
////			return this.linkScope(parentOuterScope, ast, env, exceptions);
//		}
//	}

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
		// TODO - Remove asserts? This is valid when linking an include clone to an analysis we actually care about.
//		assert this.scopes.contains(child);
//		assert this.scopes.contains(parent);
		child.addParent(parent);
	}

	public void removeDirectedEdge(Scope child, Scope parent) {
		// TODO - Remove asserts? This is valid when linking an include clone to an analysis we actually care about.
//		assert this.scopes.contains(child);
//		assert this.scopes.contains(parent);
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

	/**
	 * Clones this {@link StaticAnalysis} including its scopes, but with shared declaration and reference links in
	 * these scopes. This method should only be called after all references and declarations have been added to the
	 * scope graph, and will no longer change. This also means that for example {@link IncludeReference}s still point
	 * to their original start and end scope, and not to the cloned ones.
	 */
	@Override
	public StaticAnalysis clone() {
		Map<Scope, Scope> cloneMapping = new HashMap<>();

		Scope startScope = cloneScope(this.startScope, cloneMapping);
		Scope endScope = cloneScope(this.endScope, cloneMapping);

		Set<Scope> scopesClone = new HashSet<>();
		for(Scope scope : this.scopes) {
			scopesClone.add(cloneScope(scope, cloneMapping));
		}

		return new StaticAnalysis(startScope, endScope, scopesClone, this.isMainAnalysis);
	}

	private static Scope cloneScope(Scope scope, Map<Scope, Scope> cloneMapping) {

		// Create unlinked clone, updating the mapping from scopes to their clones.
		Scope scopeClone = scope.shallowUnlinkedClone();
		cloneMapping.put(scope, scopeClone);

		// Clone the parents, using cached clones for already cloned parents.
		for(Scope parent : scope.getParents()) {
			Scope parentClone = cloneMapping.get(parent);
			if(parentClone == null) {
				parentClone = cloneScope(parent, cloneMapping);
			}
			scopeClone.addParent(parentClone);
		}
		return scopeClone;
	}
}
