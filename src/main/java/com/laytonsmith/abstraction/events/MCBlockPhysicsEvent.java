package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.blocks.MCBlock;
import com.laytonsmith.abstraction.blocks.MCMaterial;

/**
 *
 * @author Hekta
 */
public interface MCBlockPhysicsEvent extends MCBlockEvent {

	public MCBlock getBlock();

	public MCMaterial getChangedType();
}
