package com.laytonsmith.core.constructs.generics;

import com.laytonsmith.core.constructs.generics.constraints.ExactTypeConstraint;
import com.laytonsmith.core.constructs.generics.constraints.UnboundedConstraint;
import com.laytonsmith.core.constructs.generics.constraints.UpperBoundConstraint;
import com.laytonsmith.core.constructs.generics.constraints.LowerBoundConstraint;
import com.laytonsmith.core.constructs.generics.constraints.ConstructorConstraint;
import com.laytonsmith.core.constructs.generics.constraints.Constraint;
import com.laytonsmith.PureUtilities.Common.StringUtils;
import com.laytonsmith.PureUtilities.ObjectHelpers;
import com.laytonsmith.PureUtilities.ObjectHelpers.StandardField;
import com.laytonsmith.core.FullyQualifiedClassName;
import com.laytonsmith.core.compiler.CompilerEnvironment;
import com.laytonsmith.core.compiler.CompilerWarning;
import com.laytonsmith.core.compiler.FileOptions;
import com.laytonsmith.core.constructs.CClassType;
import com.laytonsmith.core.constructs.LeftHandSideType;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.constructs.generics.constraints.VariadicTypeConstraint;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.exceptions.CRE.CRECastException;
import com.laytonsmith.core.exceptions.CRE.CREGenericConstraintException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * A Constraints object contains the full list of Constraints for a given type parameter.
 */
@SuppressWarnings({"RedundantIfStatement", "ConstantConditions"})
public class Constraints implements Iterable<Constraint> {

	@StandardField
	private final SortedSet<Constraint> constraints;
	@StandardField
	private final String typename;

	private final boolean isVariadic;

	private final List<Constraint> unorderedConstraints;

	/**
	 * Constructs a new constraint object.Note that if this is being used on the LHS, no validation is done
	 * @param t The code target
	 * @param location The location of the Constraints
	 * @param constraints The constraints. This is an unordered list, but they will be normalized into their
	 *                    natural order.
	 */
	public Constraints(Target t, ConstraintLocation location, Constraint... constraints) {
		this.unorderedConstraints = Arrays.asList(constraints);
		this.constraints = new TreeSet<>(unorderedConstraints);
		if(location == ConstraintLocation.RHS) {
			if(constraints.length != 1 || !(constraints[0] instanceof ExactTypeConstraint)) {
				throw new CREGenericConstraintException("Constraints (other than a single ExactType constraint)"
						+ " cannot be used on the RHS. This definition contains " + constraints.length + " "
						+ " constraint(s), of type(s): " + StringUtils.Join(constraints, ", ",
								(item) -> item.getClass().toString()), t);
			}
		}
		if(location == ConstraintLocation.DEFINITION) {
			typename = ConstraintValidator.ValidateDefinition(this.constraints, t);
		} else {
			typename = "?";
		}
		boolean isVariadic = false;
		for(Constraint c : constraints) {
			if(c instanceof VariadicTypeConstraint) {
				if(isVariadic) {
					throw new CREGenericConstraintException("Only one variadic type definition may be in the parameter", t);
				}
				isVariadic = true;
			}
		}
		this.isVariadic = isVariadic;
	}

	public int size() {
		return constraints.size();
	}

	/**
	 * Returns the name of the type. T for instance, or ? if this is a wildcard (defined on LHS).
	 * @return The typename
	 */
	public String getTypeName() {
		return typename;
	}

	/**
	 * Works like toString, but uses the class's simple name.
	 * @return
	 */
	public String toSimpleString() {
		StringBuilder b = new StringBuilder();
		boolean doComma = false;
		for(Constraint c : constraints) {
			if(doComma) {
				b.append(" & ");
			}
			doComma = true;
			b.append(c.toSimpleString());
		}
		return b.toString();
	}

	@Override
	public String toString() {
		StringBuilder b = new StringBuilder();
		boolean doComma = false;
		for(Constraint c : constraints) {
			if(doComma) {
				b.append(" & ");
			}
			doComma = true;
			b.append(c.toString());
		}
		return b.toString();
	}

