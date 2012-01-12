/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.aliasengine.functions;

import com.laytonsmith.puls3.core.functions.IVariableList;
import com.laytonsmith.puls3.core.functions.Math;
import com.laytonsmith.puls3.abstraction.MCPlayer;
import com.laytonsmith.puls3.abstraction.MCServer;
import com.laytonsmith.puls3.core.Env;
import com.laytonsmith.puls3.core.exceptions.ConfigCompileException;
import com.laytonsmith.puls3.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.puls3.core.constructs.IVariable;
import com.laytonsmith.puls3.core.Static;
import com.laytonsmith.testing.C;
import com.laytonsmith.testing.StaticTest;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import static com.laytonsmith.testing.StaticTest.*;
import static org.mockito.Mockito.*;

/**
 *
 * @author Layton
 */
public class MathTest {
    MCServer fakeServer;
    MCPlayer fakePlayer;
    IVariableList varList;
    Env env = new Env();
    public MathTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }
    
    @Before
    public void setUp() {
        fakePlayer = GetOnlinePlayer();
        fakeServer = GetFakeServer();

        varList = new IVariableList();
        varList.set(new IVariable("var", C.onstruct(1), 0, null));
        varList.set(new IVariable("var2", C.onstruct(2.5), 0, null));
        env.SetVarList(varList);
        env.SetPlayer(fakePlayer);
    }
    
    @After
    public void tearDown() {
    }
    
    @Test
    public void testAbs(){
        Math.abs a = new Math.abs();
        assertCEquals(C.onstruct(5), a.exec(0, null, env, C.onstruct(5)));
        assertCEquals(C.onstruct(3), a.exec(0, null, env, C.onstruct(-3)));
        assertCEquals(C.onstruct(0), a.exec(0, null, env, C.onstruct(0)));
    }
    
    @Test
    public void testAdd(){
        Math.add a = new Math.add();
        assertCEquals(C.onstruct(7), a.exec(0, null, env, C.onstruct(5), C.onstruct(2)));
        assertCEquals(C.onstruct(6), a.exec(0, null, env, C.onstruct(3), C.onstruct(3)));
        assertCEquals(C.onstruct(-4), a.exec(0, null, env, C.onstruct(-3), C.onstruct(-1)));
        assertCEquals(C.onstruct(1), a.exec(0, null, env, C.onstruct(1), C.onstruct(0)));
        assertCEquals(C.onstruct(3.1415), a.exec(0, null, env, C.onstruct(3), C.onstruct(0.1415)));
    }
    
    @Test
    public void testDec() throws ConfigCompileException{
        Math.dec a = new Math.dec();
        IVariable v = (IVariable)a.exec(0, null, env, new IVariable("var", C.onstruct(1), 0, null));
        IVariable v2 = (IVariable)a.exec(0, null, env,new IVariable("var2", C.onstruct(2.5), 0, null));
        assertCEquals(C.onstruct(0), v.ival());
        assertCEquals(C.onstruct(1.5), v2.ival());
        StaticTest.SRun("assign(@var, 0) dec(@var, 2) msg(@var)", fakePlayer);
        verify(fakePlayer).sendMessage("-2");
    }
    
    @Test
    public void testDivide(){
        Math.divide a = new Math.divide();
        assertCEquals(C.onstruct(2.5), a.exec(0, null, env, C.onstruct(5), C.onstruct(2)));
        assertCEquals(C.onstruct(1), a.exec(0, null, env, C.onstruct(3), C.onstruct(3)));
        assertCEquals(C.onstruct(3), a.exec(0, null, env, C.onstruct(-3), C.onstruct(-1)));
        assertCEquals(C.onstruct(Double.POSITIVE_INFINITY), a.exec(0, null, env, C.onstruct(1), C.onstruct(0)));
    }
    
    @Test
    public void testInc() throws ConfigCompileException{
        Math.inc a = new Math.inc();
        IVariable v = (IVariable)a.exec(0, null, env, new IVariable("var", C.onstruct(1), 0, null));
        IVariable v2 = (IVariable)a.exec(0, null, env,new IVariable("var2", C.onstruct(2.5), 0, null));
        assertCEquals(C.onstruct(2), v.ival());
        assertCEquals(C.onstruct(3.5), v2.ival());
        StaticTest.SRun("assign(@var, 0) inc(@var, 2) msg(@var)", fakePlayer);
        verify(fakePlayer).sendMessage("2");
    }
    
    @Test
    public void testMod(){
        Math.mod a = new Math.mod();
        assertCEquals(C.onstruct(1), a.exec(0, null, env, C.onstruct(5), C.onstruct(2)));
        assertCEquals(C.onstruct(0), a.exec(0, null, env, C.onstruct(3), C.onstruct(3)));
        assertCEquals(C.onstruct(-1), a.exec(0, null, env, C.onstruct(-3), C.onstruct(-2)));
    }
    
    @Test
    public void testMultiply(){
        Math.multiply a = new Math.multiply();
        assertCEquals(C.onstruct(10), a.exec(0, null, env, C.onstruct(5), C.onstruct(2)));
        assertCEquals(C.onstruct(9), a.exec(0, null, env, C.onstruct(3), C.onstruct(3)));
        assertCEquals(C.onstruct(6), a.exec(0, null, env, C.onstruct(-3), C.onstruct(-2)));
        assertCEquals(C.onstruct(5), a.exec(0, null, env, C.onstruct(10), C.onstruct(0.5)));
    }
    
    @Test
    public void testPow(){
        Math.pow a = new Math.pow();
        assertCEquals(C.onstruct(25), a.exec(0, null, env, C.onstruct(5), C.onstruct(2)));
        assertCEquals(C.onstruct(27), a.exec(0, null, env, C.onstruct(3), C.onstruct(3)));
        assertCEquals(C.onstruct(1), a.exec(0, null, env, C.onstruct(-1), C.onstruct(-2)));
    }
    
    @Test
    public void testRand(){
        Math.rand a = new Math.rand();
        for(int i = 0; i < 1000; i++){
            long j = Static.getInt(a.exec(0, null, env, C.onstruct(10)));
            if(!(j < 10 && j >= 0)){
                fail("Expected a number between 0 and 10, but got " + j);
            }
            j = Static.getInt(a.exec(0, null, env, C.onstruct(10), C.onstruct(20)));
            if(!(j < 20 && j >= 10)){
                fail("Expected a number between 10 and 20, but got " + j);
            }
        }
        try{
            a.exec(0, null, env, C.onstruct(20), C.onstruct(10));
            fail("Didn't expect this test to pass");
        } catch(ConfigRuntimeException e){}
        try{
            a.exec(0, null, env, C.onstruct(-1));
            fail("Didn't expect this test to pass");
        } catch(ConfigRuntimeException e){}
        try{
            a.exec(0, null, env, C.onstruct(87357983597853791L));
            fail("Didn't expect this test to pass");
        } catch(ConfigRuntimeException e){}
    }
    
    @Test
    public void testSubtract(){
        Math.subtract a = new Math.subtract();
        assertCEquals(C.onstruct(3), a.exec(0, null, env, C.onstruct(5), C.onstruct(2)));
        assertCEquals(C.onstruct(0), a.exec(0, null, env, C.onstruct(3), C.onstruct(3)));
        assertCEquals(C.onstruct(-1), a.exec(0, null, env, C.onstruct(-3), C.onstruct(-2)));
        assertCEquals(C.onstruct(3), a.exec(0, null, env, C.onstruct(3.1415), C.onstruct(0.1415)));
    }
    
    @Test
    public void testFloor(){
        Math.floor a = new Math.floor();
        assertCEquals(C.onstruct(3), a.exec(0, null, env, C.onstruct(3.8415)));
        assertCEquals(C.onstruct(-4), a.exec(0, null, env, C.onstruct(-3.1415)));
    }
    
    @Test public void testCeil(){
        Math.ceil a = new Math.ceil();
        assertCEquals(C.onstruct(4), a.exec(0, null, env, C.onstruct(3.1415)));
        assertCEquals(C.onstruct(-3), a.exec(0, null, env, C.onstruct(-3.1415)));
    }
    
    @Test public void testSqrt() throws ConfigCompileException{
        assertEquals("3", StaticTest.SRun("sqrt(9)", fakePlayer));
        assertEquals("Test failed", java.lang.Math.sqrt(2), Double.parseDouble(StaticTest.SRun("sqrt(2)", fakePlayer)), .000001);        
        try{
            StaticTest.SRun("sqrt(-1)", fakePlayer);
            fail("Did not expect to pass");
        } catch(ConfigRuntimeException e){
            //pass
        }
    }
    
    @Test public void testMin() throws ConfigCompileException{
        assertEquals("-2", StaticTest.SRun("min(2, array(5, 6, 4), -2)", fakePlayer));
    }
    
    @Test public void testMax() throws ConfigCompileException{
        assertEquals("50", StaticTest.SRun("max(6, 7, array(4, 4, 50), 2, 5)", fakePlayer));
    }
}
