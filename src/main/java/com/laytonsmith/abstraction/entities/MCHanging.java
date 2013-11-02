package com.laytonsmith.abstraction.entities;

import com.laytonsmith.abstraction.enums.MCBlockFace;

/**
 * 
 * @author jb_aero
 */
public interface MCHanging extends MCEntity {

	public MCBlockFace getFacing();
	public void setFacingDirection(MCBlockFace direction);
	public boolean setFacingDirection(MCBlockFace direction, boolean force);
}