package com.laytonsmith.abstraction.bukkit;

import com.laytonsmith.abstraction.MCCrossbowMeta;
import com.laytonsmith.abstraction.MCItemStack;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.CrossbowMeta;

import java.util.ArrayList;
import java.util.List;

public class BukkitMCCrossbowMeta extends BukkitMCItemMeta implements MCCrossbowMeta {

	CrossbowMeta cbm;

	public BukkitMCCrossbowMeta(CrossbowMeta im) {
		super(im);
		this.cbm = im;
	}

	@Override
	public boolean hasChargedProjectiles() {
		return cbm.hasChargedProjectiles();
	}

	@Override
	public List<MCItemStack> getChargedProjectiles() {
		List<MCItemStack> projectiles = new ArrayList<>();
		for(ItemStack item : cbm.getChargedProjectiles()) {
			projectiles.add(new BukkitMCItemStack(item));
		}
		return projectiles;
	}

	@Override
	public void setChargedProjectiles(List<MCItemStack> projectiles) {
		List<ItemStack> proj = new ArrayList<>();
		for(MCItemStack item : projectiles) {
			if(item.getHandle() != null) {
				proj.add((ItemStack) item.getHandle());
			}
		}
		cbm.setChargedProjectiles(proj);
	}
}
