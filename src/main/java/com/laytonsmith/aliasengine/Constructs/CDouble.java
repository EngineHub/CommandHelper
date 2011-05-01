/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.laytonsmith.aliasengine.Constructs;

import com.laytonsmith.aliasengine.ConfigRuntimeException;

/**
 *
 * @author layton
 */
public class CDouble extends Construct{
    double val;

    public CDouble(String value, int line_num){
        super(TType.CONSTRUCT, value, ConstructType.INT, line_num);
        try{
            val = Double.parseDouble(value);
        } catch(NumberFormatException e){
            throw new ConfigRuntimeException("Could not cast " + value + " to double");
        }
    }

    public CDouble(double value, int line_num){
        super(TType.CONSTRUCT, Double.toString(value), ConstructType.DOUBLE, line_num);
        val = value;
    }

    public double getDouble(){
        return val;
    }
}
