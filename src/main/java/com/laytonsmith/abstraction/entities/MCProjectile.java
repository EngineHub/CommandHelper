
package com.laytonsmith.abstraction.entities;

import com.laytonsmith.abstraction.MCMetadatable;

/**
 *
 * @author Layton
 */
public interface MCProjectile extends MCEntity, MCMetadatable {
	public boolean doesBounce();
	public MCLivingEntity getShooter();
	public void setBounce(boolean doesBounce);
	public void setShooter(MCLivingEntity shooter);
}