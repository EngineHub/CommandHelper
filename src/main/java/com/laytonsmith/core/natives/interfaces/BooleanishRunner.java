/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.core.natives.interfaces;

import com.laytonsmith.PureUtilities.Common.Annotations.InterfaceRunnerFor;
import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.core.MSVersion;
import com.laytonsmith.core.constructs.CClassType;

/**
 *
 */
@InterfaceRunnerFor(Booleanish.class)
public class BooleanishRunner extends AbstractMixedInterfaceRunner {
	@Override
	public String docs() {
		return "A value that is Booleanish is a non-boolean value, that can be converted to boolean.";
	}

	@Override
	public Version since() {
		return MSVersion.V3_3_4;
	}

	@Override
	public CClassType[] getSuperclasses() {
		return new CClassType[]{Mixed.TYPE};
	}

	@Override
	public CClassType[] getInterfaces() {
		return CClassType.EMPTY_CLASS_ARRAY;
	}

	@Override
	public ObjectType getObjectType() {
		return ObjectType.INTERFACE;
	}
}
