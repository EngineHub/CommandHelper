

package com.laytonsmith.abstraction.bukkit.events;

import com.laytonsmith.abstraction.*;
import com.laytonsmith.abstraction.blocks.MCBlock;
import com.laytonsmith.abstraction.blocks.MCBlockFace;
import com.laytonsmith.abstraction.bukkit.BukkitMCBookMeta;
import com.laytonsmith.abstraction.bukkit.BukkitMCEntity;
import com.laytonsmith.abstraction.bukkit.BukkitMCItemStack;
import com.laytonsmith.abstraction.bukkit.BukkitMCLocation;
import com.laytonsmith.abstraction.bukkit.BukkitMCPlayer;
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
import org.bukkit.event.player.PlayerPreLoginEvent.Result;
import org.bukkit.inventory.ItemStack;

/**
 *
 * @author layton
 */
public class BukkitPlayerEvents {

	public static class BukkitMCPlayerItemConsumeEvent 
			implements MCPlayerItemConsumeEvent {
		PlayerItemConsumeEvent pic;
		public BukkitMCPlayerItemConsumeEvent(PlayerItemConsumeEvent event) {
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

		@Override
		public Object _GetObject() {
			return pic;
		}
		
		public static BukkitMCPlayerItemConsumeEvent _instantiate(
				MCPlayer player, MCItemStack item) {
			return new BukkitMCPlayerItemConsumeEvent(
					new PlayerItemConsumeEvent(((BukkitMCPlayer) player)._Player(), 
							((BukkitMCItemStack) item).asItemStack()));
		}

		@Override
		public MCPlayer getPlayer() {
            return new BukkitMCPlayer(pic.getPlayer());
        }
		
	}

	public static class BukkitMCPlayerBedEvent implements MCPlayerBedEvent {
		MCBlock block;
		PlayerEvent event;

		public BukkitMCPlayerBedEvent(PlayerBedEnterEvent event) {
			this.event = event;
			this.block = new BukkitMCBlock(event.getBed());
		}
		
		public BukkitMCPlayerBedEvent(PlayerBedLeaveEvent event) {
			this.event = event;
			this.block = new BukkitMCBlock(event.getBed());
		}
		
		@Override
		public MCBlock getBed() {
			return block;
		}

		@Override
		public MCPlayer getPlayer() {
			return new BukkitMCPlayer(event.getPlayer());
		}

		@Override
		public Object _GetObject() {
			return event;
		}
	}

    public static class BukkitMCPlayerKickEvent implements MCPlayerKickEvent {
        PlayerKickEvent e;

        public BukkitMCPlayerKickEvent(PlayerKickEvent e){
            this.e = e;
        }

		@Override
        public Object _GetObject() {
            return e;
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

		@Override
        public MCPlayer getPlayer() {
            return new BukkitMCPlayer(e.getPlayer());
        }

    }

	@abstraction(type=Implementation.Type.BUKKIT)
	public static class BukkitMCPlayerTeleportEvent implements MCPlayerTeleportEvent {
		PlayerTeleportEvent e;

		public BukkitMCPlayerTeleportEvent(PlayerTeleportEvent e) {
			this.e = e;
		}

		@Override
		public MCPlayer getPlayer() {
			return new BukkitMCPlayer(e.getPlayer());
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
		public Object _GetObject() {
			return e;
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

	public static class BukkitMCPlayerLoginEvent implements MCPlayerLoginEvent {
		PlayerLoginEvent event;

		public BukkitMCPlayerLoginEvent(PlayerLoginEvent e) {
            event = e;
        }

		@Override
		public Object _GetObject() {
			return event;
		}

		@Override
		public String getName() {
			return event.getPlayer().getName();
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

		@Override
		public MCPlayer getPlayer() {
			return new BukkitMCPlayer(event.getPlayer());
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
        public static class BukkitMCPlayerChatEvent implements MCPlayerChatEvent{
        AsyncPlayerChatEvent pce;
        public BukkitMCPlayerChatEvent(AsyncPlayerChatEvent event) {
            pce = event;
        }

        public BukkitMCPlayerChatEvent(BukkitMCPlayerChatEvent event) {
            pce = event.pce;
        }

		@Override
        public Object _GetObject() {
            return pce;
        }

        public static BukkitMCPlayerChatEvent _instantiate(MCPlayer player, String message, String format){
			AsyncPlayerChatEvent apce = new AsyncPlayerChatEvent(false, ((BukkitMCPlayer)player)._Player(), message,
                    new HashSet<Player>(Arrays.asList(Bukkit.getServer().getOnlinePlayers())));
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
            List<MCPlayer> players = new ArrayList<MCPlayer>();
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
        public MCPlayer getPlayer() {
            return new BukkitMCPlayer(pce.getPlayer());
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
    public static class BukkitMCPlayerQuitEvent implements MCPlayerQuitEvent{
        PlayerQuitEvent pce;
        public BukkitMCPlayerQuitEvent(PlayerQuitEvent event) {
            pce = event;
        }

		@Override
        public Object _GetObject() {
            return pce;
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

		@Override
        public MCPlayer getPlayer() {
            return new BukkitMCPlayer(pce.getPlayer());
        }

    }


    @abstraction(type=Implementation.Type.BUKKIT)
    public static class BukkitMCPlayerJoinEvent implements MCPlayerJoinEvent{
        PlayerJoinEvent pje;
        public BukkitMCPlayerJoinEvent(PlayerJoinEvent e){
            pje = e;
        }

		@Override
        public MCPlayer getPlayer() {
            return new BukkitMCPlayer(pje.getPlayer());
        }

		@Override
        public String getJoinMessage() {
            return pje.getJoinMessage();
        }

		@Override
        public void setJoinMessage(String message) {
            pje.setJoinMessage(message);
        }

		@Override
        public Object _GetObject() {
            return pje;
        }

        public static PlayerJoinEvent _instantiate(MCPlayer player, String message) {
            return new PlayerJoinEvent(((BukkitMCPlayer)player)._Player(), message);
        }

    }

    @abstraction(type=Implementation.Type.BUKKIT)
    public static class BukkitMCPlayerInteractEvent implements MCPlayerInteractEvent{

        PlayerInteractEvent pie;

        public BukkitMCPlayerInteractEvent(PlayerInteractEvent e){
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
        public MCPlayer getPlayer() {
            return new BukkitMCPlayer(pie.getPlayer());
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

		@Override
        public Object _GetObject() {
            return pie;
        }

    }

    @abstraction(type=Implementation.Type.BUKKIT)
    public static class BukkitMCPlayerRespawnEvent implements MCPlayerRespawnEvent {

        PlayerRespawnEvent pre;
        public BukkitMCPlayerRespawnEvent(PlayerRespawnEvent event) {
            pre = event;
        }

		@Override
        public Object _GetObject() {
            return pre;
        }

        public static BukkitMCPlayerRespawnEvent _instantiate(MCPlayer player, MCLocation location, boolean isBedSpawn) {
            return new BukkitMCPlayerRespawnEvent(new PlayerRespawnEvent(((BukkitMCPlayer)player)._Player(),
					((BukkitMCLocation)location)._Location(), isBedSpawn));
        }

		@Override
        public MCPlayer getPlayer() {
            return new BukkitMCPlayer(pre.getPlayer());
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

		@Override
        public Object _GetObject() {
            return pde;
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
    public static class BukkitMCPlayerCommandEvent implements MCPlayerCommandEvent{
        PlayerCommandPreprocessEvent pcpe;
        boolean isCancelled = false;
        public BukkitMCPlayerCommandEvent(PlayerCommandPreprocessEvent pcpe){
            this.pcpe = pcpe;
        }
		@Override
        public Object _GetObject() {
            return pcpe;
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

		@Override
        public MCPlayer getPlayer() {
            return new BukkitMCPlayer(pcpe.getPlayer());
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
    public static class BukkitMCWorldChangedEvent implements MCWorldChangedEvent{
        PlayerChangedWorldEvent pcwe;
        public BukkitMCWorldChangedEvent(PlayerChangedWorldEvent e){
            pcwe = e;
        }

		@Override
        public MCPlayer getPlayer() {
            return new BukkitMCPlayer(pcwe.getPlayer());
        }

		@Override
        public MCWorld getFrom() {
            return new BukkitMCWorld(pcwe.getFrom());
        }

		@Override
        public MCWorld getTo() {
            return new BukkitMCWorld(pcwe.getPlayer().getWorld());
        }

		@Override
        public Object _GetObject() {
            return pcwe;
        }

        public static BukkitMCWorldChangedEvent _instantiate(MCPlayer entity, MCWorld from){
            return new BukkitMCWorldChangedEvent(new PlayerChangedWorldEvent(((BukkitMCPlayer)entity)._Player(), ((BukkitMCWorld)from).__World()));
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

		@Override
		public MCLocation getFrom() {
			return from;
		}

		@Override
		public MCLocation getTo() {
			return to;
		}

		@Override
		public MCPlayer getPlayer() {
			return player;
		}

		@Override
		public Object _GetObject() {
			return null;
		}

		@Override
		public void setCancelled(boolean state) {
			cancelled = state;
		}

		@Override
		public boolean isCancelled() {
			return cancelled;
		}

	}

	public static class BukkitMCPlayerFishEvent implements MCPlayerFishEvent {
	
		PlayerFishEvent e;
		public BukkitMCPlayerFishEvent(PlayerFishEvent event) {
			e = event;
		}
		
		@Override
		public Object _GetObject() {
			return e;
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
		
		@Override
		public MCPlayer getPlayer() {
			return new BukkitMCPlayer(e.getPlayer());
		}
	}
	
	public static class BukkitMCGamemodeChangeEvent implements MCGamemodeChangeEvent {

		PlayerGameModeChangeEvent gmc;
		public BukkitMCGamemodeChangeEvent(PlayerGameModeChangeEvent event) {
			gmc = event;
		}
		
		@Override
		public MCGameMode getNewGameMode() {
			return BukkitMCGameMode.getConvertor().getAbstractedEnum(gmc.getNewGameMode());
		}
		
		@Override
		public MCPlayer getPlayer() {
			return new BukkitMCPlayer(gmc.getPlayer());
		}

		@Override
		public Object _GetObject() {
			return gmc;
		}
	}

	public static class BukkitMCChatTabCompleteEvent implements MCChatTabCompleteEvent {
	
		PlayerChatTabCompleteEvent tc;
		public BukkitMCChatTabCompleteEvent(PlayerChatTabCompleteEvent event) {
			this.tc = event;
		}
	
		@Override
		public Object _GetObject() {
			return tc;
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

		@Override
		public MCPlayer getPlayer() {
			return new BukkitMCPlayer(tc.getPlayer());
		}
	}

	public static class BukkitMCExpChangeEvent implements MCExpChangeEvent {

		PlayerExpChangeEvent ec;
		public BukkitMCExpChangeEvent(PlayerExpChangeEvent event) {
			ec = event;
		}
		
		@Override
		public MCPlayer getPlayer() {
			return new BukkitMCPlayer(ec.getPlayer());
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
	public static class BukkitMCPlayerEditBookEvent implements MCPlayerEditBookEvent {

		PlayerEditBookEvent pebe;

		public BukkitMCPlayerEditBookEvent(PlayerEditBookEvent event) {
			pebe = event;
		}

		@Override
		public Object _GetObject() {
			return pebe;
		}

		@Override
		public MCPlayer getPlayer() {
			return new BukkitMCPlayer(pebe.getPlayer());
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
}
