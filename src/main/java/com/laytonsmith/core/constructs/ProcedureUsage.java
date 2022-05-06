package com.laytonsmith.core.constructs;

import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.annotations.typeof;
import com.laytonsmith.core.MSVersion;
import com.laytonsmith.core.Procedure;
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
import com.laytonsmith.core.objects.ObjectModifier;
import com.laytonsmith.core.objects.ObjectType;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;

/**
 *
 *
 */
@typeof("ms.lang.Procedure")
public class ProcedureUsage extends Construct implements Callable {

	private static final Constraints RETURN_TYPE = new Constraints(Target.UNKNOWN, ConstraintLocation.DEFINITION, new UnboundedConstraint(Target.UNKNOWN, "ReturnType"));
	private static final Constraints PARAMETERS = new Constraints(Target.UNKNOWN, ConstraintLocation.DEFINITION, new VariadicTypeConstraint(Target.UNKNOWN, "Parameters"));

	@SuppressWarnings("FieldNameHidesFieldInSuperclass")
	public static final CClassType TYPE = CClassType.getWithGenericDeclaration(ProcedureUsage.class,
			new GenericDeclaration(Target.UNKNOWN, RETURN_TYPE, PARAMETERS))
			.withSuperParameters(GenericTypeParameters.nativeBuilder(Callable.TYPE)
				.addParameter("ReturnType", RETURN_TYPE)
				.addParameter("Parameters", PARAMETERS));

	private final Procedure proc;
	private final Environment env;

	public ProcedureUsage(Procedure proc, Environment env, Target t) {
		super(proc.getName(), ConstructType.FUNCTION, t);
		this.proc = proc;
		this.env = env;
	}

	@Override
	public boolean isDynamic() {
		return true;
	}

	@Override
	public Version since() {
		return MSVersion.V3_3_5;
	}

	@Override
	public String docs() {
		return "Represents a first class reference to a proc call. The reference is based on the value at time"
				+ " of initial reference creation, and can be sent to execute and otherwise stored in variables."
				+ " This is the preferred usage over using call_proc, though that is still useful in some cases,"
				+ " and so is sometimes more appropriate. Note that in general, it is possible to bypass access"
				+ " modifiers if a reference to a proc is leaked incorrectly. This is sometimes desired, but"
				+ " in general is probably undesirable.";
	}

	@Override
	public CClassType[] getSuperclasses() {
		return new CClassType[]{Mixed.TYPE};
	}

	@Override
	public CClassType[] getInterfaces() {
		return new CClassType[]{Callable.TYPE};
	}

	@Override
	public ObjectType getObjectType() {
		return ObjectType.CLASS;
	}

	@Override
	public Set<ObjectModifier> getObjectModifiers() {
		return EnumSet.of(ObjectModifier.FINAL, ObjectModifier.IMMUTABLE, ObjectModifier.NATIVE);
	}

	@Override
	public Mixed executeCallable(Environment environment, Target t, Mixed... values) throws ConfigRuntimeException,
			ProgramFlowManipulationException, CancelCommandException {
		return proc.execute(Arrays.asList(values), environment, t);
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
