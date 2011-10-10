/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.testing;

import com.laytonsmith.PureUtilities.SerializedPersistance;
import com.laytonsmith.aliasengine.Constructs.CEntry;
import com.laytonsmith.aliasengine.Constructs.Construct;
import com.laytonsmith.aliasengine.Constructs.Token;
import com.laytonsmith.aliasengine.GenericTreeNode;
import com.laytonsmith.aliasengine.MScriptCompiler;
import com.laytonsmith.aliasengine.functions.exceptions.ConfigCompileException;
import com.laytonsmith.aliasengine.functions.exceptions.ConfigRuntimeException;
import com.sk89q.commandhelper.CommandHelperPlugin;
import static org.junit.Assert.*;
import static com.laytonsmith.testing.StaticTest.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import static org.mockito.Mockito.*;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Layton
 */
public class ArrayTest {
    Player fakePlayer;
    public ArrayTest() {
    }

    @Before
    public void setUp() {
        fakePlayer = StaticTest.GetOnlinePlayer();
    }
    
    @Test public void testAssociativeCreation() throws ConfigCompileException{
        assertEquals("{0: 0}", SRun("array(0: 0)", fakePlayer));
    }
    
    @Test public void testAssociativeCreation2() throws ConfigCompileException{
        SRun("assign(@arr, array(0, 1))"
                + "msg(@arr)"
                + "array_set(@arr, 2, 2)"
                + "msg(@arr)"
                + "array_set(@arr, 0, 0)"
                + "msg(@arr)"
                + "array_set(@arr, 'key', 'value')"
                + "msg(@arr)"
                + "array_set(@arr, 10, 'value')"
                + "msg(@arr)", 
             fakePlayer);
        
        verify(fakePlayer).sendMessage("{0, 1}");
        verify(fakePlayer, times(2)).sendMessage("{0, 1, 2}");
        verify(fakePlayer).sendMessage("{0: 0, 1: 1, 2: 2, key: value}");
        verify(fakePlayer).sendMessage("{0: 0, 1: 1, 2: 2, 10: value, key: value}");
    }
    
    @Test public void testArrayGetWithAssociativeArray() throws ConfigCompileException{
        assertEquals("test", SRun("g(assign(@arr, array()) array_set(@arr, 'val', 'test')) array_get(@arr, 'val')", fakePlayer));
    }
    
    @Test(expected=ConfigRuntimeException.class)
    public void testAssociativeSlicing() throws ConfigCompileException{
        SRun("array(0: 0, 1: 1, 2: 2)[1..-1]", fakePlayer);
    }
    
    @Test public void testAssociativeCopy() throws ConfigCompileException{
        SRun("assign(@arr, array(0: 0, 1: 1))"
                + "assign(@arr2, @arr[])"
                + "array_set(@arr2, 0, 1)"
                + "msg(@arr)"
                + "msg(@arr2)", fakePlayer);
        verify(fakePlayer).sendMessage("{0: 0, 1: 1}");
        verify(fakePlayer).sendMessage("{0: 1, 1: 1}");
    }
    
    @Test public void testArrayKeyNormalization() throws ConfigCompileException{
        assertEquals("{0: 0}", SRun("array(false: 0)", fakePlayer));
        assertEquals("{1: 1}", SRun("array(true: 1)", fakePlayer));
        assertEquals("{: empty}", SRun("array(null: empty)", fakePlayer));
        assertEquals("{2.3: 2.3}", SRun("array(2.3: 2.3)", fakePlayer));
    }
    
    @Test public void testArrayKeys() throws ConfigCompileException{
        assertEquals("{0, 1, potato}", SRun("array_keys(array(potato: 5, 1: 44, 0: 'i can count to'))", fakePlayer));
    }
    
    @Test public void testArrayNormalize() throws ConfigCompileException{
        assertEquals("{3, 2, 1}", SRun("array_normalize(array(0: 3, 1: 2, 2: 1))", fakePlayer));
    }
    
    @Test public void testArrayPushOnAssociativeArray() throws ConfigCompileException{
        SRun("assign(@arr, array(0: 0, 1: 1, potato: potato))"
                + "msg(@arr)"
                + "array_push(@arr, tomato)"
                + "msg(@arr)", fakePlayer);
        verify(fakePlayer).sendMessage("{0: 0, 1: 1, potato: potato}");
        verify(fakePlayer).sendMessage("{0: 0, 1: 1, 2: tomato, potato: potato}");        
    }
    
    @Test public void testPushingANegativeIndexOnArray() throws ConfigCompileException{
        SRun("assign(@arr, array(0, 1, 2))"
                + "array_set(@arr, -1, -1)"
                + "msg(@arr[-1])", fakePlayer);
        verify(fakePlayer).sendMessage("-1");
    }
    
    @Test public void testAssociativeArraySerialization() throws ConfigCompileException{
        Plugin fakePlugin = mock(Plugin.class);        
        CommandHelperPlugin.persist = new SerializedPersistance(new File("plugins/CommandHelper/persistance.ser"), fakePlugin);
        when(fakePlayer.isOp()).thenReturn(true);
        SRun("assign(@arr, array(0: 0, 1: 1, potato: potato))"
                + "store_value(potato, @arr)"
                + "msg(get_value(potato))", fakePlayer);
        verify(fakePlayer).sendMessage("{0: 0, 1: 1, potato: potato}");
    }
    
    @Test public void testIsAssociative() throws ConfigCompileException{
        assertEquals("true", SRun("is_associative(array(0: 0))", fakePlayer));
        assertEquals("false", SRun("is_associative(array(0))", fakePlayer));
    }
}
