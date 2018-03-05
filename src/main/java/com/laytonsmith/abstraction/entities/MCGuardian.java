package com.laytonsmith.abstraction.entities;

import com.laytonsmith.abstraction.MCLivingEntity;

public interface MCGuardian extends MCLivingEntity {

	boolean isElder();

	void setElder(boolean shouldBeElder);
}
