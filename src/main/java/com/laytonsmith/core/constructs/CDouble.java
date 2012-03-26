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
public class CDouble extends Construct implements Cloneable{
    
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
        return (CDouble) super.clone();
    }

    @Override
    public boolean isDynamic() {
        return true;
    }
}
