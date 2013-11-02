package com.laytonsmith.abstraction.bukkit;

import com.laytonsmith.abstraction.AbstractionObject;
import org.bukkit.util.Vector;

import com.laytonsmith.abstraction.MCVector;

/**
 *
 * @author Hekta
 */
public class BukkitMCVector implements MCVector {

	protected Vector vector;

	public BukkitMCVector(Vector v) {
		this.vector = v;
	}

    public BukkitMCVector(AbstractionObject ao) {
        this((Vector) ao.getHandle());
    }

	@Override
    public Vector getHandle() {
        return vector;
    }

	public double getX() {
		return vector.getX();
	}

	public double getY() {
		return vector.getY();
	}

	public double getZ() {
		return vector.getZ();
	}

	public double length() {
		return vector.length();
	}
}