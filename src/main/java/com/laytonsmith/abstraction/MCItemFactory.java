package com.laytonsmith.abstraction;

import com.laytonsmith.abstraction.blocks.MCMaterial;

public interface MCItemFactory {
	MCItemMeta asMetaFor(MCItemMeta meta, MCItemStack stack);
	MCItemMeta asMetaFor(MCItemMeta meta, MCMaterial material);
	boolean equals(MCItemMeta meta1, MCItemMeta meta2);
	MCColor getDefaultLeatherColor();
	MCItemMeta getItemMeta(MCMaterial material);
	boolean isApplicable(MCItemMeta meta, MCItemStack stack);
	boolean isApplicable(MCItemMeta meta, MCMaterial material);
}
