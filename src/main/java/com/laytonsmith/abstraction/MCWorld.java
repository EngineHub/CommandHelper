/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.abstraction;

import com.laytonsmith.abstraction.blocks.MCBlock;
import com.laytonsmith.core.constructs.Construct;
import java.io.File;
import java.util.List;

/**
 *
 * @author layton
 */
public interface MCWorld {
    public List<MCLivingEntity> getLivingEntities();
    public String getName();
    public MCBlock getBlockAt(int x, int y, int z);

    public MCEntity spawn(MCLocation l, Class mobType);

    public void playEffect(MCLocation l, MCEffect mCEffect, int e, int data);

    public void dropItemNaturally(MCLocation l, MCItemStack is);

    public void dropItem(MCLocation l, MCItemStack is);

    public void strikeLightning(MCLocation GetLocation);

    public void strikeLightningEffect(MCLocation GetLocation);

    public void setStorm(boolean b);

    public MCLocation getSpawnLocation();

    public void refreshChunk(int x, int z);

    public void setTime(long time);
    
    public long getTime();
    
    public Construct spawnMob(String name, String subClass, int qty, MCLocation location, int line_num, File file);
}
