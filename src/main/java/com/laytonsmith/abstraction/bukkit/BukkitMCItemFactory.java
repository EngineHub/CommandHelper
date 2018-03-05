package com.laytonsmith.abstraction.bukkit;

import com.laytonsmith.abstraction.MCColor;
import com.laytonsmith.abstraction.MCItemFactory;
import com.laytonsmith.abstraction.MCItemMeta;
import com.laytonsmith.abstraction.MCItemStack;
import com.laytonsmith.abstraction.blocks.MCMaterial;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFactory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class BukkitMCItemFactory implements MCItemFactory {

	ItemFactory f;

	public BukkitMCItemFactory(ItemFactory itemFactory) {
		this.f = itemFactory;
	}

	@Override
	public MCItemMeta asMetaFor(MCItemMeta meta, MCItemStack stack) {
		ItemMeta bmeta = ((BukkitMCItemMeta) meta).asItemMeta();
		ItemStack bstack = ((BukkitMCItemStack) stack).asItemStack();
		return BukkitConvertor.BukkitGetCorrectMeta(f.asMetaFor(bmeta, bstack));
	}

	@Override
	public MCItemMeta asMetaFor(MCItemMeta meta, MCMaterial material) {
		ItemMeta bmeta = ((BukkitMCItemMeta) meta).asItemMeta();
		Material bmat = (Material) material.getHandle();
		return BukkitConvertor.BukkitGetCorrectMeta(f.asMetaFor(bmeta, bmat));
	}

	@Override
	public boolean equals(MCItemMeta meta1, MCItemMeta meta2) {
		return f.equals(((BukkitMCItemMeta) meta1).asItemMeta(), ((BukkitMCItemMeta) meta2).asItemMeta());
	}

	@Override
	public MCColor getDefaultLeatherColor() {
		return BukkitMCColor.GetMCColor(f.getDefaultLeatherColor());
	}

	@Override
	public MCItemMeta getItemMeta(MCMaterial material) {
		if(material == null) {
			return null;
		}
		ItemMeta im = f.getItemMeta((Material) material.getHandle());
		return BukkitConvertor.BukkitGetCorrectMeta(im);
	}

	@Override
	public boolean isApplicable(MCItemMeta meta, MCItemStack stack) {
		ItemMeta bmeta = ((BukkitMCItemMeta) meta).asItemMeta();
		ItemStack bstack = ((BukkitMCItemStack) stack).asItemStack();
		return f.isApplicable(bmeta, bstack);
	}

	@Override
	public boolean isApplicable(MCItemMeta meta, MCMaterial material) {
		ItemMeta bmeta = ((BukkitMCItemMeta) meta).asItemMeta();
		Material bmat = (Material) material.getHandle();
		return f.isApplicable(bmeta, bmat);
	}

}
