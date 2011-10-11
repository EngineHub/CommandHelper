/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.aliasengine;

import com.laytonsmith.aliasengine.functions.exceptions.ConfigRuntimeException;
import com.laytonsmith.aliasengine.Constructs.*;
import com.laytonsmith.testing.C;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Layton
 */
public class TestStatic {
    
    public TestStatic() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }
    
    @Before
    public void setUp() {
    }

    @Test
    public void testGetNumber() {
        System.out.println("getNumber");
        assertEquals(1.0, Static.getNumber(C.String("1.0")), 0.0);
        assertEquals(1.0, Static.getNumber(C.String("1")), 0.0);
        assertEquals(1.0, Static.getNumber(C.Int(1)), 0.0);
        assertEquals(1.0, Static.getNumber(C.Double(1.0)), 0.0);
    }

    @Test
    public void testGetDouble() {
        System.out.println("getDouble");
        assertEquals(1.0, Static.getDouble(C.String("1.0")), 0.0);
        assertEquals(1.0, Static.getDouble(C.String("1")), 0.0);
        assertEquals(1.0, Static.getDouble(C.Int(1)), 0.0);
        assertEquals(1.0, Static.getDouble(C.Double(1.0)), 0.0);
    }

    @Test
    public void testGetInt() {
        System.out.println("getInt");
        assertEquals(1, Static.getInt(C.Int(1)));
        assertEquals(1, Static.getInt(C.String("1")));
        try{
            Static.getInt(C.Double(1.0));
            fail("Should not have been able to parse 1.0 as an int");
        } catch(ConfigRuntimeException e){ /* Test Passed */ }
    }

    @Test
    public void testGetBoolean() {
        System.out.println("getBoolean");
        assertEquals(true, Static.getBoolean(C.Boolean(true)));
        assertEquals(true, Static.getBoolean(C.String("non-empty string")));
        assertEquals(false, Static.getBoolean(C.String("")));
        assertEquals(true, Static.getBoolean(C.Int(1)));
        assertEquals(false, Static.getBoolean(C.Int(0)));
    }

    @Test
    public void testAnyDoubles() {
        System.out.println("anyDoubles");
        assertTrue(Static.anyDoubles(C.Int(0), C.Int(1), C.Double(1)));
        assertFalse(Static.anyDoubles(C.Int(1)));
    }

    @Test
    public void testAnyStrings() {
        System.out.println("anyStrings");
        assertTrue(Static.anyStrings(C.Int(0), C.Int(1), C.String("")));
        assertFalse(Static.anyStrings(C.Int(1)));
    }

    @Test
    public void testAnyBooleans() {
        System.out.println("anyBooleans");
        assertTrue(Static.anyBooleans(C.Int(0), C.Int(1), C.Boolean(true)));
        assertFalse(Static.anyBooleans(C.Int(1)));
    }

    @Test
    public void testGetLogger() {
        System.out.println("getLogger");
        assertNotNull(Static.getLogger());
    }

////    @Test(expected=NotInitializedYetException.class)
////    public void testGetServer() {
////        System.out.println("getServer");
////        Static.getServer();
////    }

    @Test(expected=NotInitializedYetException.class)
    public void testGetAliasCore() {
        System.out.println("getAliasCore");
        Static.getAliasCore();
    }

//    @Test(expected=NotInitializedYetException.class)
//    public void testGetPersistance() {
//        System.out.println("getPersistance");
//        Static.getPersistance();
//    }

//    @Test(expected=NotInitializedYetException.class)
//    public void testGetPermissionsResolverManager() {
//        System.out.println("getPermissionsResolverManager");
//        Static.getPermissionsResolverManager();
//    }

    @Test(expected=NotInitializedYetException.class)
    public void testGetVersion() {
        System.out.println("getVersion");
        Static.getVersion();
    }

    @Test
    public void testGetPreferences() {
        System.out.println("getPreferences");
        Static.getPreferences();
    }

    @Test
    public void testResolveConstruct() {
        System.out.println("resolveConstruct");
        assertTrue(Static.resolveConstruct("1", 0, null) instanceof CInt);
        assertTrue(Static.resolveConstruct("true", 0, null) instanceof CBoolean);
        assertTrue(Static.resolveConstruct("false", 0, null) instanceof CBoolean);
        assertTrue(Static.resolveConstruct("null", 0, null) instanceof CNull);
        assertTrue(Static.resolveConstruct("1.1", 0, null) instanceof CDouble);
        assertTrue(Static.resolveConstruct("string", 0, null) instanceof CString);
    }
}
