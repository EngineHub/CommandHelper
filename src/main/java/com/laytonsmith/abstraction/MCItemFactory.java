package com.laytonsmith.abstraction;

import com.laytonsmith.abstraction.blocks.MCMaterial;

/**
 * 
 * @author jb_aero
 */
public interface MCItemFactory {

	public MCItemMeta asMetaFor(MCItemMeta meta, MCItemStack stack);
	public MCItemMeta asMetaFor(MCItemMeta meta, MCMaterial material);
	public boolean equals(MCItemMeta meta1, MCItemMeta meta2);
	public MCColor getDefaultLeatherColor();
	public MCItemMeta getItemMeta(MCMaterial material);
	public boolean isApplicable(MCItemMeta meta, MCItemStack stack);
	public boolean isApplicable(MCItemMeta meta, MCMaterial material);
	
}
