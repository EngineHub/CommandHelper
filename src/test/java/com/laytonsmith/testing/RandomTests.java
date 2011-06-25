/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.testing;

import com.laytonsmith.aliasengine.Constructs.Construct;
import org.junit.Test;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static com.laytonsmith.testing.StaticTest.*;
import static org.junit.Assert.*;

/**
 *
 * @author Layton
 */
public class RandomTests {
    @Test public static void testConstuctToString(){
        System.out.println("Construct.toString");
        assertEquals("hello", new Construct("hello", Construct.ConstructType.STRING, 0).toString());
    }
}
