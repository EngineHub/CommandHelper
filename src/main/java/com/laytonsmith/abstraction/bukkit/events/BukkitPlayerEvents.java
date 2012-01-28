/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.abstraction.bukkit.events;

import com.laytonsmith.abstraction.Implementation;
import com.laytonsmith.abstraction.MCItemStack;
import com.laytonsmith.abstraction.MCPlayer;
import com.laytonsmith.abstraction.blocks.MCBlock;
import com.laytonsmith.abstraction.bukkit.BukkitMCItemStack;
import com.laytonsmith.abstraction.bukkit.BukkitMCLocation;
import com.laytonsmith.abstraction.bukkit.BukkitMCPlayer;
import com.laytonsmith.abstraction.bukkit.BukkitMCWorld;
import com.laytonsmith.abstraction.bukkit.blocks.BukkitMCBlock;
import com.laytonsmith.core.constructs.CArray;
import com.laytonsmith.core.constructs.CInt;
import com.laytonsmith.core.constructs.CString;
import com.laytonsmith.core.constructs.Construct;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.constructs.CNull;
import com.laytonsmith.core.events.AbstractEvent;
import com.laytonsmith.core.events.Event;
import com.laytonsmith.core.events.EventHandlerInterface;
import com.laytonsmith.core.events.EventMixinInterface;
import com.laytonsmith.core.events.Prefilters;
import com.laytonsmith.core.events.Prefilters.PrefilterType;
import com.laytonsmith.core.events.abstraction;
import com.laytonsmith.core.exceptions.EventException;
import com.laytonsmith.core.exceptions.PrefilterNonMatchException;
import java.util.Map;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;

/**
 *
 * @author layton
 */
public class BukkitPlayerEvents {
    @abstraction(load=com.laytonsmith.core.events.drivers.PlayerEvents.player_join.class,
            type=Implementation.Type.BUKKIT)
    public static class player_join implements EventHandlerInterface{
        public boolean matches(Map<String, Construct> prefilter, Object e) throws PrefilterNonMatchException {
            if(e instanceof PlayerJoinEvent){
                PlayerJoinEvent ple = (PlayerJoinEvent) e;
                if(prefilter.containsKey("player")){
                    if(!ple.getPlayer().getName().equals(prefilter.get("player_name").val())){
                        return false;
                    }
                }                
                Prefilters.match(prefilter, "join_message", ple.getJoinMessage(), PrefilterType.REGEX);
                return true;
            }
            return false;
        }

        public Map<String, Construct> evaluate(Object e, EventMixinInterface mixin) throws EventException {
            if(e instanceof PlayerJoinEvent){
                PlayerJoinEvent ple = (PlayerJoinEvent) e;
                Map<String, Construct> map = mixin.evaluate_helper(e);
                //map.put("player", new CString(ple.getPlayer().getName(), 0, null));
                map.put("join_message", new CString(ple.getJoinMessage(), 0, null));
                return map;
            } else{
                throw new EventException("Cannot convert e to PlayerLoginEvent");
            }
        }
        
        public Object convert(CArray manual){
            PlayerJoinEvent e = new PlayerJoinEvent(((BukkitMCPlayer)Static.GetPlayer(manual.get("player").val(), 0, null))._Player(), manual.get("join_message").val());
            return e;
        }

        public boolean modifyEvent(String key, Construct value, Object event) {
            if(event instanceof PlayerJoinEvent){
                PlayerJoinEvent pje = (PlayerJoinEvent)event;
                if(key.equals("join_message")){
                    if(value instanceof CNull){
                        pje.setJoinMessage(null);
                        return pje.getJoinMessage() == null;
                    } else {
                        pje.setJoinMessage(value.val());
                        return pje.getJoinMessage().equals(value.val());
                    }
                }
            }
            return false;
        }

        public EventMixinInterface customMixin(AbstractEvent e) {
            return null;
        }
    }
    
    
    @abstraction(load=com.laytonsmith.core.events.drivers.PlayerEvents.player_interact.class,
            type=Implementation.Type.BUKKIT)
    public static class player_interact implements EventHandlerInterface{
        public boolean matches(Map<String, Construct> prefilter, Object e) throws PrefilterNonMatchException {
            if(e instanceof PlayerInteractEvent){
                PlayerInteractEvent pie = (PlayerInteractEvent)e;
                if(((PlayerInteractEvent)e).getAction().equals(Action.PHYSICAL)){
                    return false;
                }
                if(prefilter.containsKey("button")){
                    if(pie.getAction().equals(Action.LEFT_CLICK_AIR) || pie.getAction().equals(Action.LEFT_CLICK_BLOCK)){
                        if(!prefilter.get("button").val().toLowerCase().equals("left")){
                            return false;
                        }
                    }
                    if(pie.getAction().equals(Action.RIGHT_CLICK_AIR) || pie.getAction().equals(Action.RIGHT_CLICK_BLOCK)){
                        if(!prefilter.get("button").val().toLowerCase().equals("right")){
                            return false;
                        }
                    }
                }
                
                Prefilters.match(prefilter, "item", Static.ParseItemNotation(new BukkitMCItemStack(pie.getItem())), PrefilterType.ITEM_MATCH);
                Prefilters.match(prefilter, "block", Static.ParseItemNotation(new BukkitMCBlock(pie.getClickedBlock())), PrefilterType.ITEM_MATCH);
                Prefilters.match(prefilter, "player", pie.getPlayer().getName(), PrefilterType.MACRO);
                
                return true;
            }
            return false;
        }

