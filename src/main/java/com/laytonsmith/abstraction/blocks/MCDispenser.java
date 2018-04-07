package com.laytonsmith.abstraction.blocks;

import com.laytonsmith.abstraction.MCInventoryHolder;

public interface MCDispenser extends MCBlockState, MCInventoryHolder {
	boolean dispense();
	MCBlockProjectileSource getBlockProjectileSource();
}
