package com.laytonsmith.abstraction.bukkit;

import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.ItemStack;

import com.laytonsmith.abstraction.MCCraftingInventory;
import com.laytonsmith.abstraction.MCItemStack;
import com.laytonsmith.abstraction.MCRecipe;

public class BukkitMCCraftingInventory extends BukkitMCInventory implements MCCraftingInventory {

	CraftingInventory ci;
	public BukkitMCCraftingInventory(CraftingInventory inventory) {
		super(inventory);
		ci = inventory;
	}

	@Override
	public MCItemStack[] getMatrix() {
		MCItemStack[] matrix = new MCItemStack[ci.getMatrix().length];
		for (int i=0; i<matrix.length; i++) {
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
		return ci.getResult() == null ? null : new BukkitMCItemStack(ci.getResult());
	}

	@Override
	public void setMatrix(MCItemStack[] contents) {
		ItemStack[] matrix = new ItemStack[contents.length];
		for (int i=0; i<matrix.length; i++) {
			matrix[i] = contents[i] == null ? null : ((BukkitMCItemStack) contents[i]).asItemStack();
		}
		ci.setMatrix(matrix);
	}

	@Override
	public void setResult(MCItemStack result) {
		ci.setResult(result == null ? null : ((BukkitMCItemStack) result).asItemStack());
	}
}
