/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.aliasengine.functions;

import com.laytonsmith.aliasengine.MScriptCompiler;
import com.laytonsmith.aliasengine.Constructs.Construct;
import com.laytonsmith.aliasengine.exceptions.CancelCommandException;
import com.laytonsmith.aliasengine.exceptions.ConfigCompileException;
import com.laytonsmith.aliasengine.exceptions.ConfigRuntimeException;
import com.laytonsmith.aliasengine.Constructs.CArray;
import com.laytonsmith.aliasengine.Constructs.CInt;
import com.laytonsmith.testing.C;
import com.laytonsmith.testing.StaticTest;
import org.bukkit.entity.Player;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.Mockito.*;
import static com.laytonsmith.testing.StaticTest.*;
import static org.junit.Assert.*;

/**
 *
 * @author Layton
 */
public class ArrayHandlingTest {

    static Player fakePlayer;
    static CArray commonArray;

    public ArrayHandlingTest() {
    }

    @Before
    public void setUp() {
        fakePlayer = StaticTest.GetOnlinePlayer();
        commonArray = new CArray(0, null, new CInt(1, 0, null), new CInt(2, 0, null), new CInt(3, 0, null));
    }

    /**
     * Test of docs method, of class ArrayHandling.
     */
    @Test
    public void testDocs() {
        TestClassDocs(ArrayHandling.docs(), ArrayHandling.class);
    }

    @Test
    public void testArraySize() throws ConfigCompileException, CancelCommandException {
        ArrayHandling.array_size a = new ArrayHandling.array_size();
        TestBoilerplate(a, "array_size");
        CArray arr = commonArray;
        Construct ret = a.exec(0, null, fakePlayer, arr);
        assertReturn(ret, C.Int);
        assertCEquals(C.onstruct(3), ret);
    }
    
    @Test(expected=Exception.class)
    public void testArraySizeEx() throws CancelCommandException{
        ArrayHandling.array_size a = new ArrayHandling.array_size();
        a.exec(0, null, fakePlayer, C.Int(0));
    }

    @Test
    public void testArraySet1() throws  ConfigCompileException {
        String script =
                "assign(@array, array(1,2,3)) msg(@array) array_set(@array, 2, 1) msg(@array)";
        //MScriptCompiler.execute(MScriptCompiler.compile(MScriptCompiler.lex(script, null)), fakePlayer, null, null);
        StaticTest.Run(script, fakePlayer);
        verify(fakePlayer).sendMessage("{1, 2, 3}");
        verify(fakePlayer).sendMessage("{1, 2, 1}");
    }
    
    @Test public void testArraySet2() throws ConfigCompileException{
        SRun("assign(@array, array(1, 2)) assign(@array2, @array) array_set(@array, 0, 2) msg(@array) msg(@array2)", fakePlayer);
        verify(fakePlayer, times(2)).sendMessage("{2, 2}");
    }
    
    @Test public void testArrayReferenceBeingCorrect() throws ConfigCompileException{
        SRun("assign(@array, array(1, 2)) assign(@array2, @array[]) array_set(@array, 0, 2) msg(@array) msg(@array2)", fakePlayer);
        verify(fakePlayer).sendMessage("{2, 2}");
        verify(fakePlayer).sendMessage("{1, 2}");
    }
    
    @Test public void testArrayReferenceBeingCorrectWithArrayGet() throws ConfigCompileException{
        SRun("assign(@array, array(1, 2)) assign(@array2, array_get(@array)) array_set(@array, 0, 2) msg(@array) msg(@array2)", fakePlayer);
        verify(fakePlayer).sendMessage("{2, 2}");
        verify(fakePlayer).sendMessage("{1, 2}");
    }
    
    //This is valid behavior now.
//    @Test(expected=ConfigRuntimeException.class)
//    public void testArraySetEx() throws CancelCommandException, ConfigCompileException{
//        String script =
//                "assign(@array, array()) array_set(@array, 3, 1) msg(@array)";
//        MScriptCompiler.execute(MScriptCompiler.compile(MScriptCompiler.lex(script, null)), fakePlayer, null, null);
//    }

    @Test
    public void testArrayContains() throws CancelCommandException {
        ArrayHandling.array_contains a = new ArrayHandling.array_contains();
        TestBoilerplate(a, "array_contains");
        assertCEquals(C.onstruct(true), a.exec(0, null, fakePlayer, commonArray, C.onstruct(1)));
        assertCEquals(C.onstruct(false), a.exec(0, null, fakePlayer, commonArray, C.onstruct(55)));
    }
    
