package com.laytonsmith.core.constructs;

import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.annotations.typeof;
import com.laytonsmith.core.MSVersion;
import com.laytonsmith.core.constructs.generics.ConstraintLocation;
import com.laytonsmith.core.constructs.generics.Constraints;
import com.laytonsmith.core.constructs.generics.GenericDeclaration;
import com.laytonsmith.core.constructs.generics.GenericParameters;
import com.laytonsmith.core.constructs.generics.GenericTypeParameters;
import com.laytonsmith.core.constructs.generics.constraints.UnboundedConstraint;
import com.laytonsmith.core.constructs.generics.constraints.VariadicTypeConstraint;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.exceptions.CancelCommandException;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.exceptions.ProgramFlowManipulationException;
import com.laytonsmith.core.natives.interfaces.Callable;
import com.laytonsmith.core.natives.interfaces.Mixed;

/**
 *
 */
@typeof("ms.lang.NativeClosure")
public class CNativeClosure extends Construct implements Callable {

	private static final Constraints RETURN_TYPE = new Constraints(Target.UNKNOWN, ConstraintLocation.DEFINITION, new UnboundedConstraint(Target.UNKNOWN, "ReturnType"));
	private static final Constraints PARAMETERS = new Constraints(Target.UNKNOWN, ConstraintLocation.DEFINITION, new VariadicTypeConstraint(Target.UNKNOWN, "Parameters"));

	@SuppressWarnings("FieldNameHidesFieldInSuperclass")
	public static final CClassType TYPE = CClassType.getWithGenericDeclaration(CNativeClosure.class,
			new GenericDeclaration(Target.UNKNOWN, RETURN_TYPE, PARAMETERS))
			.withSuperParameters(GenericTypeParameters.nativeBuilder(Callable.TYPE)
				.addParameter("ReturnType", RETURN_TYPE)
				.addParameter("Parameters", PARAMETERS));

	public static interface ClosureRunnable {
		Mixed execute(Target t, Environment env, Mixed... args);
	}

	private final ClosureRunnable runnable;
	private final Environment env;

	public CNativeClosure(ClosureRunnable runnable, Environment env) {
		// UNKNOWN because these cannot be constructed in user code.
		super("native closure", ConstructType.CLOSURE, Target.UNKNOWN);
		this.runnable = runnable;
		this.env = env;
	}

	@Override
	public boolean isDynamic() {
		return true;
	}

	@Override
	public Mixed executeCallable(Environment environment, Target t, Mixed... values) throws ConfigRuntimeException, ProgramFlowManipulationException, CancelCommandException {
		return runnable.execute(t, environment, values);
	}

	@Override
	public CClassType[] getSuperclasses() {
		return CClassType.EMPTY_CLASS_ARRAY;
	}

	@Override
	public CClassType[] getInterfaces() {
		return new CClassType[]{Callable.TYPE};
	}

	@Override
	public String docs() {
		return "Represents a natively created closure. These cannot be created in code, but implement Callable, and"
				+ " so can be passed around in user code when obtained through native functions.";
	}

	@Override
	public Version since() {
		return MSVersion.V3_3_5;
	}

	@Override
	public Environment getEnv() {
		return this.env;
	}

	@Override
	public GenericParameters getGenericParameters() {
		return null;
	}


}
