package com.laytonsmith.abstraction.bukkit.events;

import com.laytonsmith.abstraction.Implementation;
import com.laytonsmith.abstraction.MCBookMeta;
import com.laytonsmith.abstraction.MCEntity;
import com.laytonsmith.abstraction.MCHumanEntity;
import com.laytonsmith.abstraction.MCItemStack;
import com.laytonsmith.abstraction.MCLocation;
import com.laytonsmith.abstraction.MCPlayer;
import com.laytonsmith.abstraction.MCTravelAgent;
import com.laytonsmith.abstraction.MCWorld;
import com.laytonsmith.abstraction.StaticLayer;
import com.laytonsmith.abstraction.blocks.MCBlock;
import com.laytonsmith.abstraction.blocks.MCBlockFace;
import com.laytonsmith.abstraction.bukkit.BukkitMCBookMeta;
import com.laytonsmith.abstraction.bukkit.BukkitMCEntity;
import com.laytonsmith.abstraction.bukkit.BukkitMCHumanEntity;
import com.laytonsmith.abstraction.bukkit.BukkitMCItemStack;
import com.laytonsmith.abstraction.bukkit.BukkitMCLocation;
import com.laytonsmith.abstraction.bukkit.BukkitMCPlayer;
import com.laytonsmith.abstraction.bukkit.BukkitMCServer;
import com.laytonsmith.abstraction.bukkit.BukkitMCTravelAgent;
import com.laytonsmith.abstraction.bukkit.BukkitMCWorld;
import com.laytonsmith.abstraction.bukkit.blocks.BukkitMCBlock;
import com.laytonsmith.abstraction.bukkit.entities.BukkitMCFishHook;
import com.laytonsmith.abstraction.bukkit.events.BukkitEntityEvents.BukkitMCEntityDeathEvent;
import com.laytonsmith.abstraction.entities.MCFishHook;
import com.laytonsmith.abstraction.enums.MCAction;
import com.laytonsmith.abstraction.enums.MCFishingState;
import com.laytonsmith.abstraction.enums.MCGameMode;
import com.laytonsmith.abstraction.enums.MCTeleportCause;
import com.laytonsmith.abstraction.enums.bukkit.BukkitMCAction;
import com.laytonsmith.abstraction.enums.bukkit.BukkitMCFishingState;
import com.laytonsmith.abstraction.enums.bukkit.BukkitMCGameMode;
import com.laytonsmith.abstraction.enums.bukkit.BukkitMCTeleportCause;
import com.laytonsmith.abstraction.events.MCChatTabCompleteEvent;
import com.laytonsmith.abstraction.events.MCExpChangeEvent;
import com.laytonsmith.abstraction.events.MCFoodLevelChangeEvent;
import com.laytonsmith.abstraction.events.MCGamemodeChangeEvent;
import com.laytonsmith.abstraction.events.MCPlayerBedEvent;
import com.laytonsmith.abstraction.events.MCPlayerChatEvent;
import com.laytonsmith.abstraction.events.MCPlayerCommandEvent;
import com.laytonsmith.abstraction.events.MCPlayerDeathEvent;
import com.laytonsmith.abstraction.events.MCPlayerEditBookEvent;
import com.laytonsmith.abstraction.events.MCPlayerEvent;
import com.laytonsmith.abstraction.events.MCPlayerFishEvent;
import com.laytonsmith.abstraction.events.MCPlayerInteractEvent;
import com.laytonsmith.abstraction.events.MCPlayerItemConsumeEvent;
import com.laytonsmith.abstraction.events.MCPlayerJoinEvent;
import com.laytonsmith.abstraction.events.MCPlayerKickEvent;
import com.laytonsmith.abstraction.events.MCPlayerLoginEvent;
import com.laytonsmith.abstraction.events.MCPlayerMoveEvent;
import com.laytonsmith.abstraction.events.MCPlayerPortalEvent;
import com.laytonsmith.abstraction.events.MCPlayerPreLoginEvent;
import com.laytonsmith.abstraction.events.MCPlayerQuitEvent;
import com.laytonsmith.abstraction.events.MCPlayerRespawnEvent;
import com.laytonsmith.abstraction.events.MCPlayerTeleportEvent;
import com.laytonsmith.abstraction.events.MCPlayerToggleFlightEvent;
import com.laytonsmith.abstraction.events.MCPlayerToggleSneakEvent;
import com.laytonsmith.abstraction.events.MCPlayerToggleSprintEvent;
import com.laytonsmith.abstraction.events.MCWorldChangedEvent;
import com.laytonsmith.annotations.abstraction;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.TravelAgent;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerBedLeaveEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerChatTabCompleteEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerEditBookEvent;
import org.bukkit.event.player.PlayerEvent;
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
import org.bukkit.event.player.PlayerPreLoginEvent.Result;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.event.player.PlayerToggleSprintEvent;
import org.bukkit.inventory.ItemStack;

