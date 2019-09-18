package com.laytonsmith.core;

import com.laytonsmith.core.compiler.OptimizationUtilities;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import com.laytonsmith.testing.StaticTest;
import java.io.File;
import java.util.Set;
import static org.junit.Assert.assertEquals;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * This class tests optimizations by looking at the tree after optimization occurs to see if it matches expectation.
 *
 * This is also used to test the lexer/compiler at a low level
 */
public class OptimizationTest {

	static Set<Class<? extends Environment.EnvironmentImpl>> envs = Environment.getDefaultEnvClasses();

	@BeforeClass
	public static void setUpClass() {
		StaticTest.InstallFakeServerFrontend();
	}

	public String optimize(String script) throws Exception {
		return OptimizationUtilities.optimize(script, null, envs, null);
	}

	@Test
	public void testTestFramework() throws Exception {
		//This just tests to see that the basic framework works. This shouldn't optimize.
		assertEquals("msg('this is a string','so is this')", optimize("msg(\n 'this is a string',\nso is this\n)"));
		assertEquals("msg('\\'quoted\\'')", optimize("msg( '\\'quoted\\'' )"));
	}

	@Test
	public void testIfBasic() throws Exception {
		assertEquals("msg('hi')", optimize("if(true){ msg('hi') } else { msg('fail') }"));
	}

	@Test
	public void testIfWithBraces() throws Exception {
		assertEquals("if(dyn(),msg('hi'),msg('hi'))", optimize("if(dyn()){ msg('hi') } else { msg('hi') }"));
	}

	@Test
	public void testIfElse() throws Exception {
		assertEquals("ifelse(dyn(1),msg(''),dyn(2),msg(''),msg(''))", optimize("if(dyn(1)){ msg('') } else if(dyn(2)){ msg('') } else { msg('') }"));
	}

	@Test
	public void testIfElseWithDie() throws Exception {
		assertEquals("ifelse(is_null($pl),die(''),not(dyn($pl)),die(concat($pl,'')))",
				optimize("if(is_null($pl)) {\ndie('') } else if(!dyn($pl)){ die($pl.'') }"));
	}

	// Need to add this back too
//	@Test public void testNestedIfsWithRemoval() throws Exception {
//		assertEquals("p()", optimize("ifelse(1, if(0, msg('')), msg(''))"));
//	}
	@Test
	public void testMultipleLinesInBraces() throws Exception {
		assertEquals("if(dyn(false),msg('nope'),sconcat(msg('hi'),msg('hi')))", optimize("if(dyn(false)){\n"
				+ "msg('nope')\n"
				+ "} else {\n"
				+ " msg('hi')\n"
				+ " msg('hi')\n"
				+ "}"));
	}

	//TODO: These tests are not intended to be corrected in master, so I'm removing them for now
//	@Test public void testProcOptimization1() throws Exception{
//		//The proc stays there, but the call to it should be consolidated
//		assertEquals("sconcat(proc('_add',@a,@b,return(add(@a,@b))),4)", optimize("proc(_add, @a, @b, return(@a + @b)) _add(2, 2)"));
//	}
	@Test
	public void testProcOptimizationRecursion() throws Exception {
		assertEquals("sconcat(proc('_loop',@a,if(gt(@a,0),_loop(subtract(@a,1)),return(@a))),_loop(2))",
				optimize("proc(_loop, @a, if(@a > 0, _loop(@a - 1), return(@a))) _loop(2)"));
	}

//	@Test(expected=ConfigCompileException.class)
//	public void testProcOptimization2() throws Exception{
//		optimize("proc(_divide, @a, return(@a / 0)) _divide(1)");
//	}
	@Test
	public void testProcOptimization3() throws Exception {
		//Rather, lack of optimization
		assertEquals("sconcat(proc('_nope',msg('Hi')),_nope())", optimize("proc(_nope, msg('Hi')) _nope()"));
	}

//	@Test
//	public void testProcOptimiztion4() throws Exception{
//		//Test embedded procs
//		assertEquals("sconcat(proc('_outer',sconcat(proc('_inner',@a,return(@a)),'blah')),_inner('huh'))",
//				optimize("proc(_outer, proc(_inner, @a, return(@a)) _inner('blah')) _inner('huh')"));
//	}
	@Test
	public void testProcReturn() throws Exception {
		assertEquals("sconcat(proc('_proc',return(array(1))),array_get(_proc(),0))",
				optimize("proc(_proc, return(array(1))) _proc()[0]"));
	}

