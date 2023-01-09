package com.laytonsmith.abstraction.entities;

import com.laytonsmith.abstraction.enums.MCFoxType;

public interface MCFox extends MCAnimal {
	MCFoxType getVariant();
	void setVariant(MCFoxType type);
	boolean isCrouching();
	void setCrouching(boolean crouching);
	boolean isSitting();
	void setSitting(boolean sitting);
}
