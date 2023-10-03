package com.laytonsmith.core.constructs.generics;

import com.laytonsmith.PureUtilities.Either;
import com.laytonsmith.PureUtilities.ObjectHelpers;
import com.laytonsmith.PureUtilities.Pair;
import com.laytonsmith.core.constructs.CClassType;
import com.laytonsmith.core.constructs.LeftHandSideType;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.constructs.generics.constraints.ExactTypeConstraint;
import com.laytonsmith.core.environments.Environment;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A GenericTypeParameters object holds the either typename or concrete type parameters within a class or method
 * definition.
 */
public class GenericTypeParameters {

	@ObjectHelpers.StandardField
	private final List<Either<LeftHandSideType, Pair<String, Constraints>>> parameters;
	@ObjectHelpers.StandardField
	private final CClassType forType;
	private final Target target;
	private final Environment env;

	public static GenericTypeParameters fromTypes(CClassType forType, Target t, Environment env, ConcreteGenericParameter... constraints) {
		return new GenericTypeParameters(forType, t, env, Arrays.asList(constraints).stream()
				.map(item -> Either.left(item)).toList().toArray(Either[]::new));
	}

	public static GenericTypeParameters fromTypenames(CClassType forType, Target t, Environment env, Pair<String, Constraints>... typenames) {
		return new GenericTypeParameters(forType, t, env, Arrays.asList(typenames).stream()
				.map(item -> Either.right(item)).toList().toArray(Either[]::new));
	}

	public GenericTypeParameters(CClassType forType, Target t, Environment env, Either<LeftHandSideType, Pair<String, Constraints>>... parameters) {
		List<Constraints> validation = new ArrayList<>();
		for(Either<LeftHandSideType, Pair<String, Constraints>> param : parameters) {
			if(param.hasLeft()) {
				LeftHandSideType type = param.getLeft().get();
				Constraints c = new Constraints(t, ConstraintLocation.LHS,
						new ExactTypeConstraint(t, type));
				validation.add(c);
			} else {
				validation.add(param.getRight().get().getValue());
			}
		}
		ConstraintValidator.ValidateLHS(t, forType, validation, env);
		this.parameters = Arrays.asList(parameters);
		this.forType = forType;
		this.target = t;
		this.env = env;
	}

	/**
	 * Returns a list of the Constraints objects. Each Constraints object represents a single type parameter, though
	 * itself can contain multiple individual Constraint objects.
	 *
	 * @return
	 */
	public List<Either<LeftHandSideType, Pair<String, Constraints>>> getParameters() {
		return new ArrayList<>(parameters);
	}

	/**
	 * Returns the Constraints object at the specified location. Equivalent to {@code getParameters().get(location)}.
	 *
	 * @param location The parameter location, 0 indexed.
	 * @return The Constraints object governing the given parameter.
	 */
	public Constraints getParameter(int location) {
		Either<LeftHandSideType, Pair<String, Constraints>> param = parameters.get(location);
		Constraints c;
		if(param.hasLeft()) {
			LeftHandSideType type = param.getLeft().get();
			c = new Constraints(Target.UNKNOWN, ConstraintLocation.LHS,
					new ExactTypeConstraint(Target.UNKNOWN, type));
		} else {
			c = param.getRight().get().getValue();
		}
		return c;
	}

	/**
	 * Returns a List of LeftHandSideTypes. This will only work if all parameters are ConcreteGenericParameters,
	 * otherwise a RuntimeException is thrown.
	 *
	 * @return
	 */
	public List<LeftHandSideType> toLeftHandSideTypes() {
		List<LeftHandSideType> ret = new ArrayList<>();
		for(Either<LeftHandSideType, Pair<String, Constraints>> param : parameters) {
			if(param.hasRight()) {
				throw new RuntimeException("Cannot create LeftHandSideType from type parameters, contains typenames.");
			}
			ret.add(param.getLeft().get());
		}
		return ret;
	}

	/**
	 * @return Returns a LeftHandGenericUse equivalent, with each parameter converted into a single ExactTypeConstraint
	 * Constraints object. This only works if all parameters are ConcretGenericParameters.
	 */
	public LeftHandGenericUse toLeftHandGenericUse() {
		List<LeftHandGenericUseParameter> params = new ArrayList<>();
		int count = 0;
		for(Either<LeftHandSideType, Pair<String, Constraints>> param : parameters) {
			if(param.hasRight()) {
				throw new RuntimeException("Cannot create LeftHandSideType from type parameters, contains typenames.");
			}
			params.add(param.getLeft().get().toLeftHandGenericUse(forType, target, env, ConstraintLocation.RHS, count++));
		}
		return new LeftHandGenericUse(forType, target, env, params);
	}

