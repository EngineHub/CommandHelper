package com.laytonsmith.core;

import com.laytonsmith.abstraction.MCPlayer;
import com.laytonsmith.core.compiler.OptimizationUtilities;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.exceptions.CRE.CRECastException;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import com.laytonsmith.core.functions.Exceptions;
import com.laytonsmith.testing.StaticTest;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import org.junit.Before;
import static com.laytonsmith.testing.StaticTest.SRun;
import static org.mockito.Mockito.*;

/**
 * 
 */
public class NewExceptionHandlingTest {

	@BeforeClass
	public static void setUpClass(){
		StaticTest.InstallFakeServerFrontend();
	}

    public String optimize(String script) throws Exception {
        return OptimizationUtilities.optimize(script, null);
    }

	MCPlayer fakePlayer;

    @Before
    public void setUp() {
        fakePlayer = StaticTest.GetOnlinePlayer();
    }

	@Test public void testBasicKeywordUsage() throws Exception {
		assertEquals("complex_try(null,assign(IOException,@e,null),null,assign(Exception,@e,null),null,null)", optimize("try { } catch(IOException @e){ } catch(Exception @e){ } finally { }"));
	}

	@Test public void testTryFinallyKeywordUsage() throws Exception {
		assertEquals("complex_try(msg('a'),msg('b'))", optimize("try { msg(\"a\"); } finally { msg(\"b\"); }"));
	}

	@Test public void testBasicUsage() throws Exception {
		SRun("try { throw(IOException, ''); } catch(IOException @e) { msg('exception'); }", fakePlayer);
		verify(fakePlayer).sendMessage("exception");
	}

	@Test public void testExceptionTrickle() throws Exception {
		SRun("try { throw(IOException, ''); } catch(NullPointerException @e) { msg('no run'); } catch(IOException @e){ msg('run'); }", fakePlayer);
		verify(fakePlayer).sendMessage("run");
	}

	@Test public void testExceptionInheritance() throws Exception {
		SRun("try { throw(IOException, ''); } catch(Exception @e) { msg('run'); }", fakePlayer);
		verify(fakePlayer).sendMessage("run");
	}

	@Test public void testExceptionObjectCorrect() throws Exception {
		SRun(
				/* 1 */ "try {\n"
				/* 2 */ + "throw(IOException, 'message'); \n"
				/* 3 */ + "} catch(IOException @e){ \n"
				/* 4 */ + "msg(@e); \n"
				/* 5 */ + "}", fakePlayer);
		verify(fakePlayer).sendMessage("{causedBy: null, classType: IOException, message: message, stackTrace: {{file: Unknown file, id: <<main code>>, line: 2}}}");
	}

	@Test public void testExceptionObjectCorrect2() throws Exception {
		// Test the line numbers for a complex exception
		SRun(
				/* 01 */ "proc _a(){\n"
				/* 02 */ + "_b();\n"
				/* 03 */ + "}\n"
				/* 04 */ + "proc _b(){\n"
				/* 05 */ + "_c();\n"
				/* 06 */ + "}\n"
				/* 07 */ + "proc _c(){\n"
				/* 08 */ + "throw(IOException, 'message');\n"
				/* 09 */ + "}\n"
				/* 10 */ + "try {\n"
				/* 11 */ + "_a();\n"
				/* 12 */ + "} catch(IOException @e){\n"
				/* 13 */ + "msg(@e);\n"
				/* 14 */ + "}", fakePlayer);
		verify(fakePlayer).sendMessage("{causedBy: null, classType: IOException, message: message, stackTrace:"
				+ " {"
				+ "{file: Unknown file, id: proc _c, line: 8}, "
				+ "{file: Unknown file, id: proc _b, line: 5}, "
				+ "{file: Unknown file, id: proc _a, line: 2}, "
				+ "{file: Unknown file, id: <<main code>>, line: 11}}"
				+ "}");
	}
	
	@Test public void testExceptionObjectCorrect3() throws Exception {
		// Test the line numbers for a complex exception, which is generated internally.
		SRun(
				/* 01 */ "proc _a(){\n"
				/* 02 */ + "_b();\n"
				/* 03 */ + "}\n"
				/* 04 */ + "proc _b(){\n"
				/* 05 */ + "_c();\n"
				/* 06 */ + "}\n"
				/* 07 */ + "proc _c(){\n"
				/* 08 */ + "@a = (1 / dyn(0));\n"
				/* 09 */ + "}\n"
				/* 10 */ + "try {\n"
				/* 11 */ + "_a();\n"
				/* 12 */ + "} catch(RangeException @e){\n"
				/* 13 */ + "msg(@e);\n"
				/* 14 */ + "}", fakePlayer);
		verify(fakePlayer).sendMessage("{causedBy: null, classType: RangeException, message: Division by 0!, stackTrace:"
				+ " {"
				+ "{file: Unknown file, id: proc _c, line: 8}, "
				+ "{file: Unknown file, id: proc _b, line: 5}, "
				+ "{file: Unknown file, id: proc _a, line: 2}, "
				+ "{file: Unknown file, id: <<main code>>, line: 11}}"
				+ "}");
	}

	@Test public void testFinallyRunsOnNormal() throws Exception {
		SRun("try { noop(); } catch(Exception @e) { msg('nope'); } finally { msg('run'); }", fakePlayer);
		verify(fakePlayer).sendMessage("run");
	}

