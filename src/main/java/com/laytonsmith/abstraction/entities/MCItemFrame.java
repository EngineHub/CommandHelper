package com.laytonsmith.abstraction.entities;

import com.laytonsmith.abstraction.MCItemStack;
import com.laytonsmith.abstraction.enums.MCRotation;

public interface MCItemFrame extends MCHanging {

	MCItemStack getItem();

	void setItem(MCItemStack item);

	MCRotation getRotation();

	void setRotation(MCRotation rotation);

	boolean isVisible();

	void setVisible(boolean visible);

	boolean isFixed();

	void setFixed(boolean fixed);
}
