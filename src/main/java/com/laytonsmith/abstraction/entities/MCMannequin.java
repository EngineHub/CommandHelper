package com.laytonsmith.abstraction.entities;

import com.laytonsmith.abstraction.MCLivingEntity;

public interface MCMannequin extends MCLivingEntity {
	boolean isImmovable();
	void setImmovable(boolean immovable);
}
