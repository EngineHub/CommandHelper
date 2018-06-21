package com.laytonsmith.abstraction.entities;

import com.laytonsmith.abstraction.MCLivingEntity;
import com.laytonsmith.abstraction.enums.MCDyeColor;

public interface MCShulker extends MCLivingEntity {

	MCDyeColor getColor();
	void setColor(MCDyeColor color);

}
