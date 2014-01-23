package com.laytonsmith.core;

import com.laytonsmith.core.compiler.OptimizationUtilities;
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
        return OptimizationUtilities.optimize(script, null);
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
	
	@Test public void testIfElse() throws ConfigCompileException {
		assertEquals("ifelse(dyn(1),msg(''),dyn(2),msg(''),msg(''))", optimize("if(dyn(1)){ msg('') } else if(dyn(2)){ msg('') } else { msg('') }"));
	}
	
	@Test public void testIfElseWithDie() throws ConfigCompileException {
		assertEquals("ifelse(is_null($pl),die(''),not(ponline(player($pl))),die(concat($pl,'')))", 
				optimize("if(is_null($pl)) {\ndie('') } else if(!ponline(player($pl))){ die($pl.'') }"));
	}
	
	@Test public void testNestedIfsWithRemoval() throws ConfigCompileException {
		assertEquals("p()", optimize("ifelse(1, if(0, msg('')), msg(''))"));
	}
    
    @Test public void testMultipleLinesInBraces() throws ConfigCompileException{
        assertEquals("ifelse(dyn(false),msg('nope'),sconcat(msg('hi'),msg('hi')))", optimize("if(dyn(false)){\n"
                + "msg('nope')\n"
                + "} else {\n"
                + " msg('hi')\n"
                + " msg('hi')\n"
                + "}"));
    }
    
	//TODO: These tests are not intended to be corrected in master, so I'm removing them for now
//    @Test public void testProcOptimization1() throws ConfigCompileException{
//        //The proc stays there, but the call to it should be consolidated
//        assertEquals("sconcat(proc('_add',@a,@b,return(add(@a,@b))),4)", optimize("proc(_add, @a, @b, return(@a + @b)) _add(2, 2)"));
//    }
	
	@Test public void testProcOptimizationRecursion() throws Exception{
		assertEquals("sconcat(proc('_loop',@a,ifelse(gt(@a,0),_loop(subtract(@a,1)),return(@a))),_loop(2))", 
				optimize("proc(_loop, @a, if(@a > 0, _loop(@a - 1), return(@a))) _loop(2)"));
	}
    
//    @Test(expected=ConfigCompileException.class) 
//    public void testProcOptimization2() throws ConfigCompileException{
//        optimize("proc(_divide, @a, return(@a / 0)) _divide(1)");
//    }
    
    @Test
    public void testProcOptimization3() throws ConfigCompileException{
        //Rather, lack of optimization
        assertEquals("sconcat(proc('_nope',msg('Hi')),_nope())", optimize("proc(_nope, msg('Hi')) _nope()"));
    }
    
