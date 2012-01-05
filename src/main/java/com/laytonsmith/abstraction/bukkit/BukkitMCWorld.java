/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.abstraction.bukkit;

import com.laytonsmith.abstraction.MCEffect;
import com.laytonsmith.abstraction.MCEntity;
import com.laytonsmith.abstraction.MCItemStack;
import com.laytonsmith.abstraction.MCLivingEntity;
import com.laytonsmith.abstraction.MCLocation;
import com.laytonsmith.abstraction.MCWorld;
import com.laytonsmith.abstraction.blocks.MCBlock;
import com.laytonsmith.abstraction.bukkit.blocks.BukkitMCBlock;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Effect;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

/**
 *
 * @author layton
 */
public class BukkitMCWorld implements MCWorld{
    World w;
    public BukkitMCWorld(World w){
        this.w = w;
    }
    
    public World __World(){
        return w;
    }
    
    public List<MCLivingEntity> getLivingEntities(){
        if(w.getLivingEntities() == null){
            return null;
        }
        List<MCLivingEntity> list = new ArrayList<MCLivingEntity>();
        for(LivingEntity e : w.getLivingEntities()){
            list.add(new BukkitMCLivingEntity(e));
        }
        return list;
    }

    public String getName() {
        return w.getName();
    }

    public MCBlock getBlockAt(int x, int y, int z) {
        if(w.getBlockAt(x, y, z) == null){
            return null;
        }
        return new BukkitMCBlock(w.getBlockAt(x, y, z));
    }

    public MCEntity spawn(MCLocation l, Class mobType) {
        return new BukkitMCEntity(w.spawn(((BukkitMCLocation)l).l, mobType));
    }

    public void playEffect(MCLocation l, MCEffect mCEffect, int e, int data) {
        w.playEffect(((BukkitMCLocation)l).l, Effect.valueOf(mCEffect.name()), e, data);
    }

    public void dropItemNaturally(MCLocation l, MCItemStack is) {
        w.dropItemNaturally(((BukkitMCLocation)l).l, ((BukkitMCItemStack)is).is);
    }

    public void dropItem(MCLocation l, MCItemStack is) {
        w.dropItem(((BukkitMCLocation)l).l, ((BukkitMCItemStack)is).is);
    }

    public void strikeLightning(MCLocation GetLocation) {
        w.strikeLightning(((BukkitMCLocation)GetLocation).l);
    }

    public void strikeLightningEffect(MCLocation GetLocation) {
        w.strikeLightningEffect(((BukkitMCLocation)GetLocation).l);
    }

    public void setStorm(boolean b) {
        w.setStorm(b);
    }

    public MCLocation getSpawnLocation() {
        return new BukkitMCLocation(w.getSpawnLocation());
    }

    public void refreshChunk(int x, int z) {
        w.refreshChunk(x, z);
    }

    public void setTime(long time) {
        w.setTime(time);
    }

    public long getTime() {
        return w.getTime();
    }

}
