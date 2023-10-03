package com.laytonsmith.core.constructs.generics;

import com.laytonsmith.core.constructs.generics.constraints.ExactTypeConstraint;
import com.laytonsmith.core.constructs.generics.constraints.Constraint;
import com.laytonsmith.PureUtilities.ObjectHelpers;
import com.laytonsmith.PureUtilities.ObjectHelpers.StandardField;
import com.laytonsmith.core.compiler.signature.FunctionSignature;
import com.laytonsmith.core.constructs.CClassType;
import com.laytonsmith.core.constructs.LeftHandSideType;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.exceptions.CRE.CREGenericConstraintException;

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
	private final List<LeftHandSideType> parameters;

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

	public static final class GenericParametersBuilder {

		private final GenericParameters p;

		private GenericParametersBuilder(GenericParameters p, GenericDeclaration forType) {
			this.p = p;
			p.genericDeclaration = forType;
		}

		/**
		 * Adds a new parameter.Each parameter consists of a CClassType, and optionally a LeftHandGenericUse.For
		 * instance, in the statement <code>new A&lt;B&lt;? extends C&gt;&gt;</code> where A is the class being
		 * constructed, with signature <code>class A&lt;T&gt;</code> and B is a concrete class itself with a single
		 * template parameter, and C being another class, then this method would be called with the parameters
		 * <code>B</code> and a new instance of the LeftHandGenericUse class representing the constraint
		 * <code>? extends C</code>.
		 *
		 * @param type The concrete class type
		 * @param genericStatement The LHS generic statement for this parameter. This may be null if the type did not
		 * include a generic statement.
		 * @param env The environment
		 * @param t The code target
		 * @return this, for easy chaining. Use build() to construct the final object.
		 * @throws CREGenericConstraintException If the generic statement does not validate against the type.
		 */
		public GenericParametersBuilder addParameter(CClassType type, LeftHandGenericUse genericStatement, Environment env, Target t) {
			return addParameter(new ConcreteGenericParameter(type, genericStatement, t, env));
		}

		/**
		 * Adds a new ConcreteGenericParameter.
		 *
		 * @param type
		 * @return
		 */
		public GenericParametersBuilder addParameter(ConcreteGenericParameter type) {
			return addParameter(type.asLeftHandSideType());
		}

		/**
		 * Add a LeftHandSideType parameter.
		 *
		 * @param type
		 * @return
		 */
		public GenericParametersBuilder addParameter(LeftHandSideType type) {
			p.parameters.add(type);
			return this;
		}

		/**
		 * Adds a new native parameter. Unlike the normal method, this will cause an error if the parameters are
		 * incorrect.
		 *
		 * @param nativeType
		 * @param nativeGenericStatement
		 * @return
		 */
		public GenericParametersBuilder addNativeParameter(CClassType nativeType, LeftHandGenericUse nativeGenericStatement) {
			return addParameter(nativeType, nativeGenericStatement, null, Target.UNKNOWN);
		}

		/**
		 * Returns if this builder object is empty. If it is, calling build causes an error, so it's important to check
		 * this first if you are using this generically.
		 *
		 * @return
		 */
		public boolean isEmpty() {
			return p.parameters.isEmpty();
		}

		/**
		 * If the parameter set only contains native classes, this method can be used instead, which will cause Errors
		 * instead of user compiler errors.
		 *
		 * @return
		 */
		public GenericParameters buildNative() {
			return build(Target.UNKNOWN, null);
		}

		/**
		 * Returns the fully constructed object.
		 *
		 * @param t
		 * @param env
		 * @return
		 */
		public GenericParameters build(Target t, Environment env) {
			if(p.parameters.isEmpty()) {
				throw new Error("Empty parameter builders cannot be used. Check for this condition with isEmpty()");
			}
			ConstraintValidator.ValidateParametersToDeclaration(t, env, p, p.genericDeclaration, null);
			return p;
		}

		/**
		 * If these parameters are being built before the type is known (during compilation, mainly) then forType may be
		 * set to null, and then this method called. Calling the normal build methods if forType is null will cause an
		 * error.
		 * <p>
		 * These parameters must be validated later independently.
		 *
		 * @return
		 */
		public GenericParameters buildWithoutValidation() {
			if(p.parameters.isEmpty()) {
				throw new Error("Empty parameter builders cannot be used. Check for this condition with isEmpty()");
			}
			return p;
		}
	}

	/**
	 * Begins construction of a new GenericParameters object, which represents the RHS of the generic declaration.The
	 * actual GenericDeclaration object is passed in in order to validate the types against the constraints.Each
	 * instance of a class which has a GenericDeclaration will have one of these objects in it, associated with that
	 * particular instance. This data is not lost after compilation, and types are reified for runtime use.
	 * <p>
	 * Each parameter consists of a CClassType, and optionally a LeftHandGenericUse. For instance, in the statement
	 * <code>new A&lt;B&lt;? extends C&gt;&gt;</code> where A is the class being constructed, with signature
	 * <code>class A&lt;T&gt;</code> and B is a concrete class itself with a single template parameter, and C being
	 * another class, then this method would be called with the parameters <code>B</code> and a new instance of the
	 * LeftHandGenericUse class representing the constraint <code>? extends C</code>.
	 *
	 * @param forType The type that these parameters are being added to.
	 * @param type The concrete class type
	 * @param genericStatement The LHS generic statement for this parameter. This may be null if the type did not
	 * include a generic statement.
	 * @param env The environment
	 * @param t The code target
	 * @return this, for easy chaining. Use build() to construct the final object.
	 * @throws CREGenericConstraintException If the generic statement does not validate against the type.
	 */
	public static GenericParametersBuilder addParameter(CClassType forType, CClassType type, LeftHandGenericUse genericStatement, Environment env, Target t) {
		return emptyBuilder(forType).addParameter(type, genericStatement, env, t);
	}

	/**
	 * Begins construction of a new GenericParameters object.This should only be used with native, hardcoded classes, as
	 * incorrect usage will cause an Error.
	 *
	 * @param forType The type that these parameters are being added to.
	 * @param type
	 * @param nativeGenericStatement
	 * @return
	 */
	public static GenericParametersBuilder addNativeParameter(CClassType forType, CClassType type,
			LeftHandGenericUse nativeGenericStatement) {
		return emptyBuilder(forType).addParameter(type, nativeGenericStatement, null, Target.UNKNOWN);
	}

	/**
	 * Returns an empty builder. Note that calling build on an empty builder is an error.
	 *
	 * @param forType The type that these parameters are being added to.
	 * @return
	 */
	public static GenericParametersBuilder emptyBuilder(CClassType forType) {
		GenericParameters gp = new GenericParameters();
		return new GenericParametersBuilder(gp, forType == null ? null : forType.getGenericDeclaration());
	}

	/**
	 * Returns an empty builder. Note that calling build on an empty builder is an error.
	 *
	 * @param forSignature The function signature that these are targetting.
	 * @return
	 */
	public static GenericParametersBuilder emptyBuilder(FunctionSignature forSignature) {
		GenericParameters gp = new GenericParameters();
		return new GenericParametersBuilder(gp, forSignature.getGenericDeclaration());
	}

	private GenericParameters() {
		parameters = new ArrayList<>();
	}

	/**
	 * Returns a list of the parameters that were defined on this class type.
	 *
	 * @return
	 */
	public List<LeftHandSideType> getParameters() {
		return new ArrayList<>(parameters);
	}

	/**
	 * Converts this concrete GenericParameter object into a LeftHandGenericUse equivalent.It uses the
	 * ExactTypeConstraint constraint for all parameters, but makes for easier comparisons.
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
			LeftHandSideType parameter = parameters.get(i);
			Constraint c = new ExactTypeConstraint(Target.UNKNOWN, parameter);
			constraints[i] = new Constraints(Target.UNKNOWN, ConstraintLocation.LHS, c);
		}
		LeftHandGenericUse lhgu = new LeftHandGenericUse(forType, Target.UNKNOWN, env, constraints);
		return lhgu;
	}

	/**
	 * Returns a new GenericTypeParameters equivalent object. Note that all the types will be single ExactType
	 * constraints, rather than typenames.
	 *
	 * @param forType The type that these generics will be associated with.
	 * @param t The code target where these were defined, for validation errors.
	 * @param env The environment.
	 * @return
	 */
	public GenericTypeParameters toGenericTypeParameters(CClassType forType, Target t, Environment env) {
		GenericTypeParameters.GenericTypeParametersBuilder builder = GenericTypeParameters.emptyBuilder(forType, t, env);
		for(LeftHandSideType lhst : parameters) {
			builder.addParameter(lhst);
		}
		return builder.build();
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
		for(LeftHandSideType p : parameters) {
			if(doComma) {
				b.append(", ");
			}
			doComma = true;
			b.append(p.toString());
		}
		b.append(">");
		return b.toString();
	}

}
