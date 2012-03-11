/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.core;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Marks a function as an API function, which includes it in the list of functions.
 * @author Layton
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface api {
    /**
     * This is a list of valid classes that are valid to be tagged with this annotation.
     */
    public static enum ValidClasses{
        FUNCTION(com.laytonsmith.core.functions.Function.class),
        EVENT(com.laytonsmith.core.events.Event.class);
        private static List<Class> classes = null;
        Class classType;
        private ValidClasses(Class c){
            classType = c;
        }
        
        /**
         * Returns a copy of the list of valid classes that may be tagged with
         * the api annotation.
         * @return 
         */
        public static List<Class> Classes(){
            if(classes == null){
                Class[] cc = new Class[ValidClasses.values().length];
                for(int i = 0; i < ValidClasses.values().length; i++){
                    cc[i] = ValidClasses.values()[i].classType;
                }
                classes = Arrays.asList(cc);
            }
            return new ArrayList<Class>(classes);
        }
        
        /**
         * Returns true if the specified class extends a valid class.
         * @param c
         * @return 
         */
        public static boolean IsValid(Class c){
            for(Class cc : Classes()){
                if(cc.isAssignableFrom(c)){
                    return true;
                }
            }
            return false;
        }
    }
}
