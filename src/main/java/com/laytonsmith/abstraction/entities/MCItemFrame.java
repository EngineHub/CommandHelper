package com.laytonsmith.abstraction.entities;

import com.laytonsmith.abstraction.MCHanging;
import com.laytonsmith.abstraction.MCItemStack;
import com.laytonsmith.abstraction.enums.MCRotation;

public interface MCItemFrame extends MCHanging {
	MCItemStack getItem();
	void setItem(MCItemStack item);

	MCRotation getRotation();
	void setRotation(MCRotation rotation);
}