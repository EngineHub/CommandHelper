package com.laytonsmith.core.natives.interfaces;

import com.laytonsmith.PureUtilities.Common.Annotations.InterfaceRunnerFor;
import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.core.MSVersion;
import com.laytonsmith.core.constructs.CClassType;
import java.util.EnumSet;
import java.util.Set;

/**
 *
 */
@InterfaceRunnerFor(Mixed.class)
public class MixedRunner extends AbstractMixedInterfaceRunner {

	@Override
	public String docs() {
		return "The root datatype. All datatypes extend mixed.";
	}

	@Override
	public Version since() {
		return MSVersion.V3_0_1;
	}

	// Interestingly, this is the only class that will return empty arrays for both
	// of these methods.
	@Override
	public CClassType[] getSuperclasses() {
		return CClassType.EMPTY_CLASS_ARRAY;
	}

	@Override
	public CClassType[] getInterfaces() {
		return CClassType.EMPTY_CLASS_ARRAY;
	}

	@Override
	public ObjectType getObjectType() {
		return ObjectType.CLASS;
	}

	@Override
	public Set<ObjectModifier> getObjectModifiers() {
		return EnumSet.of(ObjectModifier.PUBLIC);
	}

}
