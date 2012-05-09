/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.abstraction.bukkit;

import com.laytonsmith.abstraction.bukkit.events.BukkitAbstractEventMixin;
import com.laytonsmith.abstraction.bukkit.events.drivers.*;
import com.laytonsmith.abstraction.*;
import com.laytonsmith.commandhelper.CommandHelperPlugin;
import com.laytonsmith.core.Static;
import java.util.Set;
import java.util.TreeSet;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
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
    public MCItemStack GetItemStack(int type, int data, int qty) {
        return new BukkitMCItemStack(new ItemStack(type, qty, (short)0, (byte)data));
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
        chp.registerEvent(BlockListener);
        chp.registerEvent(EntityListener);
        chp.registerEvent(InventoryListener);
        chp.registerEvent(PlayerListener);
        chp.registerEvent(ServerListener);
        chp.registerEvent(VehicleListener);
        chp.registerEvent(WeatherListener);
        chp.registerEvent(WorldListener);        
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
    
    /**
     * We don't want to allow scripts to clear other plugin's tasks
     * on accident, so only ids registered through our interface
     * can also be cancelled.
     */
    private static Set<Integer> validIDs = new TreeSet<Integer>();

    public int SetFutureRunnable(long ms, Runnable r) {
        int id = Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(CommandHelperPlugin.self, r, (long)(ms / 50));
        validIDs.add(id);
        return id;
    }
    
    public int SetFutureRepeater(long ms, long initialDelay, Runnable r){
        int id = Bukkit.getServer().getScheduler().scheduleAsyncRepeatingTask(CommandHelperPlugin.self, r, (long)(initialDelay / 50), (long)(ms / 50));
        validIDs.add(id);
        return id;        
    }

    public void ClearAllRunnables() {
        ((BukkitMCServer)Static.getServer()).__Server().getScheduler().cancelTasks(CommandHelperPlugin.self);
    }

    public void ClearFutureRunnable(int id) {
        if(validIDs.contains(id)){
            Bukkit.getServer().getScheduler().cancelTask(id);
            validIDs.remove(id);
        }
    }

    public MCEntity GetCorrectEntity(MCEntity e) {

        Entity be = ((BukkitMCEntity)e)._Entity();
        if(be instanceof Tameable){
            return new BukkitMCTameable(be);
        }
        
        if(be instanceof LivingEntity){
            return new BukkitMCLivingEntity(((LivingEntity)be));
        }
        
        if(be instanceof HumanEntity){
            return new BukkitMCHumanEntity((HumanEntity)be);
        }
        
        if(be instanceof Player){
            return new BukkitMCPlayer((Player)be);
        }        
        
        return e;
    }

}
