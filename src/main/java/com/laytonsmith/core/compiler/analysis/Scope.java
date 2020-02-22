package com.laytonsmith.core.compiler.analysis;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Represents a scope that can be used to form a scope graph.
 * @author P.J.S. Kools
 */
public class Scope {

	private Scope parent;
	private final Map<Namespace, Map<String, Declaration>> declarations = new HashMap<>();

	/**
	 * Creates a new scope without parent.
	 */
	public Scope() {
		this(null);
	}

	/**
	 * Creates a new scope with the given parent.
	 * @param parent - The parent scope.
	 */
	private Scope(Scope parent) {
		this.parent = parent;
	}

	public Scope getParent() {
		return this.parent;
	}

	public void setParent(Scope parent) {
		this.parent = parent;
	}

	/**
	 * Creates a new scope with the current scope as parent.
	 * @return The new scope.
	 */
	public Scope createNewChild() {
		return new Scope(this);
	}

	/**
	 * Adds a declaration to this scope. If a declaration with the same name is already defined in this scope, then it
	 * will be overwritten.
	 * @param decl - The declaration to add.
	 */
	public void addDeclaration(Declaration decl) {
		Map<String, Declaration> declIdMap = this.declarations.get(decl.getNamespace());
		if(declIdMap == null) {
			declIdMap = new HashMap<>();
			this.declarations.put(decl.getNamespace(), declIdMap);
		}
		declIdMap.put(decl.getIdentifier(), decl);
	}

	/**
	 * Gets the declaration matching the identifier and namespace from this scope.
	 * @param namespace
	 * @param identifier
	 * @return The {@link Declaration} or null if no such declaration exists.
	 */
	public Declaration getDeclarationLocal(Namespace namespace, String identifier) {
		Map<String, Declaration> declIdMap = this.declarations.get(namespace);
		if(declIdMap == null) {
			return null;
		}
		return declIdMap.get(identifier);
	}

	/**
	 * Gets the declaration matching the identifier and namespace from this scope and its (in)direct parents.
	 * If multiple declarations match, then the one closest to this scope is returned (itself, then its parent, etc).
	 * @param namespace
	 * @param identifier
	 * @return The {@link Declaration} or null if no such declaration exists.
	 */
	public Declaration getDeclaration(Namespace namespace, String identifier) {
		Declaration decl = this.getDeclarationLocal(namespace, identifier);
		return (decl == null && this.parent != null ? this.parent.getDeclaration(namespace, identifier) : decl);
	}

	/**
	 * Gets all declarations present in this scope.
	 * @param namespace - The {@link Namespace} for which to find the declarations.
	 * @return The {@link Set} of declarations.
	 */
	public Set<Declaration> getAllDeclarationsLocal(Namespace namespace) {
		Map<String, Declaration> declIdMap = this.declarations.get(namespace);
		if(declIdMap == null) {
			return Collections.emptySet();
		}
		return new HashSet<>(declIdMap.values());
	}
}
