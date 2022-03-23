package com.laytonsmith.core.constructs.generics;

import com.laytonsmith.PureUtilities.ObjectHelpers;
import com.laytonsmith.PureUtilities.ObjectHelpers.StandardField;
import com.laytonsmith.PureUtilities.Pair;
import com.laytonsmith.core.constructs.CClassType;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.environments.Environment;

import java.util.ArrayList;
import java.util.List;

/**
 * This class represents the RHS of a class type definition with generics. For instance, in the statement
 * <code>new A&lt;B&gt;</code>, this represents B. In general, this contains only concrete classes, however these
 * classes themselves may have generic parameters, in which case they will contain the LHS information for those
 * parameters. However, at a top line level, everything maps to concrete class types.
 * <p>
 * Note that in general, it's impossible to construct an instance of this with no parameters, and this is intentional.
 * For instances of classes without generics, or those with generics, but whose instance used diamond inheritance, these
 * are handled by representing the GenericParameters object with null, and requires special handling in general anyways.
 */
@StandardField
public final class GenericParameters {

	private GenericDeclaration genericDeclaration;
	private final List<Pair<CClassType, LeftHandGenericUse>> parameters;

	/**
	 * When representing MethodScript generics in Java, and there is an inheritance chain with generics at each step,
	 * they have to be passed up the chain appropriately.For instance, given {@code class A<T>} and
	 * {@code class B<U, T> extends A<T>}, then construction of the instance B will be passed the parameters for both U
	 * and T. However, the superclass, class A, also needs to know about parameter T. This method can help select the
	 * subset of parameters which needs to be passed to the superclass, by providing the selected parameter names, which
	 * will be pulled from this collection of parameters in order, and then returned as a new GenericParameters object,
	 * which can then be passed to the superclass (which may do further processing of the parameters to pass to any
	 * further direct super classes).
	 *
	 * @param superDeclaration The declaration object of the super class.
	 * @param parameters The names of the parameters that you wish to pull from the passed in parameters. They will be
	 * returned in the order selected.
	 * @return A subset of the parameters.
	 */
	public GenericParameters subset(GenericDeclaration superDeclaration, String... parameters) {
		GenericParameters newParams = new GenericParameters();
		// Convert the parameter names to places
		for(int i = 0; i < parameters.length; i++) {
			String p = parameters[i];
			for(int j = 0; j < superDeclaration.getParameterCount(); j++) {
				if(p.equals(superDeclaration.getConstraints().get(j).getTypeName())) {
					newParams.parameters.add(this.parameters.get(j));
				}
			}
		}
		newParams.genericDeclaration = superDeclaration;
		return newParams;
	}

	/**
	 * Returns true if this parameter set is a subtype of the LHS constraints.This does not check the base types against
	 * each other, so this can't be used in place of a full instanceof check, as it merely compares generics themselves.
	 *
	 * @param generics The generics to check if this is a subtype of.
	 * @param env
	 * @return
	 */
	public boolean isInstanceof(LeftHandGenericUse generics, Environment env) {
		if((generics == null && genericDeclaration != null) || generics.getConstraints().size() != parameters.size()) {
			return false;
		}
		for(int i = 0; i < parameters.size(); i++) {
			Pair<CClassType, LeftHandGenericUse> pair = parameters.get(i);
			Constraints constraints = generics.getConstraints().get(i);
			if(!isInstanceofParameter(pair.getKey(), pair.getValue(), constraints, env)) {
				return false;
			}
		}
		return true;
	}

	private static boolean isInstanceofParameter(CClassType rhsType, LeftHandGenericUse rhsGenerics, Constraints lhs, Environment env) {
		return lhs.withinBounds(rhsType, rhsGenerics, env);
	}

	public static final class GenericParametersBuilder {

		GenericParameters p;

		private GenericParametersBuilder(GenericParameters p) {
			this.p = p;
		}

		/**
		 * Adds a new parameter. Each parameter consists of a CClassType, and optionally a LeftHandGenericUse. For
		 * instance, in the statement <code>new A&lt;B&lt;? extends C&gt;&gt;</code> where A is the class being
		 * constructed, with signature <code>class A&lt;T&gt;</code> and B is a concrete class itself with a single
		 * template parameter, and C being another class, then this method would be called with the parameters
		 * <code>B</code> and a new instance of the LeftHandGenericUse class representing the constraint
		 * <code>? extends C</code>.
		 *
		 * @param type The concrete class type
		 * @param genericStatement The LHS generic statement for this parameter. This may be null if the type did not
		 * include a generic statement.
		 * @return this, for easy chaining. Use build() to construct the final object.
		 */
		public GenericParametersBuilder addParameter(CClassType type, LeftHandGenericUse genericStatement) {
			p.parameters.add(new Pair<>(type, genericStatement));
			return this;
		}