	@Test public void testFinallyRunsOnException() throws Exception {
		SRun("try { throw(IOException, ''); } catch(Exception @e) { msg('exception'); } finally { msg('run'); }", fakePlayer);
		verify(fakePlayer).sendMessage("exception");
		verify(fakePlayer).sendMessage("run");
	}

	@Test public void testFinallyRunsAndReturnIsCorrect() throws Exception {
		SRun("proc _a(){ try { noop(); return('value'); } catch(Exception @e) { msg('nope'); } finally { msg('run'); } }"
				+ "msg(_a());", fakePlayer);
		verify(fakePlayer).sendMessage("run");
		verify(fakePlayer).sendMessage("value");
	}

	@Test public void testHiddenThrowSetsOffLog() throws Exception {
		CHLog log = StaticTest.InstallFakeLogger();
		StaticTest.SetPrivate(Exceptions.complex_try.class, "doScreamError", true, boolean.class);
		try {
			SRun("try { throw(IOException, 'hidden'); } finally { throw(CastException, 'shown'); }", fakePlayer);
			fail("Expected an exception");
		} catch(CRECastException ex){
		} catch(Exception ex){
			fail("Expected an exception, but of type CRECastException");
		}
		verify(log).Log(eq(CHLog.Tags.RUNTIME), eq(LogLevel.WARNING), anyString(), any(Target.class));
	}

	@Test(expected = ConfigCompileException.class)
	public void testDuplicateExceptionTypeThrowsException() throws Exception {
		SRun("try { } catch(CastException @e){ } catch(CastException @e){ }", fakePlayer);
	}

	@Test public void testExceptionTypeIsCorrectInMulticatch() throws Exception {
		SRun("try { throw(CastException, ''); } catch(CastException @e){ msg('run'); } catch(IOException @e){ msg('no run'); }", fakePlayer);
		SRun("try { throw(IOException, ''); } catch(CastException @e){ msg('no run'); } catch(IOException @e){ msg('run'); }", fakePlayer);
		verify(fakePlayer, times(2)).sendMessage("run");
	}
	
	@Test public void testNestedTryWorks() throws Exception {
		SRun("try {"
				+ "try {"
				+ "	throw(IOException, '');"
				+ "} catch(CastException @e){"
				+ "	msg('nope');"
				+ "}"
				+ "} catch(IOException @ex){"
				+ "	msg('run');"
				+ "}", fakePlayer);
		verify(fakePlayer).sendMessage("run");
	}
	
	@Test(expected = ConfigCompileException.class)
	public void testFinallyMustBeLast() throws Exception {
		SRun("try { } finally { } catch(Exception @e){ }", fakePlayer);
	}
	
	@Test(expected = ConfigCompileException.class)
	public void testFinallyErrors() throws Exception {
		SRun("finally { }", fakePlayer);
	}
	
	@Test(expected = ConfigCompileException.class)
	public void testCatchErrors() throws Exception {
		SRun("catch(Exception @e) { }", fakePlayer);
	}
	
	@Test(expected = ConfigCompileException.class)
	public void testCatchErrors2() throws Exception {
		SRun("catch { }", fakePlayer);
	}
	
	@Test(expected = ConfigCompileException.class)
	public void testCatchOnlyAllows1Parameter1() throws Exception {
		SRun("try { } catch(Exception @e, IOException @b) { }", fakePlayer);
	}
	
	@Test(expected = ConfigCompileException.class)
	public void testCatchOnlyAllows1Parameter2() throws Exception {
		SRun("catch(){ }", fakePlayer);
	}
	
	@Test(expected = ConfigCompileException.class)
	public void testTryAloneFails() throws Exception {
		SRun("try{ }", fakePlayer);
	}
	
	@Test
	public void testCausedBy() throws Exception {
		SRun(
			/* 01 */ "proc _a(){\n"
			/* 02 */ + "		throw(IOException, 'original');\n"
			/* 03 */ + "}\n"
			/* 04 */ + "proc _b(){\n"
			/* 05 */ + "		_a();\n"
			/* 06 */ + "}\n"
			/* 07 */ + "try {\n"
			/* 08 */ + "		try {\n"
			/* 09 */ + "			_b();\n"
			/* 10 */ + "		} catch(IOException @e){\n"
			/* 11 */ + "			throw(CastException, 'new', @e);\n"
			/* 12 */ + "		}\n"
			/* 13 */ + "} catch(CastException @e){\n"
			/* 14 */ + "		msg(@e);\n"
			/* 15 */ + "}\n"
			, fakePlayer);
		verify(fakePlayer).sendMessage(
				"{"
					+ "causedBy: {"
						+ "causedBy: null, "
						+ "classType: IOException, "
						+ "message: original, "
						+ "stackTrace: {"
							+ "{"
								+ "file: Unknown file, "
								+ "id: proc _a, "
								+ "line: 2"
							+ "}, {"
								+ "file: Unknown file, "
								+ "id: proc _b, "
								+ "line: 5"
							+ "}, {"
								+ "file: Unknown file, "
								+ "id: <<main code>>, "
								+ "line: 9"
							+ "}"
						+ "}"
					+ "}, "
					+ "classType: CastException, "
					+ "message: new, "
					+ "stackTrace: {"
						+ "{"
							+ "file: Unknown file, "
							+ "id: <<main code>>, "
							+ "line: 11"
						+ "}"
					+ "}"
				+ "}");
	}

}
