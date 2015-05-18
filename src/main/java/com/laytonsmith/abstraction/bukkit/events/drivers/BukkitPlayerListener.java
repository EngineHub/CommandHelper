package com.laytonsmith.abstraction.bukkit.events.drivers;

import com.laytonsmith.abstraction.bukkit.entities.BukkitMCPlayer;
import com.laytonsmith.abstraction.bukkit.events.BukkitPlayerEvents;
import com.laytonsmith.commandhelper.CommandHelperPlugin;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.events.Driver;
import com.laytonsmith.core.events.EventUtils;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerBedLeaveEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerChatTabCompleteEvent;
import org.bukkit.event.player.PlayerEditBookEvent;
import org.bukkit.event.player.PlayerExpChangeEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerPreLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.event.player.PlayerToggleSprintEvent;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * 
 */
public class BukkitPlayerListener implements Listener {

	@EventHandler(priority = EventPriority.LOWEST)
    public void onFoodLevelChange(FoodLevelChangeEvent e) {
		BukkitPlayerEvents.BukkitMCFoodLevelChangeEvent pke = new BukkitPlayerEvents.BukkitMCFoodLevelChangeEvent(e);
		EventUtils.TriggerListener(Driver.FOOD_LEVEL_CHANGED, "food_level_changed", pke);
    }
	
    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerKick(PlayerKickEvent e) {
		BukkitPlayerEvents.BukkitMCPlayerKickEvent pke = new BukkitPlayerEvents.BukkitMCPlayerKickEvent(e);
		EventUtils.TriggerListener(Driver.PLAYER_KICK, "player_kick", pke);
    }
	
	@EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerBedEnter(PlayerBedEnterEvent e) {
		BukkitPlayerEvents.BukkitMCPlayerBedEvent be = new BukkitPlayerEvents.BukkitMCPlayerBedEvent(e);
		EventUtils.TriggerListener(Driver.PLAYER_BED_EVENT, "player_enter_bed", be);
    }
	