/**
 *
 *
 */
public class BukkitPlayerEvents {
	@abstraction(type=Implementation.Type.BUKKIT)
	public static class BukkitMCFoodLevelChangeEvent implements MCFoodLevelChangeEvent {
		FoodLevelChangeEvent event;

		public BukkitMCFoodLevelChangeEvent(FoodLevelChangeEvent event) {
			this.event = event;
		}
		
		@Override
		public MCHumanEntity getEntity() {
			return new BukkitMCHumanEntity(event.getEntity());
		}
		
		@Override
		public int getDifference() {
			return ((Player)event.getEntity()).getFoodLevel() - getFoodLevel();
		}

		@Override
		public int getFoodLevel() {
			return event.getFoodLevel();
		}

		@Override
		public void setFoodLevel(int level) {
			event.setFoodLevel(level);
		}

		@Override
		public boolean isCancelled() {
			return event.isCancelled();
		}

		@Override
		public void setCancelled(boolean cancel) {
			event.setCancelled(cancel);
		}

		@Override
		public Object _GetObject() {
			return event;
		}
	}
	
	@abstraction(type=Implementation.Type.BUKKIT)
	public static abstract class BukkitMCPlayerEvent implements MCPlayerEvent {

		PlayerEvent pe;

		public BukkitMCPlayerEvent(PlayerEvent event) {
			this.pe = event;
		}

		@Override
		public Object _GetObject() {
			return pe;
		}

		@Override
		public MCPlayer getPlayer() {
			return new BukkitMCPlayer(pe.getPlayer());
		}
	}

	public static class BukkitMCPlayerItemConsumeEvent extends BukkitMCPlayerEvent
			implements MCPlayerItemConsumeEvent {
		PlayerItemConsumeEvent pic;

		public BukkitMCPlayerItemConsumeEvent(PlayerItemConsumeEvent event) {
			super(event);
			pic = event;
		}

		@Override
		public MCItemStack getItem() {
			return new BukkitMCItemStack(pic.getItem());
		}

		@Override
		public void setItem(MCItemStack item) {
			pic.setItem(((BukkitMCItemStack) item).asItemStack());
		}

		public static BukkitMCPlayerItemConsumeEvent _instantiate(
				MCPlayer player, MCItemStack item) {
			return new BukkitMCPlayerItemConsumeEvent(
					new PlayerItemConsumeEvent(((BukkitMCPlayer) player)._Player(),
							((BukkitMCItemStack) item).asItemStack()));
		}
	}

	public static class BukkitMCPlayerBedEvent  extends BukkitMCPlayerEvent
	implements MCPlayerBedEvent {
		MCBlock block;
		PlayerEvent event;

		public BukkitMCPlayerBedEvent(PlayerBedEnterEvent event) {
			super(event);
			this.event = event;
			this.block = new BukkitMCBlock(event.getBed());
		}

		public BukkitMCPlayerBedEvent(PlayerBedLeaveEvent event) {
			super(event);
			this.event = event;
			this.block = new BukkitMCBlock(event.getBed());
		}

		@Override
		public MCBlock getBed() {
			return block;
		}
	}

