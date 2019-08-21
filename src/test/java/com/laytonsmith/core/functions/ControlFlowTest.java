package com.laytonsmith.core.functions;

import static com.laytonsmith.testing.StaticTest.GetFakeServer;
import static com.laytonsmith.testing.StaticTest.GetOnlinePlayer;
import static com.laytonsmith.testing.StaticTest.RunCommand;
import static com.laytonsmith.testing.StaticTest.SRun;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.laytonsmith.abstraction.MCPlayer;
import com.laytonsmith.abstraction.MCServer;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.environments.CommandHelperEnvironment;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.testing.StaticTest;

public class ControlFlowTest {

	MCPlayer fakePlayer;
	MCServer fakeServer;
	com.laytonsmith.core.environments.Environment env;

	public ControlFlowTest() throws Exception {
		env = Static.GenerateStandaloneEnvironment();
		env = env.cloneAndAdd(new CommandHelperEnvironment());
	}

	@BeforeClass
	public static void setUpClass() throws Exception {
		StaticTest.InstallFakeServerFrontend();
	}

	@AfterClass
	public static void tearDownClass() throws Exception {
	}

	@Before
	public void setUp() {
		fakeServer = GetFakeServer();
		fakePlayer = GetOnlinePlayer(fakeServer);
		env.getEnv(CommandHelperEnvironment.class).SetPlayer(fakePlayer);
	}

	@After
	public void tearDown() {
	}

	@Test(timeout = 10000)
	public void testIf() throws Exception {
		SRun("if(true, msg('correct'), msg('incorrect'))", fakePlayer);
		SRun("if(false, msg('incorrect'), msg('correct'))", fakePlayer);
		verify(fakePlayer, times(2)).sendMessage("correct");
	}

	@Test(timeout = 10000)
	public void testIf2() throws Exception {
		SRun("assign(@true, true)\n"
				+ "if(@true, msg('Hello World!'))", fakePlayer);
		verify(fakePlayer).sendMessage("Hello World!");
	}

	@Test
	public void testIfelse() throws Exception {
		assertEquals("3", SRun("ifelse("
				+ "false, 1,"
				+ "false, 2,"
				+ "true, 3,"
				+ "true, 4,"
				+ "false, 5)", null));
		assertEquals("4", SRun("ifelse("
				+ "false, 1,"
				+ "false, 2,"
				+ "false, 3,"
				+ "add(2, 2))", null));
	}

	@Test(timeout = 10000)
	public void testSwitch() throws Exception {
		assertEquals("correct", SRun("switch(3,"
				+ "1, wrong,"
				+ "2, wrong,"
				+ "3, correct,"
				+ "4, wrong)", null));
		assertEquals("correct", SRun("switch(4,"
				+ "1, wrong,"
				+ "2, wrong,"
				+ "3, wrong,"
				+ "correct)", null));
	}

	@Test
	public void testSwitch2() throws Exception {
		SRun("switch(2, 1, msg('nope'), 2, msg('yep'))", fakePlayer);
		verify(fakePlayer).sendMessage("yep");
	}

	@Test
	public void testSwitch3() throws Exception {
		SRun("assign(@args, 'test')"
				+ "switch(@args,"
				+ "'test',"
				+ "msg('test'),"
				+ "msg('default')"
				+ ")",
				fakePlayer);
		verify(fakePlayer).sendMessage("test");
	}

	@Test(timeout = 10000)
	public void testSwitchWithArray() throws Exception {
		assertEquals("correct", SRun("switch(3,"
				+ "array(1, 2), wrong,"
				+ "array(3, 4), correct,"
				+ "5, wrong)", null));
	}

	@Test(timeout = 10000)
	public void testSwitchWithNestedArrayAsDefaultReturn() throws Exception {
		assertEquals("{{correct}}", SRun("switch(5,"
				+ "'case1', wrong,"
				+ "'case2', also wrong,"
				+ " array(array('correct')))", null));
	}

	@Test
	public void testSwitchWithRange() throws Exception {
		SRun("switch(dyn(1)){"
				+ "case 0..5: msg('yes')"
				+ "case 6..10: msg('no')"
				+ "}"
				+ "switch(dyn(1)){"
				+ "case 1..5: msg('yes')"
				+ "}"
				+ "switch(dyn(1)){"
				+ "case 5..0: msg('yes')"
				+ "}"
				+ "switch(dyn(1)){"
				+ "case 1..2: case 3..4: msg('yes')"
				+ "}", fakePlayer);
		verify(fakePlayer, times(4)).sendMessage("yes");
	}

