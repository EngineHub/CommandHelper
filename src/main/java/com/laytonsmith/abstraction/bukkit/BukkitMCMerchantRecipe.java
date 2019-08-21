package com.laytonsmith.abstraction.bukkit;

import com.laytonsmith.abstraction.MCItemStack;
import com.laytonsmith.abstraction.MCMerchantRecipe;
import com.laytonsmith.abstraction.enums.MCRecipeType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MerchantRecipe;

import java.util.ArrayList;
import java.util.List;

public class BukkitMCMerchantRecipe extends BukkitMCRecipe implements MCMerchantRecipe {

	private MerchantRecipe handle;
	public BukkitMCMerchantRecipe(MerchantRecipe recipe) {
		super(recipe);
		handle = recipe;
	}

	@Override
	public String getKey() {
		return null;
	}

	@Override
	public MerchantRecipe getHandle() {
		return handle;
	}

	@Override
	public boolean equals(Object obj) {
		return getHandle().equals(obj);
	}

	@Override
	public int hashCode() {
		return getHandle().hashCode();
	}

	@Override
	public String toString() {
		return getHandle().toString();
	}

	@Override
	public int getMaxUses() {
		return getHandle().getMaxUses();
	}

	@Override
	public void setMaxUses(int maxUses) {
		getHandle().setMaxUses(maxUses);
	}

	@Override
	public int getUses() {
		return getHandle().getUses();
	}

	@Override
	public void setUses(int uses) {
		getHandle().setUses(uses);
	}

	@Override
	public boolean hasExperienceReward() {
		return getHandle().hasExperienceReward();
	}

	@Override
	public void setHasExperienceReward(boolean flag) {
		getHandle().setExperienceReward(flag);
	}

	@Override
	public List<MCItemStack> getIngredients() {
		List<MCItemStack> ret = new ArrayList<>();
		for(ItemStack s : getHandle().getIngredients()) {
			ret.add(new BukkitMCItemStack(s));
		}
		return ret;
	}

	@Override
	public void setIngredients(List<MCItemStack> ingredients) {
		int i = 0;
		List<ItemStack> ings = new ArrayList<>();
		for(MCItemStack s : ingredients) {
			if(++i > 2) {
				break;
				// This recipe type only supports two ingredients.
				// The Bukkit set method does not include built in enforcement
			}
			ings.add((ItemStack) s.getHandle());
		}
		getHandle().setIngredients(ings);
	}

	@Override
	public MCRecipeType getRecipeType() {
		return MCRecipeType.MERCHANT;
	}

	@Override
	public String getGroup() {
		return "";
	}

	@Override
	public void setGroup(String group) {}
}
