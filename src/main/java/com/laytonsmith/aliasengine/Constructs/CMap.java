/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.laytonsmith.aliasengine.Constructs;

import com.laytonsmith.aliasengine.functions.exceptions.ConfigRuntimeException;
import com.laytonsmith.aliasengine.functions.Exceptions.ExceptionType;
import java.io.File;
import java.util.Map;

/**
 *
 * @author layton
 */
public class CMap extends Construct{
    Map<String, Construct> array;
    public CMap(){
        super(null, ConstructType.MAP, 0, null);
    }

    public Construct getValue(String key, int line_num, File file){
        if(array.containsKey(key)){
            throw new ConfigRuntimeException("The value '" + key + "' does not exist", ExceptionType.FormatException, line_num, file);
        }
        return array.get(key);
    }

    public void setValue(String key, Construct value){
        array.put(key, value);
    }
}
