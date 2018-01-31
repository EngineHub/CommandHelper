package com.laytonsmith.abstraction.bukkit;

import com.laytonsmith.abstraction.MCCraftingInventory;
import com.laytonsmith.abstraction.MCItemStack;
import com.laytonsmith.abstraction.MCRecipe;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.ItemStack;

public class BukkitMCCraftingInventory extends BukkitMCInventory implements MCCraftingInventory {

	CraftingInventory ci;
	public BukkitMCCraftingInventory(CraftingInventory inventory) {
		super(inventory);
		ci = inventory;
	}

	@Override
	public MCItemStack[] getMatrix() {
		MCItemStack[] matrix = new MCItemStack[ci.getMatrix().length];
		for(int i=0; i<matrix.length; i++) {
			matrix[i] = ci.getMatrix()[i] == null ? null : new BukkitMCItemStack(ci.getMatrix()[i]);
		}
		return matrix;
	}

	@Override
	public MCRecipe getRecipe() {
		return BukkitConvertor.BukkitGetRecipe(ci.getRecipe());
	}

	@Override
	public MCItemStack getResult() {
		ItemStack is = ci.getResult();
		if(is == null) {
			return null;
		}
		return new BukkitMCItemStack(is);
	}

	@Override
	public void setMatrix(MCItemStack[] contents) {
		ItemStack[] matrix = new ItemStack[contents.length];
		for(int i=0; i<matrix.length; i++) {
			matrix[i] = contents[i] == null ? null : ((BukkitMCItemStack) contents[i]).asItemStack();
		}
		ci.setMatrix(matrix);
	}

	@Override
	public void setResult(MCItemStack result) {
		if(result == null) {
			ci.setResult(null);
		} else {
			ci.setResult(((BukkitMCItemStack) result).asItemStack());
		}
	}
}