        public Map<String, Construct> evaluate(Object e, EventMixinInterface mixin) throws EventException {
            if(e instanceof PlayerInteractEvent){
                PlayerInteractEvent pie = (PlayerInteractEvent) e;
                Map<String, Construct> map = mixin.evaluate_helper(e);
                //map.put("player", new CString(pie.getPlayer().getName(), 0, null));
                Action a = pie.getAction();
                map.put("action", new CString(a.name().toLowerCase(), 0, null));
                map.put("block", new CString(Static.ParseItemNotation(new BukkitMCBlock(pie.getClickedBlock())), 0, null));
                if(a == Action.LEFT_CLICK_AIR || a == Action.LEFT_CLICK_BLOCK){
                    map.put("button", new CString("left", 0, null));
                } else {
                    map.put("button", new CString("right", 0, null));
                }
                if(a == Action.LEFT_CLICK_BLOCK || a == Action.RIGHT_CLICK_BLOCK){
                    map.put("facing", new CString(pie.getBlockFace().name().toLowerCase(), 0, null));
                    Block b = pie.getClickedBlock();
                    map.put("location", new CArray(0, null, new CInt(b.getX(), 0, null),
                            new CInt(b.getY(), 0, null), new CInt(b.getZ(), 0, null), 
                            new CString(b.getWorld().getName(), 0, null)));
                }
                map.put("item", new CString(Static.ParseItemNotation(new BukkitMCItemStack(pie.getItem())), 0, null));
                return map;
            } else {
                throw new EventException("Cannot convert e to PlayerInteractEvent");
            }
        }
        
        @Override
        public Object convert(CArray manual){
            Player p = ((BukkitMCPlayer)Static.GetPlayer(manual.get("player"), 0, null))._Player();
            Action a = Action.valueOf(manual.get("action").val().toUpperCase());
            ItemStack is = ((BukkitMCItemStack)Static.ParseItemNotation("player_interact event", manual.get("item").val(), 1, 0, null)).__ItemStack();
            Block b = ((BukkitMCBlock)Static.GetLocation(manual.get("location"), null, 0, null).getBlock()).__Block();
            BlockFace bf = BlockFace.valueOf(manual.get("facing").val());
            PlayerInteractEvent e = new PlayerInteractEvent(p, a, is, b, bf);            
            return e;
        }

        public boolean modifyEvent(String key, Construct value, Object event) {
            if(event instanceof PlayerInteractEvent){
                PlayerInteractEvent pie = (PlayerInteractEvent)event;
                
            }
            return false;
        }

        public EventMixinInterface customMixin(AbstractEvent e) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }
    
    @abstraction(load = com.laytonsmith.core.events.drivers.PlayerEvents.player_spawn.class,
    type = Implementation.Type.BUKKIT)
    public static class player_spawn implements EventHandlerInterface {

        public boolean matches(Map<String, Construct> prefilter, Object e) throws PrefilterNonMatchException {
            if (e instanceof PlayerRespawnEvent) {
                PlayerRespawnEvent event = (PlayerRespawnEvent) e;
                Prefilters.match(prefilter, "x", event.getRespawnLocation().getBlockX(), PrefilterType.EXPRESSION);
                Prefilters.match(prefilter, "y", event.getRespawnLocation().getBlockY(), PrefilterType.EXPRESSION);
                Prefilters.match(prefilter, "z", event.getRespawnLocation().getBlockZ(), PrefilterType.EXPRESSION);
                Prefilters.match(prefilter, "world", event.getRespawnLocation().getWorld().getName(), PrefilterType.STRING_MATCH);
                return true;
            }
            return false;
        }
        
        public Map<String, Construct> evaluate(Object e, EventMixinInterface mixin) throws EventException {
            if (e instanceof PlayerRespawnEvent) {
                PlayerRespawnEvent event = (PlayerRespawnEvent) e;
                Map<String, Construct> map = mixin.evaluate_helper(e);
                //the helper puts the player in for us
                CArray location = Static.GetLocationArray(new BukkitMCLocation(event.getRespawnLocation()));
                map.put("location", location);
                return map;
            } else {
                throw new EventException("Cannot convert e to PlayerRespawnEvent");
            }
        }
        
        public Object convert(CArray manual) {
            //For firing off the event manually, we have to convert the CArray into an
            //actual object that will trigger it
            Player p = ((BukkitMCPlayer)Static.GetPlayer(manual.get("player")))._Player();
            Location l = ((BukkitMCLocation)Static.GetLocation(manual.get("location"), new BukkitMCWorld(p.getWorld()), 0, null))._Location();
            PlayerRespawnEvent e = new PlayerRespawnEvent(p, l, false);
            return e;
        }
        
        public boolean modifyEvent(String key, Construct value, Object event) {
            if (event instanceof PlayerRespawnEvent) {
                PlayerRespawnEvent e = (PlayerRespawnEvent) event;
                if (key.equals("location")) {
                    //Change this parameter in e to value
                    e.setRespawnLocation(((BukkitMCLocation)Static.GetLocation(value, new BukkitMCWorld(e.getPlayer().getWorld()), 0, null))._Location());
                    return true;
                }
            }
            return false;
        }
        
        public EventMixinInterface customMixin(AbstractEvent e) {
            return null;
        }
    }
}