    public static class BukkitMCPlayerKickEvent  extends BukkitMCPlayerEvent
	implements MCPlayerKickEvent {
        PlayerKickEvent e;

        public BukkitMCPlayerKickEvent(PlayerKickEvent e){
			super(e);
            this.e = e;
        }

		@Override
        public String getMessage() {
            return e.getLeaveMessage();
        }

		@Override
        public void setMessage(String message) {
            e.setLeaveMessage(message);
        }

		@Override
        public String getReason() {
            return e.getReason();
        }

		@Override
        public void setReason(String message) {
            e.setReason(message);
        }

		@Override
        public boolean isCancelled() {
            return e.isCancelled();
        }

		@Override
        public void setCancelled(boolean cancelled) {
            e.setCancelled(cancelled);
        }
    }

	@abstraction(type=Implementation.Type.BUKKIT)
	public static class BukkitMCPlayerTeleportEvent  extends BukkitMCPlayerEvent
	implements MCPlayerTeleportEvent {
		PlayerTeleportEvent e;

		public BukkitMCPlayerTeleportEvent(PlayerTeleportEvent e) {
			super(e);
			this.e = e;
		}

		@Override
		public MCLocation getFrom() {
			return new BukkitMCLocation(e.getFrom());
		}

		@Override
		public MCLocation getTo() {
			return new BukkitMCLocation(e.getTo());
		}

		@Override
		public MCTeleportCause getCause() {
			return BukkitMCTeleportCause.getConvertor().getAbstractedEnum(e.getCause());
		}

		@Override
		public void setFrom(MCLocation oldloc) {
			e.setFrom(((BukkitMCLocation) oldloc)._Location());
		}

		@Override
		public void setTo(MCLocation newloc) {
			World w = ((BukkitMCWorld)newloc.getWorld()).__World();
			Location loc = new Location(
				w,
				newloc.getX(),
				newloc.getY(),
				newloc.getZ(),
				newloc.getPitch(),
				newloc.getYaw()
			);

			e.setTo(loc);
		}

		@Override
		public void setCancelled(boolean state) {
			e.setCancelled(state);
		}

		@Override
		public boolean isCancelled() {
			return e.isCancelled();
		}
	}

	@abstraction(type=Implementation.Type.BUKKIT)
	public static class BukkitMCPlayerPortalEvent extends BukkitMCPlayerTeleportEvent
			implements MCPlayerPortalEvent {

		PlayerPortalEvent p;
		public BukkitMCPlayerPortalEvent(PlayerPortalEvent event) {
			super(event);
			p = event;
		}

		@Override
		public void useTravelAgent(boolean useTravelAgent) {
			p.useTravelAgent(useTravelAgent);
		}

		@Override
		public boolean useTravelAgent() {
			return p.useTravelAgent();
		}

		@Override
		public MCTravelAgent getPortalTravelAgent() {
			return new BukkitMCTravelAgent(p.getPortalTravelAgent());
		}

		@Override
		public void setPortalTravelAgent(MCTravelAgent travelAgent) {
			p.setPortalTravelAgent((TravelAgent) travelAgent.getHandle());
		}

		@Override
		public MCLocation getTo() {
			if (e.getTo() == null) {
				return null;
			}
			return new BukkitMCLocation(e.getTo());
		}
	}

	public static class BukkitMCPlayerLoginEvent  extends BukkitMCPlayerEvent
	implements MCPlayerLoginEvent {
		PlayerLoginEvent event;

		public BukkitMCPlayerLoginEvent(PlayerLoginEvent e) {
			super(e);
            event = e;
        }

		@Override
		public String getName() {
			return event.getPlayer().getName();
		}

		@Override
		public String getUniqueId() {
			return event.getPlayer().getUniqueId().toString();
		}

		@Override
		public String getKickMessage() {
			return event.getKickMessage();
		}

		@Override
		public void setKickMessage(String msg) {
			event.setKickMessage(msg);
		}

		@Override
		public String getResult() {
			return event.getResult().toString();
		}

		@Override
		public void setResult(String rst) {
			event.setResult(PlayerLoginEvent.Result.valueOf(rst.toUpperCase()));
		}

		@Override
		public String getIP() {
			return event.getAddress().getHostAddress();
		}
	}

	public static class BukkitMCPlayerPreLoginEvent implements MCPlayerPreLoginEvent {
		PlayerPreLoginEvent event;

