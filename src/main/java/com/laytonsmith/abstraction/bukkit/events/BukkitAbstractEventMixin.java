/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.abstraction.bukkit.events;

import com.laytonsmith.abstraction.Implementation;
import com.laytonsmith.abstraction.bukkit.BukkitMCServer;
import com.laytonsmith.core.constructs.CString;
import com.laytonsmith.core.constructs.Construct;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.events.AbstractEvent;
import com.laytonsmith.core.events.EventMixinInterface;
import com.laytonsmith.core.exceptions.EventException;
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
 *
 * @author layton
 */
public class BukkitAbstractEventMixin implements EventMixinInterface{
    
    AbstractEvent mySuper;
    
    public BukkitAbstractEventMixin(AbstractEvent mySuper){
        this.mySuper = mySuper;
    }

    public void cancel(Object e){
        if (e instanceof Cancellable) {
            ((Cancellable) e).setCancelled(true);
        }
    }
    
    @Override
    public Map<String, Construct> evaluate_helper(Object e) throws EventException{
        Map<String, Construct> map = new HashMap<String, Construct>();
        map.put("type", new CString(mySuper.getName(), 0, null));
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
    
    public void manualTrigger(Object e){
        if(e instanceof org.bukkit.event.Event){
            ((BukkitMCServer)Static.getServer()).__Server().getPluginManager().callEvent((org.bukkit.event.Event)e);
        }
    }

    public boolean isCancellable(Object o) {
        return (o instanceof Cancellable);
    }
    
}
