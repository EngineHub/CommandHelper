/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.abstraction.bukkit.events;

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
import org.bukkit.inventory.ItemStack;

/**
 *
 * @author layton
 */
public class BukkitPlayerEvents {

    @abstraction(type=Implementation.Type.BUKKIT)
    public static class BukkitMCPlayerChatEvent implements MCPlayerChatEvent{
        PlayerChatEvent pce;
        public BukkitMCPlayerChatEvent(PlayerChatEvent event) {
            pce = event;            
        }

        public Object _GetObject() {
            return pce;
        }
        
        public static BukkitMCPlayerChatEvent _instantiate(MCPlayer player, String message){
            return new BukkitMCPlayerChatEvent(new PlayerChatEvent(((BukkitMCPlayer)player)._Player(), message));
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
                    Action.valueOf(action.name()), ((BukkitMCItemStack)itemstack).__ItemStack(),
                    ((BukkitMCBlock)clickedBlock).__Block(), BlockFace.valueOf(clickedFace.name())));
        }
        
        public MCAction getAction() {
            return MCAction.valueOf(pie.getAction().name());
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
        
        public static BukkitMCPlayerRespawnEvent _instantiate(MCPlayer player, MCLocation location,
                boolean isBedSpawn){
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
    }
    
    @abstraction(type=Implementation.Type.BUKKIT)
    public static class BukkitMCPlayerDeathEvent implements MCPlayerDeathEvent {
        EntityDeathEvent ede;
        public BukkitMCPlayerDeathEvent(EntityDeathEvent event) {
            ede = event;
        }

        public Object _GetObject() {
            return ede;
        }
        
        public static BukkitMCPlayerDeathEvent _instantiate(MCPlayer entity, List<MCItemStack> listOfDrops,
                int droppedExp, String deathMessage){
            List<ItemStack> drops = new ArrayList<ItemStack>();
            return new BukkitMCPlayerDeathEvent(new PlayerDeathEvent(((BukkitMCPlayer)entity)._Player(), drops, droppedExp, deathMessage));
        }

        public List<MCItemStack> getDrops() {
            List<ItemStack> islist = ede.getDrops();
            List<MCItemStack> drops = new ArrayList<MCItemStack>();
            for(ItemStack is : islist){
                drops.add(new BukkitMCItemStack(is));
            }
            return drops;
        }
        public void clearDrops() {
            ede.getDrops().clear();
        }
        public void addDrop(MCItemStack is){
            ede.getDrops().add(((BukkitMCItemStack)is).__ItemStack());
        }

        public MCEntity getEntity() {
            return new BukkitMCPlayer((Player)ede.getEntity());
        }

        public int getDroppedExp() {
            return ede.getDroppedExp();
        }

        public String getDeathMessage() {
            return ((PlayerDeathEvent)ede).getDeathMessage();
        }

        public void setDroppedExp(int i) {
            ede.setDroppedExp(i);
        }

        public void setDeathMessage(String nval) {
            ((PlayerDeathEvent)ede).setDeathMessage(nval);
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
   
}
