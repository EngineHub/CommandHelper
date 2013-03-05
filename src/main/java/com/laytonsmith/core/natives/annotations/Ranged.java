package com.laytonsmith.core.natives.annotations;

import com.laytonsmith.annotations.typename;
import com.laytonsmith.core.CHVersion;

/**
 *
 * @author lsmith
 */
@typename("Ranged")
public class Ranged extends MAnnotation {
	
	public double min = Double.MIN_VALUE;
	public double max = Double.MAX_VALUE;
	public boolean minInclusive = true;
	public boolean maxInclusive = false;
	
	/**
	 * Creates a new Ranged annotation, assuming min is inclusive, and max is exclusive.
	 * @param min
	 * @param max 
	 */
	public Ranged(double min, double max){
		this.min = min;
		this.max = max;
	}
	
	public Ranged(double min, boolean minInclusive, double max, boolean maxInclusive){
		this(min, max);
		this.minInclusive = minInclusive;
		this.maxInclusive = maxInclusive;
	}
	
	public String docs() {
		return "Used on an argument to indicate that it is a ranged argument. The type of the argument must be a number.";
	}

	public CHVersion since() {
		return CHVersion.V3_3_1;
	}

	@java.lang.Override
	public String toString() {
		return (minInclusive?"[":"(") + min + ", " + max + (maxInclusive?"]":")");
	}
}
