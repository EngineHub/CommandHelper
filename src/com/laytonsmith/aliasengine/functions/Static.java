/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.aliasengine.functions;

import com.laytonsmith.aliasengine.ConfigRuntimeException;
import com.laytonsmith.aliasengine.Constructs.*;

/**
 * This class contains various functions to simplify data type juggling. It provides methods for auto-casting constructs into
 * various (real) java data types.
 * @author Layton
 */
public class Static {

    public static double getNumber(Construct c) {
        double d;
        if (c instanceof CInt) {
            d = ((CInt) c).getInt();
        } else if (c instanceof CDouble) {
            d = ((CDouble) c).getDouble();
        } else {
            throw new ConfigRuntimeException("Expecting a number, but recieved " + c.val() + " instead");
        }
        return d;
    }

    public static double getDouble(Construct c) {
        try {
            return getNumber(c);
        } catch (ConfigRuntimeException e) {
            throw new ConfigRuntimeException("Expecting a double, but recieved " + c.val() + " instead");
        }
    }

    public static int getInt(Construct c) {
        int i;
        if (c instanceof CInt) {
            i = ((CInt) c).getInt();
        } else {
            throw new ConfigRuntimeException("Expecting an integer, but recieved " + c.val() + " instead");
        }
        return i;
    }
    
    public static boolean getBoolean(Construct c){
        boolean b = false;
        if(c instanceof CBoolean){
            b = ((CBoolean)c).getBoolean();
        } else if(c instanceof CString){
            b = (c.val().length() > 0);
        } else if(c instanceof CInt || c instanceof CDouble){
            b = (getNumber(c) > 0 || getNumber(c) < 0);
        }     
        return b;
    }

    public static boolean anyDoubles(Construct... c) {
        for (int i = 0; i < c.length; i++) {
            if (c[i] instanceof CDouble) {
                return true;
            }
        }
        return false;
    }
    
    public static boolean anyStrings(Construct... c){
        for(int i = 0; i < c.length; i++){
            if(c[i] instanceof CString){
                return true;
            }
        }
        return false;
    }
    
    public static boolean anyBooleans(Construct... c){
        for(int i = 0; i < c.length; i++){
            if(c[i] instanceof CBoolean){
                return true;
            }
        }
        return false;
    }
}
