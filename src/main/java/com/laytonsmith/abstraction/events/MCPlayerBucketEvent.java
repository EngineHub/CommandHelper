package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.MCItemStack;
import com.laytonsmith.abstraction.blocks.MCBlock;
import com.laytonsmith.abstraction.blocks.MCBlockFace;
import com.laytonsmith.abstraction.blocks.MCMaterial;
import com.laytonsmith.abstraction.enums.MCEquipmentSlot;

public interface MCPlayerBucketEvent extends MCPlayerEvent {
	MCBlock getBlock();

	MCBlock getBlockClicked();

	MCBlockFace getBlockFace();

	MCMaterial getBucket();

	MCEquipmentSlot getHand();

	MCItemStack getItemStack();
}
