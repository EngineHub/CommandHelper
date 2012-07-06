/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.abstraction;

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
