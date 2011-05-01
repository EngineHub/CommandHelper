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
public class CInt extends Construct{
    int val;
    public CInt(String value, int line_num){
        super(TType.CONSTRUCT, value, ConstructType.INT, line_num);
        try{
            val = Integer.parseInt(value);
        } catch(NumberFormatException e){
            throw new ConfigRuntimeException("Could not parse " + value + " as an integer");
        }
    }

    public CInt(int value, int line_num){
        super(TType.CONSTRUCT, Integer.toString(value), ConstructType.INT, line_num);
        val = value;
    }

    public int getInt(){
        return val;
    }

}
