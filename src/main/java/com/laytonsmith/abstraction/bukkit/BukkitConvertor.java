/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.abstraction.bukkit;

import com.laytonsmith.abstraction.Convertor;
import com.laytonsmith.abstraction.Implementation;
import com.laytonsmith.abstraction.MCEnchantment;
import com.laytonsmith.abstraction.MCItemStack;
import com.laytonsmith.abstraction.MCLocation;
import com.laytonsmith.abstraction.MCServer;
import com.laytonsmith.abstraction.MCWorld;
import com.laytonsmith.abstraction.bukkit.events.BukkitAbstractEventMixin;
import com.laytonsmith.abstraction.bukkit.events.drivers.*;
import com.laytonsmith.abstraction.convert;
import com.laytonsmith.core.events.EventList;
import com.laytonsmith.core.events.Driver;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.commandhelper.CommandHelperPlugin;
import java.util.Iterator;
import java.util.SortedSet;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Category;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;

/**
 *
 * @author layton
 */
@convert(type=Implementation.Type.BUKKIT)
public class BukkitConvertor implements Convertor {

    public MCLocation GetLocation(MCWorld w, double x, double y, double z, float yaw, float pitch) {
        World w2 = null;
        if(w != null){
            w2 = ((BukkitMCWorld)w).__World();
        }
        return new BukkitMCLocation(new Location(w2, x, y, z, yaw, pitch));
    }

    public Class GetServerEventMixin() {
        return BukkitAbstractEventMixin.class;
    }

    public MCEnchantment[] GetEnchantmentValues() {
        MCEnchantment[] ea = new MCEnchantment[Enchantment.values().length];
        Enchantment[] oea = Enchantment.values();
        for (int i = 0; i < ea.length; i++) {
            ea[i] = new BukkitMCEnchantment(oea[i]);
        }
        return ea;

    }

    public MCEnchantment GetEnchantmentByName(String name) {
        return new BukkitMCEnchantment(Enchantment.getByName(name));
    }

    public MCServer GetServer() {
        return BukkitMCServer.Get();
    }

    public MCItemStack GetItemStack(int type, int qty) {
        return new BukkitMCItemStack(new ItemStack(type, qty));
    }
    
    public static final BukkitBlockListener BlockListener = new BukkitBlockListener();
    public static final BukkitEntityListener EntityListener = new BukkitEntityListener();
    public static final BukkitInventoryListener InventoryListener = new BukkitInventoryListener();
    public static final BukkitPlayerListener PlayerListener = new BukkitPlayerListener();
    public static final BukkitServerListener ServerListener = new BukkitServerListener();
    public static final BukkitVehicleListener VehicleListener = new BukkitVehicleListener();
    public static final BukkitWeatherListener WeatherListener = new BukkitWeatherListener();
    public static final BukkitWorldListener WorldListener = new BukkitWorldListener();

    public void Startup(CommandHelperPlugin chp) {
        for(Driver type : Driver.values()){            
            SortedSet<com.laytonsmith.core.events.Event> set = EventList.GetEvents(type);
            if(set == null){
                continue;
            }
            Iterator<com.laytonsmith.core.events.Event> i = set.iterator();
            while(i.hasNext()){
                com.laytonsmith.core.events.Event e = i.next();
                Listener l = null;
                Category c = BukkitConvertor.GetBukkitType(e.driver()).getCategory();
                switch(c){
                    case BLOCK:
                        l = BlockListener;
                        break;
                    case ENTITY:
                        l = EntityListener;
                        break;
                    case INVENTORY:
                        l = InventoryListener;
                        break;
                    case PLAYER:
                        l = PlayerListener;
                        break;
                    case SERVER:
                        l = ServerListener;
                        break;
                    case VEHICLE:
                        l = VehicleListener;
                        break;
                    case WEATHER:
                        l = WeatherListener;
                        break;
                    case WORLD:
                        l = WorldListener;
                        break;
                }
                chp.registerEvent(BukkitConvertor.GetBukkitType(e.driver()), l, Priority.Lowest);
            }
        }
    }
    
    public static Event.Type GetBukkitType(Driver tt){
        Event.Type t = null;
        switch(tt){
            case PLAYER_INTERACT:
                t = Event.Type.PLAYER_INTERACT;
                break;
            case PLAYER_JOIN:
                t = Event.Type.PLAYER_JOIN;
                break;
        }
        if(t == null){
            throw new ConfigRuntimeException("Incompatible event! " + t, null, 0, null);
        }
        return t;
    }
    
    public static Driver GetGenericType(Event.Type tt){
        Driver t = null;
        switch(tt){
            case PLAYER_INTERACT:
                t = Driver.PLAYER_INTERACT;
                break;
            case PLAYER_JOIN:
                t = Driver.PLAYER_JOIN;
                break;
        }
        if(t == null){
            throw new ConfigRuntimeException("Incompatible event! " + t, null, 0, null);
        }
        return t;
    }

    public int LookupItemId(String materialName) {
        if(Material.matchMaterial(materialName) != null){
            return new MaterialData(Material.matchMaterial(materialName)).getItemTypeId();
        } else {
            return -1;
        }
    }

    public String LookupMaterialName(int id) {
        return Material.getMaterial(id).toString();
    }
}
