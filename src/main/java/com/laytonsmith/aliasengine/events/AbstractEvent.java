/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.aliasengine.events;

import com.laytonsmith.aliasengine.Constructs.CArray;
import com.laytonsmith.aliasengine.Constructs.CString;
import com.laytonsmith.aliasengine.Constructs.Construct;
import com.laytonsmith.aliasengine.Env;
import com.laytonsmith.aliasengine.Script;
import com.laytonsmith.aliasengine.Static;
import com.laytonsmith.aliasengine.exceptions.EventException;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.event.Cancellable;
import org.bukkit.event.block.BlockEvent;
import org.bukkit.event.entity.EntityEvent;
import org.bukkit.event.inventory.FurnaceBurnEvent;
import org.bukkit.event.inventory.FurnaceSmeltEvent;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.server.ServerEvent;
import org.bukkit.event.vehicle.VehicleEvent;
import org.bukkit.event.weather.WeatherEvent;
import org.bukkit.event.world.WorldEvent;

/**
 * This helper class implements a few of the common functions in event, and
 * most (all?) Events should extend this class.
 * @author layton
 */
public abstract class AbstractEvent implements Event, Comparable<Event> {

    /**
     * This is what should happen when the event is cancelled. Some events may
     * do nothing, for instance, a player logout event cannot be cancelled. By
     * default, if the event is an instance of Cancellable, it is cancelled.
     * @param e 
     */
    public void cancel(Object e) {
        if (e instanceof Cancellable) {
            ((Cancellable) e).setCancelled(true);
        }
    }

    /**
     * If the event needs to run special code when a player binds the event, it
     * can be done here. By default, an UnsupportedOperationException is thrown,
     * but is caught and ignored.
     */
    public void bind() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * If the event needs to run special code at server startup, it can be done
     * here. By default, an UnsupportedOperationException is thrown, but is caught
     * and ignored.
     */
    public void hook() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    /**
     * For Bukkit events, there are a few standard items that get added to the
     * event object. This function does that for the event.
     * @param e
     * @return
     * @throws EventException 
     */
    public Map<String, Construct> evaluate_helper(Object e) throws EventException{
        Map<String, Construct> map = new HashMap<String, Construct>();
        map.put("type", new CString(this.getName(), 0, null));
        String macro = "";
        if(e instanceof BlockEvent){
            macro = "block";
        } else if(e instanceof EntityEvent){
            macro = "entity";
        } else if(e instanceof FurnaceBurnEvent || e instanceof FurnaceSmeltEvent){
            macro = "inventory";
        } else if(e instanceof PlayerEvent){
            macro = "player";
        } else if(e instanceof ServerEvent){
            macro = "server";
        } else if(e instanceof VehicleEvent){
            macro = "vehicle";
        } else if(e instanceof WeatherEvent){
            macro = "weather";
        } else if(e instanceof WorldEvent){
            macro = "world";
        } else {
            macro = "custom";
        }
        map.put("macrotype", new CString(macro, 0, null));
        return map;
    }
    
    /**
     * This function is run when the actual event occurs. By default, the script
     * is simply run with the BoundEvent's environment, however this could be overridden
     * if necessary. It should eventually run the script however.
     * @param s
     * @param b 
     */
    public void execute(Script s, BoundEvent b){     
        s.run(null, b.getEnv(), null);
    }

    /**
     * For sorting and optimizing events, we need a comparison operation. By default
     * it is compared by looking at the event name.
     * @param o
     * @return 
     */
    public int compareTo(Event o) {
        return this.getName().compareTo(o.getName());
    }
    
    /**
     * By default, if Object is a bukkit event, we trigger it. Otherwise, nothing
     * is done.
     * @param e 
     */
    public void manualTrigger(Object e){
        if(e instanceof org.bukkit.event.Event){
            Static.getServer().getPluginManager().callEvent((org.bukkit.event.Event)e);
        }
    }
    
    /**
     * Since most events are bukkit events, we return true by default.
     * @return 
     */
    public boolean supportsExternal(){
        return true;
    }
    
    /**
     * If it is ok to by default do a simple conversion from a CArray to a
     * Map, this method can do it for you. Likely this is not acceptable,
     * so hard-coding the conversion will be necessary.
     * @param manualObject
     * @return 
     */
    public static Object DoConvert(CArray manualObject){
        Map<String, Construct> map = new HashMap<String, Construct>();
        for(Construct key : manualObject.keySet()){
            map.put(key.val(), manualObject.get(key, 0));
        }
        return map;        
    }
    
}
