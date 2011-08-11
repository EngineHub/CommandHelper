/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.laytonsmith.aliasengine.Constructs;

import com.laytonsmith.aliasengine.functions.exceptions.ConfigRuntimeException;
import com.laytonsmith.aliasengine.functions.Exceptions.ExceptionType;
import java.io.File;

/**
 *
 * @author layton
 */
public class CBoolean extends Construct{
    boolean val;
    public CBoolean(boolean value, int line_num, File file){
        super(Boolean.toString(value), ConstructType.BOOLEAN, line_num, file);
        val = value;
    }

    public CBoolean(String value, int line_num, File file){
        super(value, ConstructType.BOOLEAN, line_num, file);
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
                    throw new ConfigRuntimeException("Could not parse value " + value + " into a Boolean type", ExceptionType.FormatException, line_num, file);
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

}
