package com.laytonsmith.core.natives.annotations;

import com.laytonsmith.annotations.typename;
import com.laytonsmith.core.CHVersion;
import com.laytonsmith.core.compiler.CompilerAwareAnnotation;
import com.laytonsmith.core.constructs.CNumber;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.functions.Exceptions;
import com.laytonsmith.core.natives.interfaces.Mixed;

/**
 *
 */
@typename("Ranged")
public class Ranged extends MAnnotation implements CompilerAwareAnnotation {
	
	/**
	 * A pre-built Ranged annotation that represents all positive numbers, including 0
	 */
	public static final Ranged POSITIVE = new Ranged(0, true, Integer.MAX_VALUE, true);
	/**
	 * A pre-built Ranged annotation that represents all negative numbers, including 0
	 */
	public static final Ranged NEGATIVE = new Ranged(Integer.MIN_VALUE, true, 0, true);
	
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

	@Override
	public String toString() {
		return (minInclusive?"[":"(") + min + ", " + max + (maxInclusive?"]":")");
	}

	@Override
	public MAnnotation[] getMetaAnnotations() {
		return new MAnnotation[]{
			new TargetRestriction(ElementType.ASSIGNABLE),
			new TypeRestriction(CNumber.class)
		};
	}
	

	public void validateParameter(Mixed parameter, Target t) throws ConfigRuntimeException {
		CNumber num = (CNumber) parameter;
		double d = num.castToDouble(t);
		if(((!minInclusive && d <= min) || (minInclusive && d < min)) || ((!maxInclusive && d >= max) || (maxInclusive && d > max))){
			throw new Exceptions.RangeException("Expecting a value between " + toString() + ", but found " + d, t);
		}
	}
}
