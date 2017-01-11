package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.MCItemStack;
import com.laytonsmith.abstraction.MCPlayer;
import com.laytonsmith.abstraction.blocks.MCBlock;
import com.laytonsmith.core.events.BindableEvent;

import java.util.List;

/**
 *
 * @author EntityReborn
 */
public interface MCBlockBreakEvent extends BindableEvent {

	public MCPlayer getPlayer();

	public MCBlock getBlock();

	public int getExpToDrop();

	public void setExpToDrop(int exp);

	public List<MCItemStack> getDrops();

	public void setDrops(List<MCItemStack> drops);

	public boolean isModified();
}