		public BukkitMCPlayerPreLoginEvent(PlayerPreLoginEvent e) {
            event = e;
        }

		@Override
		public Object _GetObject() {
			return event;
		}

		@Override
		public String getName() {
			return event.getName();
		}

		@Override
		public String getKickMessage() {
			return event.getKickMessage();
		}

		@Override
		public void setKickMessage(String msg) {
			event.setKickMessage(msg);
		}

		@Override
		public String getResult() {
			return event.getResult().toString();
		}

		@Override
		public void setResult(String rst) {
			event.setResult(Result.valueOf(rst.toUpperCase()));
		}

		@Override
		public String getIP() {
			return event.getAddress().toString();
		}

	}

	@abstraction(type=Implementation.Type.BUKKIT)
    public static class BukkitMCPlayerChatEvent  extends BukkitMCPlayerEvent
	implements MCPlayerChatEvent{
        AsyncPlayerChatEvent pce;

        public BukkitMCPlayerChatEvent(AsyncPlayerChatEvent event) {
			super(event);
            pce = event;
        }

        public BukkitMCPlayerChatEvent(BukkitMCPlayerChatEvent event) {
			super(event.pce);
            pce = event.pce;
        }

        public static BukkitMCPlayerChatEvent _instantiate(MCPlayer player, String message, String format){
			AsyncPlayerChatEvent apce = new AsyncPlayerChatEvent(false, ((BukkitMCPlayer)player)._Player(), message,
                    new HashSet<>(BukkitMCServer.getOnlinePlayersOverwrite(Bukkit.getServer())));
			apce.setFormat(format);
            return new BukkitMCPlayerChatEvent(apce);
        }

		@Override
        public String getMessage() {
            return pce.getMessage();
        }

		@Override
        public void setMessage(String message) {
            pce.setMessage(message);
        }

		@Override
        public List<MCPlayer> getRecipients() {
            List<MCPlayer> players = new ArrayList<>();
            for(Player p : pce.getRecipients()){
                players.add(new BukkitMCPlayer(p));
            }
            return players;
        }

		@Override
        public void setRecipients(List<MCPlayer> list) {
            pce.getRecipients().clear();
            for(MCPlayer p  : list){
                pce.getRecipients().add(((BukkitMCPlayer)p)._Player());
            }
        }

		@Override
		public String getFormat() {
			return pce.getFormat();
		}

		@Override
		public void setFormat(String format) {
			pce.setFormat(format);
		}
    }

	@abstraction(type=Implementation.Type.BUKKIT)
    public static class BukkitMCPlayerQuitEvent  extends BukkitMCPlayerEvent
	implements MCPlayerQuitEvent{
        PlayerQuitEvent pce;

        public BukkitMCPlayerQuitEvent(PlayerQuitEvent event) {
			super(event);
            pce = event;
        }

        public static BukkitMCPlayerQuitEvent _instantiate(MCPlayer player, String message){
            return new BukkitMCPlayerQuitEvent(new PlayerQuitEvent(((BukkitMCPlayer)player)._Player(), message));
        }

		@Override
        public String getMessage() {
            return pce.getQuitMessage();
        }

		@Override
        public void setMessage(String message) {
            pce.setQuitMessage(message);
        }
    }


    @abstraction(type=Implementation.Type.BUKKIT)
    public static class BukkitMCPlayerJoinEvent  extends BukkitMCPlayerEvent
	implements MCPlayerJoinEvent{
        PlayerJoinEvent pje;

        public BukkitMCPlayerJoinEvent(PlayerJoinEvent e){
			super(e);
            pje = e;
        }

		@Override
        public String getJoinMessage() {
            return pje.getJoinMessage();
        }

		@Override
        public void setJoinMessage(String message) {
            pje.setJoinMessage(message);
        }

        public static PlayerJoinEvent _instantiate(MCPlayer player, String message) {
            return new PlayerJoinEvent(((BukkitMCPlayer)player)._Player(), message);
        }
    }

