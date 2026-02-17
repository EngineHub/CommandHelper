package com.laytonsmith.core.compiler.analysis;

/**
 * Represents the namespace of declarations and references in a scope graph.
 * @author P.J.S. Kools
 */
public enum Namespace {
	VARIABLE,
	IVARIABLE,
	IVARIABLE_ASSIGN,
	PROCEDURE,
	INCLUDE,
	RETURNABLE,
	BREAKABLE,
	CONTINUABLE
}
