package com.laytonsmith.abstraction.entities;

import com.laytonsmith.abstraction.MCTameable;
import com.laytonsmith.abstraction.enums.MCDyeColor;

public interface MCWolf extends MCTameable {

	MCDyeColor getCollarColor();

	boolean isAngry();

	boolean isSitting();

	void setAngry(boolean angry);

	void setSitting(boolean sitting);

	void setCollarColor(MCDyeColor color);
}
