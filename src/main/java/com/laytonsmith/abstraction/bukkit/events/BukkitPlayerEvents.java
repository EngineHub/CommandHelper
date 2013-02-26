

package com.laytonsmith.abstraction.bukkit.events;

import com.laytonsmith.abstraction.*;
import com.laytonsmith.abstraction.blocks.MCBlock;
import com.laytonsmith.abstraction.blocks.MCBlockFace;
import com.laytonsmith.abstraction.bukkit.BukkitMCItemStack;
import com.laytonsmith.abstraction.bukkit.BukkitMCLocation;
import com.laytonsmith.abstraction.bukkit.BukkitMCPlayer;
import com.laytonsmith.abstraction.bukkit.BukkitMCWorld;
import com.laytonsmith.abstraction.bukkit.blocks.BukkitMCBlock;
import com.laytonsmith.abstraction.enums.MCAction;
import com.laytonsmith.abstraction.enums.bukkit.BukkitMCAction;
import com.laytonsmith.abstraction.events.*;
import com.laytonsmith.annotations.abstraction;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.player.PlayerPreLoginEvent.Result;
import org.bukkit.inventory.ItemStack;

/**
 *
 * @author layton
 */
public class BukkitPlayerEvents {

    public static class BukkitMCPlayerKickEvent implements MCPlayerKickEvent {
        PlayerKickEvent e;

        public BukkitMCPlayerKickEvent(PlayerKickEvent e){
            this.e = e;
        }

        public Object _GetObject() {
            return e;
        }

        public String getMessage() {
            return e.getLeaveMessage();
        }

        public void setMessage(String message) {
            e.setLeaveMessage(message);
        }

        public String getReason() {
            return e.getReason();
        }

        public void setReason(String message) {
            e.setReason(message);
        }

        public boolean isCancelled() {
            return e.isCancelled();
        }

        public void setCancelled(boolean cancelled) {
            e.setCancelled(cancelled);
        }

        public MCPlayer getPlayer() {
            return new BukkitMCPlayer(e.getPlayer());
        }

    }

	public static class BukkitMCPlayerTeleportEvent implements MCPlayerTeleportEvent {
		PlayerTeleportEvent e;

		public BukkitMCPlayerTeleportEvent(PlayerTeleportEvent e) {
			this.e = e;
		}

		public MCPlayer getPlayer() {
			return new BukkitMCPlayer(e.getPlayer());
		}

		public MCLocation getFrom() {
			return new BukkitMCLocation(e.getFrom());
		}

		public MCLocation getTo() {
			return new BukkitMCLocation(e.getTo());
		}

		public String getCause() {
			return e.getCause().name();
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

			e.setTo(loc);
		}

		public Object _GetObject() {
			return e;
		}

		public void setCancelled(boolean state) {
			e.setCancelled(state);
		}

		public boolean isCancelled() {
			return e.isCancelled();
		}
	}

	public static class BukkitMCPlayerLoginEvent implements MCPlayerLoginEvent {
		PlayerLoginEvent event;

		public BukkitMCPlayerLoginEvent(PlayerLoginEvent e) {
            event = e;
        }

		public Object _GetObject() {
			return event;
		}

		public String getName() {
			return event.getPlayer().getName();
		}

		public String getKickMessage() {
			return event.getKickMessage();
		}

		public void setKickMessage(String msg) {
			event.setKickMessage(msg);
		}

		public String getResult() {
			return event.getResult().toString();
		}

		public void setResult(String rst) {
			event.setResult(PlayerLoginEvent.Result.valueOf(rst.toUpperCase()));
		}

		public String getIP() {
			return event.getAddress().getHostAddress();
		}

		public MCPlayer getPlayer() {
			return new BukkitMCPlayer(event.getPlayer());
		}

	}

	public static class BukkitMCPlayerPreLoginEvent implements MCPlayerPreLoginEvent {
		PlayerPreLoginEvent event;

		public BukkitMCPlayerPreLoginEvent(PlayerPreLoginEvent e) {
            event = e;
        }

		public Object _GetObject() {
			return event;
		}

		public String getName() {
			return event.getName();
		}

		public String getKickMessage() {
			return event.getKickMessage();
		}

		public void setKickMessage(String msg) {
			event.setKickMessage(msg);
		}

		public String getResult() {
			return event.getResult().toString();
		}

		public void setResult(String rst) {
			event.setResult(Result.valueOf(rst.toUpperCase()));
		}

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

        public Object _GetObject() {
            return pce;
        }

        public static BukkitMCPlayerChatEvent _instantiate(MCPlayer player, String message, String format){
			AsyncPlayerChatEvent apce = new AsyncPlayerChatEvent(false, ((BukkitMCPlayer)player)._Player(), message,
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
                pce.getRecipients().add(((BukkitMCPlayer)p)._Player());
            }
        }

