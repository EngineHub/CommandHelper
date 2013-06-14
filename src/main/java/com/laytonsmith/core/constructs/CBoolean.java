


package com.laytonsmith.core.constructs;

import com.laytonsmith.annotations.typename;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.functions.Exceptions.ExceptionType;
import com.laytonsmith.core.natives.interfaces.Operators;

/**
 *
 */
@typename("boolean")
public class CBoolean extends CPrimitive implements Cloneable {
    
    public static final long serialVersionUID = 1L;
    private final boolean val;
    public CBoolean(boolean value, Target t){
        super(Boolean.toString(value), t);
        val = value;
    }

    public CBoolean(String value, Target t){
        super(value, t);
        boolean tempVal;
        try{
            int i = Integer.parseInt(value);
            if(i == 0){
                tempVal = false;
            } else {
                tempVal = true;
            }
        } catch(NumberFormatException e){
            try{
                double d = Double.parseDouble(value);
                if(d == 0){
                    tempVal = false;
                } else {
                    tempVal = true;
                }
            } catch(NumberFormatException f){
                try{
                    tempVal = Boolean.parseBoolean(value);
                } catch(NumberFormatException g){
                    throw new ConfigRuntimeException("Could not parse value " + value + " into a Boolean type", ExceptionType.FormatException, t);
                }
            }
        }
        val = tempVal;
    }

    @Override
    public String toString() {
        if(val){
            return "true";
        } else{
            return "false";
        }
    }
    
    @Override
    public CBoolean clone() throws CloneNotSupportedException{
        return this;
    }

    @Override
    public boolean isDynamic() {
        return false;
    }

	public String typeName() {
		return "boolean";
	}

	@Override
	public String castToString() {
		return val ? "true" : "false";
	}

	@Override
	public double castToDouble(Target t) {
		return val ? 1.0 : 0.0;
	}

	@Override
	public long castToInt(Target t) {
		return val ? 1 : 0;
	}

	@Override
	public boolean castToBoolean() {
		return val;
	}

}
