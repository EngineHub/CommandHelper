package com.laytonsmith.abstraction.entities;

import com.laytonsmith.abstraction.MCLivingEntity;

/**
 *
 * @author Hekta
 */
public interface MCIronGolem extends MCLivingEntity {
	public boolean isPlayerCreated();
	public void setPlayerCreated(boolean playerCreated);
}