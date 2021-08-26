package com.laytonsmith.core.asm;

import com.laytonsmith.PureUtilities.Common.OSUtils;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import static org.junit.Assert.fail;
import static com.laytonsmith.core.asm.AsmIntegrationTestUtils.skipTest;
import static com.laytonsmith.core.asm.AsmIntegrationTestUtils.integrationTest;
import static com.laytonsmith.core.asm.AsmIntegrationTestUtils.integrationTestAndReturn;
import static com.laytonsmith.core.asm.AsmIntegrationTestUtils.installToolchain;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class AsmIntegrationTests {

	@Test
	public void aaaTestToolchainInstallation() throws Exception {
		if(!skipTest()) {
			installToolchain();
		}
	}

	@Test
	public void testHelloWorld() throws Exception {
		integrationTest("Hello, World!",
				"sys_out('Hello, World!');");
	}

	@Test
	public void testRand() throws Exception {
		if(skipTest()) {
			return;
		}
		String output = integrationTestAndReturn("sys_out(rand());");
		double rand = Double.parseDouble(output);
		if(rand < 0 || rand > 1) {
			fail();
		}

		output = integrationTestAndReturn("sys_out(rand(1, 10));");
		int randI = Integer.parseInt(output);
		if(randI < 1 || randI > 10) {
			fail();
		}
	}

	@Test
	public void testAssign() throws Exception {
		integrationTest("12345", "<! strict > int @i = 12345; sys_out(@i);");
		integrationTest("Hello" + OSUtils.GetLineEnding() + "Hello" + OSUtils.GetLineEnding() + "World", "<! strict >\n"
				+ "string @h = 'Hello';\n"
				+ "string @w = 'World';\n"
				+ "sys_out(@h);\n"
				+ "sys_out(@h);\n"
				+ "sys_out(@w);\n");
	}
}
