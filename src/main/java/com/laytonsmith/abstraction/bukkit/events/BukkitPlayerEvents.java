/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.abstraction.bukkit.events;

import com.laytonsmith.abstraction.blocks.MCBlockFace;
import com.laytonsmith.abstraction.*;
import com.laytonsmith.abstraction.blocks.MCBlock;
import com.laytonsmith.abstraction.bukkit.BukkitMCItemStack;
import com.laytonsmith.abstraction.bukkit.BukkitMCLocation;
import com.laytonsmith.abstraction.bukkit.BukkitMCPlayer;
import com.laytonsmith.abstraction.bukkit.BukkitMCWorld;
import com.laytonsmith.abstraction.bukkit.blocks.BukkitMCBlock;
import com.laytonsmith.abstraction.events.*;
import com.laytonsmith.core.events.abstraction;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
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
	
	@abstraction(type=Implementation.Type.BUKKIT)
    public static class BukkitMCPlayerChatEvent implements MCPlayerChatEvent{
        public static BukkitMCPlayerChatEvent _instantiate(MCPlayer player, String message){
            return new BukkitMCPlayerChatEvent(new PlayerChatEvent(((BukkitMCPlayer)player)._Player(), message));
        }
        PlayerChatEvent pce;

        public BukkitMCPlayerChatEvent(PlayerChatEvent event) {
            pce = event;            
        }
        
        public Object _GetObject() {
            return pce;
        }

        public String getMessage() {
            return pce.getMessage();
        }

        public MCPlayer getPlayer() {
            return new BukkitMCPlayer(pce.getPlayer());
        }

        public List<MCPlayer> getRecipients() {
            List<MCPlayer> players = new ArrayList<MCPlayer>();
            for(Player p : pce.getRecipients()){
                players.add(new BukkitMCPlayer(p));
            }
            return players;            
        }

        public void setMessage(String message) {
            pce.setMessage(message);
        }

        public void setRecipients(List<MCPlayer> list) {
            pce.getRecipients().clear();
            for(MCPlayer p  : list){
                pce.getRecipients().add(((BukkitMCPlayer)p)._Player());
            }
        }
        
    }
	
	@abstraction(type=Implementation.Type.BUKKIT)
    public static class BukkitMCPlayerCommandEvent implements MCPlayerCommandEvent{
        public static BukkitMCPlayerCommandEvent _instantiate(MCPlayer entity, String command){
            return new BukkitMCPlayerCommandEvent(new PlayerCommandPreprocessEvent(((BukkitMCPlayer)entity)._Player(), command));
        }
        boolean isCancelled = false;
        PlayerCommandPreprocessEvent pcpe;
        public BukkitMCPlayerCommandEvent(PlayerCommandPreprocessEvent pcpe){
            this.pcpe = pcpe;
        }
        
        public Object _GetObject() {            
            return pcpe;
        }

        public void cancel() {
            pcpe.setMessage("/commandhelper null");
            isCancelled = true;
        }

        public String getCommand(){
            return pcpe.getMessage();
        }                
        
        public MCPlayer getPlayer() {
            return new BukkitMCPlayer(pcpe.getPlayer());
        }

        public boolean isCancelled() {
            return isCancelled;
        }

        public void setCommand(String val) {
            pcpe.setMessage(val);
        }
        
    }

	@abstraction(type=Implementation.Type.BUKKIT)
    public static class BukkitMCPlayerDeathEvent implements MCPlayerDeathEvent {
        public static BukkitMCPlayerDeathEvent _instantiate(MCPlayer entity, List<MCItemStack> listOfDrops,
                int droppedExp, String deathMessage){
            List<ItemStack> drops = new ArrayList<ItemStack>();
            return new BukkitMCPlayerDeathEvent(new PlayerDeathEvent(((BukkitMCPlayer)entity)._Player(), drops, droppedExp, deathMessage));
        }
        EntityDeathEvent ede;

        public BukkitMCPlayerDeathEvent(EntityDeathEvent event) {
            ede = event;
        }
        
        public Object _GetObject() {
            return ede;
        }

        public void addDrop(MCItemStack is){
            ede.getDrops().add(((BukkitMCItemStack)is).__ItemStack());
        }
        public void clearDrops() {
            ede.getDrops().clear();
        }
        public String getDeathMessage() {
            return ((PlayerDeathEvent)ede).getDeathMessage();
        }

        public int getDroppedExp() {
            return ede.getDroppedExp();
        }

        public List<MCItemStack> getDrops() {
            List<ItemStack> islist = ede.getDrops();
            List<MCItemStack> drops = new ArrayList<MCItemStack>();
            for(ItemStack is : islist){
                drops.add(new BukkitMCItemStack(is));
            }
            return drops;
        }

        public MCEntity getEntity() {
            return new BukkitMCPlayer((Player)ede.getEntity());
        }

        public void setDeathMessage(String nval) {
            ((PlayerDeathEvent)ede).setDeathMessage(nval);
        }

        public void setDroppedExp(int i) {
            ede.setDroppedExp(i);
        }

    }
	
	@abstraction(type=Implementation.Type.BUKKIT)
    public static class BukkitMCPlayerInteractEvent implements MCPlayerInteractEvent{

        public static BukkitMCPlayerInteractEvent _instantiate(MCPlayer player, MCAction action, MCItemStack itemstack,
                MCBlock clickedBlock, MCBlockFace clickedFace){
            return new BukkitMCPlayerInteractEvent(new PlayerInteractEvent(((BukkitMCPlayer)player)._Player(), 
                    Action.valueOf(action.name()), ((BukkitMCItemStack)itemstack).__ItemStack(),
                    ((BukkitMCBlock)clickedBlock).__Block(), BlockFace.valueOf(clickedFace.name())));
        }
        
        PlayerInteractEvent pie;
        
        public BukkitMCPlayerInteractEvent(PlayerInteractEvent e){
            pie = e;
        }
        
        public Object _GetObject() {
            return pie;
        }

        public MCAction getAction() {
            return MCAction.valueOf(pie.getAction().name());
        }

        public MCBlockFace getBlockFace() {
            return MCBlockFace.valueOf(pie.getBlockFace().name());
        }

        public MCBlock getClickedBlock() {
            return new BukkitMCBlock(pie.getClickedBlock());
        }

        public MCItemStack getItem() {
            return new BukkitMCItemStack(pie.getItem());
        }

        public MCPlayer getPlayer() {
            return new BukkitMCPlayer(pie.getPlayer());
        }
        
    }

    
    @abstraction(type=Implementation.Type.BUKKIT)
    public static class BukkitMCPlayerJoinEvent implements MCPlayerJoinEvent{
        public static PlayerJoinEvent _instantiate(MCPlayer player, String message) {
            return new PlayerJoinEvent(((BukkitMCPlayer)player)._Player(), message);
        }
        PlayerJoinEvent pje;

        public BukkitMCPlayerJoinEvent(PlayerJoinEvent e){
            pje = e;
        }

        public Object _GetObject() {
            return pje;
        }

        public String getJoinMessage() {
            return pje.getJoinMessage();
        }

        public MCPlayer getPlayer() {
            return new BukkitMCPlayer(pje.getPlayer());
        }

        public void setJoinMessage(String message) {
            pje.setJoinMessage(message);
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

		public String getIP() {
			return event.getAddress().getHostAddress();
		}

		public String getKickMessage() {
			return event.getKickMessage();
		}

		public String getName() {
			return event.getPlayer().getName();
		}

		public MCPlayer getPlayer() {
			return new BukkitMCPlayer(event.getPlayer());
		}

		public String getResult() {
			return event.getResult().toString();
		}

		public void setKickMessage(String msg) {
			event.setKickMessage(msg);
		}

		public void setResult(String rst) {
			event.setResult(PlayerLoginEvent.Result.valueOf(rst.toUpperCase()));
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

		public String getIP() {
			return event.getAddress().toString();
		}

		public String getKickMessage() {
			return event.getKickMessage();
		}

		public String getName() {
			return event.getName();
		}

		public String getResult() {
			return event.getResult().toString();
		}

		public void setKickMessage(String msg) {
			event.setKickMessage(msg);
		}

		public void setResult(String rst) {
			event.setResult(Result.valueOf(rst.toUpperCase()));
		}
		
	}
    
    @abstraction(type=Implementation.Type.BUKKIT)
    public static class BukkitMCPlayerQuitEvent implements MCPlayerQuitEvent{
        public static BukkitMCPlayerQuitEvent _instantiate(MCPlayer player, String message){
            return new BukkitMCPlayerQuitEvent(new PlayerQuitEvent(((BukkitMCPlayer)player)._Player(), message));
        }
        PlayerQuitEvent pce;

        public BukkitMCPlayerQuitEvent(PlayerQuitEvent event) {
            pce = event;            
        }
        
        public Object _GetObject() {
            return pce;
        }

        public String getMessage() {
            return pce.getQuitMessage();
        }

        public MCPlayer getPlayer() {
            return new BukkitMCPlayer(pce.getPlayer());
        }

        public void setMessage(String message) {
            pce.setQuitMessage(message);
        }
        
    }
    
    @abstraction(type=Implementation.Type.BUKKIT)
    public static class BukkitMCPlayerRespawnEvent implements MCPlayerRespawnEvent {

        public static BukkitMCPlayerRespawnEvent _instantiate(MCPlayer player, MCLocation location,
                boolean isBedSpawn){
            return new BukkitMCPlayerRespawnEvent(new PlayerRespawnEvent(((BukkitMCPlayer)player)._Player(),
                    ((BukkitMCLocation)location)._Location(), isBedSpawn));
        }
        PlayerRespawnEvent pre;

        public BukkitMCPlayerRespawnEvent(PlayerRespawnEvent event) {
            pre = event;
        }
        
        public Object _GetObject() {
            return pre;
        }

        public MCPlayer getPlayer() {
            return new BukkitMCPlayer(pre.getPlayer());
        }

        public MCLocation getRespawnLocation() {
            return new BukkitMCLocation(pre.getRespawnLocation());
        }

        public void setRespawnLocation(MCLocation location) {
            pre.setRespawnLocation(((BukkitMCLocation)location)._Location());
        }
    }
    
    @abstraction(type=Implementation.Type.BUKKIT)
    public static class BukkitMCWorldChangedEvent implements MCWorldChangedEvent{
        public static BukkitMCWorldChangedEvent _instantiate(MCPlayer entity, MCWorld from){
            return new BukkitMCWorldChangedEvent(new PlayerChangedWorldEvent(((BukkitMCPlayer)entity)._Player(), ((BukkitMCWorld)from).__World()));
        }
        PlayerChangedWorldEvent pcwe;

        public BukkitMCWorldChangedEvent(PlayerChangedWorldEvent e){
            pcwe = e;
        }

        public Object _GetObject() {
            return pcwe;
        }

        public MCWorld getFrom() {
            return new BukkitMCWorld(pcwe.getFrom());
        }

        public MCPlayer getPlayer() {
            return new BukkitMCPlayer(pcwe.getPlayer());
        }
        
        public MCWorld getTo() {
            return new BukkitMCWorld(pcwe.getPlayer().getWorld());
        }
        
    }
   
}
