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
@InterfaceRunnerFor(ValueType.class)
public class ValueTypeInterfaceRunner extends AbstractMixedInterfaceRunner {
	@Override
	public String docs() {
		return "Any object that supports pass-by-value semantics should implement this interface. Value types must"
				+ " meet a few assumptions, they are immutable, it may be faster to pass by value than by reference,"
				+ " and if value a and b are equal, then they could be also the same reference with no ill effects"
				+ " to any programs. The compiler may choose to pass by value or by reference, and it should be"
				+ " the same either way.";
	}

	@Override
	public Version since() {
		return MSVersion.V3_3_1;
	}

	@Override
	public CClassType[] getSuperclasses() {
		return new CClassType[]{Mixed.TYPE};
	}

	@Override
	public CClassType[] getInterfaces() {
		return CClassType.EMPTY_CLASS_ARRAY;
	}
}
