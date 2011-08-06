/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.aliasengine.functions;

import org.bukkit.entity.Player;
import org.bukkit.Server;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import static org.mockito.Mockito.*;
import static com.laytonsmith.testing.StaticTest.*;

/**
 *
 * @author Layton
 */
public class PlayerManangementTest {

    Server fakeServer;
    Player fakePlayer;
    
    public PlayerManangementTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }
    
    @Before
    public void setUp() {
        fakePlayer = mock(Player.class);
        fakeServer = mock(Server.class);
        when(fakePlayer.getServer()).thenReturn(fakeServer);
    }
    
    @After
    public void tearDown() {
    }

    
}
