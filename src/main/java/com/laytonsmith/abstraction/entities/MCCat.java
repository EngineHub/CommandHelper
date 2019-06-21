package com.laytonsmith.abstraction.entities;

import com.laytonsmith.abstraction.enums.MCCatType;
import com.laytonsmith.abstraction.enums.MCDyeColor;

public interface MCCat extends MCTameable {
	MCDyeColor getCollarColor();
	void setCollarColor(MCDyeColor color);
	boolean isSitting();
	void setSitting(boolean sitting);
	MCCatType getCatType();
	void setCatType(MCCatType type);
}
