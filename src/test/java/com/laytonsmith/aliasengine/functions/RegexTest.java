/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.aliasengine.functions;

import com.laytonsmith.aliasengine.functions.exceptions.ConfigCompileException;
import com.laytonsmith.aliasengine.functions.exceptions.ConfigRuntimeException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static com.laytonsmith.testing.StaticTest.*;

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

    @Test public void testRegMatch() throws ConfigCompileException{
        assertEquals("{word}", SRun("reg_match('word', 'This is a word')", null));
        assertEquals("{}", SRun("reg_match('word', 'This is an airplane')", null));
        assertEquals("{word, word}", SRun("reg_match('(word)', 'This is a word')", null));
        assertEquals("{This is a word, word}", SRun("reg_match('This is a (word)', 'This is a word')", null));
        assertEquals("{WORD}", SRun("reg_match(array(word, i), 'THIS IS A WORD')", null));
        try{
            SRun("reg_match(array(word, l), hi)", null);
            fail();
        } catch(ConfigRuntimeException e){
            //Pass
        }
    }
    
    @Test public void testRegMatchAll() throws ConfigCompileException{
        assertEquals("{{This is a word, word}, {This is a word, word}}", SRun("reg_match_all('This is a (word)', 'word, This is a word, This is a word')", null));
        assertEquals("{}", SRun("reg_match_all('word', 'yay')", null));
    }
    
    @Test public void testRegReplace() throws ConfigCompileException{
        assertEquals("word", SRun("reg_replace('This is a (word)', '$1', 'This is a word')", null));
        assertEquals("It's a wordy day!", SRun("reg_replace('sunn', 'word', 'It\\'s a sunny day!')", null));
    }
    
    @Test public void testRegSplit() throws ConfigCompileException{
        assertEquals("{one, two, three}", SRun("reg_split('\\\\|', 'one|two|three')", null));
    }
    
    @Test public void testRegCount() throws ConfigCompileException{
        assertEquals("3", SRun("reg_count('/', '///yay')", null));
        assertEquals("0", SRun("reg_count('poppycock', 'tiddly winks')", null));
    }
}
