package com.laytonsmith.abstraction.entities;

import com.laytonsmith.abstraction.MCEntity;
import com.laytonsmith.abstraction.blocks.MCBlockFace;

public interface MCHanging extends MCEntity {

	MCBlockFace getFacing();

	void setFacingDirection(MCBlockFace direction);

	boolean setFacingDirection(MCBlockFace direction, boolean force);
}
