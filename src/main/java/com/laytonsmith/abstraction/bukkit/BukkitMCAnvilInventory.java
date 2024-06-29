package com.laytonsmith.abstraction.bukkit;

import com.laytonsmith.abstraction.MCAnvilInventory;
import com.laytonsmith.abstraction.MCItemStack;
import org.bukkit.inventory.AnvilInventory;

public class BukkitMCAnvilInventory extends BukkitMCInventory implements MCAnvilInventory {

	AnvilInventory ai;

	public BukkitMCAnvilInventory(AnvilInventory inventory) {
		super(inventory);
		ai = inventory;
	}

	@Override
	public MCItemStack getFirstItem() {
		return new BukkitMCItemStack(ai.getItem(0));
	}

	@Override
	public MCItemStack getSecondItem() {
		return new BukkitMCItemStack(ai.getItem(1));
	}

	@Override
	public MCItemStack getResult() {
		return new BukkitMCItemStack(ai.getItem(2));
	}

	@Override
	public int getMaximumRepairCost() {
		return ai.getMaximumRepairCost();
	}

	@Override
	public int getRepairCost() {
		return ai.getRepairCost();
	}

	@Override
	public int getRepairCostAmount() {
		return ai.getRepairCostAmount();
	}

	@Override
	public String getRenameText() {
		return ai.getRenameText();
	}

	@Override
	public void setFirstItem(MCItemStack stack) {
		ai.setItem(0, ((BukkitMCItemStack) stack).asItemStack());
	}

	@Override
	public void setSecondItem(MCItemStack stack) {
		ai.setItem(1, ((BukkitMCItemStack) stack).asItemStack());
	}

	@Override
	public void setResult(MCItemStack stack) {
		ai.setItem(2, ((BukkitMCItemStack) stack).asItemStack());
	}

	@Override
	public void setMaximumRepairCost(int levels) {
		ai.setMaximumRepairCost(levels);
	}

	@Override
	public void setRepairCost(int levels) {
		ai.setRepairCost(levels);
	}

	@Override
	public void setRepairCostAmount(int levels) {
		ai.setRepairCostAmount(levels);
	}
}
