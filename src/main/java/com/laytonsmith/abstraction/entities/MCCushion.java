package com.laytonsmith.abstraction.entities;

import com.laytonsmith.abstraction.MCEntity;
import com.laytonsmith.abstraction.enums.MCDyeColor;

public interface MCCushion extends MCEntity {

	MCDyeColor getColor();

	void setColor(MCDyeColor color);

}
