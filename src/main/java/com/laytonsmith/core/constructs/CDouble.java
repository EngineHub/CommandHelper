package com.laytonsmith.core.constructs;

import com.laytonsmith.annotations.typename;

/**
 *
 * @author layton
 */
@typename("double")
public class CDouble extends Construct implements Cloneable, CNumber {
    
    public static final long serialVersionUID = 1L;
    final double val;

    public CDouble(double value, Target t){
        super(Double.toString(value), t);
        val = value;
    }

    public double getDouble(){
        return val;
    }
    
    @Override
    public CDouble clone() throws CloneNotSupportedException{
        return this;
    }

    @Override
    public boolean isDynamic() {
        return true;
    }

	public String typeName() {
		return "double";
	}
}