	@Test(timeout = 10000)
	public void testFor1() throws Exception {
		String config = "/for = >>>\n"
				+ " assign(@array, array())"
				+ " for(assign(@i, 0), lt(@i, 5), inc(@i),\n"
				+ "     array_push(@array, @i)\n"
				+ " )\n"
				+ " msg(@array)\n"
				+ "<<<\n";
		RunCommand(config, fakePlayer, "/for");
		verify(fakePlayer).sendMessage("{0, 1, 2, 3, 4}");
	}

	@Test(expected = ConfigRuntimeException.class, timeout = 10000)
	public void testFor2() throws Exception {
		String script
				= "   assign(@array, array())"
				+ " for('nope', lt(@i, 5), inc(@i),\n"
				+ "     array_push(@array, @i)\n"
				+ " )\n"
				+ " msg(@array)\n";
		SRun(script, fakePlayer);
	}

	@Test(timeout = 10000)
	public void testForeach1() throws Exception {
		String config = "/for = >>>\n"
				+ " assign(@array, array(1, 2, 3, 4, 5))\n"
				+ " assign(@array2, array())"
				+ " foreach(@array, @i,\n"
				+ "     array_push(@array2, @i)\n"
				+ " )\n"
				+ " msg(@array2)\n"
				+ "<<<\n";
		RunCommand(config, fakePlayer, "/for");
		verify(fakePlayer).sendMessage("{1, 2, 3, 4, 5}");
	}

	@Test(timeout = 10000)
	public void testForeach2() throws Exception {
		String config = "/for = >>>\n"
				+ " assign(@array, array(1, 2, 3, 4, 5))\n"
				+ " assign(@array2, array())"
				+ " foreach(@array, @i,\n"
				+ "     if(equals(@i, 1), continue(2))"
				+ "     array_push(@array2, @i)\n"
				+ " )\n"
				+ " msg(@array2)\n"
				+ "<<<\n";
		RunCommand(config, fakePlayer, "/for");
		verify(fakePlayer).sendMessage("{3, 4, 5}");
	}

	@Test(timeout = 10000)
	public void testForeach3() throws Exception {
		String config = "/for = >>>\n"
				+ " assign(@array, array(1, 2, 3, 4, 5))\n"
				+ " assign(@array1, array(1, 2, 3, 4, 5))\n"
				+ " assign(@array2, array())\n"
				+ " foreach(@array1, @j,"
				+ "     foreach(@array, @i,\n"
				+ "         if(equals(@i, 3), break(2))"
				+ "         array_push(@array2, @i)\n"
				+ "     )\n"
				+ " )"
				+ " msg(@array2)\n"
				+ "<<<\n";
		RunCommand(config, fakePlayer, "/for");
		verify(fakePlayer).sendMessage("{1, 2}");
	}

	@Test(timeout = 10000)
	public void testForeachWithArraySlice() throws Exception {
		SRun("foreach(1..2, @i, msg(@i))", fakePlayer);
		verify(fakePlayer).sendMessage("1");
		verify(fakePlayer).sendMessage("2");
	}

	@Test(timeout = 10000)
	public void testForeachWithKeys1() throws Exception {
		SRun("@array = array(1: 'one', 2: 'two') @string = '' foreach(@array, @key, @value,"
				+ " @string .= (@key.':'.@value.';')) msg(@string)", fakePlayer);
		verify(fakePlayer).sendMessage("1:one;2:two;");
	}

	@Test(timeout = 10000)
	public void testForeachWithKeys2() throws Exception {
		SRun("@array = array('one': 1, 'two': 2) @string = '' foreach(@array, @key, @value,"
				+ " @string .= (@key.':'.@value.';')) msg(@string)", fakePlayer);
		verify(fakePlayer).sendMessage("one:1;two:2;");
	}

	@Test(timeout = 10000)
	public void testForeachWithKeys3() throws Exception {
		SRun("@array = array('one': 1, 'two': 2)"
				+ "\nforeach(@array, @key, @value){\n\tmsg(@key.':'.@value)\n}", fakePlayer);
		verify(fakePlayer).sendMessage("one:1");
		verify(fakePlayer).sendMessage("two:2");
	}

	@Test
	public void testForelse() throws Exception {
		SRun("forelse(assign(@i, 0), @i < 0, @i++, msg('fail'), msg('pass'))", fakePlayer);
		verify(fakePlayer).sendMessage("pass");
		verify(fakePlayer, times(0)).sendMessage("fail");
	}

