package com.laytonsmith.core.constructs.generics;

import com.laytonsmith.PureUtilities.Pair;
import com.laytonsmith.core.FullyQualifiedClassName;
import com.laytonsmith.core.constructs.CClassType;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.exceptions.CRE.CREGenericConstraintException;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A Constraints object contains the full list of Constraints for a given type parameter.
 */
public class Constraints extends AbstractList<Constraint> {

	private final List<Constraint> list;
	private final String typename;

	/**
	 * Constructs a new constraint object. Note that if this is being used on the LHS, no validation is done
	 * @param constraints
	 */
	public Constraints(Target t, ConstraintLocation location, Constraint... constraints) {
		this.list = Arrays.asList(constraints);
		if(location == ConstraintLocation.RHS) {
			if(constraints.length != 1 || !(constraints[0] instanceof ExactType)) {
				throw new CREGenericConstraintException("Constraints cannot be used on the RHS", t);
			}
		}
		if(location == ConstraintLocation.DEFINITION) {
			typename = ConstraintValidator.ValidateDefinition(this.list);
		} else {
			typename = "?";
		}
	}

	@Override
	public Constraint get(int index) {
		return list.get(index);
	}

	@Override
	public int size() {
		return list.size();
	}

	/**
	 * Returns the name of the type. T for instance, or ? if this is a wildcard (defined on LHS).
	 * @return
	 */
	public String getTypeName() {
		return typename;
	}

