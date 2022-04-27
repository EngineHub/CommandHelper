package com.laytonsmith.core.constructs.generics;

import com.laytonsmith.core.constructs.CClassType;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.environments.Environment;

import java.util.Arrays;
import java.util.List;

/**
 * The UnqualifiedLeftHandGenericUse class, like the other Unqualified classes, is a simple wrapper around the
 * unqualified class names and other relevant information which is eventually qualified in a second pass, once the
 * relevant types have all had a chance to be defined.
 */
public class UnqualifiedLeftHandGenericUse {

	private final List<UnqualifiedConstraints> constraints;
	private final Target target;

	/**
	 * Constructs a new UnqualifiedLeftHandGenericUse object.
	 * @param t The code target where the usage is declared in code.
	 * @param constraints The UnqualifiedConstraints for this generic use.
	 */
	public UnqualifiedLeftHandGenericUse(Target t, UnqualifiedConstraints... constraints) {
		this.target = t;
		this.constraints = Arrays.asList(constraints);
	}

	public LeftHandGenericUse qualify(CClassType forType, Environment env) {
		Constraints[] c = new Constraints[constraints.size()];
		List<Constraints> declarationConstraints = forType.getGenericDeclaration().getConstraints();
		for(int i = 0; i < c.length; i++) {
			c[i] = constraints.get(i).qualify(ConstraintLocation.LHS, declarationConstraints.get(i), env);
		}
		return new LeftHandGenericUse(forType, target, env, c);
	}
}
