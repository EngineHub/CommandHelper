package com.laytonsmith.core;

import com.laytonsmith.core.constructs.CFunction;
import com.laytonsmith.core.constructs.CString;
import com.laytonsmith.core.constructs.Construct;
import com.laytonsmith.core.constructs.IVariable;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import static com.laytonsmith.testing.StaticTest.SRun;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import static org.junit.Assert.*;
import org.junit.*;
import org.junit.runner.RunWith;
import static org.mockito.Mockito.*;

/**
 * This class tests optimizations by looking at the tree after optimization occurs to see
 * if it matches expectation.
 * @author layton
 */
public class OptimizationTest {
    
    public String optimize(String script) throws ConfigCompileException{
        GenericTreeNode<Construct> tree = MethodScriptCompiler.compile(MethodScriptCompiler.lex(script, null));        
        StringBuilder b = new StringBuilder();
        //The root always contains null.
        for(GenericTreeNode<Construct> child : tree.getChildren()){
            b.append(optimize0(child));
        }        
        return b.toString();
    }
    private String optimize0(GenericTreeNode<Construct> node){
        if(node.data instanceof CFunction){
            StringBuilder b = new StringBuilder();
            boolean first = true;
            b.append(((CFunction)node.data).val()).append("(");
            for(GenericTreeNode<Construct> child : node.children){
                if(!first){
                    b.append(",");
                }
                first = false;
                b.append(optimize0(child));
            }
            b.append(")");
            return b.toString();
        } else if(node.data instanceof CString){
            //strings
            return new StringBuilder().append("'").append(node.data.val().replaceAll("\t", "\\t").replaceAll("\n", "\\n").replace("\\", "\\\\").replace("'", "\\'")).append("'").toString();
        } else if(node.data instanceof IVariable){
            return ((IVariable)node.data).getName();
        } else {
            //static
            return node.data.toString();
        }
    }
    
    @Test public void testTestFramework() throws ConfigCompileException{
        //This just tests to see that the basic framework works. This shouldn't optimize.
        assertEquals("msg('this is a string','so is this')", optimize("msg(\n 'this is a string',\nso is this\n)"));        
        assertEquals("msg('\\'quoted\\'')", optimize("msg( '\\'quoted\\'' )"));
    }
    
    @Test public void testIfBasic() throws ConfigCompileException{
        assertEquals("msg('hi')", optimize("if(true){ msg('hi') } else { msg('fail') }"));
    }
    
    @Test public void testIfWithBraces() throws ConfigCompileException{
        assertEquals("ifelse(dyn(),msg('hi'),msg('hi'))", optimize("if(dyn()){ msg('hi') } else { msg('hi') }"));
    }
    
    @Test public void testMultipleLinesInBraces() throws ConfigCompileException{
        assertEquals("ifelse(dyn(false),msg('nope'),sconcat(msg('hi'),msg('hi')))", optimize("if(dyn(false)){\n"
                + "msg('nope')\n"
                + "} else {\n"
                + " msg('hi')\n"
                + " msg('hi')\n"
                + "}"));
    }
    
    @Test public void testProcOptimization1() throws ConfigCompileException{
        //The proc stays there, but the call to it should be consolidated
        assertEquals("sconcat(proc('_add',@a,@b,return(add(@a,@b))),4)", optimize("proc(_add, @a, @b, return(@a + @b)) _add(2, 2)"));
    }
    
    @Test(expected=ConfigCompileException.class) 
    public void testProcOptimization2() throws ConfigCompileException{
        optimize("proc(_divide, @a, return(@a / 0)) _divide(1)");
    }
    
    @Test
    public void testProcOptimization3() throws ConfigCompileException{
        //Rather, lack of optimization
        assertEquals("sconcat(proc('_nope',msg('Hi')),_nope())", optimize("proc(_nope, msg('Hi')) _nope()"));
    }
    
    @Test
    public void testProcOptimiztion4() throws ConfigCompileException{
        //Test embedded procs
        assertEquals("sconcat(proc('_outer',sconcat(proc('_inner',@a,return(@a)),'blah')),_inner('huh'))", 
                optimize("proc(_outer, proc(_inner, @a, return(@a)) _inner('blah')) _inner('huh')"));
    }
    
    //TODO: This is a bit ambitious for now, put this back at some point, and then make it pass.
//    @Test public void testAssign() throws ConfigCompileException{
//        //In this test, there's no way it won't ever be 'hi', so do a replacement (we still need to keep
//        //the assign, because it does need to go into the variable table for reflective purposes)
//        assertEquals("sconcat(assign(@a,'hi'),msg('hi'))", optimize("assign(@a, 'hi') msg(@a)"));
//        //In this case, the first use may be hardcoded, but after the if, it may have changed, so we
//        //can no longer assume it's always going to be 'hi'
//        assertEquals("sconcat(assign(@a,'hi'),msg('hi'),if(dyn(),assign(@a,'bye')),msg(@a))",
//                optimize(""
//                + "assign(@a, 'hi')"
//                + "msg(@a)"
//                + "if(dyn(), assign(@a, 'bye'))"
//                + "msg(@a)"));
//        //In this case, we have a worthless assignment; We know @a is already 'hi' and it's always going
//        //to be that, and we're trying to assign 'hi' again, so we can completely remove this from
//        //the code, at which point the last msg can be optimized.
//        assertEquals("sconcat(assign(@a,'hi'),msg('hi'),if(dyn(),null),msg('hi'))",
//                optimize(""
//                + "assign(@a, 'hi')"
//                + "msg(@a)"
//                + "if(dyn(), assign(@a, 'hi'))"
//                + "msg(@a)"));
//    }
}
