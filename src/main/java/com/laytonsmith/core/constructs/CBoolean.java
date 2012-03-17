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
public class CBoolean extends Construct implements Cloneable{
    
    public static final long serialVersionUID = 1L;
    private boolean val;
    public CBoolean(boolean value, Target t){
        super(Boolean.toString(value), ConstructType.BOOLEAN, t);
        val = value;
    }

    public CBoolean(String value, Target t){
        super(value, ConstructType.BOOLEAN, t);
        try{
            int i = Integer.parseInt(value);
            if(i == 0){
                val = false;
            } else {
                val = true;
            }
        } catch(NumberFormatException e){
            try{
                double d = Double.parseDouble(value);
                if(d == 0){
                    val = false;
                } else {
                    val = true;
                }
            } catch(NumberFormatException f){
                try{
                    val = Boolean.parseBoolean(value);
                } catch(NumberFormatException g){
                    throw new ConfigRuntimeException("Could not parse value " + value + " into a Boolean type", ExceptionType.FormatException, t);
                }
            }
        }
    }

    public boolean getBoolean(){
        return val;
    }

    @Override
    public String val() {
        if(val){
            return "true";
        } else{
            return "false";
        }
    }
    
    @Override
    public CBoolean clone() throws CloneNotSupportedException{
        return (CBoolean) super.clone();
    }

}
