package com.laytonsmith.abstraction.entities;

import com.laytonsmith.abstraction.MCItemStack;
import com.laytonsmith.abstraction.MCProjectile;

/**
 *
 * @author Hekta
 */
public interface MCThrownPotion extends MCProjectile {

	public MCItemStack getItem();
	public void setItem(MCItemStack item);
}