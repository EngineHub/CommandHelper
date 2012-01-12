/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.aliasengine.events;

import com.laytonsmith.puls3.core.events.Prefilters;
import com.laytonsmith.puls3.core.constructs.Construct;
import com.laytonsmith.puls3.core.events.Prefilters.PrefilterType;
import com.laytonsmith.puls3.core.exceptions.PrefilterNonMatchException;
import com.laytonsmith.testing.C;
import java.util.HashMap;
import java.util.Map;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author layton
 */
public class PrefiltersTest {

    public PrefiltersTest() {
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

    /**
     * Test of match method, of class Prefilters.
     */
    @Test
    public void testRegexMatch() {
        Map<String, Construct> map = new HashMap<String, Construct>();
        map.put("x", C.String("/1|2|3/"));
        try {
            Prefilters.match(map, "x", C.Int(2), PrefilterType.REGEX);
        } catch (PrefilterNonMatchException e) {
            fail("Expected a match here");
        }
        try {
            Prefilters.match(map, "x", C.Int(4), PrefilterType.REGEX);
            fail("Did not expect a match here");
        } catch (PrefilterNonMatchException e) {
        }
    }

    @Test
    public void testItemMatch() {
        Map<String, Construct> map = new HashMap<String, Construct>();
        map.put("x", C.String("35:2"));
        try {
            Prefilters.match(map, "x", "35:4", PrefilterType.ITEM_MATCH);
        } catch (PrefilterNonMatchException e) {
            fail("Expected a match here");
        }
        try {
            Prefilters.match(map, "x", "35", PrefilterType.ITEM_MATCH);
        } catch (PrefilterNonMatchException e) {
            fail("Expected a match here");
        }
        try {
            Prefilters.match(map, "x", "36:2", PrefilterType.ITEM_MATCH);
            fail("Did not expect a match here");
        } catch (PrefilterNonMatchException e) {
        }

    }

    @Test
    public void testStringMatch() {
        Map<String, Construct> map = new HashMap<String, Construct>();
        map.put("x", C.String("test"));
        try {
            Prefilters.match(map, "x", "test", PrefilterType.STRING_MATCH);
        } catch (PrefilterNonMatchException e) {
            fail("Expected a match here");
        }
        try {
            Prefilters.match(map, "x", "nope", PrefilterType.STRING_MATCH);
            fail("Did not expect a match here");
        } catch (PrefilterNonMatchException e) {
        }
    }
    
    @Test public void testMathMatch(){
        Map<String, Construct> map = new HashMap<String, Construct>();
        map.put("x", C.String("2"));
        try {
            Prefilters.match(map, "x", "2.0", PrefilterType.MATH_MATCH);
        } catch (PrefilterNonMatchException e) {
            fail("Expected a match here");
        }
        try {
            Prefilters.match(map, "x", "2.00001", PrefilterType.MATH_MATCH);
            fail("Did not expect a match here");
        } catch (PrefilterNonMatchException e) {
        }
    }
    
    @Test public void testExpressionMatch(){
        Map<String, Construct> map = new HashMap<String, Construct>();
        map.put("x", C.String("(x > 4)"));
        try {
            Prefilters.match(map, "x", "5", PrefilterType.EXPRESSION);
        } catch (PrefilterNonMatchException e) {
            fail("Expected a match here");
        }
        try {
            Prefilters.match(map, "x", "4", PrefilterType.EXPRESSION);
            fail("Did not expect a match here");
        } catch (PrefilterNonMatchException e) {
        }
        
        map.put("x", C.String("(x == 5)"));
        try {
            Prefilters.match(map, "x", "5", PrefilterType.EXPRESSION);
        } catch (PrefilterNonMatchException e) {
            fail("Expected a match here");
        }
        try {
            Prefilters.match(map, "x", "4", PrefilterType.EXPRESSION);
            fail("Did not expect a match here");
        } catch (PrefilterNonMatchException e) {
        }
        
        map.put("x", C.String("(2 + 3)"));
        try {
            Prefilters.match(map, "x", "5", PrefilterType.EXPRESSION);
        } catch (PrefilterNonMatchException e) {
            fail("Expected a match here");
        }
        try {
            Prefilters.match(map, "x", "4", PrefilterType.EXPRESSION);
            fail("Did not expect a match here");
        } catch (PrefilterNonMatchException e) {
        }
    }
}
