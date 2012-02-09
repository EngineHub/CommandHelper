/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.abstraction.bukkit.events;

import com.laytonsmith.abstraction.Implementation;
import com.laytonsmith.abstraction.bukkit.BukkitMCItemStack;
import com.laytonsmith.abstraction.bukkit.BukkitMCLocation;
import com.laytonsmith.abstraction.bukkit.BukkitMCPlayer;
import com.laytonsmith.abstraction.bukkit.BukkitMCWorld;
import com.laytonsmith.abstraction.bukkit.blocks.BukkitMCBlock;
import com.laytonsmith.core.ObjectGenerator;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.constructs.*;
import com.laytonsmith.core.events.Prefilters.PrefilterType;
import com.laytonsmith.core.events.*;
import com.laytonsmith.core.exceptions.EventException;
import com.laytonsmith.core.exceptions.PrefilterNonMatchException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDeathEvent;
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
            Block b = ((BukkitMCBlock)ObjectGenerator.GetGenerator().location(manual.get("location"), null, 0, null).getBlock()).__Block();
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
                Prefilters.match(prefilter, "player", event.getPlayer().getName(), PrefilterType.MACRO);
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
                CArray location = ObjectGenerator.GetGenerator().location(new BukkitMCLocation(event.getRespawnLocation()));
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
            Location l = ((BukkitMCLocation)ObjectGenerator.GetGenerator().location(manual.get("location"), new BukkitMCWorld(p.getWorld()), 0, null))._Location();
            PlayerRespawnEvent e = new PlayerRespawnEvent(p, l, false);
            return e;
        }
        
        public boolean modifyEvent(String key, Construct value, Object event) {
            if (event instanceof PlayerRespawnEvent) {
                PlayerRespawnEvent e = (PlayerRespawnEvent) event;
                if (key.equals("location")) {
                    //Change this parameter in e to value
                    e.setRespawnLocation(((BukkitMCLocation)ObjectGenerator.GetGenerator().location(value, new BukkitMCWorld(e.getPlayer().getWorld()), 0, null))._Location());
                    return true;
                }
            }
            return false;
        }
        
        public EventMixinInterface customMixin(AbstractEvent e) {
            return null;
        }
    }
    
    @abstraction(load = com.laytonsmith.core.events.drivers.PlayerEvents.player_death.class,
    type = Implementation.Type.BUKKIT)
    public static class player_death implements EventHandlerInterface {
        //Check to see if this event matches the given prefilter

        public boolean matches(Map<String, Construct> prefilter, Object e) throws PrefilterNonMatchException {
            if (e instanceof EntityDeathEvent) {
                EntityDeathEvent event = (EntityDeathEvent) e;
                Prefilters.match(prefilter, "player", ((Player)event.getEntity()).getName(), PrefilterType.MACRO);
                return true;
            }
            return false;
        }

        //We have an actual event now, change it into a Map
        //that will end up being the @event object
        public Map<String, Construct> evaluate(Object e, EventMixinInterface mixin) throws EventException {
            if (e instanceof EntityDeathEvent) {
                EntityDeathEvent event = (EntityDeathEvent) e;
                Map<String, Construct> map = mixin.evaluate_helper(e);
                CArray ca = new CArray(0, null);
                for(ItemStack is : event.getDrops()){                    
                    ca.push(ObjectGenerator.GetGenerator().item(new BukkitMCItemStack(is), 0, null));
                }
                Player p = (Player)event.getEntity();
                map.put("drops", ca);
                map.put("xp", new CInt(event.getDroppedExp(), 0, null));
                try{
                    map.put("cause", new CString(event.getEntity().getLastDamageCause().getCause().name(), 0, null));
//                    Entity damager = event.getEntity().getLastDamageCause().getEntity();
//                    if(damager instanceof Player){
//                        map.put("damager", new CString(((Player)damager).getName(), 0, null));
//                    } else {
//                        map.put("damager", new CInt(damager.getEntityId(), 0, null));
//                    }
                } catch(NullPointerException ex){
                    map.put("cause", new CString(DamageCause.CUSTOM.name(), 0, null));
                }
                map.put("location", ObjectGenerator.GetGenerator().location(new BukkitMCLocation(p.getLocation())));
                //map.put("event object data name", event.getDataFromEvent());
                return map;
            } else {
                throw new EventException("Cannot convert e to EntityDeathEvent");
            }
        }
        
        public Object convert(CArray manual) {
            //For firing off the event manually, we have to convert the CArray into an
            //actual object that will trigger it
            String splayer = manual.get("player").val();
            List<ItemStack> list = new ArrayList<ItemStack>();
            CArray clist = (CArray)manual.get("drops");
            for(String key : clist.keySet()){
                list.add(((BukkitMCItemStack)ObjectGenerator.GetGenerator().item(clist.get(key), clist.getLineNum(), clist.getFile())).__ItemStack());
            }
            EntityDeathEvent e = new EntityDeathEvent(((BukkitMCPlayer)Static.GetPlayer(splayer))._Player(), list);
            return e;
        }

        //Given the paramters, change the underlying event
        public boolean modifyEvent(String key, Construct value, Object event) {
            if (event instanceof EntityDeathEvent) {
                EntityDeathEvent e = (EntityDeathEvent) event;
                if (key.equals("xp")) {
                    //Change this parameter in e to value
                    e.setDroppedExp((int)Static.getInt(value));
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
