/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.abstraction.blocks;

import com.laytonsmith.abstraction.MCItemStack;
import com.laytonsmith.abstraction.MCWorld;
import java.util.Collection;

/**
 *
 * @author layton
 */
public interface MCBlock {

    public byte getData();

    public Collection<MCItemStack> getDrops();

    public MCSign getSign();

    public MCBlockState getState();

    public MCMaterial getType();

    public int getTypeId();

    public MCWorld getWorld();

    public int getX();

    public int getY();

    public int getZ();

    public boolean isNull();

    public boolean isSign();

    public void setData(byte imeta);

    public void setTypeId(int idata);
}
