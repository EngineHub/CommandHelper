/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.testing;

import com.laytonsmith.PureUtilities.SerializedPersistance;
import com.laytonsmith.aliasengine.functions.exceptions.ConfigCompileException;
import com.laytonsmith.aliasengine.functions.exceptions.ConfigRuntimeException;
import com.sk89q.commandhelper.CommandHelperPlugin;
import static org.junit.Assert.*;
import static com.laytonsmith.testing.StaticTest.*;
import java.io.File;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
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
        assertEquals("{0: 0, 1: 1}", SRun("array(0: 0, 1: 1)", fakePlayer));
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
        assertEquals("true", SRun("is_associative(array(1: 1))", fakePlayer));
        assertEquals("false", SRun("is_associative(array(1))", fakePlayer));
    }
    
    @Test public void testFunctionResultAsAssociativeValue() throws ConfigCompileException{
        assertEquals("{1: thiswasconcated}", SRun("array(1: concat('this', was, concated))", fakePlayer));
        assertEquals("{1: this was concated}", SRun("array(1: this was concated)", fakePlayer));
    }
    
    @Test public void testDocumentationExample1() throws ConfigCompileException{
        SRun("assign(@arr, array('string key': 'value', 'string key 2': 'value')) msg(@arr['string key'])", fakePlayer);
        verify(fakePlayer).sendMessage("value");
    }
    
    @Test public void testDocumentationExample2() throws ConfigCompileException{
        SRun("assign(@arr, array(0, 1, 2, 3)) "
                + "msg(is_associative(@arr))"
                + "array_set(@arr, 4, 4)"
                + "msg(is_associative(@arr))"
                + "array_set(@arr, 0, 0)"
                + "msg(is_associative(@arr))"
                + "array_push(@arr, 5)"
                + "msg(is_associative(@arr))"
                + "msg(@arr)"
                + "array_set(@arr, 'key', 'value')"
                + "msg(is_associative(@arr))"
                , fakePlayer);
        verify(fakePlayer, times(4)).sendMessage("false");
        verify(fakePlayer).sendMessage("{0, 1, 2, 3, 4, 5}");
        verify(fakePlayer).sendMessage("true");
    }
    
    @Test public void testDocumentationExample3() throws ConfigCompileException{
        SRun("assign(@arr, array(0, 1: 1, 2, 3: 3, a: 'a'))"
                + "msg(array_keys(@arr))"
                , fakePlayer);
        verify(fakePlayer).sendMessage("{0, 1, 2, 3, a}");        
    }
    
    @Test public void testDocumentationExample4() throws ConfigCompileException{
        SRun("msg(array(0: 0, 2: 2, 1))", fakePlayer);
        SRun("assign(@arr, array()) array_set(@arr, 0, 0) array_set(@arr, 2, 2) array_push(@arr, 1)", fakePlayer);
        verify(fakePlayer).sendMessage("{0: 0, 2: 2, 3: 1}");
    }
    
    @Test public void testDocumentationExample5() throws ConfigCompileException{
        SRun("assign(@arr, array(0, 1: 1, 2, 3: 3, a: 'a'))\n"
                + "foreach(array_keys(@arr), @key, #array_keys returns {0, 1, 2, 3, a}\n"
                + "msg(@arr[@key]) #Messages the value\n"
                + ")", fakePlayer);
        verify(fakePlayer).sendMessage("0");
        verify(fakePlayer).sendMessage("1");
        verify(fakePlayer).sendMessage("2");
        verify(fakePlayer).sendMessage("3");
        verify(fakePlayer).sendMessage("a");
    }
    
    @Test public void testDocumentationExample6() throws ConfigCompileException{
        SRun("assign(@arr, array(-1: -1, 0, 1, 2: 2)) msg(@arr[-1])", fakePlayer);
        verify(fakePlayer).sendMessage("-1");
    }
    
    @Test public void testDocumentationExample7() throws ConfigCompileException{
        SRun("assign(@arr, array(-1: -1, 0, 1, 2: 2)) msg(@arr)", fakePlayer);
        verify(fakePlayer).sendMessage("{-1: -1, 0: 0, 1: 1, 2: 2}");
    }
    
    @Test public void testArraysReference1() throws ConfigCompileException{
        assertEquals("3", SRun("proc(_test, @array, return(array_size(@array))) _test(array(1, 2, 3))", fakePlayer));
    }
    
    @Test public void testArraysReturned() throws ConfigCompileException{
        SRun("proc(_test, return(array(1, 2, 3))) foreach(_test(), @i, msg(@i))", fakePlayer);
        verify(fakePlayer).sendMessage("1");
        verify(fakePlayer).sendMessage("2");
        verify(fakePlayer).sendMessage("3");
    }

}
