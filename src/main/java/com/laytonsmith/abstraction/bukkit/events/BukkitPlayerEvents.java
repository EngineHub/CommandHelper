package com.laytonsmith.abstraction.bukkit.events;

import com.laytonsmith.abstraction.entities.MCPlayer;
import com.laytonsmith.abstraction.entities.MCEntity;
import com.laytonsmith.abstraction.*;
import com.laytonsmith.abstraction.blocks.MCBlock;
import com.laytonsmith.abstraction.bukkit.BukkitConvertor;
import com.laytonsmith.abstraction.enums.MCBlockFace;
import com.laytonsmith.abstraction.bukkit.BukkitMCBookMeta;
import com.laytonsmith.abstraction.bukkit.BukkitMCItemStack;
import com.laytonsmith.abstraction.bukkit.BukkitMCLocation;
import com.laytonsmith.abstraction.bukkit.entities.BukkitMCPlayer;
import com.laytonsmith.abstraction.bukkit.BukkitMCTravelAgent;
import com.laytonsmith.abstraction.bukkit.BukkitMCVector;
import com.laytonsmith.abstraction.bukkit.BukkitMCWorld;
import com.laytonsmith.abstraction.bukkit.blocks.BukkitMCBlock;
import com.laytonsmith.abstraction.bukkit.entities.BukkitMCFish;
import com.laytonsmith.abstraction.bukkit.events.BukkitEntityEvents.BukkitMCEntityDeathEvent;
import com.laytonsmith.abstraction.entities.MCFish;
import com.laytonsmith.abstraction.enums.MCAction;
import com.laytonsmith.abstraction.enums.MCFishingState;
import com.laytonsmith.abstraction.enums.MCGameMode;
import com.laytonsmith.abstraction.enums.MCTeleportCause;
import com.laytonsmith.abstraction.enums.bukkit.BukkitMCAction;
import com.laytonsmith.abstraction.enums.bukkit.BukkitMCFishingState;
import com.laytonsmith.abstraction.enums.bukkit.BukkitMCGameMode;
import com.laytonsmith.abstraction.enums.bukkit.BukkitMCTeleportCause;
import com.laytonsmith.abstraction.events.*;
import com.laytonsmith.annotations.abstraction;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.TravelAgent;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

/**
 *
 * @author layton
 */
public class BukkitPlayerEvents {

	@abstraction(type=Implementation.Type.BUKKIT)
	public static abstract class BukkitMCPlayerEvent implements MCPlayerEvent {

		PlayerEvent pe;

		public BukkitMCPlayerEvent(PlayerEvent event) {
			this.pe = event;
		}

		public Object _GetObject() {
			return pe;
		}

		public MCPlayer getPlayer() {
			return new BukkitMCPlayer(pe.getPlayer());
		}
	}

	public static class BukkitMCPlayerItemConsumeEvent extends BukkitMCPlayerEvent implements MCPlayerItemConsumeEvent {
	
		PlayerItemConsumeEvent pic;
	
		public BukkitMCPlayerItemConsumeEvent(PlayerItemConsumeEvent event) {
			super(event);
			pic = event;
		}
	
		public MCItemStack getItem() {
			return new BukkitMCItemStack(pic.getItem());
		}

		public void setItem(MCItemStack item) {
			pic.setItem(((BukkitMCItemStack) item).asItemStack());
		}

		@Override
		public Object _GetObject() {
			return pic;
		}
		
		public static BukkitMCPlayerItemConsumeEvent _instantiate(
				MCPlayer player, MCItemStack item) {
			return new BukkitMCPlayerItemConsumeEvent(
					new PlayerItemConsumeEvent((Player) player.getHandle(), 
							((BukkitMCItemStack) item).asItemStack()));
		}
	}

	public static class BukkitMCPlayerBedEvent extends BukkitMCPlayerEvent implements MCPlayerBedEvent {

		MCBlock block;

		public BukkitMCPlayerBedEvent(PlayerBedEnterEvent event) {
			super(event);
			this.block = new BukkitMCBlock(event.getBed());
		}
		
		public BukkitMCPlayerBedEvent(PlayerBedLeaveEvent event) {
			super(event);
			this.block = new BukkitMCBlock(event.getBed());
		}
		
		public MCBlock getBed() {
			return block;
		}
	}

