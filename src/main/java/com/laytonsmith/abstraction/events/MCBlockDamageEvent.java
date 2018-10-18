package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.MCItemStack;
import com.laytonsmith.abstraction.MCPlayer;
import com.laytonsmith.abstraction.blocks.MCBlock;
import com.laytonsmith.core.events.BindableEvent;

public interface MCBlockDamageEvent extends BindableEvent {

	boolean getInstaBreak();

	MCItemStack getItemInHand();

	MCPlayer getPlayer();

	MCBlock getBlock();

	boolean isCancelled();

	void setCancelled(boolean cancel);

	void setInstaBreak(boolean bool);

}
