package com.laytonsmith.core.compiler.analysis;

/**
 * Represents a special procedure root declaration in a scope graph. This is placed at the root of the inner scope of
 * the procedure, so that it can be looked up from within the procedure. When a procedure reference within a procedure
 * cannot be resolved, it is added to this root scope declaration instead. This allows the required procedures to be
 * defined until the procesure is actually called, at which moment all references in this declaration must resolve.
 * @author P.J.S. Kools
 */
public class ProcRootDeclaration extends Declaration {

	public static final String PROC_ROOT = "~PROC_ROOT";

	public final ProcDeclaration procDecl;

	/**
	 * Creates a new {@link ProcRootDeclaration} in the {@link Namespace#PROCEDURE} namespace, named {@link #PROC_ROOT}.
	 * @param procDecl - The procedure declaration this proc root declaration belongs to.
	 */
	public ProcRootDeclaration(ProcDeclaration procDecl) {
		super(Namespace.PROCEDURE, PROC_ROOT, null, null);
		this.procDecl = procDecl;
	}
}