    @abstraction(type=Implementation.Type.BUKKIT)
    public static class BukkitMCPlayerInteractEvent extends BukkitMCPlayerEvent
	implements MCPlayerInteractEvent{
        PlayerInteractEvent pie;

        public BukkitMCPlayerInteractEvent(PlayerInteractEvent e){
			super(e);
            pie = e;
        }

        public static BukkitMCPlayerInteractEvent _instantiate(MCPlayer player, MCAction action, MCItemStack itemstack,
                MCBlock clickedBlock, MCBlockFace clickedFace){
            return new BukkitMCPlayerInteractEvent(new PlayerInteractEvent(((BukkitMCPlayer)player)._Player(),
                    BukkitMCAction.getConvertor().getConcreteEnum(action), ((BukkitMCItemStack)itemstack).__ItemStack(),
                    ((BukkitMCBlock)clickedBlock).__Block(), BlockFace.valueOf(clickedFace.name())));
        }

		@Override
        public MCAction getAction() {
            return BukkitMCAction.getConvertor().getAbstractedEnum(pie.getAction());
        }

		@Override
        public MCBlock getClickedBlock() {
            return new BukkitMCBlock(pie.getClickedBlock());
        }

		@Override
        public MCBlockFace getBlockFace() {
            return MCBlockFace.valueOf(pie.getBlockFace().name());
        }

		@Override
        public MCItemStack getItem() {
            return new BukkitMCItemStack(pie.getItem());
        }
    }

    @abstraction(type=Implementation.Type.BUKKIT)
    public static class BukkitMCPlayerRespawnEvent extends BukkitMCPlayerEvent
	implements MCPlayerRespawnEvent {
        PlayerRespawnEvent pre;

        public BukkitMCPlayerRespawnEvent(PlayerRespawnEvent event) {
			super(event);
            pre = event;
        }

        public static BukkitMCPlayerRespawnEvent _instantiate(MCPlayer player, MCLocation location, boolean isBedSpawn) {
            return new BukkitMCPlayerRespawnEvent(new PlayerRespawnEvent(((BukkitMCPlayer)player)._Player(),
					((BukkitMCLocation)location)._Location(), isBedSpawn));
        }

		@Override
        public void setRespawnLocation(MCLocation location) {
            pre.setRespawnLocation(((BukkitMCLocation)location)._Location());
        }

		@Override
        public MCLocation getRespawnLocation() {
            return new BukkitMCLocation(pre.getRespawnLocation());
        }

		@Override
        public Boolean isBedSpawn() {
            return pre.isBedSpawn();
        }
    }

    @abstraction(type=Implementation.Type.BUKKIT)
    public static class BukkitMCPlayerDeathEvent extends BukkitMCEntityDeathEvent
    		implements MCPlayerDeathEvent {
        PlayerDeathEvent pde;

        public BukkitMCPlayerDeathEvent(PlayerDeathEvent event) {
        	super(event);
            pde = event;
        }

        public static BukkitMCPlayerDeathEvent _instantiate(MCPlayer entity, List<MCItemStack> listOfDrops,
                int droppedExp, String deathMessage){
            List<ItemStack> drops = new ArrayList<ItemStack>();

            return new BukkitMCPlayerDeathEvent(new PlayerDeathEvent(((BukkitMCPlayer)entity)._Player(), drops, droppedExp, deathMessage));
        }

        @Override
        public MCPlayer getEntity() {
            return new BukkitMCPlayer(pde.getEntity());
        }

		@Override
		public MCEntity getKiller() {
			return StaticLayer.GetCorrectEntity(new BukkitMCEntity(pde.getEntity().getKiller()));
		}

		@Override
        public String getDeathMessage() {
            return pde.getDeathMessage();
        }

		@Override
        public void setDeathMessage(String nval) {
            pde.setDeathMessage(nval);
        }

		@Override
		public boolean getKeepInventory() {
			return pde.getKeepInventory();
		}

		@Override
		public void setKeepInventory(boolean keepInventory) {
			pde.setKeepInventory(keepInventory);
		}

		@Override
		public boolean getKeepLevel() {
			return pde.getKeepLevel();
		}

		@Override
		public void setKeepLevel(boolean keepLevel) {
			pde.setKeepLevel(keepLevel);
		}

		@Override
		public int getNewExp() {
			return pde.getNewExp();
		}

		@Override
		public void setNewExp(int exp) {
			pde.setNewExp(exp);
		}

		@Override
		public int getNewLevel() {
			return pde.getNewLevel();
		}

		@Override
		public void setNewLevel(int level) {
			pde.setNewLevel(level);
		}

		@Override
		public int getNewTotalExp() {
			return pde.getNewTotalExp();
		}

		@Override
		public void setNewTotalExp(int totalExp) {
			pde.setNewTotalExp(totalExp);
		}
    }

