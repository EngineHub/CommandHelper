package com.laytonsmith.testing;

import com.laytonsmith.abstraction.MCPlayer;
import static com.laytonsmith.testing.StaticTest.SRun;
import org.bukkit.plugin.Plugin;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 *
 *
 */
public class ProcedureTest {
    MCPlayer fakePlayer;
    public ProcedureTest() {
    }

    @BeforeClass
    public static void setUpClass(){
        Plugin fakePlugin = mock(Plugin.class);
    }
    @Before
    public void setUp() {
        fakePlayer = StaticTest.GetOnlinePlayer();
    }

    @Test public void testSimpleProc() throws Exception{
        SRun("proc(_blah, msg('blah')) _blah()", fakePlayer);
        verify(fakePlayer).sendMessage("blah");
    }

    @Test public void testProcWithParameters() throws Exception{
        SRun("proc(_blah, @msg, msg(@msg)) _blah('blah')", fakePlayer);
        verify(fakePlayer).sendMessage("blah");
    }

    @Test public void testProcWithArguments() throws Exception{
        SRun("proc(_blah, msg(@arguments)) _blah(1, 2, 3, 4)", fakePlayer);
        verify(fakePlayer).sendMessage("{1, 2, 3, 4}");
    }

    @Test public void testProcCalledMultipleTimes() throws Exception{
        SRun("proc(_blah, @blah, msg(@blah)) _blah('blah') _blah('blarg')", fakePlayer);
        verify(fakePlayer).sendMessage("blah");
        verify(fakePlayer).sendMessage("blarg");
    }

    @Test public void ensureOutOfScopeWorks() throws Exception{
        SRun("assign(@lol, '42') proc(_blah, msg('notlol'.@lol)) _blah()", fakePlayer);
        verify(fakePlayer).sendMessage("notlolnull");
    }

    @Test public void ensureOutOfScopeDoesntInterfere() throws Exception{
        SRun("assign(@lol, '42') proc(_blah, assign(@lol, 'yo dawg herd u leik 42')) _blah() msg(@lol)", fakePlayer);
        verify(fakePlayer).sendMessage("42");
    }

    @Test public void testProcCalledMultipleTimesWithAssign() throws Exception{
        SRun("proc(_parse_args, @args," +
                "assign(@retn, array())" +
                "foreach(range(1, length(@args), 2), @x," +
                    "array_push(@retn, @args[@x])" +
                ")" +

                "return(@retn)" +
            ")"
                + "msg(_parse_args(array(0,1,2,3,4,5,6,7)))"
                + "msg(_parse_args(array(0,1,2,3,4,5,6,7)))"
                + "msg(_parse_args(array(0,1,2,3,4,5,6,7)))", fakePlayer);

        verify(fakePlayer, times(3)).sendMessage("{1, 3, 5, 7}");
    }

}