	@Test
	public void testClosure() throws Exception {
		assertEquals("sconcat(assign(@c,closure(@target,msg(concat('Hello ',@target,'!')))),@c('world'))",
				optimize("@c = closure(@target) {msg('Hello '.@target.'!')}; @c('world');"));
	}

	@Test
	public void testUnreachableCode() throws Exception {
		assertEquals("if(dyn(0),sconcat(die()),sconcat(msg('2'),msg('3')))",
				optimize("if(dyn(0)){ die() msg('1') } else { msg('2') msg('3') }"));
		assertEquals("sconcat(die())", optimize("if(true){ die() msg('1') } else { msg('2') msg('3') }"));
	}

	@Test
	public void testUnreachableCodeWithBranchTypeFunction() throws Exception {
		assertEquals("if(@var,die(),msg(''))", optimize("if(@var){ die() } else { msg('') }"));
		assertEquals("sconcat(while(lt(rand(),0.5),die()),msg('survived'))", optimize("while(rand() < 0.5) { die(); } msg('survived');"));
	}

	@Test
	public void testUnreachableCodeComplex() throws Exception {
		assertEquals("sconcat(assign(@a,closure(return(5))),execute(@a))",
				optimize("@a = closure(){"
						+ "return(5);"
						+ "}"
						+ "execute(@a);"));
		assertEquals("sconcat(msg('a'),if(dyn(1),ifelse(dyn(1),sconcat(die()),dyn(2),sconcat(die()),sconcat(die())),msg('b')))",
				optimize("msg('a');"
						+ "if(dyn(1)){"
						+ "	if(dyn(1)){"
						+ "		die();"
						+ "		msg('bad');"
						+ "	} else if(dyn(2)){"
						+ "		die();"
						+ "		msg('bad');"
						+ "	} else {"
						+ "		die();"
						+ "		msg('bad');"
						+ "	}"
						+ "} else {"
						+ "	msg('b');"
						+ "}"));
		assertEquals("sconcat(msg('a'),die())",
				optimize("msg('a');"
						+ "die();"
						+ "if(dyn(1)){"
						+ "	if(dyn(1)){"
						+ "		die();"
						+ "		msg('bad');"
						+ "	} else if(dyn(2)){"
						+ "		die();"
						+ "		msg('bad');"
						+ "	} else {"
						+ "		die();"
						+ "		msg('bad');"
						+ "	}"
						+ "} else {"
						+ "	msg('bad');"
						+ "}"));
	}

	@Test
	public void testInnerDie() throws Exception {
		// Since p is not a branch function, we expect a die inside of that to bubble up
		assertEquals("sconcat(p(concat(die())))",
				optimize("p(concat(die(), msg('bad'))); msg('bad');"));
	}

	@Test
	public void testRegSplitOptimization1() throws Exception {
		assertEquals("split('pattern',dyn('subject'))", optimize("reg_split('pattern', dyn('subject'))"));
	}

	@Test
	public void testRegSplitOptimization2() throws Exception {
		assertEquals("split('.',dyn('subject'))", optimize("reg_split(reg_escape('.'), dyn('subject'))"));
	}

	@Test
	public void testRegReplaceOptimization1() throws Exception {
		assertEquals("replace('this is a thing','thing',dyn('hi'))", optimize("reg_replace('thing', dyn('hi'), 'this is a thing')"));
	}

	@Test
	public void testTrivialAssignmentWithEqualsSymbol() throws Exception {
		assertEquals("assign(@a,1)", optimize("@a = 1"));
	}

	@Test
	public void testAssignWithEqualsSymbol() throws Exception {
		assertEquals("sconcat(assign(@var,'ab'),'c')", optimize("@var = 'a'.'b' 'c'"));
	}

	@Test
	public void testAssignWithOperators() throws Exception {
		assertEquals("assign(@one,add(@one,1))", optimize("@one += 1"));
		assertEquals("assign(@one,subtract(@one,1))", optimize("@one -= 1"));
		assertEquals("assign(@one,multiply(@one,1))", optimize("@one *= 1"));
		assertEquals("assign(@one,divide(@one,1))", optimize("@one /= 1"));
		assertEquals("assign(@one,concat(@one,1))", optimize("@one .= 1"));
	}

