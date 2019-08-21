package com.laytonsmith.abstraction.entities;

import com.laytonsmith.abstraction.MCItemStack;

public interface MCThrownPotion extends MCProjectile {

	MCItemStack getItem();

	void setItem(MCItemStack item);
}
