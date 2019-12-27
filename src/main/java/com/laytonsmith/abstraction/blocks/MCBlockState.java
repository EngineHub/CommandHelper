package com.laytonsmith.abstraction.blocks;

import com.laytonsmith.abstraction.MCLocation;
import com.laytonsmith.abstraction.MCMetadatable;

public interface MCBlockState extends MCMetadatable {

	MCMaterial getType();

	MCBlock getBlock();

	MCLocation getLocation();

	void update();

	boolean isLockable();

	boolean isLocked();

	String getLock();

	void setLock(String key);

}
