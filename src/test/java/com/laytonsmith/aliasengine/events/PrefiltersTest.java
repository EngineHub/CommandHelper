/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.aliasengine.events;

import com.laytonsmith.aliasengine.Constructs.Construct;
import com.laytonsmith.aliasengine.events.Prefilters.PrefilterType;
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
    public void testMatch() throws Exception {
        Map<String, Construct> map = new HashMap<String, Construct>();
        map.put("x", C.String("/1|2|3/"));
        
    }
}
