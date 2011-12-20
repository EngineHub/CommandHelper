/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.aliasengine.Constructs;

import com.laytonsmith.aliasengine.Static;
import com.laytonsmith.aliasengine.exceptions.ConfigRuntimeException;
import com.laytonsmith.aliasengine.functions.Exceptions.ExceptionType;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 *
 * @author layton
 */
public class CArray extends Construct {

    private boolean associative_mode = false;
    private long next_index = 0;
    private List<Construct> array;
    private SortedMap<Construct, Construct> associative_array;
    private String mutVal;
    CArray parent = null;

    public CArray(int line_num, File file, Construct... items) {
        super(null, ConstructType.ARRAY, line_num, file);
        for(Construct item : items){
            if(item instanceof CEntry){
                //it's an associative array
                associative_mode = true;
                break;
            }
        }
        if(associative_mode){
            associative_array = new TreeMap<Construct, Construct>();
            for(Construct item : items){
                if(item instanceof CEntry){
                    associative_array.put(normalizeConstruct(((CEntry)item).ckey), ((CEntry)item).construct);
                } else {
                    int max = Integer.MIN_VALUE;            
                    for (Construct key : associative_array.keySet()) {
                        try{
                            int i = Integer.parseInt(key.val());
                            max = java.lang.Math.max(max, i);
                        } catch(NumberFormatException e){}
                    }
                    if(max == Integer.MIN_VALUE){
                        max = -1; //Special case, there are no integer indexes in here yet.
                    }
                    associative_array.put(new CInt(max + 1, item.getLineNum(), item.getFile()), item);
                    if(item instanceof CArray){
                        ((CArray)item).parent = this;
                    }
                }
            }
        } else {
            array = new ArrayList<Construct>();
            for(Construct item : items){
                array.add(item);
                if(item instanceof CArray){
                    ((CArray)item).parent = this;
                }
            }
            this.next_index = array.size();
        }
        regenValue();
    }

    /**
     * @return Whether or not this array is operating in associative mode
     */
    public boolean inAssociativeMode() {
        return associative_mode;
    }
    
    /**
     * This should only be used when copying an array that is already known to be associative, so integer keys will
     * remain associative.
     */
    public void forceAssociativeMode(){
        if(associative_array == null){
            associative_array = new TreeMap<Construct, Construct>();
        }
        associative_mode = true;
    }

    private void regenValue() {
        StringBuilder b = new StringBuilder();
        b.append("{");
        if (!associative_mode) {
            for (int i = 0; i < array.size(); i++) {
                if (i > 0) {
                    b.append(", ");
                    b.append(array.get(i).val());
                } else {
                    b.append(array.get(i).val());
                }
            }
        } else {
            boolean first = true;
            for(Construct key : associative_array.keySet()){
                if(!first){
                    b.append(", ");
                }
                first = false;
                b.append(key.val()).append(": ").append(associative_array.get(key).val());
            }
        }
        b.append("}");
        mutVal = b.toString();
        if(parent != null){
            parent.regenValue();
        }
    }

    /**
     * Pushes a new Construct onto the array
     * @param c 
     */
    public void push(Construct c) {
        if (!associative_mode) {
            array.add(c);
            next_index++;
        } else {
            int max = 0;            
            for (Construct key : associative_array.keySet()) {
                try{
                    int i = Integer.parseInt(key.val());
                    max = java.lang.Math.max(max, i);
                } catch(NumberFormatException e){}
            }
            if(c instanceof CEntry){
                associative_array.put(new CInt(max + 1, 0, null), ((CEntry)c).construct());
            } else {
                associative_array.put(new CInt(max + 1, 0, null), c);
            }
        }
        if(c instanceof CArray){
            ((CArray)c).parent = this;
        }
        regenValue();
    }
    
    /**
     * Returns the key set for this array. If it's an associative array, it simply returns
     * the key set of the map, otherwise it generates a set real quick from 0 - size-1, and
     * returns that.
     * @return 
     */
    public Set<Construct> keySet(){
        Set<Construct> set = new HashSet<Construct>(!associative_mode?array.size():associative_array.size());
        if(!associative_mode){
            for(int i = 0; i < array.size(); i++){
                set.add(new CInt(i, 0, null));
            }
            set = new TreeSet(set);
        } else {
            set = associative_array.keySet();
        }        
        return set;
    }

