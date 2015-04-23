package com.laytonsmith.PureUtilities;

/**
 * Represents a point in space and defined as having a distance and a direction.
 * 
 */
public class Vector3D {

	public static Vector3D ZERO = new Vector3D();

	private double x;
	private double y;
	private double z;

	/**
	 * Zero constructor.
	 *
	 * @deprecated use Vector3D.ZERO instead
	 */
	@Deprecated
	public Vector3D() {
		this(0, 0, 0);
	}

	public Vector3D(double x, double y) {
		this(x, y, 0);
	}

	public Vector3D(double x, double y, double z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public double X() {
		return x;
	}

	public double Y() {
		return y;
	}

	public double Z() {
		return z;
	}

	public Vector3D add(Vector3D vec) {
		this.x += vec.x;
		this.y += vec.y;
		this.z += vec.z;
		return this;
	}

	public Vector3D multiply(Vector3D vec) {
		this.x *= vec.x;
		this.y *= vec.y;
		this.z *= vec.z;
		return this;
	}

	public Vector3D multiply(double m) {
		this.x *= m;
		this.y *= m;
		this.z *= m;
		return this;
	}

	public Vector3D normalize() {
		double length = length();
		this.x /= length;
		this.y /= length;
		this.z /= length;
		return this;
	}

	public Vector3D subtract(Vector3D vec) {
		this.x -= vec.x;
		this.y -= vec.y;
		this.z -= vec.z;
		return this;
	}

	/**
	 * Gets the dot product of this vector.
	 *
	 * @param vec the other vector
	 * @return the dot product of this vector
	 */
	public double dot(Vector3D vec) {
		return x * vec.x + y * vec.y + z * vec.z;
	}

	/**
	 * Gets the cross product of this vector.
	 *
	 * @param vec the other vector
	 * @return the cross product of this vector
	 */
	public Vector3D cross(Vector3D vec) {
		return new Vector3D(
				y * vec.z - z * vec.y,
				z * vec.x - x * vec.z,
				x * vec.y - y * vec.x
		);
	}

	/**
	 * Gets the distance between this vector and another vector
	 *
	 * @param vec the other vector
	 * @return the distance between the two vectors
	 */
	public double distance(Vector3D vec) {
		double xx = x - vec.x;
		double yy = y - vec.y;
		double zz = z - vec.z;

		return Math.sqrt(xx * xx + yy + yy + zz + zz);
	}

	public double lengthSquared() {
		return x * x + y * y + z * z;
	}

	public double length() {
		return Math.sqrt(lengthSquared());
	}
}
