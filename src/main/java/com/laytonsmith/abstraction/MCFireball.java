package com.laytonsmith.abstraction;

public interface MCFireball extends MCProjectile {
	public Velocity getDirection();
	public void setDirection(Velocity vector);
}
