package com.laytonsmith.core.natives.interfaces;

import com.laytonsmith.PureUtilities.Common.Annotations.InterfaceRunnerFor;
import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.core.CHVersion;
import com.laytonsmith.core.constructs.CClassType;

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
	return CHVersion.V3_0_1;
    }

    // Interestingly, this is the only class that will return empty arrays for both
    // of these methods.

    @Override
    public CClassType[] getSuperclasses() {
	return new CClassType[]{};
    }

    @Override
    public CClassType[] getInterfaces() {
	return new CClassType[]{};
    }

}
