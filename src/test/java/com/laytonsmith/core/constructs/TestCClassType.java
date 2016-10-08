package com.laytonsmith.core.constructs;


import com.laytonsmith.PureUtilities.ClassLoading.ClassDiscovery;
import com.laytonsmith.core.TestStatic;
import com.laytonsmith.testing.StaticTest;
import java.net.URL;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import com.laytonsmith.core.natives.interfaces.Sizeable;

/**
 *
 * @author cailin
 */
public class TestCClassType {

    @Before
    public void load() {
	StaticTest.InstallFakeServerFrontend();
    }

    private static CClassType get(String ... types) {
	return new CClassType(Target.UNKNOWN, types);
    }

    @Test
    public void testInitial() throws Exception {
	NativeTypeList.getNativeClass("array");
	NativeTypeList.getNativeClass("mixed");
    }

    @Test
    public void testEquals() throws Exception {
	assertTrue(get("mixed").equals(get("mixed")));
	assertFalse(get("mixed").equals(get("array")));
    }

    @Test
    public void testEqualsWithTypeUnion() throws Exception {
	assertTrue(get("array", "int").equals(get("int", "array")));
	assertFalse(get("array", "int").equals(get("string", "array")));
    }

    @Test
    public void testDoesExtend() throws Exception {
	assertTrue(get("array").doesExtend(get("mixed")));
	assertTrue(get("array").doesExtend(get("ArrayAccess")));
	assertFalse(get("array").doesExtend(get("string")));
	assertTrue(get("array").doesExtend(get("array")));
    }

    @Test
    public void testGetMostCommonSuperClass() throws Exception {
	assertTrue(get("double", "int").getMostCommonSuperClass().equals(get("number")));
	assertTrue(get("double").getMostCommonSuperClass().equals(get("double")));
    }

    @Test
    public void testInterface() throws Exception {
	assertTrue(get("Sizeable").getUnderlyingClass() == Sizeable.class);
    }
}
