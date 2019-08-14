package com.laytonsmith.abstraction.bukkit;

import com.laytonsmith.abstraction.MCItemStack;
import com.laytonsmith.abstraction.MCRecipe;
import org.bukkit.inventory.Recipe;

public abstract class BukkitMCRecipe implements MCRecipe {

	private Recipe r;

	protected BukkitMCRecipe(Recipe rec) {
		r = rec;
	}

	@Override
	public Object getHandle() {
		return r;
	}

	@Override
	public MCItemStack getResult() {
		return new BukkitMCItemStack(r.getResult());
	}

	@Override
	public boolean equals(Object obj) {
		return r.equals(obj);
	}

	@Override
	public int hashCode() {
		return r.hashCode();
	}

	@Override
	public String toString() {
		return r.toString();
	}

}
