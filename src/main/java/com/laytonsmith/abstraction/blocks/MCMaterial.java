package com.laytonsmith.abstraction.blocks;

import com.laytonsmith.abstraction.AbstractionObject;

public interface MCMaterial extends AbstractionObject {

	MCBlockData createBlockData();

	short getMaxDurability();

	int getType();

	String getName();

	int getMaxStackSize();

	boolean hasGravity();

	boolean isBlock();

	boolean isBurnable();

	boolean isEdible();

	boolean isFlammable();

	boolean isOccluding();

	boolean isRecord();

	boolean isSolid();

	boolean isTransparent();

	boolean isInteractable();

	boolean isLegacy();

	float getHardness();

	float getBlastResistance();
}
