
package com.laytonsmith.abstraction;

/**
 *
 * 
 */
public interface MCProjectile extends MCEntity, MCMetadatable {
	public boolean doesBounce();
	public MCProjectileSource getShooter();
	public void setBounce(boolean doesBounce);
	public void setShooter(MCProjectileSource shooter);
}
