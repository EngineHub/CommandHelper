package com.laytonsmith.abstraction;

import com.methodscript.PureUtilities.Vector3D;

public interface MCFireball extends MCProjectile {

	Vector3D getDirection();

	void setDirection(Vector3D vector);
}
