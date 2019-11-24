package com.laytonsmith.abstraction.entities;

import com.laytonsmith.abstraction.MCLivingEntity;

public interface MCVex extends MCLivingEntity {
	boolean isCharging();
	void setCharging(boolean charging);
}
