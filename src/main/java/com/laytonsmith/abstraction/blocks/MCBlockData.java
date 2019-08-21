package com.laytonsmith.abstraction.blocks;

import com.laytonsmith.abstraction.AbstractionObject;

public interface MCBlockData extends AbstractionObject {
	MCMaterial getMaterial();
	String getAsString();
}
