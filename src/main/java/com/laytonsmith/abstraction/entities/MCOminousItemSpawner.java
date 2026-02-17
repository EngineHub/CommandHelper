package com.laytonsmith.abstraction.entities;

import com.laytonsmith.abstraction.MCEntity;
import com.laytonsmith.abstraction.MCItemStack;

public interface MCOminousItemSpawner extends MCEntity {

	MCItemStack getItem();

	void setItem(MCItemStack item);

	long getDelay();

	void setDelay(long delay);
}