    /**
     * 
     * @param index
     * @param c 
     */
    public void set(Construct index, Construct c) {
        if (!associative_mode) {
            try {
                int indx = (int) Static.getInt(index);
                if (indx > next_index || indx < 0) {
                    throw new ConfigRuntimeException("", 0, null);
                } else if(indx == next_index){
                    this.push(c);
                } else {
                    array.set(indx, c);
                }
            } catch (ConfigRuntimeException e) {
                //Not a number. Convert to associative.
                associative_array = new TreeMap<Construct, Construct>();
                for (int i = 0; i < array.size(); i++) {
                    associative_array.put(new CInt(i, 0, null), array.get(i));
                }
                associative_mode = true;
                array = null; // null out the original array container so it can be GC'd
            }
        }
        if (associative_mode) {
            associative_array.put(normalizeConstruct(index), c);
        }
        if(c instanceof CArray){
            ((CArray)c).parent = this;
        }
        regenValue();
    }
    
    public void set(int index, Construct c){
        this.set(new CInt(index, 0, null), c);
    }

    public Construct get(Construct index, int line_num) {
        if(!associative_mode){
            try {
                return array.get((int)Static.getInt(index));
            } catch (IndexOutOfBoundsException e) {
                throw new ConfigRuntimeException("The element at index " + index.val() + " does not exist", ExceptionType.IndexOverflowException, line_num, file);
            }
        } else {
            if(associative_array.containsKey(normalizeConstruct(index))){
                Construct val = associative_array.get(normalizeConstruct(index));
                if(val instanceof CEntry){
                    return ((CEntry)val).construct();
                }
                return val;
            } else {
                throw new ConfigRuntimeException("The element at index " + index.val() + " does not exist", ExceptionType.IndexOverflowException, line_num, file);
            }
        }
    }
    
    public Construct get(int index, int line_num){
        return this.get(new CInt(index, 0, null), line_num);
    }
    
    /**
     * This should only be used when the value in the array is being used internally
     * @param index
     * @return 
     */
    public Construct get(String index){
        return this.get(new CString(index, 0, null), 0);
    }
    
    public boolean contains(Construct c){
        if(associative_mode){
            return associative_array.containsKey(c);
        } else {
            return array.contains(c);
        }
    }
    
    public boolean contains(String c){
        return this.contains(new CString(c, 0, null));
    }

    @Override
    public String val() {
        return mutVal;
    }

    @Override
    public String toString() {
        return mutVal;
    }

    public int size() {
        if(associative_mode){
            return associative_array.size();
        } else {
            return array.size();
        }
    }

    @Override
    public CArray clone() throws CloneNotSupportedException {
        CArray clone = (CArray) super.clone();
        clone.associative_mode = associative_mode;
        if(associative_mode){
            if (array != null) {
                clone.array = new ArrayList<Construct>(this.array);
            }
        } else {
            if(associative_array != null){
                clone.associative_array = new TreeMap<Construct, Construct>(this.associative_array);
            }
        }
        clone.regenValue();
        return clone;
    }
    
    private Construct normalizeConstruct(Construct c){
        if(c instanceof CArray){
            throw new ConfigRuntimeException("Arrays cannot be used as the key in an associative array", ExceptionType.CastException, c.line_num, c.file);
        } else if(c instanceof CString || c instanceof CInt){
            return c;
        } else if(c instanceof CNull){
            return new CString("", c.line_num, c.file);
        } else if(c instanceof CBoolean){
            if(((CBoolean)c).getBoolean()){
                return new CInt(1, c.line_num, c.file);
            } else {
                return new CInt(0, c.line_num, c.file);
            }
        } else if(c instanceof CLabel){
            return normalizeConstruct(((CLabel)c).cVal());
        } else {
            return new CString(c.val(), c.line_num, c.file);
        }
    }

    public void remove(Construct construct) {
        construct = normalizeConstruct(construct);
        if(!associative_mode){
            array.remove((int)Static.getInt(construct));
        } else {
            associative_array.remove(construct);
        }
        regenValue();
    }
}
