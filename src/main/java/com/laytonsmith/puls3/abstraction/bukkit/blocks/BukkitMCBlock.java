/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.puls3.abstraction.bukkit.blocks;

import com.laytonsmith.puls3.abstraction.MCWorld;
import com.laytonsmith.puls3.abstraction.blocks.MCBlock;
import com.laytonsmith.puls3.abstraction.blocks.MCBlockState;
import com.laytonsmith.puls3.abstraction.blocks.MCMaterial;
import com.laytonsmith.puls3.abstraction.blocks.MCSign;
import com.laytonsmith.puls3.abstraction.bukkit.BukkitMCWorld;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;

/**
 *
 * @author layton
 */
public class BukkitMCBlock implements MCBlock{
    Block b;
    public BukkitMCBlock(Block b){
        this.b = b;
    }
    
    public int getTypeId(){
        return b.getTypeId();
    }
    
    public byte getData(){
        return b.getData();
    }

    public void setTypeId(int idata) {
        b.setTypeId(idata);
    }

    public void setData(byte imeta) {
        b.setData(imeta);
    }

    public MCBlockState getState() {
        if(b.getState() == null){
            return null;
        }
        return new BukkitMCBlockState(b.getState());
    }

    public MCMaterial getType() {
        if(b.getType() == null){
            return null;
        }
        return new BukkitMCMaterial(b.getType());
    }

    public MCWorld getWorld() {
        return new BukkitMCWorld(b.getWorld());
    }

    public int getX() {
        return b.getX();
    }

    public int getY() {
        return b.getY();
    }

    public int getZ() {
        return b.getZ();
    }

    public Block __Block() {
        return b;
    }

    public MCSign getSign() {
        return new BukkitMCSign((Sign)b);
    }

    public boolean isSign() {
        return (b instanceof Sign);
    }
    
    
}