	@EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerBedLeave(PlayerBedLeaveEvent e) {
		BukkitPlayerEvents.BukkitMCPlayerBedEvent be = new BukkitPlayerEvents.BukkitMCPlayerBedEvent(e);
		EventUtils.TriggerListener(Driver.PLAYER_BED_EVENT, "player_leave_bed", be);
    }
    
	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerLogin(PlayerLoginEvent e) {
		BukkitPlayerEvents.BukkitMCPlayerLoginEvent ple = new BukkitPlayerEvents.BukkitMCPlayerLoginEvent(e);
		EventUtils.TriggerListener(Driver.PLAYER_LOGIN, "player_login", ple);
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerPreLogin(PlayerPreLoginEvent e) {
		BukkitPlayerEvents.BukkitMCPlayerPreLoginEvent pple = new BukkitPlayerEvents.BukkitMCPlayerPreLoginEvent(e);
		EventUtils.TriggerListener(Driver.PLAYER_PRELOGIN, "player_prelogin", pple);
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerJoin(PlayerJoinEvent e) {
		BukkitPlayerEvents.BukkitMCPlayerJoinEvent pje = new BukkitPlayerEvents.BukkitMCPlayerJoinEvent(e);
		EventUtils.TriggerListener(Driver.PLAYER_JOIN, "player_join", pje);
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerInteract(PlayerInteractEvent e) {
		BukkitPlayerEvents.BukkitMCPlayerInteractEvent pie = new BukkitPlayerEvents.BukkitMCPlayerInteractEvent(e);
		EventUtils.TriggerListener(Driver.PLAYER_INTERACT, "player_interact", pie);
		EventUtils.TriggerListener(Driver.PLAYER_INTERACT, "pressure_plate_activated", pie);
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerRespawn(PlayerRespawnEvent event) {
		BukkitPlayerEvents.BukkitMCPlayerRespawnEvent pre = new BukkitPlayerEvents.BukkitMCPlayerRespawnEvent(event);
		EventUtils.TriggerListener(Driver.PLAYER_SPAWN, "player_spawn", pre);
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled=true)
	public void onPlayerChat(final AsyncPlayerChatEvent event) {
		if(CommandHelperPlugin.self.interpreterListener
                .isInInterpreterMode(event.getPlayer().getName())){
            //They are in interpreter mode, so we want it to handle this, not everything else.
            return;
        }
		
		if(event.isAsynchronous()){
			//The async event gets priority, and if cancelled, doesn't trigger a normal player_chat event.
			BukkitPlayerEvents.BukkitMCPlayerChatEvent pce = new BukkitPlayerEvents.BukkitMCPlayerChatEvent(event);
			EventUtils.TriggerListener(Driver.PLAYER_CHAT, "async_player_chat", pce);

			if(event.isCancelled()){
				return;
			}
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
						@Override
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
		EventUtils.TriggerListener(Driver.PLAYER_CHAT, "player_chat", pce);
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerQuit(PlayerQuitEvent event) {
		BukkitPlayerEvents.BukkitMCPlayerQuitEvent pqe = new BukkitPlayerEvents.BukkitMCPlayerQuitEvent(event);
		EventUtils.TriggerListener(Driver.PLAYER_QUIT, "player_quit", pqe);
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerChangedWorld(PlayerChangedWorldEvent event) {
		BukkitMCPlayer currentPlayer = (BukkitMCPlayer) Static.GetPlayer(event.getPlayer().getName(), Target.UNKNOWN);
		//Apparently this happens sometimes, so prevent it
		if (!event.getFrom().equals(currentPlayer._Player().getWorld())) {
			BukkitPlayerEvents.BukkitMCWorldChangedEvent wce = new BukkitPlayerEvents.BukkitMCWorldChangedEvent(event);
			EventUtils.TriggerListener(Driver.WORLD_CHANGED, "world_changed", wce);
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerTeleport(PlayerTeleportEvent event) {
		if (event.getFrom().equals(event.getTo())) {
			return;
		}
		
		BukkitPlayerEvents.BukkitMCPlayerTeleportEvent pte = new BukkitPlayerEvents.BukkitMCPlayerTeleportEvent(event);
		EventUtils.TriggerListener(Driver.PLAYER_TELEPORT, "player_teleport", pte);
	}
	

	@EventHandler(priority = EventPriority.LOWEST)
	public void onPortalEnter(PlayerPortalEvent event) {
		BukkitPlayerEvents.BukkitMCPlayerPortalEvent pe = new BukkitPlayerEvents.BukkitMCPlayerPortalEvent(event);
		EventUtils.TriggerListener(Driver.PLAYER_PORTAL_TRAVEL, "player_portal_travel", pe);
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onConsume(PlayerItemConsumeEvent event) {
		BukkitPlayerEvents.BukkitMCPlayerItemConsumeEvent pic = 
				new BukkitPlayerEvents.BukkitMCPlayerItemConsumeEvent(event);
		EventUtils.TriggerListener(Driver.PLAYER_CONSUME, "player_consume", pic);
	}
	
	@EventHandler(priority= EventPriority.LOWEST)
	public void onFish(PlayerFishEvent event) {
		BukkitPlayerEvents.BukkitMCPlayerFishEvent fish = new BukkitPlayerEvents.BukkitMCPlayerFishEvent(event);
		EventUtils.TriggerListener(Driver.PLAYER_FISH, "player_fish", fish);
	}
	
	@EventHandler(priority= EventPriority.LOWEST)
	public void onGamemodeChange(PlayerGameModeChangeEvent event) {
		BukkitPlayerEvents.BukkitMCGamemodeChangeEvent e = new BukkitPlayerEvents.BukkitMCGamemodeChangeEvent(event);
		EventUtils.TriggerListener(Driver.GAMEMODE_CHANGE, "gamemode_change", e);
	}
	
	@EventHandler(priority= EventPriority.LOWEST)
	public void onChatTab(PlayerChatTabCompleteEvent event) {
		BukkitPlayerEvents.BukkitMCChatTabCompleteEvent e = new BukkitPlayerEvents.BukkitMCChatTabCompleteEvent(event);
		EventUtils.TriggerListener(Driver.TAB_COMPLETE, "tab_complete_chat", e);
	}
	
	@EventHandler(priority= EventPriority.LOWEST)
	public void onExpChange(PlayerExpChangeEvent event) {
		BukkitPlayerEvents.BukkitMCExpChangeEvent e = new BukkitPlayerEvents.BukkitMCExpChangeEvent(event);
		EventUtils.TriggerListener(Driver.EXP_CHANGE, "exp_change", e);
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerEditBook(PlayerEditBookEvent event) {
		BukkitPlayerEvents.BukkitMCPlayerEditBookEvent pebe = new BukkitPlayerEvents.BukkitMCPlayerEditBookEvent(event);
		EventUtils.TriggerListener(Driver.BOOK_EDITED, "book_edited", pebe);
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerToggleFlight(PlayerToggleFlightEvent event) {
		BukkitPlayerEvents.BukkitMCPlayerToggleFlightEvent ptfe = new BukkitPlayerEvents.BukkitMCPlayerToggleFlightEvent(event);
		EventUtils.TriggerListener(Driver.PLAYER_TOGGLE_FLIGHT, "player_toggle_flight", ptfe);
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerToggleSneak(PlayerToggleSneakEvent event) {
		BukkitPlayerEvents.BukkitMCPlayerToggleSneakEvent ptse = new BukkitPlayerEvents.BukkitMCPlayerToggleSneakEvent(event);
		EventUtils.TriggerListener(Driver.PLAYER_TOGGLE_SNEAK, "player_toggle_sneak", ptse);
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerToggleSprint(PlayerToggleSprintEvent event) {
		BukkitPlayerEvents.BukkitMCPlayerToggleSprintEvent ptse = new BukkitPlayerEvents.BukkitMCPlayerToggleSprintEvent(event);
		EventUtils.TriggerListener(Driver.PLAYER_TOGGLE_SPRINT, "player_toggle_sprint", ptse);
	}
}
