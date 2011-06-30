/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.laytonsmith.aliasengine.Constructs;

import com.laytonsmith.aliasengine.ConfigRuntimeException;
import com.laytonsmith.aliasengine.functions.Exceptions.ExceptionType;

/**
 *
 * @author layton
 */
public class CDouble extends Construct{
    double val;

    public CDouble(String value, int line_num){
        super(value, ConstructType.INT, line_num);
        try{
            val = Double.parseDouble(value);
        } catch(NumberFormatException e){
            throw new ConfigRuntimeException("Could not cast " + value + " to double", ExceptionType.FormatException, line_num);
        }
    }

    public CDouble(double value, int line_num){
        super(Double.toString(value), ConstructType.DOUBLE, line_num);
        val = value;
    }

    public double getDouble(){
        return val;
    }
}
