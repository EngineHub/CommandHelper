package com.laytonsmith.core.objects;

import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.annotations.typeof;
import com.laytonsmith.core.Callable;
import com.laytonsmith.core.MSVersion;
import com.laytonsmith.core.ParseTree;
import com.laytonsmith.core.UnqualifiedClassName;
import com.laytonsmith.core.constructs.CClassType;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.exceptions.CancelCommandException;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.exceptions.ProgramFlowManipulationException;
import com.laytonsmith.core.natives.interfaces.Mixed;
import java.util.Set;

/**
 * A method is the foundational class for containing information about a method defined in a class. These may include
 * interfaces, where the method isn't actually defined, abstract methods, and
 */
@typeof("ms.lang.Method")
public class Method extends ElementDefinition implements Callable {

	@SuppressWarnings("FieldNameHidesFieldInSuperclass")
	public static final CClassType TYPE = CClassType.get(Method.class);

	// Need a new Parameter class
//	private final CClassType[] parameters;

	public Method(
			AccessModifier accessModifier,
			Set<ElementModifier> elementModifiers,
			UnqualifiedClassName definedIn,
			UnqualifiedClassName type,
			String name,
			ParseTree code,
			String signature,
			ConstructType constructType,
			Target t
	) {
		super(
			accessModifier,
			elementModifiers,
			definedIn,
			type,
			name,
			code,
			signature,
			constructType,
			t
		);
	}

	@Override
	public Mixed executeCallable(Environment env, Target t, Mixed... values)
			throws ConfigRuntimeException, ProgramFlowManipulationException, CancelCommandException {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public String docs() {
		return "A Method is not instantiatable in the traditional sense, however, a Method can"
				+ " be defined within a class. This class represents that definition.";
	}

	@Override
	public CClassType[] getInterfaces() {
		return CClassType.EMPTY_CLASS_ARRAY;
	}

	@Override
	public CClassType[] getSuperclasses() {
		return new CClassType[]{Mixed.TYPE};
	}

	@Override
	public Version since() {
		return MSVersion.V3_3_4;
	}

//	public CClassType[] getParameters() {
//		return parameters;
//	}

}
