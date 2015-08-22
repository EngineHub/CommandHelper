


package com.laytonsmith.core.constructs;

import com.laytonsmith.annotations.typeof;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.functions.Exceptions.ExceptionType;

/**
 *
 *
 */
@typeof("double")
public class CDouble extends CNumber implements Cloneable{

    public static final long serialVersionUID = 1L;
    final double val;

    public CDouble(String value, Target t){
        super(value, ConstructType.INT, t);
        try{
            val = Double.parseDouble(value);
        } catch(NumberFormatException e){
            throw new ConfigRuntimeException("Could not cast " + value + " to double", ExceptionType.FormatException, t);
        }
    }

    public CDouble(double value, Target t){
        super(Double.toString(value), ConstructType.DOUBLE, t);
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
        return false;
    }
}
