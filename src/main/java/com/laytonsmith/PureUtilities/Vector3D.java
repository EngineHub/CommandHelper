package com.laytonsmith.PureUtilities;

/**
 * Represents both a point in 3D space and a vector representing a direction and magnitude.
 */
public class Vector3D extends Point3D {

	/**
	 * A Vector3D with x, y and z initialized at 0.
	 */
	public static final Vector3D ZERO = new Vector3D(0, 0, 0);

	/**
	 * Copy constructor.
	 *
	 * @param other the other point
	 */
	public Vector3D(Point3D other) {
		super(other);
	}

	/**
	 * Initializes the X and Y values. Z is initialized to 0.
	 */
	public Vector3D(double x, double y) {
		super(x, y, 0);
	}

	/**
	 * Initializes the X, Y, and Z values.
	 *
	 * @param x the x value
	 * @param y the y value
	 * @param z the z value
	 */
	public Vector3D(double x, double y, double z) {
		super(x, y, z);
	}

	/**
	 * Returns a new vector of this vector added to another. While functionally similar to <code>translate()</code>,
	 * this method will return a Vector3D whereas <code>translate()</code> returns a Point3D.
	 *
	 * @param other the other vector
	 * @return a new vector
	 */
	public Vector3D add(Vector3D other) {
		return new Vector3D(x + other.x, y + other.y, z + other.z);
	}

	/**
	 * Returns a new vector of this vector multiplied by another.
	 *
	 * @param other the other vector
	 * @return a new vector
	 */
	public Vector3D subtract(Vector3D other) {
		return new Vector3D(x - other.x, y - other.y, z - other.z);
	}

	/**
	 * Returns a new vector of this vector multiplied by another.
	 *
	 * @param other the other vector
	 * @return a new vector
	 */
	public Vector3D multiply(Vector3D other) {
		return new Vector3D(x * other.x, y * other.y, z * other.z);
	}

	/**
	 * Returns a new vector of this vector multiplied by a value.
	 *
	 * @param m the value to multiply by
	 * @return a new vector
	 */
	public Vector3D multiply(double m) {
		return new Vector3D(x * m, y * m, z * m);
	}

	/**
	 * Returns a new vector of this vector divided by another.
	 *
	 * @param other the other vector
	 * @return a new vector
	 */
	public Vector3D divide(Vector3D other) {
		return new Vector3D(x / other.x, y / other.y, z / other.z);
	}

	/**
	 * Returns a new vector of this vector divided by a value.
	 *
	 * @param m the value to divide by
	 * @return a new vector
	 */
	public Vector3D divide(double m) {
		return new Vector3D(x / m, y / m, z / m);
	}

	/**
	 * Returns a new vector that is a normalized version of this vector. The new vector will have the same direction,
	 * but a magnitude of one.
	 *
	 * @return a new vector
	 */
	public Vector3D normalize() {
		double length = length();
		if(length == 0.0) {
			return Vector3D.ZERO;
		}
		return new Vector3D(x / length, y / length, z / length);
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
	 * Gets the magnitude (length) squared of this vector.
	 *
	 * @return the magnitude squared
	 */
	public double lengthSquared() {
		return x * x + y * y + z * z;
	}

	/**
	 * Gets the magnitude (length) of this vector.
	 *
	 * @return the magnitude
	 */
	public double length() {
		return Math.sqrt(lengthSquared());
	}
}
