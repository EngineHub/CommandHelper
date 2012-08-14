package com.laytonsmith.abstraction.bukkit.events.drivers;

import com.laytonsmith.abstraction.bukkit.BukkitMCPlayer;
import com.laytonsmith.abstraction.bukkit.events.BukkitPlayerEvents;
import com.laytonsmith.commandhelper.CommandHelperPlugin;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.events.BoundEvent;
import com.laytonsmith.core.events.Driver;
import com.laytonsmith.core.events.Event;
import com.laytonsmith.core.events.EventList;
import com.laytonsmith.core.events.EventUtils;
import com.laytonsmith.core.exceptions.PrefilterNonMatchException;
import com.laytonsmith.core.functions.EventBinding;
import com.laytonsmith.core.functions.Function;
import java.util.SortedSet;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
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
			if (event.isAsynchronous()) {
				//We have to do the full processing on the main server thread, and
				//block on it as well, so if we cancel it or something, the change
				//will actually take effect. The easiest way to do this is to cancel the
				//chat event, then re-run it on the main server thread. Since we're
				//registering on lowest, this will hopefully not cause any problems,
				//but if it does, tough. Barring play-dirty mode, there's not a whole
				//lot that can be done reasonably.
				
				SortedSet<BoundEvent> events = EventUtils.GetEvents(Driver.PLAYER_CHAT);
				Event driver = EventList.getEvent(Driver.PLAYER_CHAT, "player_chat");				
				//Unfortunately, due to priority issues, if any event is syncronous, all of them
				//have to be synchronous.
				boolean canBeAsync = true;
				boolean actuallyNeedToFire = false;
				//If all the events are asynchronous, we can just run it as is.
				for(BoundEvent b : events){
					//We can't just use isSync here, because cancel and modify event,
					//normally synchronous, aren't in this case, so we need to manually
					//check the full function list.
					for(Function f : b.getParseTree().getFunctions()){
						if(f instanceof EventBinding.cancel || f instanceof EventBinding.modify_event){
							continue;
						}
						if(f.runAsync() != null && f.runAsync() == false){
							//Nope, can't be run async :(
							canBeAsync = false;
						}						
					}
					try {
						if(driver.matches(b.getPrefilter(), new BukkitPlayerEvents.BukkitMCPlayerChatEvent(event))){
							//Yeah, we need to fire it, so we have to continue
							actuallyNeedToFire = true;
						}
					} catch (PrefilterNonMatchException ex) {
						//No need to fire this one
					}
				}
				
				if(!actuallyNeedToFire){
					//Yay! Prefilters finally actually optimized something!
					return;
				}
				
				if(canBeAsync){
					//Fire away!
					fireChat(event);
				} else {
					final AsyncPlayerChatEvent copy = new AsyncPlayerChatEvent(false, event.getPlayer(), event.getMessage(), event.getRecipients());
					event.setCancelled(true);
					Bukkit.getServer().getScheduler().callSyncMethod(CommandHelperPlugin.self, new Callable() {
						public Object call() throws Exception {
							Bukkit.getServer().getPluginManager().callEvent(copy);
							return null;
						}
					});
				}

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
