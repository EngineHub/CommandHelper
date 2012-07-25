

package com.laytonsmith.core.functions;

import com.laytonsmith.core.MethodScriptCompiler;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import static com.laytonsmith.testing.StaticTest.SRun;
import org.junit.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 *
 * @author Layton
 */
public class RegexTest {

    public RegexTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @Test(timeout = 10000)
    public void testRegMatch() throws ConfigCompileException {
        assertEquals("{word}", SRun("reg_match('word', 'This is a word')", null));
        assertEquals("{}", SRun("reg_match('word', 'This is an airplane')", null));
        assertEquals("{word, word}", SRun("reg_match('(word)', 'This is a word')", null));
        assertEquals("{This is a word, word}", SRun("reg_match('This is a (word)', 'This is a word')", null));
        assertEquals("{WORD}", SRun("reg_match(array(word, i), 'THIS IS A WORD')", null));
        try {
            SRun("reg_match(array(word, l), hi)", null);
            fail();
        } catch (ConfigRuntimeException e) {
            //Pass
        }
    }

    @Test(timeout = 10000)
    public void testRegMatchAll() throws ConfigCompileException {
        assertEquals("{{This is a word, word}, {This is a word, word}}", SRun("reg_match_all('This is a (word)', 'word, This is a word, This is a word')", null));
        assertEquals("{}", SRun("reg_match_all('word', 'yay')", null));
    }

    @Test(timeout = 10000)
    public void testRegReplace() throws ConfigCompileException {
        assertEquals("word", SRun("reg_replace('This is a (word)', '$1', 'This is a word')", null));
        assertEquals("It's a wordy day!", SRun("reg_replace('sunn', 'word', 'It\\'s a sunny day!')", null));
    }

    @Test(timeout = 10000)
    public void testRegSplit() throws ConfigCompileException {
        assertEquals("{one, two, three}", SRun("reg_split('\\\\|', 'one|two|three')", null));
    }

    @Test(timeout = 10000)
    public void testRegCount() throws ConfigCompileException {
        assertEquals("3", SRun("reg_count('/', '///yay')", null));
        assertEquals("0", SRun("reg_count('poppycock', 'tiddly winks')", null));
    }
    
    //Here, it's a compile error, since we're using it statically
    @Test(expected=ConfigCompileException.class)
    public void testRegFailureStatic() throws ConfigCompileException{
        MethodScriptCompiler.compile(MethodScriptCompiler.lex("reg_match('(?i)asd(', 'irrelevant')", null));
    }
    
    //Here, it's a runtime error, since we're using it dynamically
    @Test(expected=ConfigRuntimeException.class)
    public void testRegFailureDynamic() throws ConfigCompileException{
        SRun("assign(@a, '(?i)asd(') reg_match(@a, 'irrelevant')", null);        
    }
}
