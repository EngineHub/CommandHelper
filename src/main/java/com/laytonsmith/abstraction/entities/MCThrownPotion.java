package com.laytonsmith.abstraction.entities;

import com.laytonsmith.abstraction.MCItemStack;
import com.laytonsmith.abstraction.MCProjectile;

public interface MCThrownPotion extends MCProjectile {

	MCItemStack getItem();

	void setItem(MCItemStack item);
}
