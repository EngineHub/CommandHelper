package com.laytonsmith.abstraction.blocks;

import com.laytonsmith.abstraction.AbstractionObject;
import com.laytonsmith.abstraction.MCMaterialData;

public interface MCMaterial extends AbstractionObject {

	short getMaxDurability();

	int getType();

	MCMaterialData getData();

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
}
