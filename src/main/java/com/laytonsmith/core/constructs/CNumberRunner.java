package com.laytonsmith.core.constructs;

import com.methodscript.PureUtilities.Common.Annotations.InterfaceRunnerFor;
import com.methodscript.PureUtilities.Version;
import com.laytonsmith.core.CHVersion;
import com.laytonsmith.core.natives.interfaces.AbstractMixedInterfaceRunner;

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
		return CHVersion.V3_0_1;
	}

	@Override
	public CClassType[] getSuperclasses() {
		return new CClassType[]{CPrimitive.TYPE};
	}

	@Override
	public CClassType[] getInterfaces() {
		return new CClassType[]{};
	}

}
