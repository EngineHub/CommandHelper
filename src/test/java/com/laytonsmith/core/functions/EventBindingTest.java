package com.laytonsmith.core.functions;

import com.laytonsmith.abstraction.MCPlayer;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.events.EventUtils;
import com.laytonsmith.core.exceptions.CRE.CRECastException;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import com.laytonsmith.testing.AbstractIntegrationTest;
import com.laytonsmith.testing.StaticTest;
import static com.laytonsmith.testing.StaticTest.SRun;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.verify;

public class EventBindingTest extends AbstractIntegrationTest {

	MCPlayer fakePlayer;

	@Before
	public void setUp() throws Exception {
		fakePlayer = StaticTest.GetOnlinePlayer();
		StaticTest.InstallFakeConvertor(fakePlayer);
		Static.InjectPlayer(fakePlayer);
	}

	@After
	public void tearDown() {
		EventUtils.UnregisterAll();
	}

	@Test
	public void testBindReturnsId() throws Exception {
		SRun("assign(@id, bind('shutdown', null, null, @event, msg('hi')))\n"
				+ "msg(is_string(@id))", fakePlayer);
		verify(fakePlayer).sendMessage("true");
	}

	@Test
	public void testBindRegistersEvent() throws Exception {
		SRun("bind('shutdown', array(id: 'testRegisters'), null, @event, msg('hi'))", fakePlayer);
		assertNotNull(EventUtils.GetEventById("testRegisters"));
	}

	@Test
	public void testBindWithOptions() throws Exception {
		String id = SRun("bind('shutdown', array(id: 'myid', priority: 'NORMAL'), null, @event, msg('hi'))", fakePlayer);
		assertNotNull(EventUtils.GetEventById("myid"));
	}

	@Test
	public void testBindWithCustomParams() throws Exception {
		SRun("assign(@x, 'captured')\n"
				+ "bind('shutdown', array(id: 'customTest'), null, @event, @x, msg(@x))", fakePlayer);
		assertNotNull(EventUtils.GetEventById("customTest"));
	}

	@Test
	public void testBindMultiple() throws Exception {
		String id1 = SRun("bind('shutdown', array(id: 'first'), null, @event, msg('a'))", fakePlayer);
		String id2 = SRun("bind('shutdown', array(id: 'second'), null, @event, msg('b'))", fakePlayer);
		assertNotNull(EventUtils.GetEventById("first"));
		assertNotNull(EventUtils.GetEventById("second"));
	}

	@Test(expected = ConfigCompileException.class)
	public void testBindInvalidEvent() throws Exception {
		SRun("bind('not_a_real_event', null, null, @event, msg('hi'))", fakePlayer);
	}

	@Test(expected = ConfigCompileException.class)
	public void testBindTooFewArgs() throws Exception {
		SRun("bind('shutdown', null, null)", fakePlayer);
	}

	@Test(expected = CRECastException.class)
	public void testBindBadOptionsType() throws Exception {
		SRun("bind('shutdown', 'bad', null, @event, msg('hi'))", fakePlayer);
	}

	@Test(expected = CRECastException.class)
	public void testBindBadPrefilterType() throws Exception {
		SRun("bind('shutdown', null, 'bad', @event, msg('hi'))", fakePlayer);
	}

	@Test
	public void testBindResultUsedInMsg() throws Exception {
		SRun("assign(@id, bind('shutdown', null, null, @event, msg('hi')))\n"
				+ "msg(is_string(@id))", fakePlayer);
		verify(fakePlayer).sendMessage("true");
	}

	@Test
	public void testUnbindAfterBind() throws Exception {
		SRun("assign(@id, bind('shutdown', null, null, @event, msg('hi')))\n"
				+ "unbind(@id)", fakePlayer);
	}
}
