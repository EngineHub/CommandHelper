package com.laytonsmith.core.constructs.generics;

import com.laytonsmith.PureUtilities.ObjectHelpers;
import com.laytonsmith.PureUtilities.ObjectHelpers.StandardField;
import com.laytonsmith.PureUtilities.Pair;
import com.laytonsmith.core.constructs.CClassType;

import java.util.ArrayList;
import java.util.List;

/**
 * This class represents the RHS of a class type definition with generics. For instance, in the statement
 * <code>new A&lt;B&gt;</code>, this represents B. In general, this contains only concrete classes, however
 * these classes themselves may have generic parameters, in which case they will contain the LHS information
 * for those parameters. However, at a top line level, everything maps to concrete class types.
 */
@StandardField
public class GenericParameters {

	private GenericDeclaration genericDeclaration;
	List<Pair<CClassType, LeftHandGenericUse>> parameters;

	public static class GenericParametersBuilder1 {
		GenericParameters p;
		private GenericParametersBuilder1(GenericParameters p) {
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
		public GenericParametersBuilder1 addParameter(CClassType type, LeftHandGenericUse genericStatement) {
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
	 * @param declaration The generic declaration
	 */
	public static GenericParametersBuilder1 start(GenericDeclaration declaration) {
		GenericParameters gp = new GenericParameters();
		gp.genericDeclaration = declaration;
		return new GenericParametersBuilder1(gp);
	}

	private GenericParameters() {
		parameters = new ArrayList<>();
	}

	public GenericDeclaration getGenericDeclaration() {
		return this.genericDeclaration;
	}

	public List<Pair<CClassType, LeftHandGenericUse>> getParameters() {
		return parameters;
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
		return ObjectHelpers.DoToString(this);
	}

}
