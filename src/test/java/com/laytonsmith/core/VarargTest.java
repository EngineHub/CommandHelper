package com.laytonsmith.core;

import com.laytonsmith.abstraction.MCPlayer;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.exceptions.CRE.CRECastException;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import static com.laytonsmith.testing.StaticTest.*;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.Mockito.*;

/**
 *
 */
public class VarargTest {

	Environment env;
	MCPlayer fakePlayer;

	@Before
	public void setup() throws Exception {
		InstallFakeServerFrontend();
		env = Static.GenerateStandaloneEnvironment();
		fakePlayer = GetOnlinePlayer();
	}

	@Test
	public void testVarArgsInProcs() throws Exception {
		Run("void proc _test(string... @values) { msg(@values); }\n"
				+ "_test('a', 'b', 'c');", fakePlayer);
		verify(fakePlayer).sendMessage("{a, b, c}");
	}

	@Test
	public void testVarArgsInProcsWithSpace() throws Exception {
		Run("void proc _test(string ... @values) { msg(@values); }\n"
				+ "_test('a', 'b', 'c');", fakePlayer);
		verify(fakePlayer).sendMessage("{a, b, c}");
	}

	@Test
	public void testVarArgsInClosure() throws Exception {
		Run("@closure = closure(string... @values) { msg(@values); };\n"
				+ "@closure('a', 'b', 'c');", fakePlayer);
		verify(fakePlayer).sendMessage("{a, b, c}");
	}

	@Test
	public void testVarArgsInCIClosure() throws Exception {
		Run("@closure = iclosure(string... @values) { msg(@values); };\n"
				+ "@closure('a', 'b', 'c');", fakePlayer);
		verify(fakePlayer).sendMessage("{a, b, c}");

	}

	@Test
	public void testSingleVarArgsInProcs() throws Exception {
		Run("void proc _test(string... @values) { msg(@values); }\n"
				+ "_test('a');", fakePlayer);
		verify(fakePlayer).sendMessage("{a}");
	}

	@Test
	public void testSingleVarArgsInClosure() throws Exception {
		Run("@closure = closure(string... @values) { msg(@values); };\n"
				+ "@closure('a');", fakePlayer);
		verify(fakePlayer).sendMessage("{a}");
	}

	@Test
	public void testSingleVarArgsInCIClosure() throws Exception {
		Run("@closure = iclosure(string... @values) { msg(@values); };\n"
				+ "@closure('a');", fakePlayer);
		verify(fakePlayer).sendMessage("{a}");
	}

	@Test(expected = CRECastException.class)
	public void testVarArgsInRegularAssign() throws Exception {
		Run("string... @a = 'test';", fakePlayer);
	}

	// This can eventually be allowed in some circumstances, and in fact must be to fully support native typechecking,
	// but as a first run, this is fine.
	@Test(expected = ConfigCompileException.class)
	public void testVarArgsIsFinalArgProc() throws Exception {
		Run("void proc _test(string... @val, int @i) {}", fakePlayer);
	}

	@Test(expected = ConfigCompileException.class)
	public void testVarArgsIsFinalArgClosure() throws Exception {
		Run("void closure(string... @val, int @i) {}", fakePlayer);
	}

	@Test(expected = ConfigCompileException.class)
	public void testVarArgsIsFinalArgIClosure() throws Exception {
		Run("void iclosure(string... @val, int @i) {}", fakePlayer);
	}

	@Test(expected = ConfigCompileException.class)
	public void testVarArgsWithDefaultErrors() throws Exception {
		Run("void iclosure(string... @val = 'test') {}", fakePlayer);
	}

	@Test
	public void testVarArgWithProcReference() throws Exception {
		Run("proc _test(string... @values) { msg(@values); } @callable = proc _test; @callable('a', 'b', 'c');", fakePlayer);
		verify(fakePlayer).sendMessage("{a, b, c}");
	}
}
