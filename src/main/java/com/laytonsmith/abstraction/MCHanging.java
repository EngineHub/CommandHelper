package com.laytonsmith.abstraction;

import com.laytonsmith.abstraction.blocks.MCBlockFace;

public interface MCHanging extends MCEntity {
	MCBlockFace getFacing();
	void setFacingDirection(MCBlockFace direction);
	boolean setFacingDirection(MCBlockFace direction, boolean force);
}