

package com.laytonsmith.core;

import com.laytonsmith.core.constructs.CBoolean;
import com.laytonsmith.core.constructs.CClassType;
import com.laytonsmith.core.constructs.CDouble;
import com.laytonsmith.core.constructs.CInt;
import com.laytonsmith.core.constructs.CNull;
import com.laytonsmith.core.constructs.CString;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.testing.C;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 *
 */
public class TestStatic {
    Target t = Target.UNKNOWN;
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
        assertEquals(1.0, Static.getNumber(C.String("1.0"), t), 0.0);
        assertEquals(1.0, Static.getNumber(C.String("1"), t), 0.0);
        assertEquals(1.0, Static.getNumber(C.Int(1), t), 0.0);
        assertEquals(1.0, Static.getNumber(C.Double(1.0), t), 0.0);
    }

    @Test
    public void testGetDouble() {
        assertEquals(1.0, Static.getDouble(C.String("1.0"), t), 0.0);
        assertEquals(1.0, Static.getDouble(C.String("1"), t), 0.0);
        assertEquals(1.0, Static.getDouble(C.Int(1), t), 0.0);
        assertEquals(1.0, Static.getDouble(C.Double(1.0), t), 0.0);
    }

    @Test
    public void testGetInt() {
        assertEquals(1, Static.getInt(C.Int(1), t));
        assertEquals(1, Static.getInt(C.String("1"), t));
        try{
            Static.getInt(C.Double(1.0), t);
            fail("Should not have been able to parse 1.0 as an int");
        } catch(ConfigRuntimeException e){ /* Test Passed */ }
    }

    @Test
    public void testGetBoolean() {
        assertEquals(true, Static.getBoolean(C.Boolean(true)));
        assertEquals(true, Static.getBoolean(C.String("non-empty string")));
        assertEquals(false, Static.getBoolean(C.String("")));
        assertEquals(true, Static.getBoolean(C.Int(1)));
        assertEquals(false, Static.getBoolean(C.Int(0)));
    }

    @Test
    public void testAnyDoubles() {
        assertTrue(Static.anyDoubles(C.Int(0), C.Int(1), C.Double(1)));
        assertFalse(Static.anyDoubles(C.Int(1)));
    }

    @Test
    public void testAnyStrings() {
        assertTrue(Static.anyStrings(C.Int(0), C.Int(1), C.String("")));
        assertFalse(Static.anyStrings(C.Int(1)));
    }

    @Test
    public void testAnyBooleans() {
        assertTrue(Static.anyBooleans(C.Int(0), C.Int(1), C.Boolean(true)));
        assertFalse(Static.anyBooleans(C.Int(1)));
    }

    @Test
    public void testGetLogger() {
        assertNotNull(Static.getLogger());
    }

    @Test
    public void testResolveConstruct() {
        assertTrue(Static.resolveConstruct("1", Target.UNKNOWN) instanceof CInt);
        assertTrue(Static.resolveConstruct("true", Target.UNKNOWN) instanceof CBoolean);
        assertTrue(Static.resolveConstruct("false", Target.UNKNOWN) instanceof CBoolean);
        assertTrue(Static.resolveConstruct("null", Target.UNKNOWN) instanceof CNull);
        assertTrue(Static.resolveConstruct("1.1", Target.UNKNOWN) instanceof CDouble);
        assertTrue(Static.resolveConstruct("astring", Target.UNKNOWN) instanceof CString);
        assertTrue(Static.resolveConstruct("string", Target.UNKNOWN) instanceof CClassType);
    }

}
