/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.testing;

import com.laytonsmith.abstraction.MCLocation;
import com.laytonsmith.abstraction.MCPlayer;
import com.laytonsmith.abstraction.MCServer;
import com.laytonsmith.abstraction.MCWorld;
import com.laytonsmith.commandhelper.CommandHelperPlugin;
import com.laytonsmith.core.ObjectGenerator;
import com.laytonsmith.core.constructs.*;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import com.laytonsmith.core.exceptions.MarshalException;
import com.laytonsmith.core.functions.Function;
import com.laytonsmith.core.functions.FunctionList;
import static com.laytonsmith.testing.StaticTest.SRun;
import com.sk89q.worldedit.expression.Expression;
import com.sk89q.worldedit.expression.ExpressionException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
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
        Map<String, Throwable> uhohs = new HashMap<String, Throwable>();
        for(Function f : FunctionList.getFunctionList()){
            try{
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
            } catch(Throwable t){
                uhohs.put(f.getClass().getName(), t);
            }
        }
        if(!uhohs.isEmpty()){
            StringBuilder b = new StringBuilder();
            for(String key : uhohs.keySet()){
                b.append(key).append(" threw: ").append(uhohs.get(key)).append("\n");
            }
            String output = ("There was/were " + uhohs.size() + " boilerplate failure(s). Output:\n" + b.toString());
            System.out.println(output);
            fail(output); 
        }
    }
    
    @Test public void testConstuctToString(){
        assertEquals("hello", new CString("hello", Target.UNKNOWN).toString());
    }
    
    @Test public void testClone() throws CloneNotSupportedException{
        CArray c1 = C.Array(C.Void(), C.Void()).clone();
        CBoolean c2 = C.Boolean(true).clone();
        CDouble c4 = C.Double(1).clone();
        CFunction c5 = new CFunction("", Target.UNKNOWN).clone();
        CInt c6 = C.Int(1).clone();
        CNull c7 = C.Null().clone();
        CString c8 = C.String("").clone();
        CVoid c9 = C.Void().clone();
        Command c10 = new Command("/c", Target.UNKNOWN).clone();
        IVariable c12 = new IVariable("@name", C.Null(), Target.UNKNOWN).clone();
        Variable c13 = new Variable("$name", "", false, false, Target.UNKNOWN);
    }
    
    @Test public void testJSONEscapeString() throws MarshalException{
        CArray ca = new CArray(Target.UNKNOWN);
        ca.push(C.Int(1));
        ca.push(C.Double(2.2));
        ca.push(C.String("string"));
        ca.push(C.String("\"Quote\""));
        ca.push(C.Boolean(true));
        ca.push(C.Boolean(false));
        ca.push(C.Null());
        ca.push(C.Void());
        ca.push(new Command("/Command", Target.UNKNOWN));
        ca.push(new CArray(Target.UNKNOWN, new CInt(1, Target.UNKNOWN)));
        //[1, 2.2, "string", "\"Quote\"", true, false, null, "", "/Command", [1]]
        assertEquals("[1,2.2,\"string\",\"\\\"Quote\\\"\",true,false,null,\"\",\"\\/Command\",[1]]", Construct.json_encode(ca, Target.UNKNOWN));
    }
    
    @Test public void testJSONDecodeString() throws MarshalException{
        CArray ca = new CArray(Target.UNKNOWN);
        ca.push(C.Int(1));
        ca.push(C.Double(2.2));
        ca.push(C.String("string"));
        ca.push(C.String("\"Quote\""));
        ca.push(C.Boolean(true));
        ca.push(C.Boolean(false));
        ca.push(C.Null());
        ca.push(C.Void());
        ca.push(new Command("/Command", Target.UNKNOWN));
        ca.push(new CArray(Target.UNKNOWN, new CInt(1, Target.UNKNOWN)));
        StaticTest.assertCEquals(ca, Construct.json_decode("[1, 2.2, \"string\", \"\\\"Quote\\\"\", true, false, null, \"\", \"\\/Command\", [1]]", Target.UNKNOWN));
    }
    
    @Test public void testReturnArrayFromProc() throws ConfigCompileException{
        assertEquals("{1, 2, 3}", SRun("proc(_test, @var, assign(@array, array(1, 2)) array_push(@array, @var) return(@array)) _test(3)", null));
    }
    
    /*@Test*/ public void testStaticGetLocation(){
        MCWorld fakeWorld = mock(MCWorld.class);
        MCServer fakeServer = mock(MCServer.class);
        when(fakeServer.getWorld("world")).thenReturn(fakeWorld);
        CommandHelperPlugin.myServer = fakeServer;
        CArray ca1 = new CArray(Target.UNKNOWN, C.onstruct(1), C.onstruct(2), C.onstruct(3));
        CArray ca2 = new CArray(Target.UNKNOWN, C.onstruct(1), C.onstruct(2), C.onstruct(3), C.onstruct("world"));
        CArray ca3 = new CArray(Target.UNKNOWN, C.onstruct(1), C.onstruct(2), C.onstruct(3), C.onstruct(45), C.onstruct(50));
        CArray ca4 = new CArray(Target.UNKNOWN, C.onstruct(1), C.onstruct(2), C.onstruct(3), C.onstruct("world"), C.onstruct(45), C.onstruct(50));
        MCLocation l1 = ObjectGenerator.GetGenerator().location(ca1, fakeWorld, Target.UNKNOWN);
        MCLocation l2 = ObjectGenerator.GetGenerator().location(ca2, fakeWorld, Target.UNKNOWN);
        MCLocation l3 = ObjectGenerator.GetGenerator().location(ca3, fakeWorld, Target.UNKNOWN);
        MCLocation l4 = ObjectGenerator.GetGenerator().location(ca4, fakeWorld, Target.UNKNOWN);
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
