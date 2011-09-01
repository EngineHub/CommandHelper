/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.aliasengine.functions;

import com.laytonsmith.aliasengine.functions.exceptions.ConfigCompileException;
import com.laytonsmith.testing.StaticTest;
import org.junit.Test;
import org.bukkit.entity.Player;
import org.bukkit.Server;
import org.bukkit.command.ConsoleCommandSender;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import static org.mockito.Mockito.*;
import static com.laytonsmith.testing.StaticTest.*;
import static org.junit.Assert.*;

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
        fakePlayer = GetOnlinePlayer();
        fakeServer = GetFakeServer();
        when(fakePlayer.getServer()).thenReturn(fakeServer);
    }
    
    @After
    public void tearDown() {
    }

    @Test public void testPlayer() throws ConfigCompileException{
        String script = "msg(player())";
        Run(script, fakePlayer);
        verify(fakePlayer).sendMessage(fakePlayer.getName());
    }
    
    @Test public void testPlayer2() throws ConfigCompileException{
        String script = "msg(player())";
        ConsoleCommandSender c = GetFakeConsoleCommandSender();
        Run(script, c);
        verify(c).sendMessage("~console");
    }
}
