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
public class CBoolean extends Construct implements Cloneable{
    
    public static final long serialVersionUID = 1L;
    private final boolean val;
    public CBoolean(boolean value, Target t){
        super(Boolean.toString(value), ConstructType.BOOLEAN, t);
        val = value;
    }

    public CBoolean(String value, Target t){
        super(value, ConstructType.BOOLEAN, t);
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
    public CBoolean clone() throws CloneNotSupportedException{
        return this;
    }

    public boolean getBoolean(){
        return val;
    }
    
    @Override
    public boolean isDynamic() {
        return false;
    }

    @Override
    public String val() {
        if(val){
            return "true";
        } else{
            return "false";
        }
    }

}