//    @Test
//    public void testProcOptimiztion4() throws ConfigCompileException{
//        //Test embedded procs
//        assertEquals("sconcat(proc('_outer',sconcat(proc('_inner',@a,return(@a)),'blah')),_inner('huh'))", 
//                optimize("proc(_outer, proc(_inner, @a, return(@a)) _inner('blah')) _inner('huh')"));
//    }
    
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
	
	@Test public void testTrivialAssignmentWithEqualsSymbol() throws Exception{
		assertEquals("assign(@a,1)", optimize("@a = 1"));
	}
	
	@Test public void testAssignWithEqualsSymbol() throws Exception {
		assertEquals("sconcat(assign(@var,'ab'),'c')", optimize("@var = 'a'.'b' 'c'"));
	}
	
	@Test public void testAssignWithOperators() throws Exception{
		assertEquals("assign(@one,add(@one,1))", optimize("@one += 1"));
		assertEquals("assign(@one,subtract(@one,1))", optimize("@one -= 1"));
		assertEquals("assign(@one,multiply(@one,1))", optimize("@one *= 1"));
		assertEquals("assign(@one,divide(@one,1))", optimize("@one /= 1"));
		assertEquals("assign(@one,concat(@one,1))", optimize("@one .= 1"));
	}
	
	@Test public void testMultiAssign() throws Exception{
		assertEquals("assign(@one,assign(@two,''))", optimize("@one = @two = ''"));
		assertEquals("sconcat(assign(@one,assign(@two,'')),'blah')", optimize("@one = @two = '' 'blah'"));
	}
	
	@Test public void testAssignmentMixedWithAddition1() throws Exception{
		assertEquals("add(1,assign(@a,1))", optimize("1 + @a = 1"));
	}
	
	@Test public void testAssignmentMixedWithAddition2() throws Exception{
		assertEquals("add(1,assign(@a,add(@b,2)))", optimize("1 + @a = @b + 2"));
	}
	
	@Test public void testAssignmentMixedWithAddition3() throws Exception{
		assertEquals("add(1,assign(@a,add(@b,@c,2)))", optimize("1 + @a = @b + @c + 2"));
	}
	
	@Test public void testAssignmentMixedWithAddition4() throws Exception{
		assertEquals("add(1,assign(@a,add(@a,@b,@c,2)))", optimize("1 + @a += @b + @c + 2"));
	}
	
	@Test public void testAssignmentMixedWithAddition5() throws Exception{
		assertEquals("add(1,assign(@_,assign(@a,add(@a,@b,@c,2))))", optimize("1 + @_ = @a += @b + @c + 2"));
	}
	
	@Test public void testAssignmentMixedWithAddition6() throws Exception{
		assertEquals("sconcat(add(1,assign(@_,assign(@a,add(@a,@b,@c,2)))),'blah')", optimize("1 + @_ = @a += @b + @c + 2 'blah'"));
	}
	
	@Test public void testInnerIfAnded() throws Exception{
		assertEquals("ifelse(and(@a,@b),msg(''))", optimize("if(@a){ if(@b){ msg('') } }"));
	}
	
	@Test public void testInnerIfWithOtherStatements1() throws Exception{
		assertEquals("ifelse(@a,ifelse(@b,msg(''),msg('')))", optimize("if(@a){ if(@b){ msg('') } else { msg('') } }"));
	}
	
	@Test public void testInnerIfWithOtherStatements2() throws Exception{
		assertEquals("ifelse(@a,sconcat(ifelse(@b,msg('')),msg('')))", optimize("if(@a){ if(@b){ msg('') } msg('') }"));
	}
	
	@Test public void testInnerIfWithExistingAnd() throws Exception{
		assertEquals("ifelse(and(@a,@b,@c),msg(''))", optimize("if(@a && @b){ if(@c){ msg('') } }"));
	}
	
	@Test public void testForWithPostfix() throws Exception{
		assertEquals("for(assign(@i,0),lt(@i,5),inc(@i),msg(''))", optimize("for(@i = 0, @i < 5, @i++, msg(''))"));
		assertEquals("for(assign(@i,0),lt(@i,5),dec(@i),msg(''))", optimize("for(@i = 0, @i < 5, @i--, msg(''))"));
	}
	
	@Test public void testIfelseWithInnerDynamic() throws Exception{
		assertEquals("ifelse(dyn(),msg('success'))", optimize("ifelse(1, if(dyn(), msg('success')),msg('fail'))"));
	}
	
	@Test public void testAndOrPullsUp() throws Exception{
		assertEquals("or(dyn(),dyn(),dyn())", optimize("dyn() || dyn() || dyn()"));
		assertEquals("and(dyn(),dyn(),dyn())", optimize("dyn() && dyn() && dyn()"));
	}
	
	@Test public void testAndRemovesTrues() throws Exception{
		assertEquals("and(dyn(),dyn())", optimize("and(true, dyn(), true, dyn())"));
		assertEquals("true", optimize("and(true, true, true)"));
	}
	
	@Test public void testOrRemovesFalses() throws Exception{
		assertEquals("or(dyn(),dyn())", optimize("or(false, dyn(), false, dyn())"));
		assertEquals("false", optimize("or(false, false, false)"));
	}
	
	@Test public void testNoOperationIf() throws Exception {
		assertEquals("g(dyn(1))", optimize("if(dyn(1)){ }"));
	}
	
	//This won't work as easily, because the reg_count has already been evaluated
//	@Test public void testNoSideEffectsRemovesUnusedBranches1() throws Exception {
//		assertEquals("msg('hi')", optimize("if(reg_count('hi', 'hi')){ } msg('hi')"));
//	}
	
	@Test public void testNoSideEffectsRemovesUnusedBranches2() throws Exception {
		assertEquals("msg('hi')", optimize("if(reg_count('hi', dyn('hi'))){ } msg('hi')"));
	}
	
	//tests the new switch syntax
	@Test public void testSwitch1() throws Exception {
		assertEquals("switch(@a,{1, 2},msg('1, 2'),{3..4},sconcat(msg('3'),msg('4')),msg('default'))", 
				optimize("switch(@a){"
						+ "	case 1:"
						+ "	case 2:"
						+ "		msg('1, 2');"
						+ "	case 3..4:"
						+ "		msg('3');"
						+ "		msg('4');"
						+ "	case 'ignored':"
						+ "	default:"
						+ "		msg('default');"
						+ "}"));
	}
	@Test(expected = ConfigCompileException.class)
	public void testSwitch2() throws Exception {
		optimize("switch(@a){"
				+ "	case 1:"
				+ "	case 0..2:"
				+ "		msg('invalid');"
				+ "}");
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
