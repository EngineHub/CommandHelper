package com.laytonsmith.abstraction.entities;

import com.laytonsmith.abstraction.enums.MCDyeColor;

public interface MCSheep extends MCAgeable {

	MCDyeColor getColor();

	void setColor(MCDyeColor color);

	boolean isSheared();

	void setSheared(boolean shear);
}