	@Test
	public void testMultiAssign() throws Exception {
		assertEquals("assign(@one,assign(@two,''))", optimize("@one = @two = ''"));
		assertEquals("sconcat(assign(@one,assign(@two,'')),'blah')", optimize("@one = @two = '' 'blah'"));
	}

	@Test
	public void testAssignWithOr() throws Exception {
		assertEquals("assign(@one,or(@two,not(@three)))",
				optimize("@one = @two || !@three"));
	}

	@Test
	public void testAssignWithInc() throws Exception {
		assertEquals("assign(@one,or(inc(@two),inc(@three)))",
				optimize("@one = ++@two || ++@three"));
	}

	@Test
	public void testAssignWithEquals() throws Exception {
		assertEquals("assign(@one,and(@two,equals(postinc(@three),neg(@four))))",
				optimize("@one = @two && @three++ == -@four"));
	}

	@Test
	public void testAssignWithComplexSymbols() throws Exception {
		assertEquals("assign(@one,or(postinc(@one),add(inc(@two),@three)))",
				optimize("@one = @one++ || ++@two + @three"));
	}

	@Test
	public void testMultipleAdjacentAssignment() throws Exception {
		assertEquals("sconcat(assign(@one,inc(@two)),assign(ms.lang.int,@three,0),assign(@four,'test'))",
				optimize("@one = ++@two; int @three = 0; @four = 'test';"));
	}

	@Test
	public void testAdditiveAssignmentWithInc() throws Exception {
		assertEquals("assign(@one,add(@one,subtract(inc(@two),inc(@three))))",
				optimize("@one += ++@two - ++@three"));
	}

	@Test
	public void testAssignmentMixedWithAddition1() throws Exception {
		assertEquals("add(1,assign(@a,1))", optimize("1 + @a = 1"));
	}

	@Test
	public void testAssignmentMixedWithAddition2() throws Exception {
		assertEquals("add(1,assign(@a,add(@b,2)))", optimize("1 + @a = @b + 2"));
	}

	@Test
	public void testAssignmentMixedWithAddition3() throws Exception {
		assertEquals("add(1,assign(@a,add(@b,@c,2)))", optimize("1 + @a = @b + @c + 2"));
	}

	@Test
	public void testAssignmentMixedWithAddition4() throws Exception {
		assertEquals("add(1,assign(@a,add(@a,@b,@c,2)))", optimize("1 + @a += @b + @c + 2"));
	}

	@Test
	public void testAssignmentMixedWithAddition5() throws Exception {
		assertEquals("add(1,assign(@_,assign(@a,add(@a,@b,@c,2))))", optimize("1 + @_ = @a += @b + @c + 2"));
	}

	@Test
	public void testAssignmentMixedWithAddition6() throws Exception {
		assertEquals("sconcat(add(1,assign(@_,assign(@a,add(@a,@b,@c,2)))),'blah')", optimize("1 + @_ = @a += @b + @c + 2 'blah'"));
	}

	@Test
	public void testAssignmentMixedWithAddition7() throws Exception {
		assertEquals("add(@one,assign(@one,inc(@two)),@three)",
				optimize("@one + (@one = ++@two) + @three"));
	}

	@Test
	public void testInnerIfAnded() throws Exception {
		assertEquals("if(and(@a,@b),msg(''))", optimize("if(@a){ if(@b){ msg('') } }"));
	}

	@Test
	public void testInnerIfWithOtherStatements1() throws Exception {
		assertEquals("if(@a,if(@b,msg(''),msg('')))", optimize("if(@a){ if(@b){ msg('') } else { msg('') } }"));
	}

	@Test
	public void testInnerIfWithOtherStatements2() throws Exception {
		assertEquals("if(@a,sconcat(if(@b,msg('')),msg('')))", optimize("if(@a){ if(@b){ msg('') } msg('') }"));
	}

	@Test
	public void testInnerIfWithExistingAnd() throws Exception {
		assertEquals("if(and(@a,@b,@c),msg(''))", optimize("if(@a && @b){ if(@c){ msg('') } }"));
	}

