
package com.laytonsmith.PureUtilities;

/**
 * Provided various geometry convenience classes and methods.
 * 
 */
public class Geometry {
	
	private Geometry(){}
	
	public static class Point3D{
		private double x;
		private double y;
		private double z;
		
		public Point3D(){
			this(0, 0, 0);
		}
		
		public Point3D(double x, double y, double z){
			setPoints(x, y, z);
		}
		
		public void setPoints(double x, double y, double z){
			setX(x);
			setY(y);
			setZ(z);
		}
		
		public void setX(double x){
			this.x = x;
		}
		
		public void setY(double y){
			this.y = y;
		}
		
		public void setZ(double z){
			this.z = z;
		}
		
		public double getX(){
			return x;
		}
		
		public double getY(){
			return y;
		}
		
		public double getZ(){
			return z;
		}
		
		public double distance(Point3D other){
			//for efficiency, we write this out a longer way
			return Math.sqrt(
						((other.x - x) * (other.x - x))
					+	((other.y - y) * (other.y - y))
					+	((other.z - z) * (other.z - z))
			);
		}
	}
	
}