    @abstraction(type=Implementation.Type.BUKKIT)
    public static class BukkitMCPlayerCommandEvent extends BukkitMCPlayerEvent
	implements MCPlayerCommandEvent{
        PlayerCommandPreprocessEvent pcpe;
        boolean isCancelled = false;

        public BukkitMCPlayerCommandEvent(PlayerCommandPreprocessEvent pcpe){
			super(pcpe);
            this.pcpe = pcpe;
        }

		@Override
        public String getCommand(){
            return pcpe.getMessage();
        }

		@Override
        public void cancel() {
            pcpe.setMessage("/commandhelper null");
			pcpe.setCancelled(true);
            isCancelled = true;
        }

        public static BukkitMCPlayerCommandEvent _instantiate(MCPlayer entity, String command){
            return new BukkitMCPlayerCommandEvent(new PlayerCommandPreprocessEvent(((BukkitMCPlayer)entity)._Player(), command));
        }

		@Override
        public void setCommand(String val) {
            pcpe.setMessage(val);
        }

		@Override
        public boolean isCancelled() {
            return isCancelled;
        }
    }

    @abstraction(type=Implementation.Type.BUKKIT)
    public static class BukkitMCWorldChangedEvent extends BukkitMCPlayerEvent
	implements MCWorldChangedEvent{
        PlayerChangedWorldEvent pcwe;

        public BukkitMCWorldChangedEvent(PlayerChangedWorldEvent e){
			super(e);
            pcwe = e;
        }

		@Override
        public MCWorld getFrom() {
            return new BukkitMCWorld(pcwe.getFrom());
        }

		@Override
        public MCWorld getTo() {
            return new BukkitMCWorld(pcwe.getPlayer().getWorld());
        }

        public static BukkitMCWorldChangedEvent _instantiate(MCPlayer entity, MCWorld from){
            return new BukkitMCWorldChangedEvent(new PlayerChangedWorldEvent(((BukkitMCPlayer)entity)._Player(), ((BukkitMCWorld)from).__World()));
        }
    }

	public static class BukkitMCPlayerFishEvent extends BukkitMCPlayerEvent
	implements MCPlayerFishEvent {
		PlayerFishEvent e;

		public BukkitMCPlayerFishEvent(PlayerFishEvent event) {
			super(event);
			e = event;
		}

		@Override
		public MCEntity getCaught() {
			if (e.getCaught() == null) {
				return null;
			}
			return new BukkitMCEntity(e.getCaught());
		}

		@Override
		public int getExpToDrop() {
			return e.getExpToDrop();
		}

		@Override
		public MCFishHook getHook() {
			return new BukkitMCFishHook(e.getHook());
		}

		@Override
		public MCFishingState getState() {
			return BukkitMCFishingState.getConvertor().getAbstractedEnum(e.getState());
		}

		@Override
		public void setExpToDrop(int exp) {
			e.setExpToDrop(exp);
		}
	}

	public static class BukkitMCGamemodeChangeEvent extends BukkitMCPlayerEvent
	implements MCGamemodeChangeEvent {
		PlayerGameModeChangeEvent gmc;

		public BukkitMCGamemodeChangeEvent(PlayerGameModeChangeEvent event) {
			super(event);
			gmc = event;
		}

		@Override
		public MCGameMode getNewGameMode() {
			return BukkitMCGameMode.getConvertor().getAbstractedEnum(gmc.getNewGameMode());
		}
	}

