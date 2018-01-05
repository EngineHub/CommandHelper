package com.laytonsmith.abstraction.bukkit;

import com.laytonsmith.abstraction.MCFurnaceRecipe;
import com.laytonsmith.abstraction.MCItemStack;
import com.laytonsmith.abstraction.enums.MCRecipeType;
import org.bukkit.Material;
import org.bukkit.inventory.FurnaceRecipe;

/**
 * 
 * @author jb_aero
 */
public class BukkitMCFurnaceRecipe extends BukkitMCRecipe implements MCFurnaceRecipe {

	FurnaceRecipe fr;
	public BukkitMCFurnaceRecipe(FurnaceRecipe recipe) {
		super(recipe);
		fr = recipe;
	}
	
	@Override
	public MCRecipeType getRecipeType() {
		return MCRecipeType.FURNACE;
	}
	
	@Override
	public MCItemStack getResult() {
		return new BukkitMCItemStack(fr.getResult());
	}

	@Override
	public Object getHandle() {
		return fr;
	}

	@Override
	public MCItemStack getInput() {
		return new BukkitMCItemStack(fr.getInput());
	}

	@Override
	public MCFurnaceRecipe setInput(MCItemStack input) {
		int type = input.getTypeId();
		int data = 0;
		if (type < 256) {
			data = input.getData() != null ? input.getData().getData() : 0;
		} else {
			data = input.getDurability();
		}
		return this.setInput(type, data);
	}

	@Override
	public MCFurnaceRecipe setInput(int type, int data) {
		fr.setInput(Material.getMaterial(type), data);
		return this;
	}
}