	@Test
	public void testForWithPostfix() throws Exception {
		assertEquals("for(assign(@i,0),lt(@i,5),inc(@i),msg(''))", optimize("for(@i = 0, @i < 5, @i++, msg(''))"));
		assertEquals("for(assign(@i,0),lt(@i,5),dec(@i),msg(''))", optimize("for(@i = 0, @i < 5, @i--, msg(''))"));
	}

	// Need to add this back too
//	@Test public void testIfelseWithInnerDynamic() throws Exception{
//		assertEquals("if(dyn(),msg('success'))", optimize("ifelse(1, if(dyn(), msg('success')),msg('fail'))"));
//	}
	@Test
	public void testAndOrPullsUp() throws Exception {
		assertEquals("or(dyn(),dyn(),dyn())", optimize("dyn() || dyn() || dyn()"));
		assertEquals("and(dyn(),dyn(),dyn())", optimize("dyn() && dyn() && dyn()"));
	}

	@Test
	public void testAndRemovesTrues() throws Exception {
		assertEquals("and(dyn(),dyn())", optimize("and(true, dyn(), true, dyn())"));
		assertEquals("true", optimize("and(true, true, true)"));
	}

	@Test
	public void testOrRemovesFalses() throws Exception {
		assertEquals("or(dyn(),dyn())", optimize("or(false, dyn(), false, dyn())"));
		assertEquals("false", optimize("or(false, false, false)"));
	}

