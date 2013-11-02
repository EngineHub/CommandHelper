package com.laytonsmith.abstraction.entities;

import com.laytonsmith.abstraction.MCVector;

public interface MCFireball extends MCProjectile {

	public MCVector getDirection();
	public void setDirection(MCVector vector);
}