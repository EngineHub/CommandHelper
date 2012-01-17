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
import com.laytonsmith.abstraction.bukkit.BukkitMCPlayer;
import com.laytonsmith.abstraction.bukkit.blocks.BukkitMCBlock;
import com.laytonsmith.core.constructs.CArray;
import com.laytonsmith.core.constructs.CInt;
import com.laytonsmith.core.constructs.CString;
import com.laytonsmith.core.constructs.Construct;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.events.EventHandlerInterface;
import com.laytonsmith.core.events.EventMixinInterface;
import com.laytonsmith.core.events.Prefilters;
import com.laytonsmith.core.events.Prefilters.PrefilterType;
import com.laytonsmith.core.events.abstraction;
import com.laytonsmith.core.exceptions.EventException;
import com.laytonsmith.core.exceptions.PrefilterNonMatchException;
import java.util.Map;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
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
                map.put("player", new CString(ple.getPlayer().getName(), 0, null));
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
                    pje.setJoinMessage(value.val());
                    return pje.getJoinMessage().equals(value.val());
                }
            }
            return false;
        }
    }
    
    public static class player_interact implements EventHandlerInterface{
        public boolean matches(Map<String, Construct> prefilter, Object e) {
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
                
                try{
                    Prefilters.match(prefilter, "item", Static.ParseItemNotation(new BukkitMCItemStack(pie.getItem())), PrefilterType.ITEM_MATCH);
                    Prefilters.match(prefilter, "block", Static.ParseItemNotation(new BukkitMCBlock(pie.getClickedBlock())), PrefilterType.ITEM_MATCH);
                    Prefilters.match(prefilter, "player", pie.getPlayer().getName(), PrefilterType.MACRO);
                }catch(PrefilterNonMatchException x){
                    return false;
                }
                
                return true;
            }
            return false;
        }

        public Map<String, Construct> evaluate(Object e, EventMixinInterface mixin) throws EventException {
            if(e instanceof PlayerInteractEvent){
                PlayerInteractEvent pie = (PlayerInteractEvent) e;
                Map<String, Construct> map = mixin.evaluate_helper(e);
                map.put("player", new CString(pie.getPlayer().getName(), 0, null));
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
    }
}
