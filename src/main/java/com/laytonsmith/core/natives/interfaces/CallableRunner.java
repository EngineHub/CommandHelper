package com.laytonsmith.core.natives.interfaces;

import com.laytonsmith.PureUtilities.Common.Annotations.InterfaceRunnerFor;
import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.core.MSVersion;
import com.laytonsmith.core.constructs.CClassType;
import com.laytonsmith.core.constructs.generics.GenericParameters;

/**
 *
 */
@InterfaceRunnerFor(Callable.class)
public class CallableRunner extends AbstractMixedInterfaceRunner {
	@Override
	public String docs() {
		return "A common interface for anything that can be executed via execute() or generally with parenthesis,"
				+ " such as @a().";
	}

	@Override
	public Version since() {
		return MSVersion.V3_3_5;
	}

	@Override
	public CClassType[] getSuperclasses() {
		return new CClassType[]{Mixed.TYPE};
	}

	@Override
	public CClassType[] getInterfaces() {
		return CClassType.EMPTY_CLASS_ARRAY;
	}

	@Override
	public GenericParameters getGenericParameters() {
		return null;
	}
}
