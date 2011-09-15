/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.laytonsmith.aliasengine.Constructs;

import com.laytonsmith.aliasengine.functions.exceptions.ConfigRuntimeException;
import com.laytonsmith.aliasengine.functions.Exceptions.ExceptionType;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author layton
 */
public class CArray extends Construct{
    
    public static final long serialVersionUID = 1L;
    private List<Construct> array;
    private String mutVal;
    public CArray(int line_num, File file, Construct ... items){
        super(null, ConstructType.ARRAY, line_num, file);
        array = new ArrayList<Construct>();
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
        mutVal = b.toString();
    }

    /**
     * Pushes a new Construct onto the array
     * @param c 
     */
    public void push(Construct c){
        array.add(c);
        regenValue();
    }

    /**
     * 
     * @param index
     * @param c 
     */
    public void set(int index, Construct c){
        array.set(index, c);
        regenValue();
    }

    public Construct get(int index, int line_num){
        try{
            return array.get(index);
        } catch(IndexOutOfBoundsException e){
            throw new ConfigRuntimeException("The element at index " + index + " does not exist", ExceptionType.IndexOverflowException, line_num, file);
        }
    }
    
    @Override
    public String val(){
        return mutVal;
    }
    
    @Override
    public String toString(){
        return mutVal;
    }
    
    public int size(){
        return array.size();
    }
    
    @Override
    public CArray clone() throws CloneNotSupportedException{
        CArray clone = (CArray) super.clone();
        if(array != null) clone.array = new ArrayList<Construct>(this.array);
        return clone;
    }
}
