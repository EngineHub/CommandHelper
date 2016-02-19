package com.laytonsmith.abstraction.entities;

import com.laytonsmith.abstraction.MCLivingEntity;

/**
 *
 * @author Hekta
 */
public interface MCZombie extends MCLivingEntity {

	public boolean isBaby();
	public void setBaby(boolean isBaby);

	public boolean isVillager();
	public void setVillager(boolean isVillager);
}