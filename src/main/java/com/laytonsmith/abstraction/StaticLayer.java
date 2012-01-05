/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.abstraction;

import com.laytonsmith.abstraction.bukkit.BukkitMCEnchantment;
import com.laytonsmith.abstraction.bukkit.BukkitMCItemStack;
import com.laytonsmith.abstraction.bukkit.BukkitMCLocation;
import com.laytonsmith.abstraction.bukkit.BukkitMCServer;
import com.laytonsmith.abstraction.bukkit.BukkitMCWorld;
import org.bukkit.Location;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;


/**
 * Unfortunately some methods just can't be overridden.
 * @author layton
 */
public class StaticLayer {

    public static MCLocation GetLocation(MCWorld w, double x, double y, double z, float yaw, float pitch) {
        switch(backendType){
            case BUKKIT:
                return new BukkitMCLocation(new Location(((BukkitMCWorld)w).__World(), x, y, z, yaw, pitch));
        }
        return null;
    }
    
    public static MCLocation GetLocation(MCWorld w, double x, double y, double z){
        return GetLocation(w, x, y, z, 0, 0);
    }

    public static MCItemStack GetItemStack(int type, int qty) {
        switch(backendType){
            case BUKKIT:
                return new BukkitMCItemStack(new ItemStack(type, qty));
        }
        return null;
    }
    
    public static MCServer GetServer(){
        switch(backendType){
            case BUKKIT:
                return BukkitMCServer.Get();
        }
        return null;
    }
    
    public static MCEnchantment GetEnchantmentByName(String name){
        switch(backendType){
            case BUKKIT:
                return new BukkitMCEnchantment(Enchantment.getByName(name));
        }
        return null;
    }
    
    public static MCEnchantment[] GetEnchantmentValues(){
        switch(backendType){
            case BUKKIT:
                MCEnchantment[] ea = new MCEnchantment[Enchantment.values().length];
                Enchantment [] oea = Enchantment.values();
                for(int i = 0; i < ea.length; i++){
                    ea[i] = new BukkitMCEnchantment(oea[i]);
                }
                return ea;
        }
        return null;
    }
    
    static Implementation.Type backendType = null;
    static{
        backendType = Implementation.GetServerType();
    }
}
