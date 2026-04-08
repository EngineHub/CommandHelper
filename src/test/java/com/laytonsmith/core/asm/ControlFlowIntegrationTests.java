package com.laytonsmith.core.asm;

import com.laytonsmith.PureUtilities.Common.OSUtils;
import com.laytonsmith.testing.AbstractIntegrationTest;

import java.util.List;
import java.util.Map;

import org.junit.Test;

import static com.laytonsmith.core.asm.AsmIntegrationTestUtils.integrationTest;

public class ControlFlowIntegrationTests extends AbstractIntegrationTest {

	@Test
	public void testIfTrue() throws Exception {
		integrationTest("yes",
				"<! strict > if(dyn(true)) { sys_out('yes'); }");
	}

	@Test
	public void testIfFalse() throws Exception {
		integrationTest("done",
				"<! strict > if(dyn(false)) { sys_out('no'); } sys_out('done');");
	}

	@Test
	public void testIfElse() throws Exception {
		integrationTest("false branch",
				"<! strict > if(dyn(false)) { sys_out('true branch'); } else { sys_out('false branch'); }");
	}

	@Test
	public void testIfElseTrue() throws Exception {
		integrationTest("true branch",
				"<! strict > if(dyn(true)) { sys_out('true branch'); } else { sys_out('false branch'); }");
	}

	@Test
	public void testIfWithIntCondition() throws Exception {
		integrationTest("nonzero",
				"<! strict > int @x = dyn(42); if(@x) { sys_out('nonzero'); } else { sys_out('zero'); }");
	}

	@Test
	public void testIfWithZeroCondition() throws Exception {
		integrationTest("zero",
				"<! strict > int @x = dyn(0); if(@x) { sys_out('nonzero'); } else { sys_out('zero'); }");
	}

	@Test
	public void testIfElseChain() throws Exception {
		integrationTest("second",
				"<! strict > if(dyn(false)) { sys_out('first'); }"
				+ " else if(dyn(true)) { sys_out('second'); }"
				+ " else { sys_out('third'); }");
	}

	@Test
	public void testIfElseChainFallthrough() throws Exception {
		integrationTest("third",
				"<! strict > if(dyn(false)) { sys_out('first'); }"
				+ " else if(dyn(false)) { sys_out('second'); }"
				+ " else { sys_out('third'); }");
	}

	@Test
	public void testIfWithAssign() throws Exception {
		String expected = "inside" + OSUtils.GetLineEnding() + "after";
		integrationTest(expected,
				"<! strict > int @x = dyn(1);"
				+ " if(@x) { sys_out('inside'); }"
				+ " sys_out('after');");
	}

	@Test
	public void testNestedIf() throws Exception {
		integrationTest("inner",
				"<! strict > if(dyn(true)) { if(dyn(true)) { sys_out('inner'); } else { sys_out('nope'); } }");
	}
}