    @Test(expected=Exception.class)
    public void testArrayContainsEx() throws CancelCommandException{
        ArrayHandling.array_contains a = new ArrayHandling.array_contains();
        a.exec(0, null, fakePlayer, C.Int(0), C.Int(1));
    }

    @Test
    public void testArrayGet() throws CancelCommandException {
        ArrayHandling.array_get a = new ArrayHandling.array_get();
        TestBoilerplate(a, "array_get");
        assertCEquals(C.onstruct(1), a.exec(0, null, fakePlayer, commonArray, C.onstruct(0)));
    }
    
    @Test(expected=Exception.class)
    public void testArrayGetEx() throws CancelCommandException{
        ArrayHandling.array_get a = new ArrayHandling.array_get();
        a.exec(0, null, fakePlayer, C.Int(0), C.Int(1));
    }

    @Test(expected = ConfigRuntimeException.class)
    public void testArrayGetBad() throws CancelCommandException {
        ArrayHandling.array_get a = new ArrayHandling.array_get();
        a.exec(0, null, fakePlayer, commonArray, C.onstruct(55));
    }

    @Test
    public void testArrayPush() throws CancelCommandException {
        ArrayHandling.array_push a = new ArrayHandling.array_push();
        TestBoilerplate(a, "array_push");
        assertReturn(a.exec(0, null, fakePlayer, commonArray, C.onstruct(4)), C.Void);
        assertCEquals(C.onstruct(1), commonArray.get(0, 0));
        assertCEquals(C.onstruct(2), commonArray.get(1, 0));
        assertCEquals(C.onstruct(3), commonArray.get(2, 0));
        assertCEquals(C.onstruct(4), commonArray.get(3, 0));
    }
    
    @Test(expected=Exception.class)
    public void testArrayPushEx() throws CancelCommandException{
        ArrayHandling.array_push a = new ArrayHandling.array_push();
        a.exec(0, null, fakePlayer, C.Int(0), C.Int(1));
    }
    
    @Test public void testArrayResize() throws ConfigCompileException{
        String script = "assign(@array, array(1)) msg(@array) array_resize(@array, 2) msg(@array) array_resize(@array, 3, 'hello') msg(@array)";
        StaticTest.Run(script, fakePlayer);
        verify(fakePlayer).sendMessage("{1}");
        verify(fakePlayer).sendMessage("{1, null}");
        verify(fakePlayer).sendMessage("{1, null, hello}");
    }
    
    /**
     * Because we are testing a loop, we put in an infinite loop detection of 10 seconds
     * @throws ConfigCompileException 
     */
    @Test(timeout=10000) 
    public void testRange() throws ConfigCompileException{
        assertEquals("{0, 1, 2, 3, 4, 5, 6, 7, 8, 9}", SRun("range(10)", fakePlayer));
        assertEquals("{1, 2, 3, 4, 5, 6, 7, 8, 9, 10}", SRun("range(1, 11)", fakePlayer));
        assertEquals("{0, 5, 10, 15, 20, 25}", SRun("range(0, 30, 5)", fakePlayer));
        assertEquals("{0, 3, 6, 9}", SRun("range(0, 10, 3)", fakePlayer));
        assertEquals("{0, -1, -2, -3, -4, -5, -6, -7, -8, -9}", SRun("range(0, -10, -1)", fakePlayer));
        assertEquals("{}", SRun("range(0)", fakePlayer));
        assertEquals("{}", SRun("range(1, 0)", fakePlayer));
    }
    
    @Test public void testArraySliceAndNegativeIndexes() throws ConfigCompileException{
        assertEquals("e", SRun("array(a, b, c, d, e)[-1]", null));
        assertEquals("{a, b, c, d, e}", SRun("array(a, b, c, d, e)[]", null));
        assertEquals("{b, c}", SRun("array(a, b, c, d, e)[1..2]", null));
        assertEquals("{b, c, d, e}", SRun("array(a, b, c, d, e)[1..-1]", null));
        assertEquals("1", SRun("array(a, array(1, 2), c, d, e)[0..1][1][0]", null));
        assertEquals("{a, b}", SRun("array(a, b, c, d, e)[..1]", null));
        assertEquals("{c, d, e}", SRun("array(a, b, c, d, e)[2..]", null));
        assertEquals("{}", SRun("array(1, 2, 3, 4, 5)[3..0]", null));
        assertEquals("{a, b}", SRun("array_get(array(a, b))", null));
    }
}