        public MCPlayer getPlayer() {
            return new BukkitMCPlayer(pce.getPlayer());
        }

		public String getFormat() {
			return pce.getFormat();
		}

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

        public Object _GetObject() {
            return pce;
        }

        public static BukkitMCPlayerQuitEvent _instantiate(MCPlayer player, String message){
            return new BukkitMCPlayerQuitEvent(new PlayerQuitEvent(((BukkitMCPlayer)player)._Player(), message));
        }

        public String getMessage() {
            return pce.getQuitMessage();
        }

        public void setMessage(String message) {
            pce.setQuitMessage(message);
        }

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

        public MCPlayer getPlayer() {
            return new BukkitMCPlayer(pje.getPlayer());
        }

        public String getJoinMessage() {
            return pje.getJoinMessage();
        }

        public void setJoinMessage(String message) {
            pje.setJoinMessage(message);
        }

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

        public MCAction getAction() {
            return BukkitMCAction.getConvertor().getAbstractedEnum(pie.getAction());
        }

        public MCPlayer getPlayer() {
            return new BukkitMCPlayer(pie.getPlayer());
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

        public Object _GetObject() {
            return pre;
        }

        public static BukkitMCPlayerRespawnEvent _instantiate(MCPlayer player, MCLocation location, boolean isBedSpawn) {
            return new BukkitMCPlayerRespawnEvent(new PlayerRespawnEvent(((BukkitMCPlayer)player)._Player(),
					((BukkitMCLocation)location)._Location(), isBedSpawn));
        }

        public MCPlayer getPlayer() {
            return new BukkitMCPlayer(pre.getPlayer());
        }

        public void setRespawnLocation(MCLocation location) {
            pre.setRespawnLocation(((BukkitMCLocation)location)._Location());
        }

        public MCLocation getRespawnLocation() {
            return new BukkitMCLocation(pre.getRespawnLocation());
        }

        public Boolean isBedSpawn() {
            return pre.isBedSpawn();
        }
    }

    @abstraction(type=Implementation.Type.BUKKIT)
    public static class BukkitMCPlayerDeathEvent implements MCPlayerDeathEvent {
        PlayerDeathEvent pde;
		
        public BukkitMCPlayerDeathEvent(PlayerDeathEvent event) {
            pde = event;
        }

        public Object _GetObject() {
            return pde;
        }

        public static BukkitMCPlayerDeathEvent _instantiate(MCPlayer entity, List<MCItemStack> listOfDrops,
                int droppedExp, String deathMessage){
            List<ItemStack> drops = new ArrayList<ItemStack>();
			
            return new BukkitMCPlayerDeathEvent(new PlayerDeathEvent(((BukkitMCPlayer)entity)._Player(), drops, droppedExp, deathMessage));
        }

        public List<MCItemStack> getDrops() {
            List<ItemStack> islist = pde.getDrops();
            List<MCItemStack> drops = new ArrayList<MCItemStack>();
			
            for(ItemStack is : islist){
                drops.add(new BukkitMCItemStack(is));
            }
			
            return drops;
        }
		
        public void clearDrops() {
            pde.getDrops().clear();
        }
		
        public void addDrop(MCItemStack is){
            pde.getDrops().add(((BukkitMCItemStack)is).__ItemStack());
        }

        public MCEntity getEntity() {
            return new BukkitMCPlayer(pde.getEntity());
        }

        public int getDroppedExp() {
            return pde.getDroppedExp();
        }

        public String getDeathMessage() {
            return pde.getDeathMessage();
        }

        public void setDroppedExp(int i) {
            pde.setDroppedExp(i);
        }

        public void setDeathMessage(String nval) {
            pde.setDeathMessage(nval);
        }
    }

    @abstraction(type=Implementation.Type.BUKKIT)
    public static class BukkitMCPlayerCommandEvent implements MCPlayerCommandEvent{
        PlayerCommandPreprocessEvent pcpe;
        boolean isCancelled = false;
        public BukkitMCPlayerCommandEvent(PlayerCommandPreprocessEvent pcpe){
            this.pcpe = pcpe;
        }
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

        public MCPlayer getPlayer() {
            return new BukkitMCPlayer(pcpe.getPlayer());
        }

        public static BukkitMCPlayerCommandEvent _instantiate(MCPlayer entity, String command){
            return new BukkitMCPlayerCommandEvent(new PlayerCommandPreprocessEvent(((BukkitMCPlayer)entity)._Player(), command));
        }

        public void setCommand(String val) {
            pcpe.setMessage(val);
        }

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

        public MCPlayer getPlayer() {
            return new BukkitMCPlayer(pcwe.getPlayer());
        }

        public MCWorld getFrom() {
            return new BukkitMCWorld(pcwe.getFrom());
        }

        public MCWorld getTo() {
            return new BukkitMCWorld(pcwe.getPlayer().getWorld());
        }

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

}
