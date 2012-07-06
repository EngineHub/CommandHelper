/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.abstraction.bukkit;

import com.laytonsmith.abstraction.AbstractionObject;
import com.laytonsmith.abstraction.MCLocation;
import com.laytonsmith.abstraction.MCWorld;
import com.laytonsmith.abstraction.blocks.MCBlock;
import com.laytonsmith.abstraction.bukkit.blocks.BukkitMCBlock;
import org.bukkit.Location;

/**
 *
 * @author layton
 */
public class BukkitMCLocation implements MCLocation{
    Location l;
    public BukkitMCLocation(Location l){
        this.l = l;        
    }
    
    public BukkitMCLocation(AbstractionObject a){
        this((Location)null);
        if(a instanceof MCLocation){
            this.l = ((Location)a.getHandle());
        } else {
            throw new ClassCastException();
        }
    }
    
    public Object getHandle(){
        return l;
    }

    public double getX() {
        return l.getX();
    }

    public double getY() {
        return l.getY();
    }

    public double getZ() {
        return l.getZ();
    }

    public MCWorld getWorld() {
        if(l.getWorld() == null){
            return null;
        }
        return new BukkitMCWorld(l.getWorld());
    }

    public float getYaw() {
        return l.getYaw();
    }

    public float getPitch() {
        return l.getPitch();
    }

    public int getBlockX() {
        return l.getBlockX();
    }

    public int getBlockY() {
        return l.getBlockY();
    }

    public int getBlockZ() {
        return l.getBlockZ();
    }

    public MCBlock getBlock() {
        if(l.getBlock() == null){
            return null;
        }
        return new BukkitMCBlock(l.getBlock());
    }
    
    public Location _Location(){
        return l;
    }

    public void setPitch(float p) {
        l.setPitch(p);
    }

    public void setYaw(float y) {
        l.setYaw(y);
    }

    @Override
    public MCLocation clone() {
        return new BukkitMCLocation(l.clone());
    }

    public Location asLocation() {
        return l;
    }
}
