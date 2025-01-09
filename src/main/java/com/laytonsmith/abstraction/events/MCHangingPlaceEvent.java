package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.MCItemStack;
import com.laytonsmith.abstraction.MCPlayer;
import com.laytonsmith.abstraction.blocks.MCBlock;
import com.laytonsmith.abstraction.blocks.MCBlockFace;
import com.laytonsmith.abstraction.entities.MCHanging;
import com.laytonsmith.abstraction.enums.MCEquipmentSlot;
import com.laytonsmith.core.events.BindableEvent;

public interface MCHangingPlaceEvent extends BindableEvent {

	MCHanging getEntity();

	MCPlayer getPlayer();

	MCBlock getBlock();

	MCBlockFace getBlockFace();

	MCItemStack getItem();

	MCEquipmentSlot getHand();

}
