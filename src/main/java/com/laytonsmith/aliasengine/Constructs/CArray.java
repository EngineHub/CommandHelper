/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.laytonsmith.aliasengine.Constructs;

import com.laytonsmith.aliasengine.ConfigRuntimeException;
import java.util.ArrayList;
import java.util.Arrays;

/**
 *
 * @author layton
 */
public class CArray extends Construct{
    ArrayList<Construct> array = new ArrayList<Construct>();
    public CArray(int line_num, Construct ... items){
        super(null, ConstructType.ARRAY, line_num);
        array.addAll(Arrays.asList(items));
        regenValue();
    }

    private void regenValue(){
        StringBuilder b = new StringBuilder();
        b.append("{");
        for(int i = 0; i < array.size(); i++){
            if(i > 0){
                b.append(", ");
                b.append(array.get(i).val());
            } else {
                b.append(array.get(i).val());
            }
        }
        b.append("}");
        value = b.toString();
    }

    public void push(Construct c){
        array.add(c);
        regenValue();
    }

    public void set(int index, Construct c){
        array.set(index, c);
        regenValue();
    }

    public Construct get(int index){
        try{
            return array.get(index);
        } catch(IndexOutOfBoundsException e){
            throw new ConfigRuntimeException("The element at index " + index + " does not exist");
        }
    }
    
    public int size(){
        return array.size();
    }
}
