package com.laytonsmith.core.constructs;

import com.laytonsmith.PureUtilities.Common.Annotations.InterfaceRunnerFor;
import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.core.MSVersion;
import com.laytonsmith.core.natives.interfaces.AbstractMixedInterfaceRunner;
import com.laytonsmith.core.natives.interfaces.Booleanish;
import com.laytonsmith.core.natives.interfaces.Mixed;
import com.laytonsmith.core.natives.interfaces.ObjectType;
import com.laytonsmith.core.natives.interfaces.ValueType;

/**
 *
 * @author cailin
 */
@InterfaceRunnerFor(CPrimitive.class)
public class CPrimitiveRunner extends AbstractMixedInterfaceRunner {

	@Override
	public String docs() {
		return "A primitive is any non-object and non-array data type. All primitives are pass by value and"
				+ " Booleanish.";
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
		return new CClassType[]{ValueType.TYPE, Booleanish.TYPE};
	}

	@Override
	public ObjectType getObjectType() {
		return ObjectType.ABSTRACT;
	}


}