	/**
	 * Validates that the given set of Constraints is within the bounds of these contraints. This can be used to validate
	 * LHS against the class definition.Use {@link #withinBounds(CClassType, LeftHandGenericUse, Environment)} to validate RHS
	 * against LHS.
	 * @param lhs The other, presumably subtype to compare against.
	 * @param errors The ongoing list of errors with this statement.
	 * @param env The environment.
	 * @return If the provided constraints are within the bounds of these constraints.
	 */
	public boolean withinBounds(Constraints lhs, List<String> errors, Environment env) {
		for(Constraint t : constraints) {
			boolean oneIsTrue = false;
			for(Constraint c : lhs) {
				Boolean res = t.isWithinConstraint(c, env);
				if(res != null) {
					if(res) {
						oneIsTrue = true;
					} else {
						errors.add("The LHS constraint " + c + " does not"
								+ " suit the constraint defined on the class " + t);
					}
				}
			}
			if(!oneIsTrue) {
				errors.add("The class defines the constraint " + t + ", but no constraints defined on the LHS suit"
						+ " this constraint.");
			}
		}
		return errors.isEmpty();
	}

	/**
	 * Validates that this concrete type (and perhaps the concrete type's generics) fit within the boundary
	 * specified in the LHS constraint.This is used to validate the RHS against the LHS. Use
	 * {@link #withinBounds(Constraints, List, Environment)} to validate the LHS against the definition.
	 * @param type The type to check.
	 * @param rhsGenerics The LHS generics that are associated with the RHS.
	 * @param env
	 * @return If the specified RHS types passed in fit within the bounds of these Constraints
	 */
	public boolean withinBounds(LeftHandSideType type, Environment env) {
		for(Constraint c : constraints) {
			if(!c.isWithinConstraint(type, env)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Returns whether ALL of this set of constraints can support type unions on the LHS. Note that type unions
	 * can never be used on the RHS. This is generally only used when validating LHS against LHS.
	 * @return
	 */
	public boolean supportsTypeUnions() {
		for(Constraint c : constraints) {
			if(!c.supportsTypeUnions()) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Given that this is the constraints on the LHS, returns the ExactType value that should be used on the RHS if
	 * the diamond operator was used. Not all Constraints support this, so this might throw an exception.
	 * @return The most narrow ExactTypeConstraint that suits these constraints, if it's possible to do so.
	 */
	public ExactTypeConstraint convertFromDiamond(Target t) throws CREGenericConstraintException {
		// Diamond operator can currently only be used in simple cases, though we anyways check for definitely
		// wrong cases.
		ExactTypeConstraint type = null;
		for(Constraint c : constraints) {
			ExactTypeConstraint newType = c.convertFromDiamond(t);
			if(type == null) {
				type = newType;
			} else {
				throw new CREGenericConstraintException("Cannot infer generic type from LHS, please explicitely define"
						+ " the RHS generic parameters.", t);
			}
		}
		if(type == null) {
			throw new CREGenericConstraintException("Cannot infer generic type from LHS, please explicitely define the"
					+ " RHS generic parameters.", t);
		}
		return type;
	}

	/**
	 * Given a string such as <code>? extends array&lt;number&gt; & new ?()</code>, parses it into a Constraints[]
	 * object. Note that the outer angle brackets should not be provided.
	 *
	 * @param constraintDefinition The constraint definition, without the angle brackets
	 * @param location The location the constraints are being defined in. This varies behavior slightly.
	 * @param declarationConstraints The type of the declaration, if location isn't DEFINITION.
	 * @param t The code target, for exceptions
	 * @param env The environment
	 * @return An array of Constraints, each one representing a single parameter.
	 */
	public static Constraints[] BuildFromString(FileOptions fileOptions,
			String constraintDefinition, ConstraintLocation location,
			List<Constraints> declarationConstraints, Target t, Environment env) {
		int bracketStack = 0;
		int parenthesisStack = 0;
		constraintDefinition = constraintDefinition.replaceAll("\n", " ");
		constraintDefinition = constraintDefinition.replaceAll("\r", " ");
		constraintDefinition = constraintDefinition.replaceAll(" +", " ");
		List<Constraints> constraintsS = new ArrayList<>();

		StringBuilder buf = new StringBuilder();

		int declarationCount = 0;

		for(Character c : constraintDefinition.toCharArray()) {
			if(c == '<') {
				bracketStack++;
			} else if(c == '>') {
				bracketStack--;
			} else if(c == '(') {
				parenthesisStack++;
			} else if(c == ')') {
				parenthesisStack--;
			} else if(c == ',' && bracketStack == 0 && parenthesisStack == 0) {
				constraintsS.add(GetConstraints(fileOptions, buf.toString(), t, location, declarationConstraints == null
						? null : declarationConstraints.get(declarationCount++), env));
				buf = new StringBuilder();
				continue;
			}
			buf.append(c);
		}

		constraintsS.add(GetConstraints(fileOptions, buf.toString(), t, location, declarationConstraints == null
						? null : declarationConstraints.get(declarationCount++), env));

		return constraintsS.toArray(Constraints[]::new);
	}

	private static Constraints GetConstraints(FileOptions fileOptions, String s, Target t, ConstraintLocation location,
			Constraints declarationConstraints,
			Environment env) {
		int bracketStack = 0;
		int parenthesisStack = 0;
		List<Constraint> constraints = new ArrayList<>();
		StringBuilder buf = new StringBuilder();
		boolean endOfConstraint = false;
		for(Character c : s.toCharArray()) {
			if(c == '(') {
				parenthesisStack++;
			} else if(c == ')') {
				parenthesisStack--;
			} else if(c == '<') {
				bracketStack++;
			} else if(c == '>') {
				bracketStack--;
				if(bracketStack == 0 && parenthesisStack == 0) {
					// We've rebalanced, so this should be the end of the constraint
					endOfConstraint = true;
					buf.append(c);
					continue;
				}
			} else if(bracketStack == 0 && c == '&') {
				// end of constraint, process it
				constraints.add(GetConstraint(fileOptions, buf.toString(), t, location, declarationConstraints, env));
				endOfConstraint = false;
				buf = new StringBuilder();
				continue;
			}
			if(endOfConstraint && !Character.isWhitespace(c)) {
				throw new CREGenericConstraintException("Improperly formatted generic statement", t);
			}
			buf.append(c);
		}

		constraints.add(GetConstraint(fileOptions, buf.toString(), t, location, declarationConstraints, env));

		return new Constraints(t, location, constraints.toArray(Constraint[]::new));
	}

	/**
	 * Parses a constraint from a string. This is only meant as a stopgap measure until the
	 * compiler is updated.
	 * @param fileOptions The file options.
	 * @param s The string to parse.
	 * @param t The code target.
	 * @param location The location of these constraints.
	 * @param declarationConstraints The Constraints for the relevant definition.
	 * Must be provided if location is not definition.
	 * @param env The environment.
	 * @return
	 * @throws ClassNotFoundException
	 */
	/*package*/ static Constraint GetConstraint(FileOptions fileOptions, String s, Target t,
			ConstraintLocation location, Constraints declarationConstraints, Environment env) {
		// Now we know we only have one constraint to process
		s = s.trim();
		String name = "";
		String keyword = null;
		LeftHandSideType clazz;
		boolean inName = true;
		boolean inKeyword = false;
		boolean isNewConstraint = false;
		int subGenericStack = 0;
		int newParenthesisStack = 0;
		StringBuilder buf = new StringBuilder();
		for(Character c : s.trim().toCharArray()) {
			if(c == '<') {
				subGenericStack++;
			}
			if(c == '>') {
				subGenericStack--;
				buf.append(c);
				continue;
			}
			if(subGenericStack > 0) {
				buf.append(c);
				continue;
			}
			if(!isNewConstraint && inName && Character.isWhitespace(c)) {
				name = buf.toString();
				buf = new StringBuilder();
				if("new".equals(name)) {
					isNewConstraint = true;
					name = "";
					continue;
				}
				inKeyword = true;
				inName = false;
				continue;
			}

			if(inKeyword && Character.isWhitespace(c)) {
				keyword = buf.toString();
				buf = new StringBuilder();
				inKeyword = false;
				continue;
			}
			if(isNewConstraint && c == '(') {
				name = buf.toString().trim();
				newParenthesisStack++;
				buf = new StringBuilder();
				continue;
			}
			if(isNewConstraint && c == ')') {
				newParenthesisStack--;
				if(newParenthesisStack == 0) {
					List<LeftHandSideType> types = GetNewTypes(fileOptions, buf.toString(), t, env);
					return new ConstructorConstraint(t, name, types);
				}
			}
			buf.append(c);
		}
		if(!inName && !inKeyword) {
			// Now buf contains the class, which may need additional parsing
			clazz = ParseClassType(fileOptions, buf.toString(), t, env);
			if("extends".equals(keyword)) {
				return new UpperBoundConstraint(t, name, clazz);
			} else if("super".equals(keyword)) {
				return new LowerBoundConstraint(t, name, clazz);
			}
		}
		if(inName && keyword == null) {
			// Unbounded or Type
			if(location == ConstraintLocation.DEFINITION) {
				if(fileOptions != null) {
					try {
						CClassType.get(FullyQualifiedClassName.forName(buf.toString(), t, env), env);
						// This passed. It will still work just fine, this is an UnboundedConstraint
						// though, not an ExactTypeConstraint, which cannot be used in the Definition site. However,
						// this will hide the name of the real type, which should be warned against
						CompilerWarning warning
								= new CompilerWarning("Typename overrides a real type, which may be confusing.",
										t, FileOptions.SuppressWarning.GenericTypeOverrides);
						env.getEnv(CompilerEnvironment.class).addCompilerWarning(fileOptions, warning);
					} catch(CRECastException ex) {
						// Ok
					}
				}
				if(buf.toString().endsWith("...")) {
					return new VariadicTypeConstraint(t, buf.toString().substring(0, buf.toString().length() - 3));
				} else {
					return new UnboundedConstraint(t, buf.toString());
				}
			} else {
				String typename = buf.toString();
				if("?".equals(typename)) {
					return ExactTypeConstraint.AsUnboundedWildcard(t, declarationConstraints);
				} else {
					clazz = ParseClassType(fileOptions, typename, t, env);
					return new ExactTypeConstraint(t, clazz);
				}
			}
		}
		throw new CREGenericConstraintException("Malformed generic parameters", t);
	}

	/**
	 * This will contain the types as comma separated fields. (Not including the outer parenthesis.)
	 */
	private static List<LeftHandSideType> GetNewTypes(FileOptions fileOptions, String s, Target t, Environment env) {
		List<LeftHandSideType> list = new ArrayList<>();
		int bracketStack = 0;
		int parenthesisStack = 0;
		StringBuilder buf = new StringBuilder();
		for(Character c : s.toCharArray()) {
			if(bracketStack == 0 && parenthesisStack == 0 && c == ',') {
				list.add(ParseClassType(fileOptions, buf.toString(), t, env));
				buf = new StringBuilder();
				continue;
			}
			if(c == '<') {
				bracketStack++;
			} else if(c == '>') {
				bracketStack--;
			} else if(c == '(') {
				parenthesisStack++;
			} else if(c == ')') {
				parenthesisStack--;
			}
			buf.append(c);
		}

		if(!buf.isEmpty()) {
			list.add(ParseClassType(fileOptions, buf.toString(), t, env));
		}
		return list;
	}

	@SuppressWarnings("null")
	private static LeftHandSideType ParseClassType(FileOptions fileOptions, String s, Target t, Environment env) {
		s = s.trim();
		CClassType nakedType = null;
		LeftHandGenericUse lhgu = null;
		boolean inLHS = false;
		int bracketStack = 0;
		StringBuilder buf = new StringBuilder();
		for(Character c : s.toCharArray()) {
			if(c == '<') {
				if(!inLHS) {
					nakedType = CClassType.getNakedClassType(FullyQualifiedClassName.forName(buf.toString(), t, env), env);
					buf = new StringBuilder();
				}
				inLHS = true;
				bracketStack++;
			}
			if(c == '>') {
				bracketStack--;
				if(bracketStack == 0) {
					buf.append(c);
					lhgu = new LeftHandGenericUse(nakedType, t, env, BuildFromString(fileOptions, buf.toString().trim()
							.replaceAll("<(.*)>", "$1"), ConstraintLocation.LHS,
								nakedType.getGenericDeclaration().getConstraints(), t, env));
					buf = new StringBuilder();
					continue;
				}
			}
			buf.append(c);
		}
		if(!inLHS) {
			lhgu = null;
		}

		if(nakedType == null) {
			nakedType = CClassType.getNakedClassType(FullyQualifiedClassName.forName(buf.toString(), t, env), env);
		}

		return LeftHandSideType.fromCClassType(new ConcreteGenericParameter(nakedType, lhgu, t, env), t, env);
	}

	@Override
	@SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
	public boolean equals(Object o) {
		return ObjectHelpers.DoEquals(this, o);
	}

	@Override
	public int hashCode() {
		return ObjectHelpers.DoHashCode(this);
	}

	@Override
	public Iterator<Constraint> iterator() {
		return constraints.iterator();
	}

	/**
	 * For testing and other meta purposes, it may be useful to get these as an unordered list in their original
	 * declaration order. Note that this list is not used in the equals comparison, but contains the same entries.
	 * @return The Constraints, in a List in the order they were defined.
	 */
	public List<Constraint> getInDefinitionOrder() {
		return new ArrayList<>(this.unorderedConstraints);
	}

	/**
	 * Returns true if this Constraints parameter represents a variadic type. (That is, does it contain a
	 * {@link VariadicTypeConstraint} Constraint in it.)
	 * @return
	 */
	public boolean isVariadic() {
		return this.isVariadic;
	}
}
