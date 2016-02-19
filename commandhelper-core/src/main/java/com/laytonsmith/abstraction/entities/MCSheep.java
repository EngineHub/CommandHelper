package com.laytonsmith.abstraction.entities;

import com.laytonsmith.abstraction.MCAgeable;
import com.laytonsmith.abstraction.enums.MCDyeColor;

public interface MCSheep extends MCAgeable {
	
	public MCDyeColor getColor();
	
	public void setColor(MCDyeColor color);
	
	public boolean isSheared();
	
	public void setSheared(boolean shear);
}
