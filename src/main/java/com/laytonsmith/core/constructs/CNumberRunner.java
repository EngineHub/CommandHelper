package com.laytonsmith.core.constructs;

import com.laytonsmith.PureUtilities.Common.Annotations.InterfaceRunnerFor;
import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.core.MSVersion;
import com.laytonsmith.core.natives.interfaces.AbstractMixedInterfaceRunner;
import com.laytonsmith.core.objects.ObjectType;

/**
 *
 * @author cailin
 */
@InterfaceRunnerFor(CNumber.class)
public class CNumberRunner extends AbstractMixedInterfaceRunner {

	@Override
	public String docs() {
		return "A number is any double or integer number.";
	}

	@Override
	public Version since() {
		return MSVersion.V3_0_1;
	}

	@Override
	public CClassType[] getSuperclasses() {
		return new CClassType[]{CPrimitive.TYPE};
	}

	@Override
	public CClassType[] getInterfaces() {
		return CClassType.EMPTY_CLASS_ARRAY;
	}

	@Override
	public ObjectType getObjectType() {
		return ObjectType.ABSTRACT;
	}



}