	public static class BukkitMCChatTabCompleteEvent extends BukkitMCPlayerEvent
	implements MCChatTabCompleteEvent {
		PlayerChatTabCompleteEvent tc;

		public BukkitMCChatTabCompleteEvent(PlayerChatTabCompleteEvent event) {
			super(event);
			this.tc = event;
		}

		@Override
		public String getChatMessage() {
			return tc.getChatMessage();
		}

		@Override
		public String getLastToken() {
			return tc.getLastToken();
		}

		@Override
		public Collection<String> getTabCompletions() {
			return tc.getTabCompletions();
		}
	}

	public static class BukkitMCExpChangeEvent extends BukkitMCPlayerEvent
	implements MCExpChangeEvent {
		PlayerExpChangeEvent ec;

		public BukkitMCExpChangeEvent(PlayerExpChangeEvent event) {
			super(event);
			ec = event;
		}

		@Override
		public int getAmount() {
			return ec.getAmount();
		}

		@Override
		public void setAmount(int amount) {
			ec.setAmount(amount);
		}
	}

	@abstraction(type = Implementation.Type.BUKKIT)
	public static class BukkitMCPlayerEditBookEvent extends BukkitMCPlayerEvent
	implements MCPlayerEditBookEvent {
		PlayerEditBookEvent pebe;

		public BukkitMCPlayerEditBookEvent(PlayerEditBookEvent event) {
			super(event);
			pebe = event;
		}

		@Override
		public MCBookMeta getNewBookMeta() {
			return new BukkitMCBookMeta(pebe.getNewBookMeta());
		}

		@Override
		public MCBookMeta getPreviousBookMeta() {
			return new BukkitMCBookMeta(pebe.getPreviousBookMeta());
		}

		@Override
		public void setNewBookMeta(MCBookMeta bookMeta) {
			pebe.setNewBookMeta(((BukkitMCBookMeta) bookMeta).getBookMeta());
		}

		@Override
		public int getSlot() {
			return pebe.getSlot();
		}

		@Override
		public boolean isSigning() {
			return pebe.isSigning();
		}

		@Override
		public void setSigning(boolean isSigning) {
			pebe.setSigning(isSigning);
		}
	}

	@abstraction(type = Implementation.Type.BUKKIT)
	public static class BukkitMCPlayerToggleFlightEvent extends BukkitMCPlayerEvent
	implements MCPlayerToggleFlightEvent {
		PlayerToggleFlightEvent ptfe;

		public BukkitMCPlayerToggleFlightEvent(PlayerToggleFlightEvent event) {
			super(event);
			this.ptfe = event;
		}

		@Override
		public boolean isFlying() {
			return ptfe.isFlying();
		}

		@Override
		public boolean isCancelled() {
			return ptfe.isCancelled();
		}

		@Override
		public void setCancelled(boolean bln) {
			ptfe.setCancelled(bln);
		}
	}

	@abstraction(type = Implementation.Type.BUKKIT)
	public static class BukkitMCPlayerToggleSneakEvent extends BukkitMCPlayerEvent
	implements MCPlayerToggleSneakEvent {
		PlayerToggleSneakEvent ptse;

		public BukkitMCPlayerToggleSneakEvent(PlayerToggleSneakEvent event) {
			super(event);
			this.ptse = event;
		}

		@Override
		public boolean isSneaking() {
			return ptse.isSneaking();
		}

		@Override
		public boolean isCancelled() {
			return ptse.isCancelled();
		}

		@Override
		public void setCancelled(boolean bln) {
			ptse.setCancelled(bln);
		}
	}

	@abstraction(type = Implementation.Type.BUKKIT)
	public static class BukkitMCPlayerToggleSprintEvent extends BukkitMCPlayerEvent
	implements MCPlayerToggleSprintEvent {
		PlayerToggleSprintEvent ptse;

		public BukkitMCPlayerToggleSprintEvent(PlayerToggleSprintEvent event) {
			super(event);
			this.ptse = event;
		}

		@Override
		public boolean isSprinting() {
			return ptse.isSprinting();
		}

		@Override
		public boolean isCancelled() {
			return ptse.isCancelled();
		}

		@Override
		public void setCancelled(boolean bln) {
			ptse.setCancelled(bln);
		}
	}
}
