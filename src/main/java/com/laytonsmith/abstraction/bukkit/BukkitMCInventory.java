/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.abstraction.bukkit;

import com.laytonsmith.abstraction.AbstractionObject;
import com.laytonsmith.abstraction.MCInventory;
import com.laytonsmith.abstraction.MCItemStack;
import org.bukkit.inventory.PlayerInventory;

/**
 *
 * @author layton
 */
public class BukkitMCInventory implements MCInventory {

    PlayerInventory i;
    public BukkitMCInventory(AbstractionObject a){
        this((PlayerInventory)null);
        if(a instanceof MCInventory){
            this.i = ((PlayerInventory)a.getHandle());
        } else {
            throw new ClassCastException();
        }
    }
    
    public BukkitMCInventory(PlayerInventory inventory) {
        this.i = inventory;
    }
    
    public MCItemStack getBoots() {
        return new BukkitMCItemStack(this.i.getBoots());
    }

    public MCItemStack getChestplate() {
        return new BukkitMCItemStack(this.i.getChestplate());
    }

    public Object getHandle(){
        return i;
    }

    public MCItemStack getHelmet() {
        return new BukkitMCItemStack(this.i.getHelmet());
    }

    public MCItemStack getItem(int slot) {
        return new BukkitMCItemStack(i.getItem(slot));
    }

    public MCItemStack getLeggings() {
        return new BukkitMCItemStack(this.i.getLeggings());
    }

    public void setBoots(MCItemStack stack) {
        this.i.setBoots(((BukkitMCItemStack)stack).is);
    }

    public void setChestplate(MCItemStack stack) {
        this.i.setChestplate(((BukkitMCItemStack)stack).is);
    }

    public void setHelmet(MCItemStack stack) {
        this.i.setHelmet(((BukkitMCItemStack)stack).is);
    }

    public void setItem(int slot, MCItemStack stack) {
        this.i.setItem(slot, stack==null?null:((BukkitMCItemStack)stack).is);
    }

    public void setLeggings(MCItemStack stack) {
        this.i.setLeggings(((BukkitMCItemStack)stack).is);
    }
    
}
