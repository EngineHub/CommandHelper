package com.laytonsmith.abstraction.blocks;

import com.laytonsmith.abstraction.MCItemStack;
import com.laytonsmith.abstraction.MCLocation;
import com.laytonsmith.abstraction.MCMetadatable;
import com.laytonsmith.abstraction.MCWorld;

import java.util.Collection;

public interface MCBlock extends MCMetadatable {

	boolean isNull();

	MCMaterial getType();

	@Deprecated
	int getTypeId();

	byte getData();

	void setType(MCMaterial mat);

	@Deprecated
	void setTypeId(int idata);

	void setData(byte imeta);

	@Deprecated
	void setTypeAndData(int type, byte data, boolean physics);

	MCBlockState getState();

	MCWorld getWorld();

	int getX();

	int getY();

	int getZ();

	MCLocation getLocation();

	MCSign getSign();

	boolean isSign();

	MCCommandBlock getCommandBlock();

	boolean isCommandBlock();

	MCDispenser getDispenser();

	boolean isDispenser();

	boolean isSolid();

	boolean isFlammable();

	boolean isTransparent();

	boolean isOccluding();

	boolean isBurnable();

	Collection<MCItemStack> getDrops();

	Collection<MCItemStack> getDrops(MCItemStack tool);

	double getTemperature();

	int getLightLevel();

	int getBlockPower();

	boolean isBlockPowered();

	boolean isBlockIndirectlyPowered();

	MCBlock getRelative(MCBlockFace face);

	MCBlockFace getFace(MCBlock get);
}
