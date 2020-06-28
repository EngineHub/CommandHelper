package com.laytonsmith.abstraction.entities;

import com.laytonsmith.abstraction.MCLivingEntity;

public interface MCZoglin extends MCLivingEntity {
	boolean isBaby();
	void setBaby(boolean baby);
}
