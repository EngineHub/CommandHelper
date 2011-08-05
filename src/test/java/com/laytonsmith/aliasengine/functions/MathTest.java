/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.aliasengine.functions;

import com.laytonsmith.aliasengine.functions.exceptions.ConfigRuntimeException;
import com.laytonsmith.aliasengine.Constructs.IVariable;
import com.laytonsmith.aliasengine.Static;
import com.laytonsmith.testing.C;
import org.bukkit.Server;
import org.bukkit.entity.Player;
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
    Server fakeServer;
    Player fakePlayer;
    IVariableList varList;
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
        fakePlayer = mock(Player.class);
        fakeServer = mock(Server.class);
        when(fakePlayer.getServer()).thenReturn(fakeServer);
        varList = new IVariableList();
        varList.set(new IVariable("var", C.onstruct(1), 0));
        varList.set(new IVariable("var2", C.onstruct(2.5), 0));
    }
    
    @After
    public void tearDown() {
    }

    @Test
    public void testDocs() {
        System.out.println("docs");
        StaticTest.TestClassDocs(Math.docs(), Math.class);
    }
    
    @Test
    public void testAbs(){
        Math.abs a = new Math.abs();
        TestBoilerplate(a, "abs");
        assertCEquals(C.onstruct(5), a.exec(0, fakePlayer, C.onstruct(5)));
        assertCEquals(C.onstruct(3), a.exec(0, fakePlayer, C.onstruct(-3)));
        assertCEquals(C.onstruct(0), a.exec(0, fakePlayer, C.onstruct(0)));
    }
    
    @Test
    public void testAdd(){
        Math.add a = new Math.add();
        TestBoilerplate(a, "add");
        assertCEquals(C.onstruct(7), a.exec(0, fakePlayer, C.onstruct(5), C.onstruct(2)));
        assertCEquals(C.onstruct(6), a.exec(0, fakePlayer, C.onstruct(3), C.onstruct(3)));
        assertCEquals(C.onstruct(-4), a.exec(0, fakePlayer, C.onstruct(-3), C.onstruct(-1)));
        assertCEquals(C.onstruct(1), a.exec(0, fakePlayer, C.onstruct(1), C.onstruct(0)));
        assertCEquals(C.onstruct(3.1415), a.exec(0, fakePlayer, C.onstruct(3), C.onstruct(0.1415)));
    }
    
    @Test
    public void testDec(){
        Math.dec a = new Math.dec();
        TestBoilerplate(a, "dec");
        a.varList(varList);
        IVariable v = (IVariable)a.exec(0, fakePlayer, new IVariable("var", C.onstruct(1), 0));
        IVariable v2 = (IVariable)a.exec(0, fakePlayer,new IVariable("var2", C.onstruct(2.5), 0));
        assertCEquals(C.onstruct(0), v.ival());
        assertCEquals(C.onstruct(1.5), v2.ival());
    }
    
    @Test
    public void testDivide(){
        Math.divide a = new Math.divide();
        TestBoilerplate(a, "divide");
        assertCEquals(C.onstruct(2.5), a.exec(0, fakePlayer, C.onstruct(5), C.onstruct(2)));
        assertCEquals(C.onstruct(1), a.exec(0, fakePlayer, C.onstruct(3), C.onstruct(3)));
        assertCEquals(C.onstruct(3), a.exec(0, fakePlayer, C.onstruct(-3), C.onstruct(-1)));
        assertCEquals(C.onstruct(Double.POSITIVE_INFINITY), a.exec(0, fakePlayer, C.onstruct(1), C.onstruct(0)));
    }
    
    @Test
    public void testInc(){
        Math.inc a = new Math.inc();
        TestBoilerplate(a, "inc");
        a.varList(varList);
        IVariable v = (IVariable)a.exec(0, fakePlayer, new IVariable("var", C.onstruct(1), 0));
        IVariable v2 = (IVariable)a.exec(0, fakePlayer,new IVariable("var2", C.onstruct(2.5), 0));
        assertCEquals(C.onstruct(2), v.ival());
        assertCEquals(C.onstruct(3.5), v2.ival());
    }
    
    @Test
    public void testMod(){
        Math.mod a = new Math.mod();
        TestBoilerplate(a, "mod");
        assertCEquals(C.onstruct(1), a.exec(0, fakePlayer, C.onstruct(5), C.onstruct(2)));
        assertCEquals(C.onstruct(0), a.exec(0, fakePlayer, C.onstruct(3), C.onstruct(3)));
        assertCEquals(C.onstruct(-1), a.exec(0, fakePlayer, C.onstruct(-3), C.onstruct(-2)));
    }
    
    @Test
    public void testMultiply(){
        Math.multiply a = new Math.multiply();
        TestBoilerplate(a, "multiply");
        assertCEquals(C.onstruct(10), a.exec(0, fakePlayer, C.onstruct(5), C.onstruct(2)));
        assertCEquals(C.onstruct(9), a.exec(0, fakePlayer, C.onstruct(3), C.onstruct(3)));
        assertCEquals(C.onstruct(6), a.exec(0, fakePlayer, C.onstruct(-3), C.onstruct(-2)));
        assertCEquals(C.onstruct(5), a.exec(0, fakePlayer, C.onstruct(10), C.onstruct(0.5)));
    }
    
    @Test
    public void testPow(){
        Math.pow a = new Math.pow();
        TestBoilerplate(a, "pow");
        assertCEquals(C.onstruct(25), a.exec(0, fakePlayer, C.onstruct(5), C.onstruct(2)));
        assertCEquals(C.onstruct(27), a.exec(0, fakePlayer, C.onstruct(3), C.onstruct(3)));
        assertCEquals(C.onstruct(1), a.exec(0, fakePlayer, C.onstruct(-1), C.onstruct(-2)));
    }
    
    @Test
    public void testRand(){
        Math.rand a = new Math.rand();
        TestBoilerplate(a, "rand");
        for(int i = 0; i < 1000; i++){
            long j = Static.getInt(a.exec(0, fakePlayer, C.onstruct(10)));
            if(!(j < 10 && j >= 0)){
                fail("Expected a number between 0 and 10, but got " + j);
            }
            j = Static.getInt(a.exec(0, fakePlayer, C.onstruct(10), C.onstruct(20)));
            if(!(j < 20 && j >= 10)){
                fail("Expected a number between 10 and 20, but got " + j);
            }
        }
        try{
            a.exec(0, fakePlayer, C.onstruct(20), C.onstruct(10));
            fail("Didn't expect this test to pass");
        } catch(ConfigRuntimeException e){}
        try{
            a.exec(0, fakePlayer, C.onstruct(-1));
            fail("Didn't expect this test to pass");
        } catch(ConfigRuntimeException e){}
        try{
            a.exec(0, fakePlayer, C.onstruct(87357983597853791L));
            fail("Didn't expect this test to pass");
        } catch(ConfigRuntimeException e){}
    }
    
    @Test
    public void testSubtract(){
        Math.subtract a = new Math.subtract();
        TestBoilerplate(a, "subtract");
        assertCEquals(C.onstruct(3), a.exec(0, fakePlayer, C.onstruct(5), C.onstruct(2)));
        assertCEquals(C.onstruct(0), a.exec(0, fakePlayer, C.onstruct(3), C.onstruct(3)));
        assertCEquals(C.onstruct(-1), a.exec(0, fakePlayer, C.onstruct(-3), C.onstruct(-2)));
        assertCEquals(C.onstruct(3), a.exec(0, fakePlayer, C.onstruct(3.1415), C.onstruct(0.1415)));
    }
    
    @Test
    public void testFloor(){
        Math.floor a = new Math.floor();
        TestBoilerplate(a, "floor");
        assertCEquals(C.onstruct(3), a.exec(0, fakePlayer, C.onstruct(3.8415)));
        assertCEquals(C.onstruct(-4), a.exec(0, fakePlayer, C.onstruct(-3.1415)));
    }
    
    @Test public void testCeil(){
        Math.ceil a = new Math.ceil();
        TestBoilerplate(a, "ceil");
        assertCEquals(C.onstruct(4), a.exec(0, fakePlayer, C.onstruct(3.1415)));
        assertCEquals(C.onstruct(-3), a.exec(0, fakePlayer, C.onstruct(-3.1415)));
    }
}
