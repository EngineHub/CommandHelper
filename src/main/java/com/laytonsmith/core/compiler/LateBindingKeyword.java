package com.laytonsmith.core.compiler;

import com.laytonsmith.PureUtilities.ClassLoading.ClassDiscovery;
import com.laytonsmith.core.Documentation;
import com.laytonsmith.core.ParseTree;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import java.net.URL;

/**
 * A late binding keyword is one that is bound after other parts of the parse tree have been formed. They are
 * then processed afterwards as their own nodes. Late Binding Keywords can have left, right, or both associativity.
 * This is meant for keywords that don't work with low level contexts, such as ones that care about brackets or
 * other individual symbols, but instead work with the logical concept of a high level ParseTree node, which
 * has already been resolved into a single ParseTree (which may itself have other keywords).
 */
public abstract class LateBindingKeyword implements KeywordDocumentation {
	public static enum Associativity {
		/**
		 * A left associative keyword attaches to the node to its left. That is, {@code node1 keyword node2}, then
		 * node1 is associated with this keyword.
		 */
		LEFT,
		/**
		 * A left associative keyword attaches to the node to its right. That is, {@code node1 keyword node2}, then
		 * node2 is associated with this keyword.
		 */
		RIGHT,
		/**
		 * A both associative keyword attaches to the node to its left and right. That is, {@code node1 keyword node2},
		 * then node1 and node2 is associated with this keyword.
		 */
		BOTH;
	}

	public abstract ParseTree processLeftAssociative(Environment env, Target t, FileOptions fileOptions,
			ParseTree leftHandNode) throws ConfigCompileException;

	public abstract ParseTree processRightAssociative(Environment env, Target t, FileOptions fileOptions,
			ParseTree rightHandNode) throws ConfigCompileException;

	public abstract ParseTree processBothAssociative(Environment env, Target t, FileOptions fileOptions,
			ParseTree leftHandNode, ParseTree rightHandNode) throws ConfigCompileException;

	public abstract Associativity getAssociativity();

	/**
	 * Defaults to false, but if true, allows for null to be passed, indicating there was no value to pass on that side.
	 * @return
	 */
	public boolean allowEmptyValue() {
		return false;
	}

	@Override
	@SuppressWarnings("unchecked")
	public Class<? extends Documentation>[] seeAlso() {
		return new Class[]{};
	}

	@Override
	public final URL getSourceJar() {
		return ClassDiscovery.GetClassContainer(this.getClass());
	}

	@Override
	public final String getName() {
		return this.getClass().getAnnotation(Keyword.keyword.class).value();
	}

	@Override
	public final String getKeywordName() {
		return getName();
	}

}
