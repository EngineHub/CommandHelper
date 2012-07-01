/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.abstraction;

import com.laytonsmith.abstraction.blocks.MCBlock;

/**
 *
 * @author layton
 */
public interface MCLocation extends AbstractionObject{    
    public MCLocation clone();
    public MCBlock getBlock();
    public int getBlockX();
    public int getBlockY();
    public int getBlockZ();
    public float getPitch();
    public MCWorld getWorld();
    public double getX();
    public double getY();
    public float getYaw();
    
    public double getZ();
    public void setPitch(float p);
    
    public void setYaw(float y);
}
