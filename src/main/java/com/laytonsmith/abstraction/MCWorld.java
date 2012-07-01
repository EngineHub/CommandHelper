/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.abstraction;

import com.laytonsmith.abstraction.blocks.MCBlock;
import com.laytonsmith.core.constructs.CArray;
import com.laytonsmith.core.constructs.Target;
import java.util.List;

/**
 *
 * @author layton
 */
public interface MCWorld extends AbstractionObject{
    public void dropItem(MCLocation l, MCItemStack is);
    public void dropItemNaturally(MCLocation l, MCItemStack is);
    public void explosion(double x, double y, double z, float size);

    public MCBiomeType getBiome(int x, int z);

    public MCBlock getBlockAt(int x, int y, int z);

    public MCBlock getHighestBlockAt(int x, int z);

    public List<MCLivingEntity> getLivingEntities();

    public String getName();

    public MCLocation getSpawnLocation();

    public long getTime();

    public void playEffect(MCLocation l, MCEffect mCEffect, int e, int data);

    public void refreshChunk(int x, int z);

    public void setBiome(int x, int z, MCBiomeType type);
    
    public void setStorm(boolean b);
    
    public void setTime(long time);

    public MCEntity spawn(MCLocation l, Class mobType);
    
    public CArray spawnMob(String name, String subClass, int qty, MCLocation location, Target t);

    public void strikeLightning(MCLocation GetLocation);

    public void strikeLightningEffect(MCLocation GetLocation);
}
