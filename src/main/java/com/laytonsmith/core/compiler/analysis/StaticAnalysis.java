package com.laytonsmith.core.compiler.analysis;

import java.util.HashSet;
import java.util.Set;

import com.laytonsmith.core.ParseTree;
import com.laytonsmith.core.constructs.CFunction;
import com.laytonsmith.core.constructs.IVariable;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import com.laytonsmith.core.functions.Function;
import com.laytonsmith.core.functions.FunctionBase;
import com.laytonsmith.core.functions.FunctionList;
import com.laytonsmith.core.natives.interfaces.Mixed;

/**
 * This class can be used to perform static analysis.
 * @author P.J.S. Kools
 */
public class StaticAnalysis {

	private final ParseTree ast;

	public StaticAnalysis(ParseTree ast) {
		this.ast = ast;
	}

	public void analyze() {
		this.analyze(new HashSet<ConfigCompileException>());
	}

	public void analyze(Set<ConfigCompileException> exceptions) {

		// Create main scope.
		Scope mainScope = new Scope();

		// Pass the main scope to the root node, allowing it to adjust the scope graph.
		linkScope(mainScope, this.ast, exceptions);
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
	 * @param exceptions
	 * @return The returned scope from {@link Function#linkScope(Scope, ParseTree, Set)} in the first case,
	 * or the parent scope otherwise.
	 */
	public static Scope linkScope(Scope parentScope, ParseTree ast, Set<ConfigCompileException> exceptions) {
		Mixed node = ast.getData();
		if(node instanceof CFunction) {
			CFunction cFunc = (CFunction) node;
			if(cFunc.hasFunction()) {
				try {
					FunctionBase f = FunctionList.getFunction(cFunc, null);
					if(f instanceof Function) {
						Function func = (Function) f;
						return func.linkScope(parentScope, ast, exceptions);
					}
				} catch (ConfigCompileException ex) {
					// Ignore node. This should cause a compile error in a later stage.
				}
			} else if(cFunc.hasIVariable()) {

				// The function is a variable reference to a closure: '@myClosure(<args>)'.
				Declaration decl = parentScope.getDeclaration(Namespace.IVARIABLE, cFunc.val());
				if(decl == null) {
					exceptions.add(new ConfigCompileException(
							"Variable cannot be resolved: " + cFunc.val(), cFunc.getTarget()));
				}
			} else if(cFunc.hasProcedure()) {

				// The function is a procedure reference.
				/*
				 *  TODO - Add a proc reference to the scope graph.
				 *  As procedures in different files can depend on each other, this requires resolving references in
				 *  the scope graph after all files have been processed.
				 */

				// Handle the proc call arguments.
				Scope scope = parentScope;
				for(ParseTree child : ast.getChildren()) {
					scope = linkScope(scope, child, exceptions);
				}
				return scope;
			} else {
				throw new Error("Unsupported " + CFunction.class.getSimpleName()
						+ " type in static analysis for node with value: " + cFunc.val());
			}
		} else if(node instanceof IVariable) {
			IVariable ivar = (IVariable) node;
			Declaration decl = parentScope.getDeclaration(Namespace.IVARIABLE, ivar.getVariableName());
			if(decl == null) {
				exceptions.add(new ConfigCompileException(
						"Variable cannot be resolved: " + ivar.getVariableName(), ivar.getTarget()));
			} else {

				// Set the declared type of the IVariable reference.
				ast.setData(new IVariable(decl.getType(), ivar.getVariableName(), ivar.ival(), decl.getTarget()));
			}
		}
		return parentScope;
	}

	/**
	 * Handles parameter AST nodes, namely {@link IVariable}s or functions that are supposed to return
	 * an {@link IVariable}. If the given AST node is an {@link IVariable}, it is declared in a new scope that is
	 * linked to the parent scope. Otherwise, the {@link #linkScope(Scope, ParseTree, Set)} method is called with
	 * the same arguments as this method to eventually declare the parameter through the assign() function.
	 * @param parentScope
	 * @param ast
	 * @param exceptions
	 * @return The new scope in which the parameter is declared. For an invalid script, it can happen that no parameter
	 * was passed, meaning that {@link #linkScope(Scope, ParseTree, Set)} might return a scope without declaration.
	 */
	public static Scope linkParamScope(Scope parentScope, ParseTree ast, Set<ConfigCompileException> exceptions) {
		if(ast.getData() instanceof IVariable) { // Normal parameter.
			IVariable iVar = (IVariable) ast.getData();
			Scope newScope = parentScope.createNewChild();
			newScope.addDeclaration(new Declaration(
					Namespace.IVARIABLE, iVar.getVariableName(), iVar.getDefinedType(), iVar.getTarget()));
			return newScope;
		} else { // Typed parameter or assign.
			return StaticAnalysis.linkScope(parentScope, ast, exceptions);
		}
	}
}
