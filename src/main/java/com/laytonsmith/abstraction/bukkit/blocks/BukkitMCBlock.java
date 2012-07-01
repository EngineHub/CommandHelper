/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.abstraction.bukkit.blocks;

import com.laytonsmith.abstraction.MCItemStack;
import com.laytonsmith.abstraction.MCWorld;
import com.laytonsmith.abstraction.blocks.MCBlock;
import com.laytonsmith.abstraction.blocks.MCBlockState;
import com.laytonsmith.abstraction.blocks.MCMaterial;
import com.laytonsmith.abstraction.blocks.MCSign;
import com.laytonsmith.abstraction.bukkit.BukkitMCItemStack;
import com.laytonsmith.abstraction.bukkit.BukkitMCWorld;
import java.util.ArrayList;
import java.util.Collection;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.inventory.ItemStack;

/**
 *
 * @author layton
 */
public class BukkitMCBlock implements MCBlock{
    Block b;
    public BukkitMCBlock(Block b){
        this.b = b;
    }
    
    public Block __Block() {
        return b;
    }
    
    public byte getData(){
        return b.getData();
    }

    public Collection<MCItemStack> getDrops() {
        Collection<MCItemStack> collection = new ArrayList<MCItemStack>();
        for(ItemStack is : b.getDrops()){
            collection.add(new BukkitMCItemStack(is));
        }
        return collection;
    }

    public MCSign getSign() {
        return new BukkitMCSign((Sign)b.getState());
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

    public int getTypeId(){
        if(b == null){
            return 0;
        }
        return b.getTypeId();
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

    public boolean isNull() {
        return b == null;
    }

    public boolean isSign() {
        return (b.getType() == Material.SIGN || b.getType() == Material.SIGN_POST || b.getType() == Material.WALL_SIGN);
    }

    public void setData(byte imeta) {
        b.setData(imeta);
    }

    public void setTypeId(int idata) {
        b.setTypeId(idata);
    }
    
    
}
