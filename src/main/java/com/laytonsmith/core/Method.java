package com.laytonsmith.core;

import com.laytonsmith.core.natives.interfaces.Callable;
import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.annotations.typeof;
import com.laytonsmith.core.constructs.CClassType;
import com.laytonsmith.core.constructs.Construct;
import com.laytonsmith.core.constructs.Target;
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
import com.laytonsmith.core.natives.interfaces.Mixed;
import java.util.Arrays;

/**
 * A method is the foundational class for containing information about a method defined in a class. These may include
 * interfaces, where the method isn't actually defined, abstract methods, and concrete classes.
 */
@typeof("ms.lang.Method")
public class Method extends Construct implements Callable {

	private static final Constraints RETURN_TYPE = new Constraints(Target.UNKNOWN, ConstraintLocation.DEFINITION, new UnboundedConstraint(Target.UNKNOWN, "ReturnType"));
	private static final Constraints PARAMETERS = new Constraints(Target.UNKNOWN, ConstraintLocation.DEFINITION, new VariadicTypeConstraint(Target.UNKNOWN, "Parameters"));

	@SuppressWarnings("FieldNameHidesFieldInSuperclass")
	public static final CClassType TYPE = CClassType.getWithGenericDeclaration(Method.class,
			new GenericDeclaration(Target.UNKNOWN, RETURN_TYPE, PARAMETERS))
			.withSuperParameters(GenericTypeParameters.nativeBuilder(Callable.TYPE)
				.addParameter("ReturnType", RETURN_TYPE)
				.addParameter("Parameters", PARAMETERS));

	private final CClassType returnType;
	private final String name;
	private final CClassType[] parameters;
	private final ParseTree tree;
	private final Environment env;

	public Method(Target t, Environment env, CClassType returnType, String name, CClassType[] parameters, ParseTree tree) {
		super(returnType + " " + name + " " + Arrays.toString(parameters), ConstructType.FUNCTION, t);
		this.returnType = returnType;
		this.name = name;
		this.parameters = parameters;
		this.tree = tree;
		this.env = env;
	}

	@Override
	public Mixed executeCallable(Environment env, Target t, Mixed... values)
			throws ConfigRuntimeException, ProgramFlowManipulationException, CancelCommandException {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public boolean isDynamic() {
		return false;
	}

	@Override
	public String docs() {
		return "A Method is not instantiatable in the traditional sense, however, a Method can"
				+ " be defined within a class. This class represents that definition.";
	}

	@Override
	public CClassType[] getInterfaces() {
		return new CClassType[]{Callable.TYPE};
	}

	@Override
	public CClassType[] getSuperclasses() {
		return new CClassType[]{Mixed.TYPE};
	}

	@Override
	public Version since() {
		return MSVersion.V3_3_4;
	}

	public CClassType[] getParameters() {
		return parameters;
	}

	public String getMethodName() {
		return name;
	}

	public CClassType getReturnType() {
		return returnType;
	}

	public ParseTree getTree() {
		return tree;
	}

	@Override
	public GenericParameters getGenericParameters() {
		return null;
	}

	@Override
	public Environment getEnv() {
		return this.env;
	}
}
