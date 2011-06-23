/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.testing;

import com.laytonsmith.aliasengine.Constructs.*;
import com.laytonsmith.aliasengine.Static;

/**
 * This class provides methods for more easily creating different Constructs for testing purposes.
 * @author Layton
 */
public class C {
    
    //Shortcut to Construct.class
    public static Class Array = CArray.class;
    public static Class Boolean = CBoolean.class;
    public static Class Double = CDouble.class;
    public static Class Int = CInt.class;
    public static Class Null = CNull.class;
    public static Class String = CString.class;
    public static Class Void = CVoid.class;
    public static Class IVariable = IVariable.class;
    public static Class Variable = Variable.class;
    
    
    public static CArray Array(Construct ... elems){
        return new CArray(0, elems);
    }
    public static CBoolean Boolean(boolean b){
        return new CBoolean(b, 0);
    }
    public static CDouble Double(double d){
        return new CDouble(d, 0);
    }
    public static CInt Int(long val){
        return new CInt(val, 0);
    }
    public static CNull Null(){
        return new CNull(0);
    }
    public static CString String(String s){
        return new CString(s, 0);
    }
    public static CVoid Void(){
        return new CVoid(0);
    }
    public static IVariable IVariable(String name, Construct val){
        return new IVariable(name, val, 0);
    }
    public static Variable Variable(String name, String val){
        return new Variable(name, val, 0);
    }
    /**
     * Returns a construct in the same way that constructs are resolved in scripts.
     * @param val
     * @return 
     */
    public static Construct onstruct(String val){
        return Static.resolveConstruct(val, 0);
    }
    public static Construct onstruct(long val){
        return Static.resolveConstruct(Long.toString(val), 0);
    }
    public static Construct onstruct(boolean val){
        return Static.resolveConstruct((val?"true":"false"), 0);
    }
    public static Construct onstruct(double val){
        return Static.resolveConstruct(java.lang.Double.toString(val), 0);
    }
}
