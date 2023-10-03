package com.laytonsmith.core.constructs.generics;

import com.laytonsmith.PureUtilities.Pair;
import com.laytonsmith.core.UnqualifiedClassName;
import com.laytonsmith.core.constructs.CClassType;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.environments.Environment;

import java.util.ArrayList;
import java.util.List;

/**
 * An UnqualifiedGenericParameters class represents the generic parameters at the intermediate stage of compilation,
 * before all classes are fully defined. Once all classes are defined, then the generics can be qualified.
 *
 * Currently, the parameters are stored internally as a mass string, and the compiler doesn't provide different code
 * targets for each individual component, so compilation errors are less specific than they could be. In the future,
 * Unqualified versions of each component can be created and stored with their own code targets, so that compiler errors
 * can be more specific, instead of highlighting the entire parameter set.
 */
public class UnqualifiedGenericTypeParameters {

	private final List<Pair<UnqualifiedClassName, UnqualifiedLeftHandGenericUse>> parameters = new ArrayList<>();

	/**
	 * Qualifies the UnqualifiedGenericParamaters. This may return null if the type does not have them, or if this was
	 * defined with a null parameter set.
	 *
	 * @param forType
	 * @param env
	 * @param t
	 * @return
	 * @throws ClassNotFoundException
	 */
	public GenericTypeParameters qualify(CClassType forType, Environment env, Target t) throws ClassNotFoundException {
		GenericTypeParameters.GenericTypeParametersBuilder p = GenericTypeParameters.emptyBuilder(forType, t, env);
		for(int i = 0; i < parameters.size(); i++) {
			Pair<UnqualifiedClassName, UnqualifiedLeftHandGenericUse> pair = parameters.get(i);
			UnqualifiedClassName ucn = pair.getKey();
			UnqualifiedLeftHandGenericUse ulhgu = pair.getValue();
			CClassType type = CClassType.getNakedClassType(ucn.getFQCN(env), env);
			LeftHandGenericUse lhgu = ulhgu.qualify(forType, env);
			if(p == null) {
				p = GenericTypeParameters.addParameter(forType, t, env, type, lhgu);
			} else {
				p.addParameter(type, lhgu);
			}
		}
		return p == null ? null : p.build();
	}

	public static final class UnqualifiedGenericParametersBuilder {

		UnqualifiedGenericTypeParameters p;

		private UnqualifiedGenericParametersBuilder(UnqualifiedGenericTypeParameters p) {
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
		public UnqualifiedGenericParametersBuilder addParameter(UnqualifiedClassName type,
				UnqualifiedLeftHandGenericUse genericStatement) {
			p.parameters.add(new Pair<>(type, genericStatement));
			return this;
		}

		/**
		 * Returns the fully constructed object.
		 *
		 * @return
		 */
		public UnqualifiedGenericTypeParameters build() {
			return p;
		}
	}

	/**
	 * Starts building a new UnqualifiedGenericParameterBuilder, and adds the first parameter. Each parameter consists
	 * of an UnqualifiedClassName, and optionally an UnqualifiedLeftHandGenericUse. For instance, in the statement
	 * <code>new A&lt;B&lt;? extends C&gt;&gt;</code> where A is the class being constructed, with signature
	 * <code>class A&lt;T&gt;</code> and B is a concrete class itself with a single template parameter, and C being
	 * another class, then this method would be called with the parameters <code>B</code> and a new instance of the
	 * LeftHandGenericUse class representing the constraint <code>? extends C</code>.
	 *
	 * @param type The concrete class type
	 * @param genericStatement The LHS generic statement for this parameter. This may be null if the type did not
	 * include a generic statement.
	 * @return A new UnqualifiedGenericParametersBuilder. Use build() to construct the final object.
	 */
	public static UnqualifiedGenericParametersBuilder addParameter(UnqualifiedClassName type,
			UnqualifiedLeftHandGenericUse genericStatement) {
		return new UnqualifiedGenericParametersBuilder(new UnqualifiedGenericTypeParameters())
				.addParameter(type, genericStatement);
	}

}
