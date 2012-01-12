/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.puls3.abstraction.bukkit.blocks;

import com.laytonsmith.puls3.abstraction.blocks.MCBlockState;
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
