package com.laytonsmith.core.constructs;

import com.laytonsmith.annotations.typename;

/**
 *
 */
@typename("double")
public class CDouble extends CNumber implements Cloneable {
    
    public static final long serialVersionUID = 1L;

    public CDouble(double value, Target t){
        super(value, t);
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

	@Override
	public String castToString() {
		String format = String.format("%f", number);
		//Given that val is .5, format would be something like 0.500000. We will
		//trim the excess zeros from the end
		return format.replaceAll("(\\.(?:\\d*[1-9])?)0+$", "$1");
	}
}