	// This will be a nice optimization to add back soon
//	@Test public void testNoOperationIf() throws Exception {
//		assertEquals("g(dyn(1))", optimize("if(dyn(1)){ }"));
//	}
	//This won't work as easily, because the reg_count has already been evaluated
//	@Test public void testNoSideEffectsRemovesUnusedBranches1() throws Exception {
//		assertEquals("msg('hi')", optimize("if(reg_count('hi', 'hi')){ } msg('hi')"));
//	}
	// This is actually invalid, because the dyn has side effects (or rather, isn't registered to not
	// have them). ALL nodes in the condition tree must have no side effects for this to be valid. For
	// now, this isn't implemented anyways.
//	@Test public void testNoSideEffectsRemovesUnusedBranches2() throws Exception {
//		assertEquals("msg('hi')", optimize("if(reg_count('hi', dyn('hi'))){ } msg('hi')"));
//	}
	//tests the new switch syntax
	@Test
	public void testSwitch1() throws Exception {
		assertEquals("switch(@a,array(1,2),msg('1, 2'),"
				+ "array(3..4),sconcat(msg('3'),msg('4')),"
				+ "array(false),msg('false'),"
				+ "array(0.07),msg(0.07),"
				+ "msg('default'))",
				optimize("switch(@a){"
						+ "	case 1:"
						+ "	case 2:"
						+ "		msg('1, 2');"
						+ "	case 3..4:"
						+ "		msg('3');"
						+ "		msg('4');"
						+ "	case false:"
						+ "		msg('false');"
						+ "	case 00.07:"
						+ "		msg(00.07);"
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

	@Test
	public void testEmptySwitch() throws Exception {
		assertEquals("switch(dyn(1))", optimize("switch(dyn(1)){ case 1: case 2: default: }"));
	}

	// Tests "-" signs in front of values to negate them.
	@Test
	public void testMinusWithoutValueInFront() throws Exception {
		assertEquals("assign(@b,neg(@a))", optimize("@b = -@a"));
		assertEquals("assign(@b,neg(@a))", optimize("@b = - @a"));

		assertEquals("assign(@b,array(neg(@a)))", optimize("@b = array(-@a)"));
		assertEquals("assign(@b,array(neg(@a)))", optimize("@b = array(- @a)"));

		assertEquals("assign(@b,neg(array_get(@a,1)))", optimize("@b = -@a[1]"));
		assertEquals("assign(@b,neg(array_get(@a,1)))", optimize("@b = - @a[1]"));

		assertEquals("assign(@b,neg(dec(@a)))", optimize("@b = -dec(@a)"));
		assertEquals("assign(@b,neg(dec(@a)))", optimize("@b = - dec(@a)"));

		assertEquals("assign(@b,neg(array_get(array(1,2,3),1)))", optimize("@b = -array(1,2,3)[1]"));

		assertEquals("assign(@b,neg(array_get(array_get(array_get(array(array(array(2))),0),0),0)))", optimize("@b = -array(array(array(2)))[0][0][0]"));

		assertEquals("assign(@b,neg(array_get(array_get(array_get(array(array(array(2))),neg(array_get(array(1,0),1))),0),0)))",
				optimize("@b = -array(array(array(2)))[-array(1,0)[1]][0][0]"));

		// Test behaviour where the value should not be negated.
		assertEquals("assign(@c,subtract(@a,@b))", optimize("@c = @a - @b"));
		assertEquals("assign(@c,subtract(array_get(@a,0),@b))", optimize("@c = @a[0] - @b"));
		assertEquals("assign(@c,subtract(abs(@a),@b))", optimize("@c = abs(@a) - @b"));
		assertEquals("assign(@c,subtract(if(@bool,2,3),@b))", optimize("@c = if(@bool) {2} else {3} - @b"));

		assertEquals("assign(@b,subtract(dec(@a),2))", optimize("@b = dec(@a)-2"));
		assertEquals("assign(@b,subtract(dec(@a),2))", optimize("@b = dec(@a)- 2"));
	}

	//TODO: This is a bit ambitious for now, put this back at some point, and then make it pass.
//	@Test public void testAssign() throws Exception{
//		//In this test, there's no way it won't ever be 'hi', so do a replacement (we still need to keep
//		//the assign, because it does need to go into the variable table for reflective purposes)
//		assertEquals("sconcat(assign(@a,'hi'),msg('hi'))", optimize("assign(@a, 'hi') msg(@a)"));
//		//In this case, the first use may be hardcoded, but after the if, it may have changed, so we
//		//can no longer assume it's always going to be 'hi'
//		assertEquals("sconcat(assign(@a,'hi'),msg('hi'),if(dyn(),assign(@a,'bye')),msg(@a))",
//				optimize(""
//				+ "assign(@a, 'hi')"
//				+ "msg(@a)"
//				+ "if(dyn(), assign(@a, 'bye'))"
//				+ "msg(@a)"));
//		//In this case, we have a worthless assignment; We know @a is already 'hi' and it's always going
//		//to be that, and we're trying to assign 'hi' again, so we can completely remove this from
//		//the code, at which point the last msg can be optimized.
//		assertEquals("sconcat(assign(@a,'hi'),msg('hi'),if(dyn(),null),msg('hi'))",
//				optimize(""
//				+ "assign(@a, 'hi')"
//				+ "msg(@a)"
//				+ "if(dyn(), assign(@a, 'hi'))"
//				+ "msg(@a)"));
//	}
	@Test
	public void testNotinstanceofKeyword() throws Exception {
		assertEquals("msg(not(instanceof(dyn(2),ms.lang.int)))", optimize("msg(dyn(2) notinstanceof int);"));
	}

	@Test
	public void testDor() throws Exception {
		assertEquals("dor(dyn(''),dyn('a'))", optimize("dyn('') ||| dyn('a')"));
		assertEquals("dor(dyn(''),dyn('a'),dyn('b'))", optimize("dyn('') ||| dyn('a') ||| dyn('b')"));
	}

	@Test
	public void testDand() throws Exception {
		assertEquals("dand(dyn(''),dyn('a'))", optimize("dyn('') &&& dyn('a')"));
		assertEquals("dand(dyn(''),dyn('a'),dyn('b'))", optimize("dyn('') &&& dyn('a') &&& dyn('b')"));
	}

//	@Test
//	public void testDorOptimization() throws Exception {
//		assertEquals("'a'", optimize("dor(false, false, 'a')"));
//	}

	@Test
	public void testDandOptimization() throws Exception {
		assertEquals("''", optimize("dand(true, true, '')"));
	}

	@Test
	public void testCommentBlock() throws Exception {
		assertEquals(2, MethodScriptCompiler.lex("/*/ still a comment -()*/", null, new File("OptimizationTest"), true, true).size());
	}

	@Test
	public void testSwitchIc() throws Exception {
		assertEquals("switch_ic(to_lower(dyn('AsDf')),array('asdf'),msg('hello'),array('fdsa'),msg('nope'))",
				optimize("switch_ic(dyn('AsDf')) { case 'aSdF': msg('hello'); case 'fdsa': msg('nope'); }"));
	}

	@Test
	public void testNotNot() throws Exception {
		assertEquals("@value", optimize("!!@value"));
		assertEquals("not(@value)", optimize("!@value"));
		// !!!!@value (or more than 2 !!) is broken in the compiler -.-
	}
}
