/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.aliasengine.events;

import com.laytonsmith.aliasengine.Constructs.CArray;
import com.laytonsmith.aliasengine.Constructs.CInt;
import com.laytonsmith.aliasengine.Constructs.CString;
import com.laytonsmith.aliasengine.Constructs.Construct;
import com.laytonsmith.aliasengine.Static;
import com.laytonsmith.aliasengine.api;
import com.laytonsmith.aliasengine.events.Prefilters.PrefilterType;
import com.laytonsmith.aliasengine.exceptions.EventException;
import com.laytonsmith.aliasengine.functions.exceptions.PrefilterNonMatchException;
import java.util.Map;
import org.bukkit.block.Block;
import org.bukkit.event.Event.Type;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;

/**
 *
 * @author layton
 */
public class PlayerEvents {
    public static String docs(){
        return "Contains events related to a player";
    }
    
    @api public static class player_join extends AbstractEvent{

        public String getName() {
            return "player_join";
        }

        public String docs() {
            return "{player: <string match>} This event is called when a player logs in "
                    + "{player: The player's name}";
        }

        public String since() {
            return "3.3.0";
        }
        
        public org.bukkit.event.Event.Type driver(){
            return org.bukkit.event.Event.Type.PLAYER_JOIN;
        }

        public boolean matches(Map<String, Construct> prefilter, Object e) {
            if(e instanceof PlayerJoinEvent){
                PlayerJoinEvent ple = (PlayerJoinEvent) e;
                if(prefilter.containsKey("player")){
                    if(!ple.getPlayer().getName().equals(prefilter.get("player_name").val())){
                        return false;
                    }
                }                
                return true;
            }
            return false;
        }

        public Map<String, Construct> evaluate(Object e) throws EventException {
            if(e instanceof PlayerJoinEvent){
                PlayerJoinEvent ple = (PlayerJoinEvent) e;
                Map<String, Construct> map = super.evaluate_helper(e);
                map.put("player", new CString(ple.getPlayer().getName(), 0, null));
                return map;
            } else{
                throw new EventException("Cannot convert e to PlayerLoginEvent");
            }
        }
        
    }
    
    @api public static class player_interact extends AbstractEvent{

        public String getName() {
            return "player_interact";
        }

        public String docs() {
            return "{block: <item match> If the block the player interacts with is this"
                    + " | button: <string match> left or right. If they left or right clicked |"
                    + " item: <item match> The item they are holding when they interacted |"
                    + " player: <string match> The player that triggered the event} "
                    + "Fires when a player left or right clicks a block or the air"
                    + "{action: One of either: left_click_block, right_click_block, left_click_air, or right_click_air |"
                    + "block: The id of the block they clicked, or 0 if they clicked the air. If they clicked the air, "
                    + " neither facing or location will be present. |"
                    + "player: The player associated with this event |"
                    + "facing: The (lowercase) face of the block they clicked. See <<jd:org.bukkit.block.BlockFace>> for"
                    + " the possible values |"
                    + "location: The (x, y, z, world) location of the block they clicked}";
        }

        public String since() {
            return "3.3.0";
        }

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
                    Prefilters.match(prefilter, "item", Static.ParseItemNotation(pie.getItem()), PrefilterType.ITEM_MATCH);
                    Prefilters.match(prefilter, "block", Static.ParseItemNotation(pie.getClickedBlock()), PrefilterType.ITEM_MATCH);
                    Prefilters.match(prefilter, "player", pie.getPlayer().getName(), PrefilterType.MACRO);
                }catch(PrefilterNonMatchException x){
                    return false;
                }
                
                return true;
            }
            return false;
        }

        public Map<String, Construct> evaluate(Object e) throws EventException {
            if(e instanceof PlayerInteractEvent){
                PlayerInteractEvent pie = (PlayerInteractEvent) e;
                Map<String, Construct> map = super.evaluate_helper(e);
                map.put("player", new CString(pie.getPlayer().getName(), 0, null));
                Action a = pie.getAction();
                map.put("action", new CString(a.name().toLowerCase(), 0, null));
                map.put("block", new CString(Static.ParseItemNotation(pie.getClickedBlock()), 0, null));
                if(a == Action.LEFT_CLICK_BLOCK || a == Action.RIGHT_CLICK_BLOCK){
                    map.put("facing", new CString(pie.getBlockFace().name().toLowerCase(), 0, null));
                    Block b = pie.getClickedBlock();
                    map.put("location", new CArray(0, null, new CInt(b.getX(), 0, null),
                            new CInt(b.getY(), 0, null), new CInt(b.getZ(), 0, null), 
                            new CString(b.getWorld().getName(), 0, null)));
                }
                map.put("item", new CString(Static.ParseItemNotation(pie.getItem()), 0, null));
                return map;
            } else {
                throw new EventException("Cannot convert e to PlayerInteractEvent");
            }
        }

        public Type driver() {
            return Type.PLAYER_INTERACT;
        }
        
    }


    //public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {}
}
