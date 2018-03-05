package com.laytonsmith.PureUtilities;

/**
 * Represents a point in 3D space.
 */
public class Point3D {

	/**
	 * A Point3D located at [0, 0, 0] (zero)
	 */
	public static final Point3D ZERO = new Point3D(0, 0, 0);

	protected final double x;
	protected final double y;
	protected final double z;

	/**
	 * Copy constructor.
	 *
	 * @param other the other point
	 */
	public Point3D(Point3D other) {
		this.x = other.x;
		this.y = other.y;
		this.z = other.z;
	}

	/**
	 * Initializes the X and Y values. Z is initialized to 0.
	 */
	public Point3D(double x, double y) {
		this.x = x;
		this.y = y;
		this.z = 0;
	}

	/**
	 * Initializes the X, Y, and Z values.
	 *
	 * @param x the x value
	 * @param y the y value
	 * @param z the z value
	 */
	public Point3D(double x, double y, double z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	/**
	 * Gets the X value of this point.
	 *
	 * @return the x value
	 */
	public double X() {
		return x;
	}

	/**
	 * Gets the Y value of this point.
	 *
	 * @return the y value
	 */
	public double Y() {
		return y;
	}

	/**
	 * Gets the Z value of this point.
	 *
	 * @return the z value
	 */
	public double Z() {
		return z;
	}

	/**
	 * Returns a point of this point added to another point.
	 *
	 * @param other the other vector
	 * @return the translated point
	 */
	public Point3D translate(Point3D other) {
		return new Point3D(x + other.x, y + other.y, z + other.z);
	}

	/**
	 * Gets the distance squared between this point and another.
	 *
	 * @param other the other point
	 * @return the distance squared
	 */
	public double distanceSquared(Point3D other) {
		double dX = x - other.x;
		double dY = y - other.y;
		double dZ = z - other.z;

		//for efficiency, we write this out a longer way
		return dX * dX + dY * dY + dZ * dZ;
	}

	/**
	 * Gets the distance between this point and another.
	 *
	 * @param other the other point
	 * @return the distance
	 */
	public double distance(Point3D other) {
		return Math.sqrt(distanceSquared(other));
	}
}
