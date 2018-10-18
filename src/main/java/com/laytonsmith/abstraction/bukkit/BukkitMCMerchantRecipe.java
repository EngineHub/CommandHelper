package com.laytonsmith.abstraction.bukkit;

import com.laytonsmith.abstraction.MCItemStack;
import com.laytonsmith.abstraction.MCMerchantRecipe;
import com.laytonsmith.abstraction.enums.MCRecipeType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MerchantRecipe;

import java.util.ArrayList;
import java.util.List;

public class BukkitMCMerchantRecipe extends BukkitMCRecipe implements MCMerchantRecipe {

	MerchantRecipe mr;

	public BukkitMCMerchantRecipe(MerchantRecipe recipe) {
		super(recipe);
		mr = recipe;
	}

	@Override
	public void addIngredient(MCItemStack item) {
		mr.addIngredient(((ItemStack) item.getHandle()));
	}

	@Override
	public List<MCItemStack> getIngredients() {
		List<MCItemStack> list = new ArrayList<>();
		for(ItemStack is : mr.getIngredients()) {
			list.add(new BukkitMCItemStack(is));
		}
		return list;
	}

	@Override
	public int getMaxUses() {
		return mr.getMaxUses();
	}

	@Override
	public int getUses() {
		return mr.getUses();
	}

	@Override
	public boolean hasExperienceReward() {
		return mr.hasExperienceReward();
	}

	@Override
	public void removeIngredient(int index) {
		mr.removeIngredient(index);
	}

	@Override
	public void setExperienceReward(boolean flag) {
		mr.setExperienceReward(flag);
	}

	@Override
	public void setIngredients(List<MCItemStack> ingredients) {
		List<ItemStack> list = new ArrayList<>();
		for(MCItemStack mis : ingredients) {
			list.add(((ItemStack) mis.getHandle()));
		}
		mr.setIngredients(list);
	}

	@Override
	public void setMaxUses(int maxUses) {
		mr.setMaxUses(maxUses);
	}

	@Override
	public void setUses(int uses) {
		mr.setUses(uses);
	}

	@Override
	public MCRecipeType getRecipeType() {
		return MCRecipeType.MERCHANT;
	}

	@Override
	public Object getHandle() {
		return mr;
	}

}
