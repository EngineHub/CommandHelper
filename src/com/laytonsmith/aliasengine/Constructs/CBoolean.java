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
public class CBoolean extends Construct{
    boolean val;
    public CBoolean(boolean value, int line_num){
        super(TType.CONSTRUCT, null, ConstructType.BOOLEAN, line_num);
        val = value;
    }

    public CBoolean(String value, int line_num){
        super(TType.CONSTRUCT, value, ConstructType.BOOLEAN, line_num);
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
                    throw new ConfigRuntimeException("Could not parse value " + value + " into a Boolean type");
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