	@Test
	public void testForeachelse() throws Exception {
		SRun("foreachelse(array(), @val, msg('fail'), msg('pass'))", fakePlayer);
		SRun("foreachelse(array(1), @val, msg('pass'), msg('fail'))", fakePlayer);
		SRun("foreachelse(1..2, @val, msg('pass'), msg('fail'))", fakePlayer);
		verify(fakePlayer, times(4)).sendMessage("pass");
		verify(fakePlayer, times(0)).sendMessage("fail");
	}

	/**
	 * There is a bug that causes an infinite loop, so we put a 10 second timeout
	 *
	 * @throws Exception
	 */
	@Test(timeout = 10000)
	public void testContinue1() throws Exception {
		String config = "/continue = >>>\n"
				+ " assign(@array, array())"
				+ " for(assign(@i, 0), lt(@i, 5), inc(@i),\n"
				+ "     if(equals(@i, 2), continue(1))\n"
				+ "     array_push(@array, @i)\n"
				+ " )\n"
				+ " msg(@array)\n"
				+ "<<<\n";
		RunCommand(config, fakePlayer, "/continue");
		verify(fakePlayer).sendMessage("{0, 1, 3, 4}");
	}

	@Test(timeout = 10000)
	public void testContinue2() throws Exception {
		String config = "/continue = >>>\n"
				+ " assign(@array, array())"
				+ " for(assign(@i, 0), lt(@i, 5), inc(@i),\n"
				+ "     if(equals(@i, 2), continue(2))\n"
				+ "     array_push(@array, @i)\n"
				+ " )\n"
				+ " msg(@array)\n"
				+ "<<<\n";
		RunCommand(config, fakePlayer, "/continue");
		verify(fakePlayer).sendMessage("{0, 1, 4}");
	}

	@Test(timeout = 10000)
	public void testContinue3() throws Exception {
		String config = "/continue = >>>\n"
				+ " assign(@array, array())"
				+ " for(assign(@i, 0), lt(@i, 5), inc(@i),\n"
				+ "     if(equals(@i, 2), continue(3))\n"
				+ "     array_push(@array, @i)\n"
				+ " )\n"
				+ " msg(@array)\n"
				+ "<<<\n";
		RunCommand(config, fakePlayer, "/continue");
		verify(fakePlayer).sendMessage("{0, 1}");
	}

	@Test(timeout = 10000)
	public void testBreak1() throws Exception {
		String config = "/break = >>>\n"
				+ " assign(@array, array())"
				+ " for(assign(@i, 0), lt(@i, 2), inc(@i),\n"
				+ "     for(assign(@j, 0), lt(@j, 5), inc(@j),\n"
				+ "         if(equals(@j, 2), break())\n"
				+ "         array_push(@array, concat('j:', @j))\n"
				+ "     )\n"
				+ "     array_push(@array, concat('i:', @i))\n"
				+ " )\n"
				+ " msg(@array)\n"
				+ "<<<\n";
		RunCommand(config, fakePlayer, "/break");
		verify(fakePlayer).sendMessage("{j:0, j:1, i:0, j:0, j:1, i:1}");
	}

	@Test(timeout = 10000)
	public void testBreak2() throws Exception {
		String config = "/break = >>>\n"
				+ " assign(@array, array())"
				+ " for(assign(@i, 0), lt(@i, 2), inc(@i),\n"
				+ "     for(assign(@j, 0), lt(@j, 5), inc(@j),\n"
				+ "         if(equals(@j, 2), break(2))\n"
				+ "         array_push(@array, concat('j:', @j))\n"
				+ "     )\n"
				+ "     array_push(@array, concat('i:', @i))\n"
				+ " )\n"
				+ " msg(@array)\n"
				+ "<<<\n";
		RunCommand(config, fakePlayer, "/break");
		verify(fakePlayer).sendMessage("{j:0, j:1}");
	}

	@Test(timeout = 10000)
	public void testWhile() throws Exception {
		SRun("assign(@i, 2) while(@i > 0, @i-- msg('hi'))", fakePlayer);
		verify(fakePlayer, times(2)).sendMessage("hi");
	}

	@Test(timeout = 10000)
	public void testDoWhile() throws Exception {
		SRun("assign(@i, 2) dowhile(@i-- msg('hi'), @i > 0)", fakePlayer);
		verify(fakePlayer, times(2)).sendMessage("hi");
	}
}
