


package com.laytonsmith.core.constructs;

import com.laytonsmith.annotations.typeof;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.functions.Exceptions.ExceptionType;

/**
 *
 *
 */
@typeof("int")
public class CInt extends CNumber implements Cloneable{

    public static final long serialVersionUID = 1L;
    final long val;
    public CInt(String value, Target t){
        super(value, ConstructType.INT, t);
        try{
            val = Long.parseLong(value);
        } catch(NumberFormatException e){
            throw ConfigRuntimeException.BuildException("Could not parse " + value + " as an integer", ExceptionType.FormatException, t);
        }
    }

    public CInt(long value, Target t){
        super(Long.toString(value), ConstructType.INT, t);
        val = value;
    }

    public long getInt(){
        return val;
    }

    @Override
    public CInt clone() throws CloneNotSupportedException{
        return this;
    }

    @Override
    public boolean isDynamic() {
        return false;
    }

}
