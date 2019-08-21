package com.laytonsmith.abstraction.entities;

import com.laytonsmith.abstraction.MCEntity;
import com.laytonsmith.abstraction.MCMetadatable;
import com.laytonsmith.abstraction.MCProjectileSource;

public interface MCProjectile extends MCEntity, MCMetadatable {

	boolean doesBounce();

	MCProjectileSource getShooter();

	void setBounce(boolean doesBounce);

	void setShooter(MCProjectileSource shooter);
}
