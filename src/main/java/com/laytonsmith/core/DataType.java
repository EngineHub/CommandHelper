/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.core;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * All constructs have or can return a certain data type. Sometimes it is useful
 * to compare these types at compile time, but since CH isn't strictly type safe,
 * that isn't always practical. However, in many cases, if we simply knew the type
 * that could be returned, if it were invalid, we could cause a compile error
 * at that time. All constructs and functions return a data type, though for
 * many things, it may be mixed. If it is mixed, the type checking must be
 * deferred to runtime, however, in cases where a type is requested, and the
 * type returned is not that, nor can it be cast to that, a compile error can
 * be given.
 * @author Layton
 */
public class DataType {
    private static enum DataTypes{
        ARRAY,
        DOUBLE,
        INT,
        MAP,
        MIXED,
        NUMBER,
        OBJECT,
        PRIMITIVE,
        STRING;
        static{
            DOUBLE.setup(EnumSet.of(NUMBER), null);
            INT.setup(EnumSet.of(INT), null);
            STRING.setup(EnumSet.of(PRIMITIVE), null);
            MAP.setup(EnumSet.of(ARRAY), null);
            OBJECT.setup(EnumSet.of(ARRAY), null);
            NUMBER.setup(EnumSet.of(PRIMITIVE), EnumSet.of(DOUBLE, INT));
            PRIMITIVE.setup(EnumSet.of(MIXED), EnumSet.of(STRING, NUMBER));
            ARRAY.setup(EnumSet.of(MIXED), EnumSet.of(MAP, OBJECT));
            MIXED.setup(null, EnumSet.of(PRIMITIVE, ARRAY));
        }
        EnumSet<DataTypes> children;
        
        EnumSet<DataTypes> parents;
        private void setup(EnumSet<DataTypes> parents, EnumSet<DataTypes> children){
            if(parents == null){
                this.parents = EnumSet.noneOf(DataTypes.class);
            } else {
                this.parents = parents;
            }
            if(children == null){
                this.children = EnumSet.noneOf(DataTypes.class);
            } else {
                this.children = children;
            }
        }
    }
    private static DataType MIXED = new DataType(DataTypes.MIXED, null);
    private static DataType PRIMITIVE = new DataType(DataTypes.PRIMITIVE, null);
    public static DataType MIXED(){
        return MIXED;
    }
    
    public static DataType PRIMITIVE(){
        return PRIMITIVE;
    }
    Map<DataTypes, Boolean> cachedCastableTo = new HashMap<DataTypes, Boolean>();
    private DataTypes subType;        
    
    private DataTypes type;
    
    private DataType(DataTypes myType, DataTypes subType){
        if(myType != DataTypes.ARRAY && subType != null){
            throw new Error("subType cannot be set except for arrays");
        }
        this.type = myType;
        this.subType = subType;
    }
    public boolean castableTo(DataType type){
        return castableTo(type.type, null);
    }        
    
    private boolean castableTo(DataTypes type, Boolean upward){
        if(cachedCastableTo.containsKey(type)){
            return cachedCastableTo.get(type);
        } else {
            boolean answer = false;
            //We need to see if type is either a parent, or a child (or a child of our children, etc)
            if(upward == null || upward == true){
                for(DataTypes parent : this.type.parents){
                    if(parent == type){
                        answer = true;
                        break;
                    } else {
                        if(castableTo(parent, true)){
                            answer = true;
                        }
                    }
                }
            }
            if(upward == null || upward == false){
                for(DataTypes child : this.type.children){
                    if(child == type){
                        answer = true;
                        break;
                    } else {
                        castableTo(child, false);
                    }
                }
            }
            cachedCastableTo.put(type, answer);
            return answer;
        }        
    }
    public boolean isSameType(DataType type){
        return this.type == type.type;
    }
    
}
