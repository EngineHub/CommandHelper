/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.core.constructs;

import com.laytonsmith.PureUtilities.Common.StringUtils;
import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.annotations.typeof;
import com.laytonsmith.core.MSVersion;

/**
 *
 * @author Cailin
 */
@typeof("ms.lang.Package")
public class CPackage extends Construct {

	@SuppressWarnings("FieldNameHidesFieldInSuperclass")
	public static final CClassType TYPE = CClassType.get("ms.lang.Package");

	public CPackage(Target t, String... parts) {
		super(StringUtils.Join(parts, CClassType.PATH_SEPARATOR), Construct.ConstructType.IDENTIFIER, t);
	}

	@Override
	public boolean isDynamic() {
		return false;
	}

	@Override
	public String docs() {
		return "Represents the package that a class is in.";
	}

	@Override
	public Version since() {
		return MSVersion.V3_3_3;
	}

	@Override
	public CClassType[] getSuperclasses() {
		return new CClassType[0];
	}

	@Override
	public CClassType[] getInterfaces() {
		return new CClassType[0];
	}

}
