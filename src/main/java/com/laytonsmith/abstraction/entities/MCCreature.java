package com.laytonsmith.abstraction.entities;

/**
 *
 * @author Hekta
 */
public interface MCCreature extends MCLivingEntity {

	public MCLivingEntity getTarget();
	public void setTarget(MCLivingEntity target);
}