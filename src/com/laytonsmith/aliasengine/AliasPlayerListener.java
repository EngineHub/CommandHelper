package com.laytonsmith.aliasengine;

import java.util.HashMap;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerMoveEvent;

/**
 * Handle events for all Player related events
 * @author <yourname>
 */
public class AliasPlayerListener extends PlayerListener {

    private final Alias plugin;


    public AliasPlayerListener(Alias instance) {
        plugin = instance;
    }

    @Override
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        String command = event.getMessage();
        player.sendMessage("Cancelling command: " + command);
        event.setCancelled(true);
    }
    //Insert Player related code here
}
