/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.laytonsmith.core.constructs;

import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.functions.Exceptions.ExceptionType;
import java.io.File;

/**
 *
 * @author layton
 */
public class CDouble extends Construct implements Cloneable{
    
    public static final long serialVersionUID = 1L;
    final double val;

    public CDouble(String value, int line_num, File file){
        super(value, ConstructType.INT, line_num, file);
        try{
            val = Double.parseDouble(value);
        } catch(NumberFormatException e){
            throw new ConfigRuntimeException("Could not cast " + value + " to double", ExceptionType.FormatException, line_num, file);
        }
    }

    public CDouble(double value, int line_num, File file){
        super(Double.toString(value), ConstructType.DOUBLE, line_num, file);
        val = value;
    }

    public double getDouble(){
        return val;
    }
    
    @Override
    public CDouble clone() throws CloneNotSupportedException{
        return (CDouble) super.clone();
    }
}
