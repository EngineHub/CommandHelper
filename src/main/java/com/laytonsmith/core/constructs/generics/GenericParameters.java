package com.laytonsmith.core.constructs.generics;

import com.laytonsmith.PureUtilities.ObjectHelpers;
import com.laytonsmith.PureUtilities.ObjectHelpers.StandardField;
import com.laytonsmith.PureUtilities.Pair;
import com.laytonsmith.core.constructs.CClassType;
import com.laytonsmith.core.environments.Environment;

import java.util.ArrayList;
import java.util.List;

/**
 * This class represents the RHS of a class type definition with generics. For instance, in the statement
 * <code>new A&lt;B&gt;</code>, this represents B. In general, this contains only concrete classes, however
 * these classes themselves may have generic parameters, in which case they will contain the LHS information
 * for those parameters. However, at a top line level, everything maps to concrete class types.
 * <p>
 * Note that in general, it's impossible to construct an instance of this with no parameters, and this is intentional.
 * For instances of classes without generics, or those with generics, but whose instance used diamond inheritance,
 * these are handled by representing the GenericParameters object with null, and requires special handling in
 * general anyways.
 */
@StandardField
public final class GenericParameters {

	private GenericDeclaration genericDeclaration;
	private List<Pair<CClassType, LeftHandGenericUse>> parameters;

	/**
	 * Returns true if this parameter set is a subtype of the LHS constraints. This does not check the base types
	 * against each other, so this can't be used in place of a full instanceof check, as it merely compares generics
	 * themselves.
	 * @param generics The generics to check if this is a subtype of.
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
		 * instance, in the statement <code>new A&lt;B&lt;? extends C&gt;&gt;</code> where A is
		 * the class being constructed, with signature <code>class A&lt;T&gt;</code>
		 * and B is a concrete class itself with a single template parameter, and C being another class, then
		 * this method would be called with the parameters <code>B</code> and a new instance of the LeftHandGenericUse
		 * class representing the constraint <code>? extends C</code>.
		 * @param type The concrete class type
		 * @param genericStatement The LHS generic statement for this parameter. This may be null if the type did not
		 *                         include a generic statement.
		 * @return this, for easy chaining. Use build() to construct the final object.
		 */
		public GenericParametersBuilder addParameter(CClassType type, LeftHandGenericUse genericStatement) {
			p.parameters.add(new Pair<>(type, genericStatement));
			return this;
		}

		/**
		 * Returns the fully constructed object.
		 * @return
		 */
		public GenericParameters build() {
			return p;
		}
	}

	/**
	 * Begins construction of a new GenericParameters object, which represents the RHS of the generic declaration. The actual
	 * GenericDeclaration object is passed in in order to validate the types against the constraints. Each instance
	 * of a class which has a GenericDeclaration will have one of these objects in it, associated with that particular
	 * instance. This data is not lost after compilation, and types are reified for runtime use.
	 * <p>
	 * Each parameter consists of a CClassType, and optionally a LeftHandGenericUse. For
	 * instance, in the statement <code>new A&lt;B&lt;? extends C&gt;&gt;</code> where A is
	 * the class being constructed, with signature <code>class A&lt;T&gt;</code>
	 * and B is a concrete class itself with a single template parameter, and C being another class, then
	 * this method would be called with the parameters <code>B</code> and a new instance of the LeftHandGenericUse
	 * class representing the constraint <code>? extends C</code>.
	 * @param type The concrete class type
	 * @param genericStatement The LHS generic statement for this parameter. This may be null if the type did not
	 *                         include a generic statement.
	 * @return this, for easy chaining. Use build() to construct the final object.
	 */
	public static GenericParametersBuilder addParameter(CClassType type, LeftHandGenericUse genericStatement) {
		GenericParameters gp = new GenericParameters();
		return new GenericParametersBuilder(gp).addParameter(type, genericStatement);
	}

	private GenericParameters() {
		parameters = new ArrayList<>();
	}

	/**
	 * Returns a list of the parameters that were defined on this class type.
	 * @return
	 */
	public List<Pair<CClassType, LeftHandGenericUse>> getParameters() {
		return new ArrayList<>(parameters);
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
