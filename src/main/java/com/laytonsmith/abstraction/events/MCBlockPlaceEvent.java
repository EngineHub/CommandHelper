package com.laytonsmith.abstraction.events;

import org.bukkit.block.Block;
import org.bukkit.block.BlockState;

import com.laytonsmith.abstraction.MCItemStack;
import com.laytonsmith.abstraction.MCPlayer;
import com.laytonsmith.core.events.BindableEvent;

/**
 *
 * @author EntityReborn
 */
public interface MCBlockPlaceEvent extends BindableEvent{
    public MCPlayer getPlayer();
    public Block getBlock();
    public Block getBlockAgainst();
    public BlockState getBlockReplacedState();
    public MCItemStack getItemInHand();
    public boolean canBuild();
}