package com.laytonsmith.abstraction.entities;

import com.laytonsmith.abstraction.MCLivingEntity;

public interface MCPhantom extends MCLivingEntity {
	int getSize();
	void setSize(int size);
}
