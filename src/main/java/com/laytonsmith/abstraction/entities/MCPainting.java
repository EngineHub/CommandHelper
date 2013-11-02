package com.laytonsmith.abstraction.entities;

import com.laytonsmith.abstraction.enums.MCArt;

/**
 * 
 */
public interface MCPainting extends MCHanging {
	MCArt getArt();
	boolean setArt(MCArt art);
	boolean setArt(MCArt art, boolean force);
}
