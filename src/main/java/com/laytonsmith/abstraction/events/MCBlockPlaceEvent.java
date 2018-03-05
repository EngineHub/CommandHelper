package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.MCItemStack;
import com.laytonsmith.abstraction.MCPlayer;
import com.laytonsmith.abstraction.blocks.MCBlock;
import com.laytonsmith.abstraction.blocks.MCBlockState;
import com.laytonsmith.core.events.BindableEvent;

public interface MCBlockPlaceEvent extends BindableEvent {

	MCPlayer getPlayer();

	MCBlock getBlock();

	MCBlock getBlockAgainst();

	MCBlockState getBlockReplacedState();

	MCItemStack getItemInHand();

	boolean canBuild();
}
