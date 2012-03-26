/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.core.constructs;

import com.laytonsmith.core.Static;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.functions.Exceptions.ExceptionType;
import java.util.*;

/**
 *
 * @author layton
 */
public class CArray extends Construct {

    private boolean associative_mode = false;
    private long next_index = 0;
    private List<Construct> array;
    private SortedMap<String, Construct> associative_array;
    private String mutVal;
    CArray parent = null;
    
    
    public CArray(Target t){
        this(t, (Construct[])null);
    }

    public CArray(Target t,  Construct... items) {
        super(null, ConstructType.ARRAY, t);
        if(items != null){
            for(Construct item : items){
                if(item instanceof CEntry){
                    //it's an associative array
                    associative_mode = true;
                    break;
                }
            }
        }
        associative_array = new TreeMap<String, Construct>(comparator);
        array = new ArrayList<Construct>();
        if(associative_mode){
            if(items != null){
                for(Construct item : items){
                    if(item instanceof CEntry){
                        associative_array.put(normalizeConstruct(((CEntry)item).ckey), ((CEntry)item).construct);
                    } else {
                        int max = Integer.MIN_VALUE;            
                        for (String key : associative_array.keySet()) {
                            try{
                                int i = Integer.parseInt(key);
                                max = java.lang.Math.max(max, i);
                            } catch(NumberFormatException e){}
                        }
                        if(max == Integer.MIN_VALUE){
                            max = -1; //Special case, there are no integer indexes in here yet.
                        }
                        associative_array.put(Integer.toString(max + 1), item);
                        if(item instanceof CArray){
                            ((CArray)item).parent = this;
                        }
                    }
                }
            }
        } else {
            if(items != null){
                for(Construct item : items){
                    array.add(item);
                    if(item instanceof CArray){
                        ((CArray)item).parent = this;
                    }
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
            associative_array = new TreeMap<String, Construct>();
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
            for(String key : associative_array.keySet()){
                if(!first){
                    b.append(", ");
                }
                first = false;
                b.append(key).append(": ").append(associative_array.get(key).val());
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
            for (String key : associative_array.keySet()) {
                try{
                    int i = Integer.parseInt(key);
                    max = java.lang.Math.max(max, i);
                } catch(NumberFormatException e){}
            }
            if(c instanceof CEntry){
                associative_array.put(Integer.toString(max + 1), ((CEntry)c).construct());
            } else {
                associative_array.put(Integer.toString(max + 1), c);
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
    public Set<String> keySet(){
        Set<String> set = new HashSet<String>(!associative_mode?array.size():associative_array.size());
        if(!associative_mode){
            for(int i = 0; i < array.size(); i++){
                set.add(Integer.toString(i));
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
                    throw new ConfigRuntimeException("", Target.UNKNOWN);
                } else if(indx == next_index){
                    this.push(c);
                } else {
                    array.set(indx, c);
                }
            } catch (ConfigRuntimeException e) {
                //Not a number. Convert to associative.
                associative_array = new TreeMap<String, Construct>(comparator);
                for (int i = 0; i < array.size(); i++) {
                    associative_array.put(Integer.toString(i), array.get(i));
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
        this.set(new CInt(index, Target.UNKNOWN), c);
    }
    /* Shortcuts */
    
    public void set(String index, Construct c){
        set(new CString(index, c.getTarget()), c);
    }
    
    public void set(String index, String value, Target t){
        set(index, new CString(value, t));
    }
    
    public void set(String index, String value){
        set(index, value, Target.UNKNOWN);
    }

    public Construct get(Construct index, Target t) {
        if(!associative_mode){
            try {
                return array.get((int)Static.getInt(index));
            } catch (IndexOutOfBoundsException e) {
                throw new ConfigRuntimeException("The element at index \"" + index.val() + "\" does not exist", ExceptionType.IndexOverflowException, t);
            }
        } else {
            if(associative_array.containsKey(normalizeConstruct(index))){
                Construct val = associative_array.get(normalizeConstruct(index));
                if(val instanceof CEntry){
                    return ((CEntry)val).construct();
                }
                return val;
            } else {
                throw new ConfigRuntimeException("The element at index \"" + index.val() + "\" does not exist", ExceptionType.IndexOverflowException, t);
            }
        }
    }
    
    public Construct get(int index, Target t){
        return this.get(new CInt(index, t), t);
    }
    
    public Construct get(String index, Target t){
        return this.get(new CString(index, t), t);
    }
    
    public Construct get(String index){
        return this.get(index, Target.UNKNOWN);
    }
    
    public Construct get(int index){
        return this.get(index, Target.UNKNOWN);
    }
    
    public boolean containsKey(String c){
        Integer i;
        try{
            i = Integer.valueOf(c);
        } catch(NumberFormatException e){
            i = null;
        }
        if(associative_mode){
            return associative_array.containsKey(c);
        } else {
            if(i == null){
                return false;
            } else {
                return array.size() > i;
            }
        }
    }
    
    public boolean containsKey(int i){
        return this.containsKey(Integer.toString(i));
    }
    
    public boolean contains(Construct c){
        if(associative_mode){
            return associative_array.containsValue(c);
        } else {
            return array.contains(c);
        }
    }
    
    public boolean contains(String c){
        return contains(new CString(c, Target.UNKNOWN));
    }
    
    public boolean contains(int i){
        return contains(new CString(Integer.toString(i), Target.UNKNOWN));
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
                clone.associative_array = new TreeMap<String, Construct>(this.associative_array);
            }
        }
        clone.regenValue();
        return clone;
    }
    
    private String normalizeConstruct(Construct c){
        if(c instanceof CArray){
            throw new ConfigRuntimeException("Arrays cannot be used as the key in an associative array", ExceptionType.CastException, c.getTarget());
        } else if(c instanceof CString || c instanceof CInt){
            return c.val();
        } else if(c instanceof CNull){
            return "";
        } else if(c instanceof CBoolean){
            if(((CBoolean)c).getBoolean()){
                return "1";
            } else {
                return "0";
            }
        } else if(c instanceof CLabel){
            return normalizeConstruct(((CLabel)c).cVal());
        } else {
            return c.val();
        }
    }

    public Construct remove(Construct construct) {
        String c = normalizeConstruct(construct);
        Construct ret;
        if(!associative_mode){
            try{
                ret = array.remove(Integer.parseInt(c));
            } catch(NumberFormatException e){
                throw new ConfigRuntimeException("Expecting an integer, but received " + c, construct.getTarget());
            }
        } else {
            ret = associative_array.remove(c);
        }
        regenValue();
        return ret;
    }
    
    private Comparator<String> comparator = new Comparator<String>(){

        public int compare(String o1, String o2) {
            //Due to a dumb behavior in Double.parseDouble, 
            //we need to check to see if there are non-digit characters in
            //the keys, and if so, do a string comparison.
            if(o1.matches(".*[^0-9\\.]+.*") || o2.matches(".*[^0-9\\.]+.*")){
                return o1.compareTo(o2);
            }
            try{
                int i1 = Integer.parseInt(o1);
                int i2 = Integer.parseInt(o2);
                //They're both integers, do an integer comparison
                return new Integer(i1).compareTo(new Integer(i2));
            } catch(NumberFormatException e){
                try{                    
                    double d1 = Double.parseDouble(o1);
                    double d2 = Double.parseDouble(o2);
                    //They're both doubles, do a double comparison
                    return new Double(d1).compareTo(new Double(d2));
                } catch(NumberFormatException ee){
                    //Just do a string comparison
                    return o1.compareTo(o2);
                }
            }
        }
        
    };

    @Override
    public boolean isDynamic() {
        //The CArray is static, despite what you might first think.
        //The only way to get a static array is to use the array function,
        //which WILL return a static array. A function that takes an array
        //as an argument will accept the static array, and can be optimized possibly,
        //however, it is likely that the array is stored in a variable, which of couse
        //is NOT static. So, if just the array function is run, it's static, if the static
        //array is put into a variable, the staticness is lost (as it is with a number or string)
        return false;
    }
}
