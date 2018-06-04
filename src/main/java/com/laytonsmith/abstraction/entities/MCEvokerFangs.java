package com.laytonsmith.abstraction.entities;

import com.laytonsmith.abstraction.MCEntity;
import com.laytonsmith.abstraction.MCLivingEntity;

public interface MCEvokerFangs extends MCEntity {
	MCLivingEntity getOwner();
	void setOwner(MCLivingEntity owner);
}
