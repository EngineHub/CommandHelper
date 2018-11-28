package com.laytonsmith.core.constructs;

import com.laytonsmith.PureUtilities.Common.Annotations.InterfaceRunnerFor;
import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.core.MSVersion;
import com.laytonsmith.core.natives.interfaces.AbstractMixedInterfaceRunner;
import com.laytonsmith.core.natives.interfaces.Mixed;

/**
 *
 * @author cailin
 */
@InterfaceRunnerFor(CPrimitive.class)
public class CPrimitiveRunner extends AbstractMixedInterfaceRunner {

	@Override
	public String docs() {
		return "A primitive is any non-object and non-array data type. All primitives are pass by value.";
	}

	@Override
	public Version since() {
		return MSVersion.V3_0_1;
	}

	@Override
	public CClassType[] getSuperclasses() {
		return new CClassType[]{Mixed.TYPE};
	}

	@Override
	public CClassType[] getInterfaces() {
		return CClassType.EMPTY_CLASS_ARRAY;
	}
}
