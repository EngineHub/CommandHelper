/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.laytonsmith.aliasengine.Constructs;

import com.laytonsmith.aliasengine.ConfigRuntimeException;
import com.laytonsmith.aliasengine.functions.Exceptions.ExceptionType;
import java.util.Map;

/**
 *
 * @author layton
 */
public class CMap extends Construct{
    Map<String, Construct> array;
    public CMap(){
        super(null, ConstructType.MAP, 0);
    }

    public Construct getValue(String key, int line_num){
        if(array.containsKey(key)){
            throw new ConfigRuntimeException("The value '" + key + "' does not exist", ExceptionType.FormatException, line_num);
        }
        return array.get(key);
    }

    public void setValue(String key, Construct value){
        array.put(key, value);
    }
}
