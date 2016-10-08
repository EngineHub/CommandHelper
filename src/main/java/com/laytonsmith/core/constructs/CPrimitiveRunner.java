package com.laytonsmith.core.constructs;

import com.laytonsmith.PureUtilities.Common.Annotations.InterfaceRunnerFor;
import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.core.CHVersion;
import com.laytonsmith.core.natives.interfaces.AbstractMixedInterfaceRunner;

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
	return CHVersion.V3_0_1;
    }

    @Override
    public CClassType[] getSuperclasses() {
	return new CClassType[]{CClassType.MIXED};
    }

    @Override
    public CClassType[] getInterfaces() {
	return new CClassType[]{};
    }
}
