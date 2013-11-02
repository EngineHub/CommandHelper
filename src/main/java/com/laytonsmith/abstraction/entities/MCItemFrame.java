package com.laytonsmith.abstraction.entities;

import com.laytonsmith.abstraction.MCItemStack;
import com.laytonsmith.abstraction.enums.MCRotation;

/**
 *
 * @author Hekta
 */
public interface MCItemFrame extends MCHanging {

	public MCItemStack getItem();
	public void setItem(MCItemStack item);

	public MCRotation getRotation();
	public void setRotation(MCRotation rotation);
}