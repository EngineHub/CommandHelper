package com.laytonsmith.abstraction;

/**
 *
 * 
 */
public class Velocity {
	public double magnitude;
	public double x;
	public double y;
	public double z;

	public Velocity(double x, double y, double z) {
		this(Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2) + Math.pow(z, 2)), x, y, z);
	}

	public Velocity(double magnitude, double x, double y, double z) {
		this.magnitude = magnitude;
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public Velocity add(Velocity vec) {
		this.x += vec.x;
		this.y += vec.y;
		this.z += vec.z;
		return this;
	}

	public Velocity multiply(Velocity vec) {
		this.x *= vec.x;
		this.y *= vec.y;
		this.z *= vec.z;
		return this;
	}

	public Velocity multiply(double m) {
		this.x *= m;
		this.y *= m;
		this.z *= m;
		return this;
	}

	public Velocity normalize() {
		double length = length();
		this.x /= length;
		this.y /= length;
		this.z /= length;
		return this;
	}

	public Velocity subtract(Velocity vec) {
		this.x -= vec.x;
		this.y -= vec.y;
		this.z -= vec.z;
		return this;
	}

	public double length() {
		return Math.sqrt(Math.pow(this.x, 2) + Math.pow(this.y, 2) + Math.pow(this.z, 2));
	}
}
