/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.abstraction.bukkit;

import org.bukkit.Location;

import com.laytonsmith.abstraction.AbstractionObject;
import com.laytonsmith.abstraction.MCLocation;
import com.laytonsmith.abstraction.MCWorld;
import com.laytonsmith.abstraction.blocks.MCBlock;
import com.laytonsmith.abstraction.bukkit.blocks.BukkitMCBlock;

/**
 *
 * @author layton
 */
public class BukkitMCLocation implements MCLocation{
    Location l;
    public BukkitMCLocation(AbstractionObject a){
        this((Location)null);
        if(a instanceof MCLocation){
            this.l = ((Location)a.getHandle());
        } else {
            throw new ClassCastException();
        }
    }
    
    public BukkitMCLocation(Location l){
        this.l = l;        
    }
    
    public Location _Location(){
        return l;
    }

    @Override
    public MCLocation clone() {
        return new BukkitMCLocation(l.clone());
    }

    public MCBlock getBlock() {
        if(l.getBlock() == null){
            return null;
        }
        return new BukkitMCBlock(l.getBlock());
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

    public Object getHandle(){
        return l;
    }

    public float getPitch() {
        return l.getPitch();
    }

    public MCWorld getWorld() {
        if(l.getWorld() == null){
            return null;
        }
        return new BukkitMCWorld(l.getWorld());
    }

    public double getX() {
        return l.getX();
    }

    public double getY() {
        return l.getY();
    }
    
    public float getYaw() {
        return l.getYaw();
    }

    public double getZ() {
        return l.getZ();
    }

    public void setPitch(float p) {
        l.setPitch(p);
    }

    public void setYaw(float y) {
        l.setYaw(y);
    }
}