	/**
	 * @return Returns true if any of the parameters are typenames, that is, they need to be resolved.
	 */
	public boolean hasTypenames() {
		for(Either<LeftHandSideType, Pair<String, Constraints>> param : parameters) {
			if(param.hasRight()) {
				return true;
			}
		}
		return false;
	}

	public boolean isInstanceof(LeftHandGenericUse lhgu, Environment env) {
		if(lhgu.getConstraints().size() != parameters.size()) {
			return false;
		}
		for(int i = 0; i < parameters.size(); i++) {
			Constraints superClass = lhgu.getConstraints().get(i);
			Either<LeftHandSideType, Pair<String, Constraints>> sub = parameters.get(i);
			if(sub.hasLeft()) {
				if(!superClass.withinBounds(sub.getLeft().get(), env)) {
					return false;
				}
			} else {
				List<String> errors = new ArrayList<>();
				if(!superClass.withinBounds(sub.getRight().get().getValue(), errors, env)) {
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * Returns the number of parameters in this declaration.
	 *
	 * @return
	 */
	public int getParameterCount() {
		return parameters.size();
	}

	/**
	 * The type these parameters are for.
	 *
	 * @return
	 */
	public CClassType getForType() {
		return this.forType;
	}

	public String toSimpleString() {
		StringBuilder b = new StringBuilder();
		boolean joinComma = false;
		for(Either<LeftHandSideType, Pair<String, Constraints>> c : parameters) {
			if(joinComma) {
				b.append(", ");
			}
			joinComma = true;
			if(c.hasLeft()) {
				LeftHandSideType type = c.getLeft().get();
				b.append(type.getSimpleName());
			} else {
				b.append(c.getRight().get().getKey());
			}
		}
		return b.toString();
	}

	@Override
	public String toString() {
		StringBuilder b = new StringBuilder();
		boolean joinComma = false;
		for(Either<LeftHandSideType, Pair<String, Constraints>> c : parameters) {
			if(joinComma) {
				b.append(", ");
			}
			joinComma = true;
			if(c.hasLeft()) {
				LeftHandSideType type = c.getLeft().get();
				b.append(type.toString());
			} else {
				b.append(c.getRight().get().getKey());
			}
		}
		return b.toString();
	}

	@Override
	@SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
	public boolean equals(Object that) {
		return ObjectHelpers.DoEquals(this, that);
	}

	@Override
	public int hashCode() {
		return ObjectHelpers.DoHashCode(this);
	}

	/**
	 * Returns a new builder and adds a parameter to it.
	 *
	 * @param forType
	 * @param t
	 * @param env
	 * @param type
	 * @return
	 */
	public static GenericTypeParametersBuilder addParameter(CClassType forType, Target t, Environment env, Either<LeftHandSideType, Pair<String, Constraints>> type) {
		return new GenericTypeParametersBuilder(forType, t, env).addParameter(type);
	}

	/**
	 * Returns a new builder and adds a parameter to it.
	 *
	 * @param forType
	 * @param t
	 * @param env
	 * @param typename
	 * @param constraints
	 * @return
	 */
	public static GenericTypeParametersBuilder addParameter(CClassType forType, Target t, Environment env, String typename, Constraints constraints) {
		return new GenericTypeParametersBuilder(forType, t, env).addParameter(typename, constraints);
	}

	/**
	 * Returns a new builder and adds a parameter to it.
	 *
	 * @param forType
	 * @param t
	 * @param env
	 * @param parameter
	 * @return
	 */
	public static GenericTypeParametersBuilder addParameter(CClassType forType, Target t, Environment env, ConcreteGenericParameter parameter) {
		return new GenericTypeParametersBuilder(forType, t, env).addParameter(parameter);
	}

	/**
	 * Returns a new builder and adds a parameter to it.
	 *
	 * @param forType
	 * @param t
	 * @param env
	 * @param type
	 * @param lhgu
	 * @return
	 */
	public static GenericTypeParametersBuilder addParameter(CClassType forType, Target t, Environment env, CClassType type, LeftHandGenericUse lhgu) {
		return new GenericTypeParametersBuilder(forType, t, env).addParameter(type, lhgu);
	}

	/**
	 * Returns a new, empty builder.
	 *
	 * @param forType
	 * @param t
	 * @param env
	 * @return
	 */
	public static GenericTypeParametersBuilder emptyBuilder(CClassType forType, Target t, Environment env) {
		return new GenericTypeParametersBuilder(forType, t, env);
	}

	/**
	 * Returns a new, empty builder. Note that this builder can only be used to add native types to.
	 *
	 * @param forType
	 * @return
	 */
	public static GenericTypeParametersBuilder nativeBuilder(CClassType forType) {
		return new GenericTypeParametersBuilder(forType, Target.UNKNOWN, null);
	}

	public static final class GenericTypeParametersBuilder {

		List<Either<LeftHandSideType, Pair<String, Constraints>>> p = new ArrayList<>();
		CClassType forType;
		Target target;
		Environment env;

		public GenericTypeParametersBuilder(CClassType forType, Target t, Environment env) {
			this.forType = forType;
			this.target = t;
			this.env = env;
		}

		/**
		 * Adds a new parameter.
		 *
		 * @param type The Either type.
		 * @return this, for easy chaining. Use build() to construct the final object.
		 */
		public GenericTypeParametersBuilder addParameter(Either<LeftHandSideType, Pair<String, Constraints>> type) {
			p.add(type);
			return this;
		}

		/**
		 * Adds a new parameter.
		 *
		 * @param typename The typename.
		 * @param constraints The constraints governing this typename. May be null if this should be inherited, in which
		 * case the typename is looked up in the forType, and the associated Constraints object is used.
		 * @return this, for easy chaining. Use build() to construct the final object.
		 */
		public GenericTypeParametersBuilder addParameter(String typename, Constraints constraints) {
//			if(constraints == null) {
//				for(Constraints c : forType.getGenericDeclaration().getConstraints()) {
//					if(typename.equals(c.getTypeName())) {
//						constraints = c;
//						break;
//					}
//				}
//			}
			return addParameter(Either.right(new Pair<>(typename, constraints)));
		}

		/**
		 * Adds a new parameter.
		 *
		 * @param type The type to add.
		 * @return this, for easy chaining. Use build() to construct the final object.
		 */
		public GenericTypeParametersBuilder addParameter(ConcreteGenericParameter type) {
			return addParameter(type.asLeftHandSideType());
		}

		/**
		 * Adds a new parameter.
		 *
		 * @param type The type to add.
		 * @return this, for easy chaining. Use build() to construct the final object.
		 */
		public GenericTypeParametersBuilder addParameter(LeftHandSideType type) {
			return addParameter(Either.left(type));
		}

		/**
		 * Shorthand for adding a ConcreteGenericParameter. Note that for generic declarations where the class is used
		 * as a generic parameter, you must instead use {@link CClassType#RECURSIVE_DEFINITION}, which is then replaced
		 * by the actual class type during building. Otherwise, using the type directly with Class.TYPE will fail, since
		 * by definition during instantiation, it is still null.
		 *
		 * @param type
		 * @param lhgu
		 * @return
		 */
		public GenericTypeParametersBuilder addParameter(CClassType type, LeftHandGenericUse lhgu) {
			return addParameter(new ConcreteGenericParameter(type, lhgu, target, env));
		}

		/**
		 * Returns if this builder object is empty. If it is, calling build causes an error, so it's important to check
		 * this first if you are using this generically.
		 *
		 * @return
		 */
		public boolean isEmpty() {
			return p.isEmpty();
		}

		/**
		 * Returns the fully constructed object.
		 *
		 * @return
		 */
		public GenericTypeParameters build() {
			if(p.isEmpty()) {
				throw new Error("Empty parameter builders cannot be used. Check for this condition with isEmpty()");
			}
			return new GenericTypeParameters(forType, target, env, p.toArray(Either[]::new));
		}

		public GenericTypeParameters buildWithSubclassDefinition(CClassType t) {
			List<Either<LeftHandSideType, Pair<String, Constraints>>> parameters = new ArrayList<>();
			for(Either<LeftHandSideType, Pair<String, Constraints>> val : p) {
				if(val.hasLeft()) {
					if(CClassType.RECURSIVE_DEFINITION.getFQCN().getFQCN().equals(val.getLeft().get().val())) {
						parameters.add(Either.left(t.asLeftHandSideType()));
					} else {
						parameters.add(val);
					}
				} else {
					Pair<String, Constraints> pair = val.getRight().get();
					if(pair.getValue() == null) {
						Constraints c = null;
						for(Constraints r : t.getGenericDeclaration().getConstraints()) {
							if(pair.getKey().equals(r.getTypeName())) {
								c = r;
								break;
							}
						}
						pair = new Pair<>(pair.getKey(), c);
					}
					parameters.add(Either.right(pair));
				}
			}
			return new GenericTypeParameters(forType, target, env, parameters.toArray(Either[]::new));
		}
	}
}
