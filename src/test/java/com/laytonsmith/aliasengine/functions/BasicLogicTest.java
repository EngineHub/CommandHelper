/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.aliasengine.functions;

import org.bukkit.Server;
import com.laytonsmith.aliasengine.functions.exceptions.CancelCommandException;
import com.laytonsmith.aliasengine.Constructs.*;
import com.laytonsmith.aliasengine.functions.exceptions.ConfigCompileException;
import com.laytonsmith.testing.C;
import org.bukkit.entity.Player;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static com.laytonsmith.testing.StaticTest.*;
import static org.mockito.Mockito.*;

/**
 *
 * @author Layton
 */
public class BasicLogicTest {
    
    Player fakePlayer;
    Server fakeServer;
    CArray commonArray;
    CInt arg1_1;
    CInt arg1_2;
    CInt arg2_1;
    CInt argn1_1;
    CInt argn2_1;
    CBoolean _true;
    CBoolean _false;
    
    public BasicLogicTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }
    
    @Before
    public void setUp() {
        commonArray = C.Array(C.Null(), C.Int(1), C.String("2"), C.Double(3.0));
        arg1_1 = C.Int(1);
        arg1_2 = C.Int(1);
        arg2_1 = C.Int(2);
        argn1_1 = C.Int(-1);
        argn2_1 = C.Int(-2);
        _true = C.Boolean(true);
        _false = C.Boolean(false);
        fakeServer = GetFakeServer();
        fakePlayer = GetOnlinePlayer(fakeServer);
    }
    
    @After
    public void tearDown() {
    }

    @Test
    public void testDocs() {
        TestClassDocs(BasicLogic.docs(), BasicLogic.class);
    }
    
    @Test
    public void testEquals() throws CancelCommandException{
        BasicLogic.equals e = new BasicLogic.equals();
        Construct ret = null;
        assertCTrue(e.exec(0, null, fakePlayer, arg1_1, arg1_2));
        assertCFalse(e.exec(0, null, fakePlayer, arg1_1, arg2_1));
        assertCFalse(e.exec(0, null, fakePlayer, argn1_1, arg1_1));
        assertCFalse(e.exec(0, null, fakePlayer, C.onstruct(1), C.onstruct("string")));
    }
    
    public void testEqualsIC() throws ConfigCompileException{
        SRun("if(equals_ic('hi', 'HI'), msg('pass'))", fakePlayer);
        SRun("if(equals_ic('hi', 'hi'), msg('pass'))", fakePlayer);
        SRun("if(not(equals_ic('what', 'hi')), msg('pass'))", fakePlayer);
        SRun("if(equals_ic(2, 2), msg('pass'))", fakePlayer);
        SRun("if(not(equals_ic(2, 'hi')), msg('pass'))", fakePlayer);
        verify(fakePlayer, times(5)).sendMessage("pass");
    }
    
    @Test public void testAnd() throws CancelCommandException{
        BasicLogic.and a = new BasicLogic.and();
        assertCTrue(a.exec(0, null, fakePlayer, _true, _true, _true));
        assertCFalse(a.exec(0, null, fakePlayer, _true, _true, _false));
        assertCTrue(a.exec(0, null, fakePlayer, _true, _true));
        assertCFalse(a.exec(0, null, fakePlayer, _true, _false));
        assertCFalse(a.exec(0, null,fakePlayer, _false, _false));
        assertCTrue(a.exec(0, null, fakePlayer, _true));
        assertCFalse(a.exec(0, null, fakePlayer, _false));
    }
    
    @Test public void testOr() throws CancelCommandException{
        BasicLogic.or a = new BasicLogic.or();
        assertCTrue(a.exec(0, null, fakePlayer, _true, _true, _true));
        assertCTrue(a.exec(0, null, fakePlayer, _true, _true, _false));
        assertCTrue(a.exec(0, null, fakePlayer, _true, _true));
        assertCTrue(a.exec(0, null, fakePlayer, _true, _false));
        assertCFalse(a.exec(0, null, fakePlayer, _false, _false));
        assertCTrue(a.exec(0, null, fakePlayer, _true));
        assertCFalse(a.exec(0, null, fakePlayer, _false));
    }
    
    @Test public void testNot() throws CancelCommandException{
        BasicLogic.not a = new BasicLogic.not();
        assertCFalse(a.exec(0, null, fakePlayer, _true));
        assertCTrue(a.exec(0, null, fakePlayer, _false));
    }
    
    @Test public void testGt() throws CancelCommandException{
        BasicLogic.gt a = new BasicLogic.gt();
        assertCFalse(a.exec(0, null, fakePlayer, arg1_1, arg1_2));
        assertCTrue(a.exec(0, null, fakePlayer, arg2_1, arg1_1));
        assertCFalse(a.exec(0, null, fakePlayer, arg1_1, arg2_1));
        assertCFalse(a.exec(0, null, fakePlayer, argn1_1, arg1_1));
        assertCTrue(a.exec(0, null, fakePlayer, arg1_1, argn1_1));
    }
    
    @Test public void testGte() throws CancelCommandException{
        BasicLogic.gte a = new BasicLogic.gte();
        assertCTrue(a.exec(0, null, fakePlayer, arg1_1, arg1_2));
        assertCTrue(a.exec(0, null, fakePlayer, arg2_1, arg1_1));
        assertCFalse(a.exec(0, null, fakePlayer, arg1_1, arg2_1));
        assertCFalse(a.exec(0, null, fakePlayer, argn1_1, arg1_1));
        assertCTrue(a.exec(0, null, fakePlayer, arg1_1, argn1_1));
    }
    
    @Test public void testLt() throws CancelCommandException{
        BasicLogic.lt a = new BasicLogic.lt();
        assertCFalse(a.exec(0, null, fakePlayer, arg1_1, arg1_2));
        assertCFalse(a.exec(0, null, fakePlayer, arg2_1, arg1_1));
        assertCTrue(a.exec(0, null, fakePlayer, arg1_1, arg2_1));
        assertCTrue(a.exec(0, null, fakePlayer, argn1_1, arg1_1));
        assertCFalse(a.exec(0, null, fakePlayer, arg1_1, argn1_1));
    }
    
    @Test public void testLte() throws CancelCommandException{
        BasicLogic.lte a = new BasicLogic.lte();
        assertCTrue(a.exec(0, null, fakePlayer, arg1_1, arg1_2));
        assertCFalse(a.exec(0, null, fakePlayer, arg2_1, arg1_1));
        assertCTrue(a.exec(0, null, fakePlayer, arg1_1, arg2_1));
        assertCTrue(a.exec(0, null, fakePlayer, argn1_1, arg1_1));
        assertCFalse(a.exec(0, null, fakePlayer, arg1_1, argn1_1));
    }
    
    @Test public void testIf() throws ConfigCompileException{
        BasicLogic._if a = new BasicLogic._if();
        SRun("if(true, msg('correct'), msg('incorrect'))", fakePlayer);
        SRun("if(false, msg('incorrect'), msg('correct'))", fakePlayer);
        verify(fakePlayer, times(2)).sendMessage("correct");
    }
}
