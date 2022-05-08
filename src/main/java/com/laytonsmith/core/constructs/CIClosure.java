package com.laytonsmith.core.constructs;

import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.annotations.typeof;
import com.laytonsmith.core.MSVersion;
import com.laytonsmith.core.ParseTree;
import com.laytonsmith.core.constructs.generics.ConstraintLocation;
import com.laytonsmith.core.constructs.generics.Constraints;
import com.laytonsmith.core.constructs.generics.GenericDeclaration;
import com.laytonsmith.core.constructs.generics.GenericParameters;
import com.laytonsmith.core.constructs.generics.GenericTypeParameters;
import com.laytonsmith.core.constructs.generics.constraints.UnboundedConstraint;
import com.laytonsmith.core.constructs.generics.constraints.VariadicTypeConstraint;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.natives.interfaces.Mixed;

/**
 *
 */
@typeof("ms.lang.iclosure")
public class CIClosure extends CClosure {

	private static final Constraints RETURN_TYPE = new Constraints(Target.UNKNOWN, ConstraintLocation.DEFINITION, new UnboundedConstraint(Target.UNKNOWN, "ReturnType"));
	private static final Constraints PARAMETERS = new Constraints(Target.UNKNOWN, ConstraintLocation.DEFINITION, new VariadicTypeConstraint(Target.UNKNOWN, "Parameters"));

	@SuppressWarnings("FieldNameHidesFieldInSuperclass")
	public static final CClassType TYPE = CClassType.getWithGenericDeclaration(CIClosure.class,
			new GenericDeclaration(Target.UNKNOWN, RETURN_TYPE, PARAMETERS))
			.withSuperParameters(GenericTypeParameters.nativeBuilder(CClosure.TYPE)
				.addParameter("ReturnType", RETURN_TYPE)
				.addParameter("Parameters", PARAMETERS));

	public CIClosure(ParseTree node, Environment env, LeftHandSideType returnType, String[] names, Mixed[] defaults,
			Boolean[] isVarArgs, LeftHandSideType[] types, Target t) {
		super(node, env, returnType, names, defaults, isVarArgs, types, t);
	}

	@Override
	public String docs() {
		return "An iclosure is an isolated scope closure. This is more efficient than a regular closure, but it"
				+ " doesn't allow"
				+ " for access of variables outside of the scope of the closure, other than values passed in.";
	}

	@Override
	public Version since() {
		return MSVersion.V3_3_1;
	}

	@Override
	public CClassType[] getSuperclasses() {
		return new CClassType[]{CClosure.TYPE};
	}

	@Override
	public CClassType[] getInterfaces() {
		return CClassType.EMPTY_CLASS_ARRAY;
	}

	@Override
	public GenericParameters getGenericParameters() {
		GenericParameters.GenericParametersBuilder builder = GenericParameters.emptyBuilder(TYPE);
		builder.addParameter(returnType);
		for(LeftHandSideType type : types) {
			builder.addParameter(type);
		}
		return builder.buildWithoutValidation();
	}

}
