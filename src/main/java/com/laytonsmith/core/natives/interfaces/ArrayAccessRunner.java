package com.laytonsmith.core.natives.interfaces;

import com.laytonsmith.PureUtilities.Common.Annotations.InterfaceRunnerFor;
import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.core.CHVersion;
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
	return CHVersion.V3_3_1;
    }

    @Override
    public CClassType[] getSuperclasses() {
	return new CClassType[]{Mixed.TYPE, Sizeable.TYPE};
    }

    @Override
    public CClassType[] getInterfaces() {
	return new CClassType[]{};
    }

}
