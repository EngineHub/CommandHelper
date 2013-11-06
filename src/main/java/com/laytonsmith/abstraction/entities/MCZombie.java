package com.laytonsmith.abstraction.entities;

/**
 *
 * @author Hekta
 */
public interface MCZombie extends MCCreature {

	public boolean isBaby();
	public void setBaby(boolean isBaby);

	public boolean isVillager();
	public void setVillager(boolean isVillager);
}