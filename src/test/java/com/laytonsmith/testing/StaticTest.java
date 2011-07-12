/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.testing;

import com.laytonsmith.aliasengine.Constructs.Token;
import java.util.List;
import java.util.ArrayList;
import org.bukkit.entity.Player;
import com.laytonsmith.aliasengine.CancelCommandException;
import com.laytonsmith.aliasengine.ConfigRuntimeException;
import com.laytonsmith.aliasengine.Constructs.CBoolean;
import com.laytonsmith.aliasengine.Constructs.Construct;
import com.laytonsmith.aliasengine.Static;
import com.laytonsmith.aliasengine.Version;
import com.laytonsmith.aliasengine.functions.BasicLogic._equals;
import com.laytonsmith.aliasengine.functions.Function;
import java.util.Arrays;
import org.bukkit.Server;
import org.bukkit.World;
import org.mockito.Mockito;
import static org.junit.Assert.*;
/**
 * 
 * @author Layton
 */
public class StaticTest {
    /**
     * Tests the boilerplate functions in a Function. While all functions should conform to
     * at least this, it is useful to also use the more strict TestBoilerplate function.
     * @param f 
     */
    public static void TestBoilerplate(Function f, String name){
        Player fakePlayer = Mockito.mock(Player.class);
        Server fakeServer = Mockito.mock(Server.class);
        World fakeWorld = Mockito.mock(World.class);
        Mockito.when(fakePlayer.getServer()).thenReturn(fakeServer);
        Mockito.when(fakePlayer.getWorld()).thenReturn(fakeWorld);
        System.out.println(name);
        //make sure that these functions don't throw an exception. Any other results
        //are fine
        f.isRestricted();
        f.runAsync();
        f.varList(null);
        f.preResolveVariables();
        
        //name should match the given value
        if(!f.getName().equals(name)){
            fail("Expected name of function to be " + name + ", but was given " + f.getName());
        }        
        
        //docs needs to at least be more than a non-empty string, though in the future this should follow a more strict 
        //requirement set.
        if(f.docs().length() <= 0){
            fail("docs must return a non-empty string");
        }
        
        TestDocs(f);
        
        
        //if creating a version string from this yeilds bogus data, it will throw an
        //exception for us.
        if(f.since().length() <= 0){
            Version v = new Version(f.since());
        }
        if(f.numArgs().length == 0){
            fail("numArgs must return an Integer array with more than zero values");
        }
        //See if the function throws something other than a ConfigRuntimeException or CancelCommandException if we send it bad arguments,
        //keeping in mind of course, that it isn't supposed to be able to accept the wrong number of arguments. Specifically, we want to try
        //strings, numbers, arrays, and nulls
        for(Integer i : f.numArgs()){
            if(i == Integer.MAX_VALUE){
                //er.. let's just try with 100...
                i = 100;
            }
            Construct[] con = new Construct[i];
            for(int z = 0; z < 4; z++){
                for(int a = 0; a < i; a++){
                    switch(z){
                        case 0:
                            con[a] = C.onstruct("hi"); break;
                        case 1:
                            con[a] = C.onstruct(1); break;
                        case 2:
                            con[a] = C.Array(C.onstruct("hi"), C.onstruct(1)); break;
                        case 3:
                            con[a] = C.Null(); break;
                    }
                }
                try{
                    f.exec(0, fakePlayer, con);
                } catch(CancelCommandException e){
                } catch(ConfigRuntimeException e){                
                }
            }
        }
        
        
        //now the only function left to test is exec. This cannot be abstracted, unfortunately.
    }
    /**
     * Checks to see if the documentation follows the specified format
     */
    public static void TestDocs(Function f){
        //TODO
    }
    
    public static void TestClassDocs(String docs, Class container){
        System.out.println("docs for " + container.getSimpleName());
        if(docs.length() <= 0){
            fail("The docs for the " + container.getSimpleName() + " class are missing");
        }
    }
    
    /**
     * Gets the value out of s construct, ignoring information like line numbers.
     * @return 
     */
    public static Object Val(Construct c){
        return c.val();
    }
    
    /**
     * Checks to see if two constructs are equal, using the same method that MScript equals() uses. In
     * fact, this method depends on equals() working, as it actually uses the function.
     * @param expected
     * @param actual 
     */
    public static void assertCEquals(Construct expected, Construct actual) throws CancelCommandException{
        _equals e = new _equals();
        CBoolean ret = (CBoolean)e.exec(0, null, expected, actual);
        if(ret.getBoolean() == false){
            throw new AssertionError("Expected " + expected + " and " + actual + " to be equal to each other");
        }
    }
    
    /**
     * Does the opposite of assertCEquals
     * @param expected
     * @param actual
     * @throws CancelCommandException 
     */
    public static void assertCNotEquals(Construct expected, Construct actual) throws CancelCommandException{
        _equals e = new _equals();
        CBoolean ret = (CBoolean)e.exec(0, null, expected, actual);
        if(ret.getBoolean() == true){
            throw new AssertionError("Did not expect " + expected + " and " + actual + " to be equal to each other");
        }
    }
    
    /**
     * Verifies that the given construct <em>resolves</em> to true. The resolution uses Static.getBoolean to
     * do the resolution.
     * @param actual 
     */
    public static void assertCTrue(Construct actual){
        if(!Static.getBoolean(actual)){
            fail("Expected '" + actual.val() + "' to resolve to true, but it did not");
        }
    }
    
    /**
     * Verifies that the given construct <em>resolves</em> to false. The resolution uses Static.getBoolean to
     * do the resolution.
     * @param actual 
     */
    public static void assertCFalse(Construct actual){
        if(Static.getBoolean(actual)){
            fail("Expected '" + actual.val() + "' to resolve to false, but it did not");
        }
    }
    
    /**
     * This function is used to assert that the type of a construct is one of the specified types.
     * @param test
     * @param retTypes 
     */
    public static void assertReturn(Construct test, Class ... retTypes){
        if(!Arrays.asList(retTypes).contains(test.getClass())){
            StringBuilder b = new StringBuilder();
            if(retTypes.length == 1){
                b.append("Expected return type to be ").append(retTypes[0].getSimpleName())
                        .append(", but found ").append(test.getClass().getSimpleName());
            }
            else if (retTypes.length == 2){
                b.append("Expected return type to be either ").append(retTypes[0].getSimpleName())
                        .append(" or ").append(retTypes[1].getSimpleName()).append(", but found ")
                        .append(test.getClass().getSimpleName());
            } else {
                b.append("Expected return type to be one of: ");
                for(int i = 0; i < retTypes.length; i++){
                    if(i < retTypes.length - 1){
                        b.append(retTypes[i].getSimpleName()).append(", ");
                    } else {
                        b.append("or ").append(retTypes[i].getSimpleName());
                    }
                }
                b.append(", but found ").append(test.getClass().getSimpleName());
            }
            throw new AssertionError(b);
        }
    }
    
    public static List<Token> tokens(Token ... array){
        List<Token> tokens = new ArrayList<Token>();
        for(Token t : array){
            tokens.add(t);
        }
        return tokens;
    }
}
