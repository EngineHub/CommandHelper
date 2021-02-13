package com.laytonsmith.abstraction.entities;

import com.laytonsmith.abstraction.MCItemStack;
import com.laytonsmith.abstraction.MCMetadatable;

public interface MCItemProjectile extends MCProjectile, MCMetadatable {
	MCItemStack getItem();
	void setItem(MCItemStack item);
}
