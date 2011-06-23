/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.aliasengine.functions;

import com.laytonsmith.aliasengine.Constructs.*;
import com.laytonsmith.testing.C;
import org.bukkit.entity.Player;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import static com.laytonsmith.testing.StaticTest.*;

/**
 *
 * @author Layton
 */
public class BasicLogicTest {
    
    Player fakePlayer;
    CArray commonArray;
    
    public BasicLogicTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }
    
    @Before
    public void setUp() {
        commonArray = C.Array(C.Null(), C.Int(1), C.String("2"), C.Double(3.0));
    }
    
    @After
    public void tearDown() {
    }

    @Test
    public void testDocs() {
        TestClassDocs(BasicLogic.docs(), BasicLogic.class);
    }
    
    @Test
    public void testEquals(){
        BasicLogic._equals e = new BasicLogic._equals();
        TestBoilerplate(e, "equals");
        
    }
}
