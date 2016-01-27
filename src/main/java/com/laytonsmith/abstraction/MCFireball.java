package com.laytonsmith.abstraction;

import com.laytonsmith.PureUtilities.Vector3D;

public interface MCFireball extends MCProjectile {
	public Vector3D getDirection();

	public void setDirection(Vector3D vector);
}
