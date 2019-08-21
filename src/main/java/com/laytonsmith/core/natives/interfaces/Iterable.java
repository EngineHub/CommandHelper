/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.core.natives.interfaces;

import com.laytonsmith.annotations.typeof;
import com.laytonsmith.core.constructs.CClassType;

/**
 * An object that is iterable is one that can be iterated. It must implement both ArrayAccess and Sizeable.
 */
@typeof("ms.lang.Iterable")
public interface Iterable extends ArrayAccess, Sizeable {

	@SuppressWarnings("FieldNameHidesFieldInSuperclass")
	public static final CClassType TYPE = CClassType.get(Iterable.class);

}
