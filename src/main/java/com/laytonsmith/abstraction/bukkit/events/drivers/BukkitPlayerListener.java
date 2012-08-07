package com.laytonsmith.abstraction.bukkit.events.drivers;

import com.laytonsmith.abstraction.bukkit.BukkitMCPlayer;
import com.laytonsmith.abstraction.bukkit.events.BukkitPlayerEvents;
import com.laytonsmith.commandhelper.CommandHelperPlugin;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.events.Driver;
import com.laytonsmith.core.events.EventUtils;
import java.util.concurrent.Callable;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.*;

/**
 *
 * @author Layton
 */
public class BukkitPlayerListener implements Listener {

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerLogin(PlayerLoginEvent e) {
        EventUtils.TriggerListener(Driver.PLAYER_LOGIN, "player_login", new BukkitPlayerEvents.BukkitMCPlayerLoginEvent(e));
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerPreLogin(PlayerPreLoginEvent e) {
        EventUtils.TriggerListener(Driver.PLAYER_PRELOGIN, "player_prelogin", new BukkitPlayerEvents.BukkitMCPlayerPreLoginEvent(e));
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerJoin(PlayerJoinEvent e) {
        EventUtils.TriggerListener(Driver.PLAYER_JOIN, "player_join", new BukkitPlayerEvents.BukkitMCPlayerJoinEvent(e));
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerInteract(PlayerInteractEvent e) {
        EventUtils.TriggerListener(Driver.PLAYER_INTERACT, "player_interact", new BukkitPlayerEvents.BukkitMCPlayerInteractEvent(e));
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        EventUtils.TriggerListener(Driver.PLAYER_SPAWN, "player_spawn", new BukkitPlayerEvents.BukkitMCPlayerRespawnEvent(event));
    }
    
    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerChat(final AsyncPlayerChatEvent event) {
        if (EventUtils.GetEvents(Driver.PLAYER_CHAT) != null
                && !EventUtils.GetEvents(Driver.PLAYER_CHAT).isEmpty()) {
            //TODO: If the parse tree is fully async, we can do it on the async thread
            //anyways.
            if (event.isAsynchronous()) {
                //We have to do the full processing on the main server thread, and
                //block on it as well, so if we cancel it or something, the change
                //will actually take effect. The easiest way to do this is to cancel the
                //chat event, then re-run it on the main server thread. Since we're
                //registering on lowest, this will hopefully not cause any problems,
                //but if it does, tough. Barring play-dirty mode, there's not a whole
                //lot that can be done reasonably.
                
                //TODO: If none of the prefilters match, we can also not re-fire here.
                //However, we have to check to make sure all the prefilters can be
                //run async, so we can't implement this until we implement the code above
                //too.
                final AsyncPlayerChatEvent copy = new AsyncPlayerChatEvent(false, event.getPlayer(), event.getMessage(), event.getRecipients());
                event.setCancelled(true);
                Bukkit.getServer().getScheduler().callSyncMethod(CommandHelperPlugin.self, new Callable() {
                    public Object call() throws Exception {
                        Bukkit.getServer().getPluginManager().callEvent(copy);
                        return null;
                    }
                });                

            } else {
                fireChat(event);
            }
        }
    }

    private void fireChat(AsyncPlayerChatEvent event) {
        EventUtils.TriggerListener(Driver.PLAYER_CHAT, "player_chat", new BukkitPlayerEvents.BukkitMCPlayerChatEvent(event));
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerQuit(PlayerQuitEvent event) {
        EventUtils.TriggerListener(Driver.PLAYER_QUIT, "player_quit", new BukkitPlayerEvents.BukkitMCPlayerQuitEvent(event));
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerChangedWorld(PlayerChangedWorldEvent event) {
        BukkitMCPlayer currentPlayer = (BukkitMCPlayer) Static.GetPlayer(event.getPlayer().getName(), Target.UNKNOWN);
        //Apparently this happens sometimes, so prevent it
        if (!event.getFrom().equals(currentPlayer._Player().getWorld())) {
            EventUtils.TriggerListener(Driver.WORLD_CHANGED, "world_changed", new BukkitPlayerEvents.BukkitMCWorldChangedEvent(event));
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        if (!event.getFrom().getWorld().equals(event.getTo().getWorld())) {
            EventUtils.TriggerListener(Driver.WORLD_CHANGED, "world_changed", new BukkitPlayerEvents.BukkitMCWorldChangedEvent(new PlayerChangedWorldEvent(event.getPlayer(), event.getFrom().getWorld())));
        }
    }
}
