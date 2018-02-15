package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.MCItemStack;
import com.laytonsmith.abstraction.MCPlayer;
import com.laytonsmith.abstraction.blocks.MCBlock;
import com.laytonsmith.core.events.BindableEvent;

import java.util.List;

public interface MCBlockBreakEvent extends BindableEvent {
	MCPlayer getPlayer();
	MCBlock getBlock();
	int getExpToDrop();
	void setExpToDrop(int exp);
	List<MCItemStack> getDrops();
	void setDrops(List<MCItemStack> drops);
	boolean isModified();
}