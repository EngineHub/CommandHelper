
package com.laytonsmith.abstraction;

/**
 *
 * @author Layton
 */
public interface MCProjectile extends MCEntity, MCMetadatable {
	public boolean doesBounce();
	public MCProjectileSource getShooter();
	public void setBounce(boolean doesBounce);
	public void setShooter(MCProjectileSource shooter);
}