		/**
		 * Returns if this builder object is empty. If it is, calling build causes an error, so it's important to check
		 * this first if you are using this generically.
		 * @return
		 */
		public boolean isEmpty() {
			return p.parameters.isEmpty();
		}

		/**
		 * Returns the fully constructed object.
		 *
		 * @return
		 */
		public GenericParameters build() {
			if(p.parameters.isEmpty()) {
				throw new Error("Empty parameter builders cannot be used. Check for this condition with isEmpty()");
			}
			return p;
		}
	}

	/**
	 * Begins construction of a new GenericParameters object, which represents the RHS of the generic declaration. The
	 * actual GenericDeclaration object is passed in in order to validate the types against the constraints. Each
	 * instance of a class which has a GenericDeclaration will have one of these objects in it, associated with that
	 * particular instance. This data is not lost after compilation, and types are reified for runtime use.
	 * <p>
	 * Each parameter consists of a CClassType, and optionally a LeftHandGenericUse. For instance, in the statement
	 * <code>new A&lt;B&lt;? extends C&gt;&gt;</code> where A is the class being constructed, with signature
	 * <code>class A&lt;T&gt;</code> and B is a concrete class itself with a single template parameter, and C being
	 * another class, then this method would be called with the parameters <code>B</code> and a new instance of the
	 * LeftHandGenericUse class representing the constraint <code>? extends C</code>.
	 *
	 * @param type The concrete class type
	 * @param genericStatement The LHS generic statement for this parameter. This may be null if the type did not
	 * include a generic statement.
	 * @return this, for easy chaining. Use build() to construct the final object.
	 */
	public static GenericParametersBuilder addParameter(CClassType type, LeftHandGenericUse genericStatement) {
		return emptyBuilder().addParameter(type, genericStatement);
	}

	/**
	 * Returns an empty builder. Note that calling build on an empty builder is an error.
	 * @return
	 */
	public static GenericParametersBuilder emptyBuilder() {
		GenericParameters gp = new GenericParameters();
		return new GenericParametersBuilder(gp);
	}

	private GenericParameters() {
		parameters = new ArrayList<>();
	}

	/**
	 * Returns a list of the parameters that were defined on this class type.
	 *
	 * @return
	 */
	public List<Pair<CClassType, LeftHandGenericUse>> getParameters() {
		return new ArrayList<>(parameters);
	}

	/**
	 * Converts this concrete GenericParameter object into a LeftHandGenericUse equivalent.It uses the ExactType
	 * constraint for all parameters, but makes for easier comparisons.
	 *
	 * @param forType The type of the containing object. In general, this is not tracked by the GenericParameters class,
	 * because it can be used more generically, though in practice, this will certainly belong to some instance of a
	 * class, and it is this class type that should be passed in.
	 * @param env The environment.
	 * @return An equivalent LeftHandGenericUse type. The ConstraintLocation of the underlying object is set to LHS.
	 */
	public LeftHandGenericUse toLeftHandEquivalent(CClassType forType, Environment env) {
		Constraints[] constraints = new Constraints[parameters.size()];
		for(int i = 0; i < parameters.size(); i++) {
			Pair<CClassType, LeftHandGenericUse> parameter = parameters.get(i);
			Constraint c = new ExactType(Target.UNKNOWN, parameter.getKey(), parameter.getValue());
			constraints[i] = new Constraints(Target.UNKNOWN, ConstraintLocation.LHS, c);
		}
		LeftHandGenericUse lhgu = new LeftHandGenericUse(forType, Target.UNKNOWN, env, constraints);
		return lhgu;
	}

	@Override
	public boolean equals(Object that) {
		return ObjectHelpers.DoEquals(this, that);
	}

	@Override
	public int hashCode() {
		return ObjectHelpers.DoHashCode(this);
	}

	@Override
	public String toString() {
		StringBuilder b = new StringBuilder();
		b.append("<");
		boolean doComma = false;
		for(Pair<CClassType, LeftHandGenericUse> p : parameters) {
			if(doComma) {
				b.append(", ");
			}
			doComma = true;
			b.append(p.getKey().val());
			if(p.getValue() != null) {
				b.append("<").append(p.getValue()).append(">");
			}
		}
		b.append(">");
		return b.toString();
	}

}
