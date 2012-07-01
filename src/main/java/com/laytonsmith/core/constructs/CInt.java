/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.laytonsmith.core.constructs;

import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.functions.Exceptions.ExceptionType;

/**
 *
 * @author layton
 */
public class CInt extends Construct implements Cloneable{
    
    public static final long serialVersionUID = 1L;
    final long val;
    public CInt(long value, Target t){
        super(Long.toString(value), ConstructType.INT, t);
        val = value;
    }
    
    public CInt(String value, Target t){
        super(value, ConstructType.INT, t);
        try{
            val = Long.parseLong(value);
        } catch(NumberFormatException e){
            throw new ConfigRuntimeException("Could not parse " + value + " as an integer", ExceptionType.FormatException, t);
        }
    }

    public CInt clone() throws CloneNotSupportedException{
        return this;
    }
    
    public long getInt(){
        return val;
    }

    @Override
    public boolean isDynamic() {
        return false;
    }

}
