/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.abstraction.blocks;

import com.laytonsmith.abstraction.MCWorld;

/**
 *
 * @author layton
 */
public interface MCBlock {
    public int getTypeId();
    public byte getData();

    public void setTypeId(int idata);

    public void setData(byte imeta);
    
    public MCBlockState getState();

    public MCMaterial getType();

    public MCWorld getWorld();
    
    public int getX();

    public int getY();

    public int getZ();

    public MCSign getSign();

    public boolean isSign();
}
