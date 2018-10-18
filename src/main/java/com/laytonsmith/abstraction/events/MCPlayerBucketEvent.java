package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.MCItemStack;
import com.laytonsmith.abstraction.MCPlayer;
import com.laytonsmith.abstraction.blocks.MCBlock;
import com.laytonsmith.abstraction.blocks.MCBlockFace;
import com.laytonsmith.abstraction.blocks.MCMaterial;
import com.laytonsmith.core.events.BindableEvent;

public interface MCPlayerBucketEvent extends BindableEvent {

	MCBlock getBlockClicked();

	MCBlockFace getBlockFace();

	MCMaterial getBucket();

	MCItemStack getItemStack();

	MCPlayer getPlayer();

	boolean isCancelled();

	void setCancelled(boolean cancel);

	void setItemStack(MCItemStack is);

}
