package com.laytonsmith.abstraction.entities;

import com.laytonsmith.abstraction.MCLivingEntity;

public interface MCIronGolem extends MCLivingEntity {
	boolean isPlayerCreated();
	void setPlayerCreated(boolean playerCreated);
}