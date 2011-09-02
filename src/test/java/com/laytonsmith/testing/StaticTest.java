/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.testing;

import org.bukkit.command.CommandSender;
import com.laytonsmith.aliasengine.MScriptCompiler;
import com.laytonsmith.aliasengine.functions.exceptions.ConfigCompileException;
import java.lang.reflect.Field;
import com.laytonsmith.aliasengine.Constructs.Token;
import java.util.List;
import java.util.ArrayList;
import org.bukkit.entity.Player;
import com.laytonsmith.aliasengine.functions.exceptions.CancelCommandException;
import com.laytonsmith.aliasengine.functions.exceptions.ConfigRuntimeException;
import com.laytonsmith.aliasengine.Constructs.CBoolean;
import com.laytonsmith.aliasengine.Constructs.Construct;
import com.laytonsmith.aliasengine.Static;
import com.laytonsmith.aliasengine.Version;
import com.laytonsmith.aliasengine.functions.BasicLogic._equals;
import com.laytonsmith.aliasengine.functions.Function;
import com.laytonsmith.aliasengine.functions.exceptions.FunctionReturnException;
import com.laytonsmith.aliasengine.functions.exceptions.LoopBreakException;
import com.laytonsmith.aliasengine.functions.exceptions.LoopContinueException;
import com.sk89q.bukkit.migration.PermissionsResolverManager;
import com.sk89q.commandhelper.CommandHelperPlugin;
import java.util.Arrays;
import java.util.Random;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.command.ConsoleCommandSender;
import org.mockito.Mockito;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

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
    public static void TestBoilerplate(Function f, String name) {
        //For the "quality test code coverage" number, set this to true
        boolean runQualityTestsOnly = false;

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
        f.thrown();

        //name should match the given value
        if (!f.getName().equals(name)) {
            fail("Expected name of function to be " + name + ", but was given " + f.getName());
        }

        //docs needs to at least be more than a non-empty string, though in the future this should follow a more strict 
        //requirement set.
        if (f.docs().length() <= 0) {
            fail("docs must return a non-empty string");
        }

        TestDocs(f);


        //if creating a version string from this yeilds bogus data, it will throw an
        //exception for us.
        if (f.since().length() <= 0) {
            Version v = new Version(f.since());
        }
        if (f.numArgs().length == 0) {
            fail("numArgs must return an Integer array with more than zero values");
        }

        //If we want a "quality test coverage" number, we can't run this section, because it bombards the code
        //with random data to see if it fails in expected ways (to simulate how a user could run the scripts)
        //If we are interested in tests that are specific to the functions however, we shouldn't run this.
        if (!runQualityTestsOnly) {
            TestExec(f, fakePlayer);
            TestExec(f, null);
            TestExec(f, StaticTest.GetFakeConsoleCommandSender());
        }

        //now the only function left to test is exec. This cannot be abstracted, unfortunately.
    }

    /**
     * Checks to see if the documentation follows the specified format
     */
    public static void TestDocs(Function f) {
        //TODO
    }

    public static void TestExec(Function f, CommandSender p) {
        //See if the function throws something other than a ConfigRuntimeException or CancelCommandException if we send it bad arguments,
        //keeping in mind of course, that it isn't supposed to be able to accept the wrong number of arguments. Specifically, we want to try
        //strings, numbers, arrays, and nulls
        for (Integer i : f.numArgs()) {
            if (i == Integer.MAX_VALUE) {
                //er.. let's just try with 100...
                i = 100;
            }
            Construct[] con = new Construct[i];
            //Throw the book at it. Most functions will fail, and that is ok, what isn't
            //ok is if it throws an unexpected type of exception. It should only ever
            //throw a ConfigRuntimeException, or a CancelCommandException. Further,
            //if it throws a ConfigRuntimeException, the documentation should state so.
            for (int z = 0; z < 11; z++) {
                for (int a = 0; a < i; a++) {
                    switch (z) {
                        case 0:
                            con[a] = C.onstruct("hi");
                            break;
                        case 1:
                            con[a] = C.onstruct(1);
                            break;
                        case 2:
                            con[a] = C.Array(C.onstruct("hi"), C.onstruct(1));
                            break;
                        case 3:
                            con[a] = C.Null();
                            break;
                        case 4:
                            con[a] = C.onstruct(-1);
                            break;
                        case 5:
                            con[a] = C.onstruct(0);
                        case 6:
                            con[a] = C.onstruct(90000);
                        case 7:
                            con[a] = C.onstruct(a);
                        case 8:
                            con[a] = C.onstruct(new Random(System.currentTimeMillis()).nextDouble());
                        case 9:
                            con[a] = C.onstruct(new Random(System.currentTimeMillis()).nextInt());
                        case 10:
                            con[a] = C.onstruct(new Random(System.currentTimeMillis()).nextBoolean());
                    }
                }
                try {
                    f.exec(0, null, p, con);
                } catch (CancelCommandException e) {
                } catch (ConfigRuntimeException e) {
                    if (e.getExceptionType() != null) {
                        if (f.thrown() == null || !Arrays.asList(f.thrown()).contains(e.getExceptionType())) {
                            fail("The documentation for " + f.getName() + " doesn't state that it can throw a "
                                    + e.getExceptionType() + ", but it did.");
                        }
                    } //else it's uncatchable, which while it probably shouldn't happen often, it can.
                } catch (Throwable e) {
                    if (e instanceof LoopBreakException && !f.getName().equals("break")) {
                        fail("Only break() can throw LoopBreakExceptions");
                    }
                    if (e instanceof LoopContinueException && !f.getName().equals("continue")) {
                        fail("Only continue() can throw LoopContinueExceptions");
                    }
                    if (e instanceof FunctionReturnException && !f.getName().equals("return")) {
                        fail("Only return() can throw FunctionReturnExceptions");
                    }
                }
            }
        }
    }

    public static void TestClassDocs(String docs, Class container) {
        System.out.println("docs for " + container.getSimpleName());
        if (docs.length() <= 0) {
            fail("The docs for the " + container.getSimpleName() + " class are missing");
        }
    }

    /**
     * Gets the value out of s construct, ignoring information like line numbers.
     * @return 
     */
    public static Object Val(Construct c) {
        return c.val();
    }

    /**
     * Checks to see if two constructs are equal, using the same method that MScript equals() uses. In
     * fact, this method depends on equals() working, as it actually uses the function.
     * @param expected
     * @param actual 
     */
    public static void assertCEquals(Construct expected, Construct actual) throws CancelCommandException {
        _equals e = new _equals();
        CBoolean ret = (CBoolean) e.exec(0, null, null, expected, actual);
        if (ret.getBoolean() == false) {
            throw new AssertionError("Expected " + expected + " and " + actual + " to be equal to each other");
        }
    }

    /**
     * Does the opposite of assertCEquals
     * @param expected
     * @param actual
     * @throws CancelCommandException 
     */
    public static void assertCNotEquals(Construct expected, Construct actual) throws CancelCommandException {
        _equals e = new _equals();
        CBoolean ret = (CBoolean) e.exec(0, null, null, expected, actual);
        if (ret.getBoolean() == true) {
            throw new AssertionError("Did not expect " + expected + " and " + actual + " to be equal to each other");
        }
    }

    /**
     * Verifies that the given construct <em>resolves</em> to true. The resolution uses Static.getBoolean to
     * do the resolution.
     * @param actual 
     */
    public static void assertCTrue(Construct actual) {
        if (!Static.getBoolean(actual)) {
            fail("Expected '" + actual.val() + "' to resolve to true, but it did not");
        }
    }

    /**
     * Verifies that the given construct <em>resolves</em> to false. The resolution uses Static.getBoolean to
     * do the resolution.
     * @param actual 
     */
    public static void assertCFalse(Construct actual) {
        if (Static.getBoolean(actual)) {
            fail("Expected '" + actual.val() + "' to resolve to false, but it did not");
        }
    }

    /**
     * This function is used to assert that the type of a construct is one of the specified types.
     * @param test
     * @param retTypes 
     */
    public static void assertReturn(Construct test, Class... retTypes) {
        if (!Arrays.asList(retTypes).contains(test.getClass())) {
            StringBuilder b = new StringBuilder();
            if (retTypes.length == 1) {
                b.append("Expected return type to be ").append(retTypes[0].getSimpleName()).append(", but found ").append(test.getClass().getSimpleName());
            } else if (retTypes.length == 2) {
                b.append("Expected return type to be either ").append(retTypes[0].getSimpleName()).append(" or ").append(retTypes[1].getSimpleName()).append(", but found ").append(test.getClass().getSimpleName());
            } else {
                b.append("Expected return type to be one of: ");
                for (int i = 0; i < retTypes.length; i++) {
                    if (i < retTypes.length - 1) {
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

    public static List<Token> tokens(Token... array) {
        List<Token> tokens = new ArrayList<Token>();
        for (Token t : array) {
            tokens.add(t);
        }
        return tokens;
    }
    
    public static Player GetOnlinePlayer(){
        Server s = GetFakeServer();
        return GetOnlinePlayer("wraithguard01", s);
    }
    
    public static Player GetOnlinePlayer(Server s){
        return GetOnlinePlayer("wraithguard01", s);
    }
    
    public static Player GetOnlinePlayer(String name, Server s){
        Player p = mock(Player.class);
        when(p.isOnline()).thenReturn(true);
        when(p.getName()).thenReturn(name);        
        when(p.getServer()).thenReturn(s); 
        if(s != null && s.getOnlinePlayers() != null){
            List<Player> online = new ArrayList<Player>(Arrays.asList(s.getOnlinePlayers()));
            online.add(p);
            when(s.getOnlinePlayers()).thenReturn(online.toArray(new Player[]{}));
        }
        return p;
    }
    
    public static Player GetOp(String name, Server s){
        Player p = GetOnlinePlayer(name, s);
        when(p.isOp()).thenReturn(true);
        return p;
    }
    
    public static ConsoleCommandSender GetFakeConsoleCommandSender(){
        ConsoleCommandSender c = mock(ConsoleCommandSender.class);
        Server s = GetFakeServer();
        when(c.getServer()).thenReturn(s);
        return c;
    }
    
    public static Object GetVariable(Object instance, String var) throws Exception{
        return GetVariable(instance.getClass(), var, instance);
    }
    public static Object GetVariable(Class c, String var, Object instance) throws Exception{
        Field f = c.getField(var);
        f.setAccessible(true);
        return f.get(instance);
    }
    
    /**
     * Lexes, compiles, and runs a given mscript, using the given player.
     * @param script
     * @param player
     * @throws ConfigCompileException 
     */
    public static void Run(String script, CommandSender player) throws ConfigCompileException{
        MScriptCompiler.execute(MScriptCompiler.compile(MScriptCompiler.lex(script, null)), player, null, null);
    }
    
    /**
     * Creates an entire fake server environment, adding players and everything.
     */
    public static Server GetFakeServer(){
        Server fakeServer = mock(Server.class);
        String [] pnames = new String[]{"wraithguard01", "wraithguard02", "wraithguard03"};
        ArrayList<Player> pps = new ArrayList<Player>();
        for(String p : pnames){
            Player pp = GetOnlinePlayer(p, fakeServer);
            pps.add(pp);
        }
        when(fakeServer.getOnlinePlayers()).thenReturn(pps.toArray(new Player[]{}));  
        CommandHelperPlugin.myServer = fakeServer;  
        CommandHelperPlugin.perms = mock(PermissionsResolverManager.class);
        return fakeServer;
    }
    
}
