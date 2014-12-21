package com.laytonsmith.abstraction.entities;

import com.laytonsmith.abstraction.MCLivingEntity;

public interface MCGuardian extends MCLivingEntity {
	public boolean isElder();
	public void setElder(boolean shouldBeElder);
}
