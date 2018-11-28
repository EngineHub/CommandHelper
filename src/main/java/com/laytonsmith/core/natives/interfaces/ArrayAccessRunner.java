package com.laytonsmith.core.natives.interfaces;

import com.laytonsmith.PureUtilities.Common.Annotations.InterfaceRunnerFor;
import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.core.MSVersion;
import com.laytonsmith.core.constructs.CClassType;

/**
 *
 * @author cailin
 */
@InterfaceRunnerFor(ArrayAccess.class)
public class ArrayAccessRunner extends AbstractMixedInterfaceRunner {

	@Override
	public String docs() {
		return "Provides access to an object using the square bracket notation.";
	}

	@Override
	public Version since() {
		return MSVersion.V3_3_1;
	}

	@Override
	public CClassType[] getSuperclasses() {
		return new CClassType[]{Mixed.TYPE, Sizeable.TYPE};
	}

	@Override
	public CClassType[] getInterfaces() {
		return CClassType.EMPTY_CLASS_ARRAY;
	}

}
