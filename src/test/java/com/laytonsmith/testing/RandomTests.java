/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.testing;

import com.laytonsmith.abstraction.MCLocation;
import com.laytonsmith.abstraction.MCServer;
import com.laytonsmith.abstraction.MCWorld;
import com.laytonsmith.abstraction.MCPlayer;
import org.junit.Before;
import com.sk89q.worldedit.expression.ExpressionException;
import com.sk89q.worldedit.expression.Expression;
import org.bukkit.Location;
import org.bukkit.World;
import com.laytonsmith.aliasengine.Constructs.*;
import com.laytonsmith.aliasengine.Static;
import com.laytonsmith.aliasengine.exceptions.ConfigCompileException;
import com.laytonsmith.aliasengine.exceptions.MarshalException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import com.laytonsmith.aliasengine.functions.Function;
import com.laytonsmith.aliasengine.functions.FunctionList;
import com.sk89q.commandhelper.CommandHelperPlugin;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Server;
import org.junit.Test;
import static org.junit.Assert.*;
import static com.laytonsmith.testing.StaticTest.*;
import static org.mockito.Mockito.*;

/**
 *
 * @author Layton
 */
public class RandomTests {
    MCPlayer fakePlayer;
    
    @Before
    public void setUp(){
        fakePlayer = StaticTest.GetOnlinePlayer();
    }
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
        assertEquals("hello", new CString("hello", 0, null).toString());
    }
    
    @Test public void testClone() throws CloneNotSupportedException{
        CArray c1 = C.Array(C.Void(), C.Void()).clone();
        CBoolean c2 = C.Boolean(true).clone();
        CClosure c3 = new CClosure("", null, 0, null).clone();
        CDouble c4 = C.Double(1).clone();
        CFunction c5 = new CFunction("", 0, null).clone();
        CInt c6 = C.Int(1).clone();
        CNull c7 = C.Null().clone();
        CString c8 = C.String("").clone();
        CVoid c9 = C.Void().clone();
        Command c10 = new Command("/c", 0, null).clone();
        IVariable c12 = new IVariable("@name", C.Null(), 0, null).clone();
        Variable c13 = new Variable("$name", "", false, false, 0, null);
    }
    
    @Test public void testJSONEscapeString() throws MarshalException{
        CArray ca = new CArray(0, null);
        ca.push(C.Int(1));
        ca.push(C.Double(2.2));
        ca.push(C.String("string"));
        ca.push(C.String("\"Quote\""));
        ca.push(C.Boolean(true));
        ca.push(C.Boolean(false));
        ca.push(C.Null());
        ca.push(C.Void());
        ca.push(new Command("/Command", 0, null));
        ca.push(new CArray(0, null, new CInt(1, 0, null)));
        //[1, 2.2, "string", "\"Quote\"", true, false, null, "", "/Command", [1]]
        assertEquals("[1,2.2,\"string\",\"\\\"Quote\\\"\",true,false,null,\"\",\"\\/Command\",[1]]", Construct.json_encode(ca));
    }
    
    @Test public void testJSONDecodeString() throws MarshalException{
        CArray ca = new CArray(0, null);
        ca.push(C.Int(1));
        ca.push(C.Double(2.2));
        ca.push(C.String("string"));
        ca.push(C.String("\"Quote\""));
        ca.push(C.Boolean(true));
        ca.push(C.Boolean(false));
        ca.push(C.Null());
        ca.push(C.Void());
        ca.push(new Command("/Command", 0, null));
        ca.push(new CArray(0, null, new CInt(1, 0, null)));
        StaticTest.assertCEquals(ca, Construct.json_decode("[1, 2.2, \"string\", \"\\\"Quote\\\"\", true, false, null, \"\", \"\\/Command\", [1]]"));
    }
    
    @Test public void testReturnArrayFromProc() throws ConfigCompileException{
        assertEquals("{1, 2, 3}", SRun("proc(_test, @var, assign(@array, array(1, 2)) array_push(@array, @var) return(@array)) _test(3)", null));
    }
    
    /*@Test*/ public void testStaticGetLocation(){
        MCWorld fakeWorld = mock(MCWorld.class);
        MCServer fakeServer = mock(MCServer.class);
        when(fakeServer.getWorld("world")).thenReturn(fakeWorld);
        CommandHelperPlugin.myServer = fakeServer;
        CArray ca1 = new CArray(0, null, C.onstruct(1), C.onstruct(2), C.onstruct(3));
        CArray ca2 = new CArray(0, null, C.onstruct(1), C.onstruct(2), C.onstruct(3), C.onstruct("world"));
        CArray ca3 = new CArray(0, null, C.onstruct(1), C.onstruct(2), C.onstruct(3), C.onstruct(45), C.onstruct(50));
        CArray ca4 = new CArray(0, null, C.onstruct(1), C.onstruct(2), C.onstruct(3), C.onstruct("world"), C.onstruct(45), C.onstruct(50));
        MCLocation l1 = Static.GetLocation(ca1, fakeWorld, 0, null);
        MCLocation l2 = Static.GetLocation(ca2, fakeWorld, 0, null);
        MCLocation l3 = Static.GetLocation(ca3, fakeWorld, 0, null);
        MCLocation l4 = Static.GetLocation(ca4, fakeWorld, 0, null);
        assertEquals(fakeWorld, l1.getWorld());
        assertEquals(fakeWorld, l2.getWorld());
        assertEquals(fakeWorld, l3.getWorld());
        assertEquals(fakeWorld, l4.getWorld());
        assertEquals(1, l1.getX(), 0.00000000000000001);
        assertEquals(1, l2.getX(), 0.00000000000000001);
        assertEquals(1, l4.getX(), 0.00000000000000001);
        assertEquals(1, l4.getX(), 0.00000000000000001);
        assertEquals(2, l1.getY(), 0.00000000000000001);
        assertEquals(2, l2.getY(), 0.00000000000000001);
        assertEquals(2, l3.getY(), 0.00000000000000001);
        assertEquals(2, l4.getY(), 0.00000000000000001);
        assertEquals(3, l1.getZ(), 0.00000000000000001);
        assertEquals(3, l2.getZ(), 0.00000000000000001);
        assertEquals(3, l3.getZ(), 0.00000000000000001);
        assertEquals(3, l4.getZ(), 0.00000000000000001);
        assertEquals(0, l1.getYaw(), 0.0000000000000000001);
        assertEquals(0, l2.getYaw(), 0.0000000000000000001);
        assertEquals(45, l3.getYaw(), 0.0000000000000000001);
        assertEquals(45, l4.getYaw(), 0.0000000000000000001);
        assertEquals(0, l1.getPitch(), 0.0000000000000000001);
        assertEquals(0, l2.getPitch(), 0.0000000000000000001);
        assertEquals(50, l3.getPitch(), 0.0000000000000000001);
        assertEquals(50, l4.getPitch(), 0.0000000000000000001);
        CommandHelperPlugin.myServer = null;
    }        
    
    @Test public void expressionTester() throws ExpressionException{
        //verify basic usage works
        Expression e = Expression.compile("(x + 2) * y", "x", "y");        
        assertEquals(16, e.evaluate(2, 4), 0.00001);
    }
    
    @Test public void testProcScope() throws ConfigCompileException{
        SRun("proc(_b, assign(@a, 2)) assign(@a, 1) _b() msg(@a)", fakePlayer);
        verify(fakePlayer).sendMessage("1");
    }
    
    @Test public void testDataLookup() throws ConfigCompileException{
        assertEquals("1", SRun("data_values(stone)", fakePlayer));
        assertEquals("4", SRun("data_values(cstone)", fakePlayer));
        assertEquals("6:2", SRun("data_values(birchsapling)", fakePlayer));
        assertEquals("35:14", SRun("data_values(redwool)", fakePlayer));
        assertEquals("35:14", SRun("data_values('wool:red')", fakePlayer));
        assertEquals("35:14", SRun("data_values(REDWOOL)", fakePlayer));
    }
    
}
