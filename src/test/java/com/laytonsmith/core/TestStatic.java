

package com.laytonsmith.core;

import com.laytonsmith.core.constructs.*;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.testing.C;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author Layton
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

    @Test(expected=NotInitializedYetException.class)
    public void testGetVersion() {
        Static.getVersion();
    }

    @Test
    public void testResolveConstruct() {
        assertTrue(Static.resolveConstruct("1", Target.UNKNOWN) instanceof CInt);
        assertTrue(Static.resolveConstruct("true", Target.UNKNOWN) instanceof CBoolean);
        assertTrue(Static.resolveConstruct("false", Target.UNKNOWN) instanceof CBoolean);
        assertTrue(Static.resolveConstruct("null", Target.UNKNOWN).isNull());
        assertTrue(Static.resolveConstruct("1.1", Target.UNKNOWN) instanceof CDouble);
        assertTrue(Static.resolveConstruct("string", Target.UNKNOWN) instanceof CString);
    }
        
}
