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
public class CInt extends Construct{
    long val;
    public CInt(String value, int line_num){
        super(value, ConstructType.INT, line_num);
        try{
            val = Long.parseLong(value);
        } catch(NumberFormatException e){
            throw new ConfigRuntimeException("Could not parse " + value + " as an integer", ExceptionType.FormatException, line_num);
        }
    }

    public CInt(long value, int line_num){
        super(Long.toString(value), ConstructType.INT, line_num);
        val = value;
    }

    public long getInt(){
        return val;
    }

}
