package com.laytonsmith.abstraction.bukkit.events.drivers;

import com.laytonsmith.abstraction.bukkit.BukkitMCPlayer;
import com.laytonsmith.abstraction.bukkit.events.BukkitPlayerEvents;
import com.laytonsmith.commandhelper.CommandHelperPlugin;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.events.Driver;
import com.laytonsmith.core.events.EventUtils;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
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
    public void onPlayerKick(PlayerKickEvent e) {
		BukkitPlayerEvents.BukkitMCPlayerKickEvent pke = new BukkitPlayerEvents.BukkitMCPlayerKickEvent(e);
        EventUtils.TriggerExternal(pke);
		EventUtils.TriggerListener(Driver.PLAYER_KICK, "player_kick", pke);
    }
	
	@EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerBedEnter(PlayerBedEnterEvent e) {
		BukkitPlayerEvents.BukkitMCPlayerBedEvent be = new BukkitPlayerEvents.BukkitMCPlayerBedEvent(e);
        EventUtils.TriggerExternal(be);
		EventUtils.TriggerListener(Driver.PLAYER_BED_EVENT, "player_enter_bed", be);
    }
	
	@EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerBedLeave(PlayerBedLeaveEvent e) {
		BukkitPlayerEvents.BukkitMCPlayerBedEvent be = new BukkitPlayerEvents.BukkitMCPlayerBedEvent(e);
        EventUtils.TriggerExternal(be);
		EventUtils.TriggerListener(Driver.PLAYER_BED_EVENT, "player_leave_bed", be);
    }
    
	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerLogin(PlayerLoginEvent e) {
		BukkitPlayerEvents.BukkitMCPlayerLoginEvent ple = new BukkitPlayerEvents.BukkitMCPlayerLoginEvent(e);
		EventUtils.TriggerExternal(ple);
		EventUtils.TriggerListener(Driver.PLAYER_LOGIN, "player_login", ple);
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerPreLogin(PlayerPreLoginEvent e) {
		BukkitPlayerEvents.BukkitMCPlayerPreLoginEvent pple = new BukkitPlayerEvents.BukkitMCPlayerPreLoginEvent(e);
		EventUtils.TriggerExternal(pple);
		EventUtils.TriggerListener(Driver.PLAYER_PRELOGIN, "player_prelogin", pple);
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerJoin(PlayerJoinEvent e) {
		BukkitPlayerEvents.BukkitMCPlayerJoinEvent pje = new BukkitPlayerEvents.BukkitMCPlayerJoinEvent(e);
		EventUtils.TriggerExternal(pje);
		EventUtils.TriggerListener(Driver.PLAYER_JOIN, "player_join", pje);
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerInteract(PlayerInteractEvent e) {
		BukkitPlayerEvents.BukkitMCPlayerInteractEvent pie = new BukkitPlayerEvents.BukkitMCPlayerInteractEvent(e);
		EventUtils.TriggerExternal(pie);
		EventUtils.TriggerListener(Driver.PLAYER_INTERACT, "player_interact", pie);
		EventUtils.TriggerListener(Driver.PLAYER_INTERACT, "pressure_plate_activated", pie);
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerRespawn(PlayerRespawnEvent event) {
		BukkitPlayerEvents.BukkitMCPlayerRespawnEvent pre = new BukkitPlayerEvents.BukkitMCPlayerRespawnEvent(event);
		EventUtils.TriggerExternal(pre);
		EventUtils.TriggerListener(Driver.PLAYER_SPAWN, "player_spawn", pre);
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled=true)
	public void onPlayerChat(final AsyncPlayerChatEvent event) {
		if(CommandHelperPlugin.self.interpreterListener
                .isInInterpreterMode(new BukkitMCPlayer(event.getPlayer()))){
            //They are in interpreter mode, so we want it to handle this, not everything else.
            return;
        }
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
				
//				SortedSet<BoundEvent> events = EventUtils.GetEvents(Driver.PLAYER_CHAT);
//				Event driver = EventList.getEvent(Driver.PLAYER_CHAT, "player_chat");				
//				//Unfortunately, due to priority issues, if any event is syncronous, all of them
//				//have to be synchronous.
//				boolean canBeAsync = true;
//				boolean actuallyNeedToFire = false;
//				//If all the events are asynchronous, we can just run it as is.
//				for(BoundEvent b : events){
//					//We can't just use isSync here, because cancel and modify event,
//					//normally synchronous, aren't in this case, so we need to manually
//					//check the full function list.
//					for(Function f : b.getParseTree().getFunctions()){
//						if(f instanceof EventBinding.cancel || f instanceof EventBinding.modify_event){
//							continue;
//						}
//						if(f.runAsync() != null && f.runAsync() == false){
//							//Nope, can't be run async :(
//							canBeAsync = false;
//						}						
//					}
//					try {
//						if(driver.matches(b.getPrefilter(), new BukkitPlayerEvents.BukkitMCPlayerChatEvent(event))){
//							//Yeah, we need to fire it, so we have to continue
//							actuallyNeedToFire = true;
//						}
//					} catch (PrefilterNonMatchException ex) {
//						//No need to fire this one
//					}
//				}
//				
//				if(!actuallyNeedToFire){
//					//Yay! Prefilters finally actually optimized something!
//					return;
//				}
				
				//Until there is a more reliable way to detect isConst() on a parse tree, (that supports procs)
				//this must always be synchronous.
				boolean canBeAsync = false;
				if(canBeAsync){
					//Fire away!
					fireChat(event);
				} else {
					final AsyncPlayerChatEvent copy = new AsyncPlayerChatEvent(false, event.getPlayer(), event.getMessage(), event.getRecipients());
					copy.setFormat(event.getFormat());
					//event.setCancelled(true);
					Future f = Bukkit.getServer().getScheduler().callSyncMethod(CommandHelperPlugin.self, new Callable() {
						public Object call() throws Exception {
							onPlayerChat(copy);
							return null;
						}
					});					
					while(true){
						try {
								f.get();
								break;
						} catch (InterruptedException ex) {
							//I don't know why this happens, but screw it, we're gonna try again, and it's gonna like it.
						} catch (ExecutionException ex) {
							Logger.getLogger(BukkitPlayerListener.class.getName()).log(Level.SEVERE, null, ex);
							break;
						}
					}
					event.setCancelled(copy.isCancelled());
					event.setMessage(copy.getMessage());
					event.setFormat(copy.getFormat());
				}

			} else {
				fireChat(event);
			}
		}
	}

	private void fireChat(AsyncPlayerChatEvent event) {
		BukkitPlayerEvents.BukkitMCPlayerChatEvent pce = new BukkitPlayerEvents.BukkitMCPlayerChatEvent(event);
		EventUtils.TriggerExternal(pce);
		EventUtils.TriggerListener(Driver.PLAYER_CHAT, "player_chat", pce);
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerQuit(PlayerQuitEvent event) {
		BukkitPlayerEvents.BukkitMCPlayerQuitEvent pqe = new BukkitPlayerEvents.BukkitMCPlayerQuitEvent(event);
		EventUtils.TriggerExternal(pqe);
		EventUtils.TriggerListener(Driver.PLAYER_QUIT, "player_quit", pqe);
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerChangedWorld(PlayerChangedWorldEvent event) {
		BukkitMCPlayer currentPlayer = (BukkitMCPlayer) Static.GetPlayer(event.getPlayer().getName(), Target.UNKNOWN);
		//Apparently this happens sometimes, so prevent it
		if (!event.getFrom().equals(currentPlayer._Player().getWorld())) {
			BukkitPlayerEvents.BukkitMCWorldChangedEvent wce = new BukkitPlayerEvents.BukkitMCWorldChangedEvent(event);
			EventUtils.TriggerExternal(wce);
			EventUtils.TriggerListener(Driver.WORLD_CHANGED, "world_changed", wce);
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerTeleport(PlayerTeleportEvent event) {
		if (event.getFrom().equals(event.getTo())) {
			return;
		}
		
		BukkitPlayerEvents.BukkitMCPlayerTeleportEvent pte = new BukkitPlayerEvents.BukkitMCPlayerTeleportEvent(event);
		EventUtils.TriggerExternal(pte);
		EventUtils.TriggerListener(Driver.PLAYER_TELEPORT, "player_teleport", pte);
	
		if (!event.getFrom().getWorld().equals(event.getTo().getWorld()) && !event.isCancelled()) {
			BukkitPlayerEvents.BukkitMCWorldChangedEvent wce = new BukkitPlayerEvents.BukkitMCWorldChangedEvent(new PlayerChangedWorldEvent(event.getPlayer(), event.getFrom().getWorld()));
			EventUtils.TriggerExternal(wce);
			EventUtils.TriggerListener(Driver.WORLD_CHANGED, "world_changed", wce);
		}
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void onConsume(PlayerItemConsumeEvent event) {
		BukkitPlayerEvents.BukkitMCPlayerItemConsumeEvent pic = 
				new BukkitPlayerEvents.BukkitMCPlayerItemConsumeEvent(event);
		EventUtils.TriggerExternal(pic);
		EventUtils.TriggerListener(Driver.PLAYER_CONSUME, "player_consume", pic);
	}
	
	@EventHandler(priority= EventPriority.LOWEST)
	public void onFish(PlayerFishEvent event) {
		BukkitPlayerEvents.BukkitMCPlayerFishEvent fish = new BukkitPlayerEvents.BukkitMCPlayerFishEvent(event);
		EventUtils.TriggerExternal(fish);
		EventUtils.TriggerListener(Driver.PLAYER_FISH, "player_fish", fish);
	}
}
