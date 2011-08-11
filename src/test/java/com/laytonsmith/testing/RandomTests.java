/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.testing;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import com.laytonsmith.aliasengine.functions.Function;
import com.laytonsmith.aliasengine.Constructs.Construct;
import com.laytonsmith.aliasengine.functions.FunctionList;
import com.sun.org.apache.bcel.internal.util.Class2HTML;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.Test;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static com.laytonsmith.testing.StaticTest.*;
import static org.junit.Assert.*;

/**
 *
 * @author Layton
 */
public class RandomTests {
    /**
     * This function automatically tests all the boilerplate portions of all functions. Note that
     * this can be disabled in the StaticTest class, so that high quality test coverage can be measured.
     */
    @Test public void testAllBoilerplate(){
        for(Function f : FunctionList.getFunctionList()){
            StaticTest.TestBoilerplate(f, f.getName());
            Class upper = f.getClass().getEnclosingClass();
            if(upper == null){
                fail(f.getName() + " is not enclosed in an upper class.");
            }
            try {
                Method m = upper.getMethod("docs", new Class[]{});
                try{
                    String docs = m.invoke(null, new Object[]{}).toString();
                    StaticTest.TestClassDocs(docs, upper);
                } catch (NullPointerException ex){
                    fail(upper.getName() + "'s docs function should be static");
                }
            } catch (IllegalAccessException ex) {
                Logger.getLogger(RandomTests.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IllegalArgumentException ex) {
                Logger.getLogger(RandomTests.class.getName()).log(Level.SEVERE, null, ex);
            } catch (InvocationTargetException ex) {
                fail(upper.getName() + " throws an exception!");
            } catch (NoSuchMethodException ex) {
                fail(upper.getName() + " does not include a class level documentation function.");
            } catch (SecurityException ex) {
                Logger.getLogger(RandomTests.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    @Test public void testConstuctToString(){
        System.out.println("Construct.toString");
        assertEquals("hello", new Construct("hello", Construct.ConstructType.STRING, 0, null).toString());
    }
    
}
