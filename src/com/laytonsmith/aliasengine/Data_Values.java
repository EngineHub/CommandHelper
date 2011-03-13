/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.laytonsmith.aliasengine;

/**
 *
 * @author layton
 */
public class Data_Values {
    public static String val(String val) throws CancelCommandException{
        try{
            return Integer.toString(Integer.parseInt(val));
        } catch(NumberFormatException e){
            val = val.toLowerCase();
            if(val.equals("stone")){
                return "1";
            }


            else{
                //Couldn't find the data value
                throw new CancelCommandException("Could not find data value for \"" + val + "\"");
            }
        }
    }
}