    public static class BukkitMCPlayerKickEvent extends BukkitMCPlayerEvent implements MCPlayerKickEvent {

        PlayerKickEvent pke;

        public BukkitMCPlayerKickEvent(PlayerKickEvent event) {
			super(event);
            this.pke = event;
        }

		@Override
        public Object _GetObject() {
            return pke;
        }

        public String getMessage() {
            return pke.getLeaveMessage();
        }

        public void setMessage(String message) {
            pke.setLeaveMessage(message);
        }

        public String getReason() {
            return pke.getReason();
        }

        public void setReason(String message) {
            pke.setReason(message);
        }

        public boolean isCancelled() {
            return pke.isCancelled();
        }

        public void setCancelled(boolean cancelled) {
            pke.setCancelled(cancelled);
        }
    }

	@abstraction(type=Implementation.Type.BUKKIT)
	public static class BukkitMCPlayerTeleportEvent extends BukkitMCPlayerEvent implements MCPlayerTeleportEvent {

		PlayerTeleportEvent pte;

		public BukkitMCPlayerTeleportEvent(PlayerTeleportEvent event) {
			super(event);
			this.pte = event;
		}

		public MCLocation getFrom() {
			return new BukkitMCLocation(pte.getFrom());
		}

		public MCLocation getTo() {
			return new BukkitMCLocation(pte.getTo());
		}

		public MCTeleportCause getCause() {
			return BukkitMCTeleportCause.getConvertor().getAbstractedEnum(pte.getCause());
		}

		public void setFrom(MCLocation oldloc) {
			pte.setFrom((Location) oldloc.getHandle());
		}

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

			pte.setTo(loc);
		}

		@Override
		public Object _GetObject() {
			return pte;
		}

		public void setCancelled(boolean state) {
			pte.setCancelled(state);
		}

