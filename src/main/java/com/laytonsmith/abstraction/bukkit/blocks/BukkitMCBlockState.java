/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.abstraction.bukkit.blocks;

import com.laytonsmith.abstraction.blocks.MCBlockState;
import org.bukkit.block.BlockState;

/**
 *
 * @author layton
 */
class BukkitMCBlockState implements MCBlockState {
    
    BlockState bs;

    public BukkitMCBlockState(BlockState state) {
        this.bs = state;
    }
    
}
