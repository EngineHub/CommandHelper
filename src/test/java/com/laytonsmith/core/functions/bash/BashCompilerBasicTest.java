package com.laytonsmith.core.functions.bash;

import com.laytonsmith.core.api;
import com.laytonsmith.core.compiler.MethodScriptStaticCompiler;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import org.junit.*;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;

/**
 *
 * @author layton
 */
public class BashCompilerBasicTest {
    
    @Test public void testBasicIf() throws ConfigCompileException{
        assertEquals("if [ 1 ]; then\n5\nfi\n", MethodScriptStaticCompiler.compile("if(dyn(1), 5)", api.Platforms.COMPILER_BASH, null));        
    }
    
    @Test public void testIfElse() throws ConfigCompileException{
        assertEquals("if [ 1 ]; then\n5\nelse\n5\nfi\n", MethodScriptStaticCompiler.compile("if(dyn(1), 5, 5)", api.Platforms.COMPILER_BASH, null));
    }
    
    @Test public void testAnd() throws ConfigCompileException{
        assertEquals("if [ 1 == 1 ]; then\n5\nfi\n", MethodScriptStaticCompiler.compile("if(1 == dyn(1)){ 5 }", api.Platforms.COMPILER_BASH, null));
    }
}
