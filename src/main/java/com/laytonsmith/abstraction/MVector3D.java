package com.laytonsmith.abstraction;

/**
 *
 * 
 */
public class MVector3D {
	public double x;
	public double y;
	public double z;

	public MVector3D() {
		this(0, 0, 0);
	}

	public MVector3D(double x, double y, double z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public MVector3D add(MVector3D vec) {
		this.x += vec.x;
		this.y += vec.y;
		this.z += vec.z;
		return this;
	}

	public MVector3D multiply(MVector3D vec) {
		this.x *= vec.x;
		this.y *= vec.y;
		this.z *= vec.z;
		return this;
	}

	public MVector3D multiply(double m) {
		this.x *= m;
		this.y *= m;
		this.z *= m;
		return this;
	}

	public MVector3D normalize() {
		double length = length();
		this.x /= length;
		this.y /= length;
		this.z /= length;
		return this;
	}

	public MVector3D subtract(MVector3D vec) {
		this.x -= vec.x;
		this.y -= vec.y;
		this.z -= vec.z;
		return this;
	}

	public double lengthSquared() {
		return Math.pow(this.x, 2) + Math.pow(this.y, 2) + Math.pow(this.z, 2);
	}

	public double length() {
		return Math.sqrt(lengthSquared());
	}
}
