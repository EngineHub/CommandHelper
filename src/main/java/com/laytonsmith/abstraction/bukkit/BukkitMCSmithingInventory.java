package com.laytonsmith.abstraction.bukkit;

import com.laytonsmith.abstraction.MCItemStack;
import com.laytonsmith.abstraction.MCRecipe;
import com.laytonsmith.abstraction.MCSmithingInventory;
import org.bukkit.inventory.SmithingInventory;
import org.bukkit.inventory.SmithingRecipe;

public class BukkitMCSmithingInventory extends BukkitMCInventory implements MCSmithingInventory {

	SmithingInventory si;

	public BukkitMCSmithingInventory(SmithingInventory inventory) {
		super(inventory);
		si = inventory;
	}

	@Override
	public MCItemStack getInputEquipment() {
		return new BukkitMCItemStack(si.getItem(1));
	}

	@Override
	public MCItemStack getInputMaterial() {
		return new BukkitMCItemStack(si.getItem(2));
	}

	@Override
	public MCItemStack getInputTemplate() {
		return new BukkitMCItemStack(si.getItem(0));
	}

	@Override
	public MCRecipe getRecipe() {
		if(si.getRecipe() == null) {
			return null;
		} else {
			return new BukkitMCSmithingRecipe((SmithingRecipe) si.getRecipe());
		}
	}

	@Override
	public MCItemStack getResult() {
		return new BukkitMCItemStack(si.getResult());
	}

	@Override
	public void setInputEquipment(MCItemStack stack) {
		si.setItem(1, ((BukkitMCItemStack) stack).asItemStack());
	}

	@Override
	public void setInputMaterial(MCItemStack stack) {
		si.setItem(2, ((BukkitMCItemStack) stack).asItemStack());
	}

	@Override
	public void setInputTemplate(MCItemStack stack) {
		si.setItem(0, ((BukkitMCItemStack) stack).asItemStack());
	}

	@Override
	public void setResult(MCItemStack stack) {
		si.setResult(((BukkitMCItemStack) stack).asItemStack());
	}
}