		public boolean isCancelled() {
			return pte.isCancelled();
		}
	}

	@abstraction(type=Implementation.Type.BUKKIT)
	public static class BukkitMCPlayerPortalEvent extends BukkitMCPlayerTeleportEvent implements MCPlayerPortalEvent {

		PlayerPortalEvent pte;

		public BukkitMCPlayerPortalEvent(PlayerPortalEvent event) {
			super(event);
			this.pte = event;
		}

		@Override
		public Object _GetObject() {
			return pte;
		}

		public void useTravelAgent(boolean useTravelAgent) {
			pte.useTravelAgent(useTravelAgent);
		}

		public boolean useTravelAgent() {
			return pte.useTravelAgent();
		}

		public MCTravelAgent getPortalTravelAgent() {
			return new BukkitMCTravelAgent(pte.getPortalTravelAgent());
		}

		public void setPortalTravelAgent(MCTravelAgent travelAgent) {
			pte.setPortalTravelAgent((TravelAgent) travelAgent.getHandle());
		}

		@Override
		public MCLocation getTo() {
			if (pte.getTo() == null) {
				return null;
			}
			return new BukkitMCLocation(pte.getTo());
		}
	}

	@abstraction(type=Implementation.Type.BUKKIT)
	public static class BukkitMCPlayerLoginEvent extends BukkitMCPlayerEvent implements MCPlayerLoginEvent {

		PlayerLoginEvent ple;

		public BukkitMCPlayerLoginEvent(PlayerLoginEvent event) {
			super(event);
            this.ple = event;
        }

		@Override
		public Object _GetObject() {
			return ple;
		}

		public String getName() {
			return ple.getPlayer().getName();
		}

		public String getKickMessage() {
			return ple.getKickMessage();
		}

		public void setKickMessage(String msg) {
			ple.setKickMessage(msg);
		}

		public String getResult() {
			return ple.getResult().toString();
		}

		public void setResult(String rst) {
			ple.setResult(PlayerLoginEvent.Result.valueOf(rst.toUpperCase()));
		}

		public String getIP() {
			return ple.getAddress().getHostAddress();
		}
	}

	@abstraction(type=Implementation.Type.BUKKIT)
	public static class BukkitMCPlayerPreLoginEvent implements MCPlayerPreLoginEvent {
	
		AsyncPlayerPreLoginEvent pple;

		public BukkitMCPlayerPreLoginEvent(AsyncPlayerPreLoginEvent event) {
            this.pple = event;
        }

		public Object _GetObject() {
			return pple;
		}

		public String getName() {
			return pple.getName();
		}

		public String getKickMessage() {
			return pple.getKickMessage();
		}

		public void setKickMessage(String msg) {
			pple.setKickMessage(msg);
		}

		public String getResult() {
			return pple.getLoginResult().toString();
		}

		public void setResult(String rst) {
			pple.setLoginResult(AsyncPlayerPreLoginEvent.Result.valueOf(rst.toUpperCase()));
		}

		public String getIP() {
			return pple.getAddress().toString();
		}
	}

	@abstraction(type=Implementation.Type.BUKKIT)
	public static class BukkitMCPlayerChatEvent extends BukkitMCPlayerEvent implements MCPlayerChatEvent {

        AsyncPlayerChatEvent pce;

        public BukkitMCPlayerChatEvent(AsyncPlayerChatEvent event) {
			super(event);
            this.pce = event;
        }

        public BukkitMCPlayerChatEvent(BukkitMCPlayerChatEvent event) {
			super(event.pce);
            this.pce = event.pce;
        }

		@Override
        public Object _GetObject() {
            return pce;
        }

        public static BukkitMCPlayerChatEvent _instantiate(MCPlayer player, String message, String format){
			AsyncPlayerChatEvent apce = new AsyncPlayerChatEvent(false, (Player) player.getHandle(), message,
                    new HashSet<Player>(Arrays.asList(Bukkit.getServer().getOnlinePlayers())));
			apce.setFormat(format);
            return new BukkitMCPlayerChatEvent(apce);
        }

        public String getMessage() {
            return pce.getMessage();
        }

        public void setMessage(String message) {
            pce.setMessage(message);
        }

        public List<MCPlayer> getRecipients() {
            List<MCPlayer> players = new ArrayList<MCPlayer>();
            for(Player p : pce.getRecipients()){
                players.add(new BukkitMCPlayer(p));
            }
            return players;
        }

        public void setRecipients(List<MCPlayer> list) {
            pce.getRecipients().clear();
            for(MCPlayer p  : list){
                pce.getRecipients().add((Player) p.getHandle());
            }
        }

		public String getFormat() {
			return pce.getFormat();
		}

		public void setFormat(String format) {
			pce.setFormat(format);
		}
    }

	@abstraction(type=Implementation.Type.BUKKIT)
    public static class BukkitMCPlayerQuitEvent extends BukkitMCPlayerEvent implements MCPlayerQuitEvent {

        PlayerQuitEvent pce;

        public BukkitMCPlayerQuitEvent(PlayerQuitEvent event) {
			super(event);
            this.pce = event;
        }

		@Override
        public Object _GetObject() {
            return pce;
        }

        public static BukkitMCPlayerQuitEvent _instantiate(MCPlayer player, String message){
            return new BukkitMCPlayerQuitEvent(new PlayerQuitEvent((Player) player.getHandle(), message));
        }

        public String getMessage() {
            return pce.getQuitMessage();
        }

        public void setMessage(String message) {
            pce.setQuitMessage(message);
        }
    }


    @abstraction(type=Implementation.Type.BUKKIT)
    public static class BukkitMCPlayerJoinEvent extends BukkitMCPlayerEvent implements MCPlayerJoinEvent {

        PlayerJoinEvent pje;

        public BukkitMCPlayerJoinEvent(PlayerJoinEvent event) {
			super(event);
            this.pje = event;
        }

        public String getJoinMessage() {
            return pje.getJoinMessage();
        }

        public void setJoinMessage(String message) {
            pje.setJoinMessage(message);
        }

		@Override
        public Object _GetObject() {
            return pje;
        }

        public static PlayerJoinEvent _instantiate(MCPlayer player, String message) {
            return new PlayerJoinEvent((Player) player.getHandle(), message);
        }

    }

    @abstraction(type=Implementation.Type.BUKKIT)
    public static class BukkitMCPlayerInteractEvent extends BukkitMCPlayerEvent implements MCPlayerInteractEvent{

        PlayerInteractEvent pie;

        public BukkitMCPlayerInteractEvent(PlayerInteractEvent event) {
			super(event);
            this.pie = event;
        }

        public static BukkitMCPlayerInteractEvent _instantiate(MCPlayer player, MCAction action, MCItemStack itemstack,
                MCBlock clickedBlock, MCBlockFace clickedFace){
            return new BukkitMCPlayerInteractEvent(new PlayerInteractEvent((Player) player,
                    BukkitMCAction.getConvertor().getConcreteEnum(action), ((BukkitMCItemStack)itemstack).__ItemStack(),
                    ((BukkitMCBlock)clickedBlock).__Block(), BlockFace.valueOf(clickedFace.name())));
        }

        public MCAction getAction() {
            return BukkitMCAction.getConvertor().getAbstractedEnum(pie.getAction());
        }

        public MCBlock getClickedBlock() {
            return new BukkitMCBlock(pie.getClickedBlock());
        }

        public MCBlockFace getBlockFace() {
            return MCBlockFace.valueOf(pie.getBlockFace().name());
        }

        public MCItemStack getItem() {
            return new BukkitMCItemStack(pie.getItem());
        }

		@Override
        public Object _GetObject() {
            return pie;
        }

    }

    @abstraction(type=Implementation.Type.BUKKIT)
    public static class BukkitMCPlayerRespawnEvent extends BukkitMCPlayerEvent implements MCPlayerRespawnEvent {

        PlayerRespawnEvent pre;

        public BukkitMCPlayerRespawnEvent(PlayerRespawnEvent event) {
			super(event);
            pre = event;
        }

		@Override
        public Object _GetObject() {
            return pre;
        }

        public static BukkitMCPlayerRespawnEvent _instantiate(MCPlayer player, MCLocation location, boolean isBedSpawn) {
            return new BukkitMCPlayerRespawnEvent(new PlayerRespawnEvent((Player) player.getHandle(),
					(Location) location.getHandle(), isBedSpawn));
        }

        public void setRespawnLocation(MCLocation location) {
            pre.setRespawnLocation((Location) location.getHandle());
        }

        public MCLocation getRespawnLocation() {
            return new BukkitMCLocation(pre.getRespawnLocation());
        }

        public Boolean isBedSpawn() {
            return pre.isBedSpawn();
        }
    }

    @abstraction(type=Implementation.Type.BUKKIT)
    public static class BukkitMCPlayerDeathEvent extends BukkitMCEntityDeathEvent implements MCPlayerDeathEvent {
        PlayerDeathEvent pde;
		
        public BukkitMCPlayerDeathEvent(PlayerDeathEvent event) {
        	super(event);
            pde = event;
        }

		@Override
        public Object _GetObject() {
            return pde;
        }

        public static BukkitMCPlayerDeathEvent _instantiate(MCPlayer entity, List<MCItemStack> listOfDrops,
                int droppedExp, String deathMessage){
            List<ItemStack> drops = new ArrayList<ItemStack>();
			
            return new BukkitMCPlayerDeathEvent(new PlayerDeathEvent((Player) entity.getHandle(), drops, droppedExp, deathMessage));
        }
        
        @Override
        public MCPlayer getEntity() {
            return new BukkitMCPlayer(pde.getEntity());
        }
		
		public MCEntity getKiller() {
			return BukkitConvertor.BukkitGetCorrectEntity(pde.getEntity().getKiller());
		}

        public String getDeathMessage() {
            return pde.getDeathMessage();
        }

        public void setDeathMessage(String nval) {
            pde.setDeathMessage(nval);
        }

		public boolean getKeepLevel() {
			return pde.getKeepLevel();
		}

		public void setKeepLevel(boolean keepLevel) {
			pde.setKeepLevel(keepLevel);
		}

		public int getNewExp() {
			return pde.getNewExp();
		}

		public void setNewExp(int exp) {
			pde.setNewExp(exp);
		}

		public int getNewLevel() {
			return pde.getNewLevel();
		}

		public void setNewLevel(int level) {
			pde.setNewLevel(level);
		}

		public int getNewTotalExp() {
			return pde.getNewTotalExp();
		}

		public void setNewTotalExp(int totalExp) {
			pde.setNewTotalExp(totalExp);
		}
    }

    @abstraction(type=Implementation.Type.BUKKIT)
    public static class BukkitMCPlayerCommandEvent extends BukkitMCPlayerEvent implements MCPlayerCommandEvent {

        PlayerCommandPreprocessEvent pcpe;
        boolean isCancelled = false;

        public BukkitMCPlayerCommandEvent(PlayerCommandPreprocessEvent event){
			super(event);
            this.pcpe = event;
        }

		@Override
        public Object _GetObject() {
            return pcpe;
        }

        public String getCommand(){
            return pcpe.getMessage();
        }

        public void cancel() {
            pcpe.setMessage("/commandhelper null");
			pcpe.setCancelled(true);
            isCancelled = true;
        }

        public static BukkitMCPlayerCommandEvent _instantiate(MCPlayer entity, String command){
            return new BukkitMCPlayerCommandEvent(new PlayerCommandPreprocessEvent((Player) entity.getHandle(), command));
        }

        public void setCommand(String val) {
            pcpe.setMessage(val);
        }

        public boolean isCancelled() {
            return isCancelled;
        }
    }

    @abstraction(type=Implementation.Type.BUKKIT)
    public static class BukkitMCWorldChangedEvent extends BukkitMCPlayerEvent implements MCWorldChangedEvent {

        PlayerChangedWorldEvent pcwe;

        public BukkitMCWorldChangedEvent(PlayerChangedWorldEvent e) {
			super(e);
            pcwe = e;
        }

        public MCWorld getFrom() {
            return new BukkitMCWorld(pcwe.getFrom());
        }

        public MCWorld getTo() {
            return new BukkitMCWorld(pcwe.getPlayer().getWorld());
        }

		@Override
        public Object _GetObject() {
            return pcwe;
        }

        public static BukkitMCWorldChangedEvent _instantiate(MCPlayer entity, MCWorld from){
            return new BukkitMCWorldChangedEvent(new PlayerChangedWorldEvent((Player) entity.getHandle(), ((BukkitMCWorld)from).__World()));
        }

    }

	@abstraction(type=Implementation.Type.BUKKIT)
    public static class BukkitMCPlayerMovedEvent implements MCPlayerMoveEvent{

		BukkitMCLocation from;
		BukkitMCLocation to;
		BukkitMCPlayer player;
		boolean cancelled = false;
		public BukkitMCPlayerMovedEvent(Player p, Location from, Location to){
			this.player = new BukkitMCPlayer(p);
			this.from = new BukkitMCLocation(from);
			this.to = new BukkitMCLocation(to);
		}

		public MCLocation getFrom() {
			return from;
		}

		public MCLocation getTo() {
			return to;
		}

		public MCPlayer getPlayer() {
			return player;
		}

		public Object _GetObject() {
			return null;
		}

		public void setCancelled(boolean state) {
			cancelled = state;
		}

		public boolean isCancelled() {
			return cancelled;
		}
	}

	@abstraction(type = Implementation.Type.BUKKIT)
	public static class BukkitMCPlayerFishEvent extends BukkitMCPlayerEvent implements MCPlayerFishEvent {
	
		PlayerFishEvent pfe;

		public BukkitMCPlayerFishEvent(PlayerFishEvent event) {
			super(event);
			this.pfe = event;
		}

		@Override
		public Object _GetObject() {
			return pfe;
		}

		public MCEntity getCaught() {
			if (pfe.getCaught() == null) {
				return null;
			}
			return BukkitConvertor.BukkitGetCorrectEntity(pfe.getCaught());
		}
	
		public int getExpToDrop() {
			return pfe.getExpToDrop();
		}

		public MCFish getHook() {
			return new BukkitMCFish(pfe.getHook());
		}
	
		public MCFishingState getState() {
			return BukkitMCFishingState.getConvertor().getAbstractedEnum(pfe.getState());
		}
	
		public void setExpToDrop(int exp) {
			pfe.setExpToDrop(exp);
		}
	}

	@abstraction(type = Implementation.Type.BUKKIT)
	public static class BukkitMCGamemodeChangeEvent extends BukkitMCPlayerEvent implements MCGamemodeChangeEvent {

		PlayerGameModeChangeEvent gmc;

		public BukkitMCGamemodeChangeEvent(PlayerGameModeChangeEvent event) {
			super(event);
			gmc = event;
		}

		public MCGameMode getNewGameMode() {
			return BukkitMCGameMode.getConvertor().getAbstractedEnum(gmc.getNewGameMode());
		}

		@Override
		public Object _GetObject() {
			return gmc;
		}
	}

	@abstraction(type = Implementation.Type.BUKKIT)
	public static class BukkitMCChatTabCompleteEvent extends BukkitMCPlayerEvent implements MCChatTabCompleteEvent {
	
		PlayerChatTabCompleteEvent tc;
		public BukkitMCChatTabCompleteEvent(PlayerChatTabCompleteEvent event) {
			super(event);
			this.tc = event;
		}

		@Override
		public Object _GetObject() {
			return tc;
		}
	
		public String getChatMessage() {
			return tc.getChatMessage();
		}
	
		public String getLastToken() {
			return tc.getLastToken();
		}
	
		public Collection<String> getTabCompletions() {
			return tc.getTabCompletions();
		}
	}

	@abstraction(type = Implementation.Type.BUKKIT)
	public static class BukkitMCExpChangeEvent extends BukkitMCPlayerEvent implements MCExpChangeEvent {

		PlayerExpChangeEvent ec;

		public BukkitMCExpChangeEvent(PlayerExpChangeEvent event) {
			super(event);
			ec = event;
		}

		@Override
		public Object _GetObject() {
			return ec;
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
	public static class BukkitMCPlayerEditBookEvent extends BukkitMCPlayerEvent implements MCPlayerEditBookEvent {

		PlayerEditBookEvent pebe;

		public BukkitMCPlayerEditBookEvent(PlayerEditBookEvent event) {
			super(event);
			this.pebe = event;
		}

		@Override
		public Object _GetObject() {
			return pebe;
		}

		public MCBookMeta getNewBookMeta() {
			return new BukkitMCBookMeta(pebe.getNewBookMeta());
		}

		public MCBookMeta getPreviousBookMeta() {
			return new BukkitMCBookMeta(pebe.getPreviousBookMeta());
		}

		public void setNewBookMeta(MCBookMeta bookMeta) {
			pebe.setNewBookMeta(((BukkitMCBookMeta) bookMeta).getBookMeta());
		}

		public int getSlot() {
			return pebe.getSlot();
		}

		public boolean isSigning() {
			return pebe.isSigning();
		}

		public void setSigning(boolean isSigning) {
			pebe.setSigning(isSigning);
		}
	}

	@abstraction(type = Implementation.Type.BUKKIT)
	public static class BukkitMCPlayerToggleFlightEvent extends BukkitMCPlayerEvent implements MCPlayerToggleFlightEvent {

		PlayerToggleFlightEvent ptfe;

		public BukkitMCPlayerToggleFlightEvent(PlayerToggleFlightEvent event) {
			super(event);
			this.ptfe = event;
		}

		@Override
		public Object _GetObject() {
			return ptfe;
		}

		public boolean isFlying() {
			return ptfe.isFlying();
		}
	}

	@abstraction(type = Implementation.Type.BUKKIT)
	public static class BukkitMCPlayerToggleSneakEvent extends BukkitMCPlayerEvent implements MCPlayerToggleSneakEvent {

		PlayerToggleSneakEvent ptse;

		public BukkitMCPlayerToggleSneakEvent(PlayerToggleSneakEvent event) {
			super(event);
			this.ptse = event;
		}

		@Override
		public Object _GetObject() {
			return ptse;
		}

		public boolean isSneaking() {
			return ptse.isSneaking();
		}
	}

	@abstraction(type = Implementation.Type.BUKKIT)
	public static class BukkitMCPlayerToggleSprintEvent extends BukkitMCPlayerEvent implements MCPlayerToggleSprintEvent {

		PlayerToggleSprintEvent ptse;

		public BukkitMCPlayerToggleSprintEvent(PlayerToggleSprintEvent event) {
			super(event);
			this.ptse = event;
		}

		@Override
		public Object _GetObject() {
			return ptse;
		}

		public boolean isSprinting() {
			return ptse.isSprinting();
		}
	}

	@abstraction(type = Implementation.Type.BUKKIT)
	public static class BukkitMCPlayerVelocityEvent extends BukkitMCPlayerEvent implements MCPlayerVelocityEvent {

		PlayerVelocityEvent pve;

		public BukkitMCPlayerVelocityEvent(PlayerVelocityEvent event) {
			super(event);
			this.pve = event;
		}

		@Override
		public Object _GetObject() {
			return pve;
		}

		public MCVector getVelocity() {
			return new BukkitMCVector(pve.getVelocity());
		}

		public void setVelocity(MCVector velocity) {
			pve.setVelocity((Vector) velocity.getHandle());
		}
	}
}