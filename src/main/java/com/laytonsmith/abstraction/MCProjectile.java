package com.laytonsmith.abstraction;

public interface MCProjectile extends MCEntity {

	public boolean doesBounce();

	public MCLivingEntity getShooter();

	public void setBounce(boolean arg0);

	public void setShooter(MCLivingEntity arg0);
}