package com.laytonsmith.core.compiler.analysis;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

/**
 * Represents a scope that can be used to form a scope graph.
 * @author P.J.S. Kools
 */
public class Scope {

	private final Set<Scope> parents;
	private final Map<Namespace, Set<Scope>> specificParents;
	private final Map<Namespace, Map<String, Declaration>> declarations;
	private final Map<Namespace, List<Reference>> references;

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
		this.parents = new HashSet<>();
		if(parent != null) {
			this.parents.add(parent);
		}
		this.specificParents = new HashMap<>();
		this.declarations = new HashMap<>();
		this.references = new HashMap<>();
	}

	private Scope(Set<Scope> parents, Map<Namespace, Set<Scope>> specificParents,
			Map<Namespace, Map<String, Declaration>> declarations, Map<Namespace, List<Reference>> references) {
		this.parents = parents;
		this.specificParents = specificParents;
		this.declarations = declarations;
		this.references = references;
	}

	public void addParent(Scope parent) {
		this.parents.add(parent);
	}

	public Set<Scope> getParents() {
		return this.parents;
	}

	public void containsParent(Scope parent) {
		this.parents.contains(parent);
	}

	public void removeParent(Scope parent) {
		this.parents.remove(parent);
	}

	/**
	 * Adds a parent scope that can only be accessed through the given namespace.
	 * @param parent - The parent scope.
	 * @param namespace - The namespace for which to allow lookups.
	 */
	public void addSpecificParent(Scope parent, Namespace namespace) {
		this.parents.add(parent);
		Set<Scope> parents = this.specificParents.get(namespace);
		if(parents == null) {
			parents = new HashSet<>();
			this.specificParents.put(namespace, parents);
		}
		parents.add(parent);
	}

	public Map<Namespace, Set<Scope>> getSpecificParents() {
		return this.specificParents;
	}

	public Set<Scope> getSpecificParents(Namespace namespace) {
		Set<Scope> parents = this.specificParents.get(namespace);
		return (parents == null ? Collections.emptySet() : parents);
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
	 * Adds a reference to this scope.
	 * @param ref - The reference to add.
	 */
	public void addReference(Reference ref) {
		List<Reference> refList = this.references.get(ref.getNamespace());
		if(refList == null) {
			refList = new ArrayList<>();
			this.references.put(ref.getNamespace(), refList);
		}
		refList.add(ref);
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
	 * Gets the declarations matching the identifier and namespace from this scope and its (in)direct parents.
	 * This is a lookup with shadowing, meaning that once a scope contains a match, then its parents are no longer
	 * considered. If multiple matches are found through different parents, then these are all returned.
	 * @param namespace
	 * @param identifier
	 * @return The {@link Declaration} or an empty set if no such declaration exists.
	 */
	public Set<Declaration> getDeclarations(Namespace namespace, String identifier) {
		Set<Scope> handledScopes = new HashSet<>();
		Stack<Scope> scopeStack = new Stack<>();
		Set<Declaration> decls = new HashSet<>();
		scopeStack.push(this);
		do {
			Scope scope = scopeStack.pop();
			if(!handledScopes.add(scope)) {
				continue;
			}
			Declaration decl = scope.getDeclarationLocal(namespace, identifier);
			if(decl != null) {
				decls.add(decl);
			} else {
				scopeStack.addAll(scope.getParents());
				scopeStack.addAll(scope.getSpecificParents(namespace));
			}
		} while(!scopeStack.empty());
		return decls;
	}

	/**
	 * Gets all declarations matching the identifier and namespace from this scope and its (in)direct parents.
	 * @param namespace
	 * @param identifier
	 * @return The {@link Declaration} or an empty set if no such declaration exists.
	 */
	public Set<Declaration> getReachableDeclarations(Namespace namespace, String identifier) {
		Set<Scope> handledScopes = new HashSet<>();
		Stack<Scope> scopeStack = new Stack<>();
		Set<Declaration> decls = new HashSet<>();
		scopeStack.push(this);
		do {
			Scope scope = scopeStack.pop();
			if(!handledScopes.add(scope)) {
				continue;
			}
			Declaration decl = scope.getDeclarationLocal(namespace, identifier);
			if(decl != null) {
				decls.add(decl);
			}
			scopeStack.addAll(scope.getParents());
			scopeStack.addAll(scope.getSpecificParents(namespace));
		} while(!scopeStack.empty());
		return decls;
	}

	/**
	 * Gets all declarations present in this scope.
	 * @param namespace - The {@link Namespace} for which to find the declarations.
	 * @return The {@link Set} of declarations.
	 */
	public Set<Declaration> getAllDeclarationsLocal(Namespace namespace) {
		Map<String, Declaration> declIdMap = this.declarations.get(namespace);
		if(declIdMap == null) {
			return new HashSet<>();
		}
		return new HashSet<>(declIdMap.values());
	}

	/**
	 * Gets all references present in this scope.
	 * @param namespace - The {@link Namespace} for which to find the references.
	 * @return The {@link Set} of references.
	 */
	public Set<Reference> getAllReferencesLocal(Namespace namespace) {
		List<Reference> refList = this.references.get(namespace);
		if(refList == null) {
			return new HashSet<>();
		}
		return new HashSet<>(refList);
	}

	// TODO - Remove if not necessary. StaticAnalysis can just loop over its scopes itself to get all references.
//	/**
//	 * Gets all references present in this scope and its (in)direct children.
//	 * @param namespace - The {@link Namespace} for which to find the references.
//	 * @return The {@link Set} of references.
//	 */
//	public Set<Reference> getAllReferences(Namespace namespace) {
//		// TODO - Implement this. Requires this scope to know about its children.
//		throw new Error("Not implemented.");
////		List<Reference> refList = this.references.get(namespace);
////		if(refList == null) {
////			return Collections.emptySet();
////		}
////		return new HashSet<>(refList);
//	}

	/**
	 * Creates a shallow unlinked clone, containing the exact same declarations and references maps.
	 * Parent and child links are not included.
	 * @return The clone.
	 */
	public Scope shallowUnlinkedClone() {
		return new Scope(new HashSet<>(), new HashMap<>(), this.declarations, this.references);
	}
}
