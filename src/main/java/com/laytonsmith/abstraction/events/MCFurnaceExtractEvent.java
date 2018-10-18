package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.MCPlayer;
import com.laytonsmith.abstraction.blocks.MCBlock;
import com.laytonsmith.abstraction.blocks.MCMaterial;
import com.laytonsmith.core.events.BindableEvent;

public interface MCFurnaceExtractEvent extends BindableEvent {

	int getExpToDrop();

	int getItemAmount();

	MCMaterial getItemType();

	MCPlayer getPlayer();

	MCBlock getBlock();

	void setExpToDrop(int exp);

}
