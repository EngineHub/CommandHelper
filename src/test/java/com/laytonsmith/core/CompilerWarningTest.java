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
import org.junit.*;
import org.mockito.Mockito;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.eq;
import static org.powermock.api.mockito.PowerMockito.mock;

/**
 * This class ensures that all code scenarios that should trigger a compiler warning do.
 * @author lsmith
 */
public class CompilerWarningTest {
	@BeforeClass
	public static void setUpClass(){
		StaticTest.InstallFakeServerFrontend();
	}
	
	CHLog fakeLogger;
	@Before
	public void installFakeLog(){
		fakeLogger = mock(CHLog.class);
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
	
	public void doVerify(CompilerWarning warning){
		try {
			verify(fakeLogger, Mockito.atLeastOnce()).CompilerWarning(eq(warning), Mockito.anyString(), Mockito.any(Target.class), Mockito.any(FileOptions.class));
		} catch (ConfigCompileException ex) {
			throw new RuntimeException(ex);
		}
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
	
	@Test public void testAssigmentInIf(){
		compile("<! strict > if(@a = 5){ msg('') }");
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
	
	@Test public void testMagicNumbers(){
		compile("<! strict > @a = 1 if(@a < 3){ msg('') }");
		doVerify(CompilerWarning.MagicNumber);
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
		verify(fakeLogger, Mockito.times(0)).CompilerWarning(eq(CompilerWarning.BareStrings), Mockito.anyString(), Mockito.any(Target.class), Mockito.any(FileOptions.class));
	}
	
	@Test public void testSuppressedWarnings2(){
		compile("<! suppresswarnings: MagicNumber; > msg(hi)");
		doVerify(CompilerWarning.SupressedWarnings);
		doVerify(CompilerWarning.BareStrings);
	}
	
}
