package com.laytonsmith.core;

import com.laytonsmith.abstraction.MCPlayer;
import com.laytonsmith.core.compiler.OptimizationUtilities;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.exceptions.CRE.CRECastException;
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
		verify(fakePlayer).sendMessage("{classType: IOException, message: message, stackTrace: {{file: Unknown file, id: <<main code>>, line: 2}}}");
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
				/* 08 */ + "throw(IOException, '');\n"
				/* 09 */ + "}\n"
				/* 10 */ + "try {\n"
				/* 11 */ + "_a();\n"
				/* 12 */ + "} catch(IOException @e){\n"
				/* 13 */ + "msg(@e);\n"
				/* 14 */ + "}", fakePlayer);
		verify(fakePlayer).sendMessage("{classType: IOException, message: message, stackTrace:"
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

}
