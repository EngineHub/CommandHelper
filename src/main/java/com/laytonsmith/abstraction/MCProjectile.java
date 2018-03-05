package com.laytonsmith.abstraction;

public interface MCProjectile extends MCEntity, MCMetadatable {

	boolean doesBounce();

	MCProjectileSource getShooter();

	void setBounce(boolean doesBounce);

	void setShooter(MCProjectileSource shooter);
}
