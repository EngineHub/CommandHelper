

package com.laytonsmith.abstraction.bukkit.events;

import com.laytonsmith.abstraction.bukkit.BukkitMCServer;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.constructs.CString;
import com.laytonsmith.core.constructs.Construct;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.events.AbstractEvent;
import com.laytonsmith.core.events.BindableEvent;
import com.laytonsmith.core.events.EventMixinInterface;
import com.laytonsmith.core.exceptions.EventException;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.block.BlockEvent;
import org.bukkit.event.entity.EntityEvent;
import org.bukkit.event.hanging.HangingEvent;
import org.bukkit.event.inventory.FurnaceBurnEvent;
import org.bukkit.event.inventory.FurnaceSmeltEvent;
import org.bukkit.event.inventory.InventoryEvent;
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

    @Override
    public void cancel(BindableEvent e, boolean state){
        if (e._GetObject() instanceof Cancellable) {
            ((Cancellable) e._GetObject()).setCancelled(state);
        }
    }
    
    @Override
    public Map<String, Construct> evaluate_helper(BindableEvent event) throws EventException{
        Map<String, Construct> map = new HashMap<String, Construct>();
        map.put("event_type", new CString(mySuper.getName(), Target.UNKNOWN));
        String macro;
        Object e = event._GetObject();
        if(e instanceof BlockEvent){
            macro = "block";
        } else if(e instanceof EntityEvent){
            macro = "entity";
            if(((EntityEvent)e).getEntity() instanceof Player){
                Entity entity = ((EntityEvent)e).getEntity();
                map.put("player", new CString(((Player)entity).getName(), Target.UNKNOWN));
            }
        } else if (e instanceof HangingEvent) {
            macro = "entity";
        } else if(e instanceof InventoryEvent || e instanceof FurnaceBurnEvent || e instanceof FurnaceSmeltEvent){
            macro = "inventory";
        } else if(e instanceof PlayerEvent){
            map.put("player", new CString(((PlayerEvent)e).getPlayer().getName(), Target.UNKNOWN));
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
        map.put("macrotype", new CString(macro, Target.UNKNOWN));
        return map;
    }
    
    @Override
    public void manualTrigger(BindableEvent e){
        if(e._GetObject() instanceof org.bukkit.event.Event){
            ((BukkitMCServer)Static.getServer()).__Server().getPluginManager().callEvent((org.bukkit.event.Event)e._GetObject());
        }
    }

    @Override
    public boolean isCancellable(BindableEvent o) {
        return (o._GetObject() instanceof Cancellable);
    }

    public boolean isCancelled(BindableEvent o) {
        if(o._GetObject() instanceof Cancellable){
            return ((Cancellable)o._GetObject()).isCancelled();
        } else {
            return false;
        }
    }
    
}
