package com.laytonsmith.core;

import com.laytonsmith.PureUtilities.ReflectionUtils;
import com.laytonsmith.abstraction.Implementation;
import com.laytonsmith.annotations.api;
import com.laytonsmith.core.compiler.CompilerEnvironment;
import com.laytonsmith.core.compiler.CompilerWarning;
import com.laytonsmith.core.compiler.FileOptions;
import com.laytonsmith.core.compiler.OptimizationUtilities;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import com.laytonsmith.testing.StaticTest;
import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.junit.*;
import org.mockito.Mockito;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.eq;
import static org.junit.Assert.*;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import static org.powermock.api.mockito.PowerMockito.mock;

/**
 * This class ensures that all code scenarios that should trigger a compiler warning do.
 */
public class CompilerWarningTest {
	@BeforeClass
	public static void setUpClass(){
		StaticTest.InstallFakeServerFrontend();
	}
	
	CHLog fakeLogger;
	Map<CompilerWarning, AtomicInteger> times;
	@Before
	public void installFakeLog() throws Exception {
		times = new EnumMap<CompilerWarning, AtomicInteger>(CompilerWarning.class);
		fakeLogger = mock(CHLog.class);
		Mockito.doAnswer(new Answer() {
			public Object answer(InvocationOnMock invocation) throws Throwable {
				CompilerWarning w = (CompilerWarning)invocation.getArguments()[0];
				FileOptions fo = (FileOptions)invocation.getArguments()[3];
				if(!fo.isWarningSuppressed(w)){
					if(!times.containsKey(w)){
						times.put(w, new AtomicInteger(1));
					} else {
						times.get(w).incrementAndGet();
					}
					return invocation.callRealMethod();
				}
				return null;
			}
		}).when(fakeLogger).CompilerWarning(Mockito.any(CompilerWarning.class), 
			Mockito.anyString(), Mockito.any(Target.class), Mockito.any(FileOptions.class));
		ReflectionUtils.set(CHLog.class, "instance", fakeLogger);
	}
	
	public void compile(String code) {
		try{
			Environment env = Environment.createEnvironment(new CompilerEnvironment(Implementation.Type.TEST, api.Platforms.INTERPRETER_JAVA));
			OptimizationUtilities.optimize(code, env);
		} catch(Exception e){
			throw new RuntimeException(e);
		}
	}
	
	public void doVerify(final CompilerWarning warning){
		verify(fakeLogger, Mockito.atLeastOnce()).Log(eq(CHLog.Tags.COMPILER), eq(LogLevel.WARNING), Mockito.argThat(new BaseMatcher<String>() {

			public boolean matches(Object item) {
				return (times.containsKey(warning) && times.get(warning).get() > 0);
			}

			public void describeTo(Description description) {
				description.appendText("<message about " + warning.toString() + ">");
			}
		}), Mockito.any(Target.class));
	}
	//Unfortunately, testing the deprecated warning is not desirable, because by definition, it's a transient error message
	
	
	@Test
	public void testUnreachableCode(){
		compile("<! strict > die() msg('')");
		doVerify(CompilerWarning.UnreachableCode);
	}
	
	@Test public void testUnassignedVariableUsage(){
		compile("<! strict > msg(@a)");
		doVerify(CompilerWarning.UnassignedVariableUsage);
	}
	
	@Test public void testAssignmentToItself(){
		compile("<! strict > @a = 5 @a = @a");
		doVerify(CompilerWarning.AssignmentToItself);
	}
	
	@Test public void testVariableBreak(){
		compile("<! strict > @a = 2 foreach(1..2,@i,foreach(1..2,@j,break(@a)))");
		doVerify(CompilerWarning.VariableBreak);
	}
	
	@Test public void testAssigmentInIf1(){
		compile("<! strict > if(@a = 5){ msg('') }");
		doVerify(CompilerWarning.AssignmentInIf);
	}
	
	@Test public void testAssigmentInIf2(){
		compile("<! strict > if(dyn(1)){ msg('') } else if(@a = 4){ msg('') }");
		doVerify(CompilerWarning.AssignmentInIf);
	}
	
	@Test public void testUseOfEval(){
		compile("<! strict > eval('msg(\\'\\')')");
		doVerify(CompilerWarning.UseOfEval);
	}
	
	@Test public void testStrictModeOff(){
		compile("msg('')");
		doVerify(CompilerWarning.StrictModeOff);
	}
	
	@Test public void testAmbiguousUnaryOperators1(){
		//This is ambiguous
		compile("<! strict > @a = 1 @b = 2 @a ++ @b");
		doVerify(CompilerWarning.AmbiguousUnaryOperators);
	}
	
	@Test public void testAmbiguousUnaryOperators2() throws ConfigCompileException{
		//These are not
		compile("(@a++) @b");
		compile("<! strict > for(@i = 0, @i < 10, @i++, msg(''))");
		compile("<! strict > for(@i = 0, @i < 10, ++@i, msg(''))");
		compile("<! strict > postinc(@a) @b");
		verify(fakeLogger, Mockito.times(0)).CompilerWarning(eq(CompilerWarning.AmbiguousUnaryOperators), Mockito.anyString(), Mockito.any(Target.class), Mockito.any(FileOptions.class));
	}
	
	@Test public void testBareStrings(){
		compile("this is bare");
		doVerify(CompilerWarning.BareStrings);
	}
	
	@Test public void testUnquotedSymbols(){
		compile("this is bare&");
		doVerify(CompilerWarning.UnquotedSymbols);
	}
	
	@Test public void testSuppressedWarnings1() throws ConfigCompileException{
		compile("<! suppresswarnings: BareStrings; > msg(hi)");
		doVerify(CompilerWarning.SupressedWarnings);
		assertTrue(!times.containsKey(CompilerWarning.BareStrings) || times.get(CompilerWarning.BareStrings).get() == 0);
	}
	
	@Test public void testSuppressedWarnings2(){
		compile("<! suppresswarnings: UnquotedSymbols, StrictModeOff; > msg(hi)");
		doVerify(CompilerWarning.SupressedWarnings);
		doVerify(CompilerWarning.BareStrings);
	}
	
}
