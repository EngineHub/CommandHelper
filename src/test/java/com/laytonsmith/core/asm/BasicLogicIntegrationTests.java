package com.laytonsmith.core.asm;

import com.laytonsmith.testing.AbstractIntegrationTest;

import java.util.List;
import java.util.Map;

import org.junit.Test;

import static com.laytonsmith.core.asm.AsmIntegrationTestUtils.integrationTest;

public class BasicLogicIntegrationTests extends AbstractIntegrationTest {

	@Test
	public void testBoolToString() throws Exception {
		List<Map.Entry<String, String>> cases = List.of(
			Map.entry("true", "<! strict > boolean @a = true; sys_out(@a);"),
			Map.entry("false", "<! strict > boolean @a = false; sys_out(@a);")
		);
		for(Map.Entry<String, String> entry : cases) {
			integrationTest(entry.getKey(), entry.getValue());
		}
	}

	@Test
	public void testEqualsConcreteTypes() throws Exception {
		List<Map.Entry<String, String>> cases = List.of(
			// int == int
			Map.entry("true", "<! strict > sys_out(dyn(1) == dyn(1));"),
			Map.entry("false", "<! strict > sys_out(dyn(1) == dyn(2));"),
			// double == double
			Map.entry("true", "<! strict > sys_out(dyn(1.5) == dyn(1.5));"),
			Map.entry("false", "<! strict > sys_out(dyn(1.5) == dyn(2.5));"),
			// int == double (numeric coercion)
			Map.entry("true", "<! strict > sys_out(dyn(1) == dyn(1.0));"),
			Map.entry("false", "<! strict > sys_out(dyn(1) == dyn(1.5));"),
			// string == string
			Map.entry("true", "<! strict > sys_out(dyn('hello') == dyn('hello'));"),
			Map.entry("false", "<! strict > sys_out(dyn('hello') == dyn('world'));"),
			// boolean == boolean
			Map.entry("true", "<! strict > sys_out(dyn(true) == dyn(true));"),
			Map.entry("false", "<! strict > sys_out(dyn(true) == dyn(false));")
		);
		for(Map.Entry<String, String> entry : cases) {
			integrationTest(entry.getKey(), entry.getValue());
		}
	}

	@Test
	public void testEqualsVariadic() throws Exception {
		List<Map.Entry<String, String>> cases = List.of(
			Map.entry("true", "<! strict > sys_out(equals(dyn(1), dyn(1), dyn(1)));"),
			Map.entry("false", "<! strict > sys_out(equals(dyn(1), dyn(1), dyn(2)));")
		);
		for(Map.Entry<String, String> entry : cases) {
			integrationTest(entry.getKey(), entry.getValue());
		}
	}

	@Test
	public void testEqualsAutoTypes() throws Exception {
		List<Map.Entry<String, String>> cases = List.of(
			// auto int == auto int
			Map.entry("true", "<! strict > auto @a = dyn(5); auto @b = dyn(5); sys_out(@a == @b);"),
			Map.entry("false", "<! strict > auto @a = dyn(5); auto @b = dyn(6); sys_out(@a == @b);"),
			// auto double == auto double
			Map.entry("true", "<! strict > auto @a = dyn(3.14); auto @b = dyn(3.14); sys_out(@a == @b);"),
			Map.entry("false", "<! strict > auto @a = dyn(3.14); auto @b = dyn(2.71); sys_out(@a == @b);"),
			// auto vs concrete
			Map.entry("true", "<! strict > auto @a = dyn(42); int @b = dyn(42); sys_out(@a == @b);"),
			Map.entry("false", "<! strict > auto @a = dyn(42); int @b = dyn(43); sys_out(@a == @b);")
		);
		for(Map.Entry<String, String> entry : cases) {
			integrationTest(entry.getKey(), entry.getValue());
		}
	}

	@Test
	public void testEqualsBoolStringCoercion() throws Exception {
		List<Map.Entry<String, String>> cases = List.of(
			// Concrete types: bool + string, boolean priority
			Map.entry("true", "<! strict > sys_out(dyn(true) == dyn('hello'));"),
			Map.entry("false", "<! strict > sys_out(dyn(false) == dyn('hello'));"),
			Map.entry("true", "<! strict > sys_out(dyn(false) == dyn(''));"),
			Map.entry("false", "<! strict > sys_out(dyn(true) == dyn(''));"),
			// Auto types: bool + string, ms_value dispatch
			Map.entry("true", "<! strict > auto @a = dyn(true); auto @b = dyn('hello'); sys_out(@a == @b);"),
			Map.entry("false", "<! strict > auto @a = dyn(false); auto @b = dyn('hello'); sys_out(@a == @b);"),
			Map.entry("true", "<! strict > auto @a = dyn(false); auto @b = dyn(''); sys_out(@a == @b);"),
			Map.entry("false", "<! strict > auto @a = dyn(true); auto @b = dyn(''); sys_out(@a == @b);")
		);
		for(Map.Entry<String, String> entry : cases) {
			integrationTest(entry.getKey(), entry.getValue());
		}
	}
}
