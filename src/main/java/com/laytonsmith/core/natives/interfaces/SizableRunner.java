package com.laytonsmith.core.natives.interfaces;

import com.laytonsmith.PureUtilities.Common.Annotations.InterfaceRunnerFor;
import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.core.CHVersion;
import com.laytonsmith.core.constructs.CClassType;

/**
 *
 */
@InterfaceRunnerFor(Sizeable.class)
public class SizableRunner extends AbstractMixedInterfaceRunner {

	@Override
	public String docs() {
		return "Any object that can report a size should implement this.";
	}

	@Override
	public Version since() {
		return CHVersion.V3_3_1;
	}

	@Override
	public CClassType[] getSuperclasses() {
		return new CClassType[]{Mixed.TYPE};
	}

	@Override
	public CClassType[] getInterfaces() {
		return new CClassType[]{};
	}

}
