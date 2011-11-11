/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.aliasengine.events;

import com.laytonsmith.aliasengine.Constructs.CString;
import com.laytonsmith.aliasengine.Constructs.Construct;
import com.laytonsmith.aliasengine.Env;
import com.laytonsmith.aliasengine.Script;
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
 * most Events should extend this class.
 * @author layton
 */
public abstract class AbstractEvent implements Event, Comparable<Event> {

    public void cancel(org.bukkit.event.Event e) {
        if (e instanceof Cancellable) {
            ((Cancellable) e).setCancelled(true);
        }
    }

    public void bind() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void hook() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public Map<String, Construct> evaluate_helper(org.bukkit.event.Event e) throws EventException{
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
        }
        map.put("macrotype", new CString(macro, 0, null));
        return map;
    }
    
    public void execute(Script s, BoundEvent b){
        
        s.run(null, b.getEnv(), null);
    }

    public int compareTo(Event o) {
        return this.getName().compareTo(o.getName());
    }
    
}
