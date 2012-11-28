package com.laytonsmith.core;

import com.laytonsmith.core.constructs.CFunction;
import com.laytonsmith.core.constructs.CString;
import com.laytonsmith.core.constructs.IVariable;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import com.laytonsmith.testing.StaticTest;
import org.junit.*;
import static org.junit.Assert.*;

/**
 * This class tests optimizations by looking at the tree after optimization occurs to see
 * if it matches expectation.
 * @author layton
 */
public class OptimizationTest {
	
	@BeforeClass
	public static void setUpClass(){
		StaticTest.InstallFakeServerFrontend();
	}
    
    public String optimize(String script) throws ConfigCompileException{
        ParseTree tree = MethodScriptCompiler.compile(MethodScriptCompiler.lex(script, null, true));        
        StringBuilder b = new StringBuilder();
        //The root always contains null.
        for(ParseTree child : tree.getChildren()){
            b.append(optimize0(child));
        }        
        return b.toString();
    }
    private String optimize0(ParseTree node){
        if(node.getData() instanceof CFunction){
            StringBuilder b = new StringBuilder();
            boolean first = true;
            b.append(((CFunction)node.getData()).val()).append("(");
            for(ParseTree child : node.getChildren()){
                if(!first){
                    b.append(",");
                }
                first = false;
                b.append(optimize0(child));
            }
            b.append(")");
            return b.toString();
        } else if(node.getData() instanceof CString){
            //strings
            return new StringBuilder().append("'").append(node.getData().val().replaceAll("\t", "\\t").replaceAll("\n", "\\n").replace("\\", "\\\\").replace("'", "\\'")).append("'").toString();
        } else if(node.getData() instanceof IVariable){
            return ((IVariable)node.getData()).getName();
        } else {
            //static
            return node.getData().toString();
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
	
	@Test public void testProcOptimizationRecursion() throws Exception{
		assertEquals("sconcat(proc('_loop',@a,if(gt(@a,0),_loop(subtract(@a,1)),return(@a))),_loop(2))", 
				optimize("proc(_loop, @a, if(@a > 0, _loop(@a - 1), return(@a))) _loop(2)"));
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
    
    @Test public void testProcReturn() throws ConfigCompileException{
        assertEquals("sconcat(proc('_proc',return(array(1))),array_get(_proc(),0))", 
                optimize("proc(_proc, return(array(1))) _proc()[0]"));
    }
	
	@Test public void testUnreachableCode() throws ConfigCompileException{
		assertEquals("sconcat(assign(@a,0),ifelse(@a,die(),sconcat(msg('2'),msg('3'))))", optimize("assign(@a, 0) if(@a){ die() msg('1') } else { msg('2') msg('3') }"));
		assertEquals("die()", optimize("if(true){ die() msg('1') } else { msg('2') msg('3') }"));
	}
	
	@Test public void testUnreachableCodeWithBranchTypeFunction() throws ConfigCompileException{
		assertEquals("ifelse(@var,die(),msg(''))", optimize("if(@var){ die() } else { msg('') }"));
	}
	
	@Test public void testRegSplitOptimization1() throws Exception{
		assertEquals("split('pattern',dyn('subject'))", optimize("reg_split('pattern', dyn('subject'))"));
	}
	
	@Test public void testRegSplitOptimization2() throws Exception{
		assertEquals("split('.',dyn('subject'))", optimize("reg_split(reg_escape('.'), dyn('subject'))"));
	}
	
	@Test public void testRegReplaceOptimization1() throws Exception{
		assertEquals("replace('this is a thing','thing',dyn('hi'))", optimize("reg_replace('thing', dyn('hi'), 'this is a thing')"));
	}
	
	@Test public void testAssignWithEqualsSymbol() throws Exception {
		assertEquals("sconcat(assign(@var,'ab'),'c')", optimize("@var = 'a'.'b' 'c'"));
	}
	
	@Test public void testAssignWithOperators() throws Exception{
		assertEquals("assign(@one,add(@one,1))", optimize("@one += 1"));
		assertEquals("assign(@one,subtract(@one,1))", optimize("@one -= 1"));
		assertEquals("assign(@one,multiply(@one,1))", optimize("@one *= 1"));
		assertEquals("assign(@one,divide(@one,1))", optimize("@one /= 1"));
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