	@Override
	public String toString() {
		StringBuilder b = new StringBuilder();
		boolean doComma = false;
		for(Constraint c : list) {
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
	 * LHS against the class definition. Use {@link #withinBounds(CClassType, LeftHandGenericUse)} to validate RHS
	 * against LHS.
	 * @param lhs The other, presumably subtype to compare against.
	 * @throws CREGenericConstraintException
	 */
	public boolean withinBounds(Constraints lhs, List<String> errors, Environment env) {
		for(Constraint t : list) {
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
		if(errors.size() > 0) {
			return false;
		}
		return true;
	}

	/**
	 * Validates that this concrete type (and perhaps the concrete type's generics) fit within the boundary
	 * specified in the LHS constraint. This is used to validate the RHS against the LHS. Use
	 * {@link #withinBounds(Constraints, List, Environment)} to validate the LHS against the definition.
	 * @param rhsType
	 * @param rhsGenerics
	 * @return
	 */
	public boolean withinBounds(CClassType rhsType, LeftHandGenericUse rhsGenerics, Environment env) {
		for(Constraint c : list) {
			if(!c.isWithinConstraint(rhsType, rhsGenerics, env)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Given that this is the constraints on the LHS, returns the ExactType value that should be used on the RHS if
	 * the diamond operator was used. Not all Constraints support this, so this might throw an exception.
	 * @return
	 */
	public ExactType convertFromDiamond(Target t) throws CREGenericConstraintException {
		// Diamond operator can currently only be used in simple cases, though we anyways check for definitely
		// wrong cases.
		ExactType type = null;
		for(Constraint c : list) {
			ExactType newType = c.convertFromDiamond(t);
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
	 * @param forType If the type is an unbounded wildcard, the constraints are simply inherited from the class
	 *                definition. Thus, this is a required parameter in case that's the type specified. If the constraint
	 *                specified is anything other than "?", then this parameter is unused, which means that in general,
	 *                no additional validation is performed on the type, for instance to ensure that the parameter
	 *                count matches. This type of validation is expected to be done before/elsewhere.
	 * @param constraintDefinition The constraint definition, without the angle brackets
	 * @param location The location the constraints are being defined in. This varies behavior slightly.
	 * @param t The code target, for exceptions
	 * @param env The environment
	 * @return
	 */
	public static Constraints[] BuildFromString(CClassType forType, String constraintDefinition, ConstraintLocation location, Target t, Environment env) throws ClassNotFoundException {
		int bracketStack = 0;
		int parenthesisStack = 0;
		constraintDefinition = constraintDefinition.replaceAll("\n", " ");
		constraintDefinition = constraintDefinition.replaceAll("\r", " ");
		constraintDefinition = constraintDefinition.replaceAll(" +", " ");
		List<Constraints> constraintsS = new ArrayList<>();

		StringBuilder buf = new StringBuilder();

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
				constraintsS.add(GetConstraints(forType, buf.toString(), t, location, env));
				buf = new StringBuilder();
				continue;
			}
			buf.append(c);
		}

		constraintsS.add(GetConstraints(forType, buf.toString(), t, location, env));

		return constraintsS.toArray(new Constraints[constraintsS.size()]);
	}

	private static Constraints GetConstraints(CClassType forType, String s, Target t, ConstraintLocation location, Environment env) throws ClassNotFoundException {
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
				constraints.add(GetConstraint(forType, buf.toString(), t, location, env));
				endOfConstraint = false;
				buf = new StringBuilder();
				continue;
			}
			if(endOfConstraint && !Character.isWhitespace(c)) {
				throw new CREGenericConstraintException("Improperly formatted generic statement", t);
			}
			buf.append(c);
		}

		constraints.add(GetConstraint(forType, buf.toString(), t, location, env));

		return new Constraints(t, location, constraints.toArray(new Constraint[constraints.size()]));
	}

	private static Constraint GetConstraint(CClassType forType, String s, Target t, ConstraintLocation location, Environment env) throws ClassNotFoundException {
		// Now we know we only have one constraint to process
		s = s.trim();
		String name = "";
		String keyword = null;
		Pair<CClassType, LeftHandGenericUse> clazz = null;
		boolean inName = true;
		boolean inKeyword = false;
		boolean isNewConstraint = false;
		int newParenthesisStack = 0;
		StringBuilder buf = new StringBuilder();
		for(Character c : s.trim().toCharArray()) {
			if(!isNewConstraint && inName && Character.isWhitespace(c)) {
				name = buf.toString();
				buf = new StringBuilder();
				if("new".equals(name)) {
					isNewConstraint = true;
					name = "";
					continue;
				}
				inName = false;
				inKeyword = true;
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
					List<Pair<CClassType, LeftHandGenericUse>> types = GetNewTypes(buf.toString(), t, env);
					return new ConstructorConstraint(t, name, types);
				}
			}
			buf.append(c);
		}
		if(!inName && !inKeyword) {
			// Now buf contains the class, which may need additional parsing
			clazz = ParseClassType(buf.toString(), t, env);
			if("extends".equals(keyword)) {
				return new UpperBoundConstraint(t, name, clazz.getKey(), clazz.getValue());
			} else if("super".equals(keyword)) {
				return new LowerBoundConstraint(t, name, clazz.getKey(), clazz.getValue());
			}
		}
		if(inName && keyword == null) {
			// Unbounded or Type
			if(location == ConstraintLocation.DEFINITION) {
				return new UnboundedConstraint(t, buf.toString());
			} else {
				String typename = buf.toString();
				if("?".equals(typename)) {
					return ExactType.AsUnboundedWildcard(forType.getGenericDeclaration(), t);
				} else {
					return new ExactType(t, CClassType.get(FullyQualifiedClassName.forName(typename, t, env), t, null), null);
				}
			}
		}
		throw new CREGenericConstraintException("Malformed generic parameters", t);
	}

	/**
	 * This will contain the types as comma separated fields. (Not including the outer parenthesis.)
	 * @param s
	 * @param t
	 * @return
	 */
	private static List<Pair<CClassType, LeftHandGenericUse>> GetNewTypes(String s, Target t, Environment env) throws ClassNotFoundException {
		List<Pair<CClassType, LeftHandGenericUse>> list = new ArrayList<>();
		int bracketStack = 0;
		int parenthesisStack = 0;
		StringBuilder buf = new StringBuilder();
		for(Character c : s.toCharArray()) {
			if(bracketStack == 0 && parenthesisStack == 0 && c == ',') {
				list.add(ParseClassType(buf.toString(), t, env));
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
		list.add(ParseClassType(buf.toString(), t, env));
		return list;
	}

	private static Pair<CClassType, LeftHandGenericUse> ParseClassType(String s, Target t, Environment env) throws ClassNotFoundException {
		s = s.trim();
		CClassType type = null;
		LeftHandGenericUse lhgu = null;
		boolean inLHS = false;
		int bracketStack = 0;
		StringBuilder buf = new StringBuilder();
		for(Character c : s.toCharArray()) {
			if(c == '<') {
				if(inLHS == false) {
					type = CClassType.getNakedClassType(FullyQualifiedClassName.forName(buf.toString(), t, env));
					buf = new StringBuilder();
				}
				inLHS = true;
				bracketStack++;
			}
			if(c == '>') {
				bracketStack--;
				if(bracketStack == 0) {
					buf.append(c);
					lhgu = new LeftHandGenericUse(type, t, env, BuildFromString(type, buf.toString().trim()
							.replaceAll("<(.*)>", "$1"), ConstraintLocation.LHS, t, env));
					buf = new StringBuilder();
					continue;
				}
			}
			buf.append(c);
		}
		if(!inLHS) {
			type = CClassType.getNakedClassType(FullyQualifiedClassName.forName(buf.toString(), t, env));
		}

		return new Pair<>(type, lhgu);
	}

}
