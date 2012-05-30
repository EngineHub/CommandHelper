package com.laytonsmith.abstraction.events;

import org.bukkit.block.Block;

import com.laytonsmith.abstraction.MCPlayer;
import com.laytonsmith.core.events.BindableEvent;

/**
 *
 * @author EntityReborn
 */
public interface MCBlockBreakEvent extends BindableEvent{
    public MCPlayer getPlayer();
    public Block getBlock();
}