package com.laytonsmith.abstraction.blocks;

import com.laytonsmith.abstraction.MCItemStack;
import com.laytonsmith.abstraction.MCLocation;
import com.laytonsmith.abstraction.MCMetadatable;
import com.laytonsmith.abstraction.MCWorld;

import java.util.Collection;

public interface MCBlock extends MCMetadatable {

	MCMaterial getType();

	void setType(MCMaterial mat);

	void setType(MCMaterial mat, boolean physics);

	MCBlockData getBlockData();

	void setBlockData(MCBlockData data, boolean physics);

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

	boolean isPassable();

	boolean isBanner();

	Collection<MCItemStack> getDrops();

	Collection<MCItemStack> getDrops(MCItemStack tool);

	double getTemperature();

	int getLightLevel();

	int getLightFromSky();

	int getLightFromBlocks();

	int getBlockPower();

	boolean isBlockPowered();

	boolean isBlockIndirectlyPowered();

	MCBlock getRelative(MCBlockFace face);

	MCBlockFace getFace(MCBlock get);

	boolean isEmpty();
}
