package com.laytonsmith.abstraction.bukkit.entities;

import org.bukkit.entity.ThrownPotion;
import org.bukkit.inventory.ItemStack;

import com.laytonsmith.abstraction.AbstractionObject;
import com.laytonsmith.abstraction.MCItemStack;
import com.laytonsmith.abstraction.bukkit.BukkitMCItemStack;
import com.laytonsmith.abstraction.entities.MCThrownPotion;

/**
 *
 * @author Hekta
 */
public class BukkitMCThrownPotion extends BukkitMCProjectile implements MCThrownPotion {

	public BukkitMCThrownPotion(ThrownPotion potion) {
		super(potion);
	}

	public BukkitMCThrownPotion(AbstractionObject ao) {
		this((ThrownPotion) ao.getHandle());
	}

	@Override
	public ThrownPotion getHandle() {
		return (ThrownPotion) metadatable;
	}

	public MCItemStack getItem() {
		return new BukkitMCItemStack(getHandle().getItem());
	}

	public void setItem(MCItemStack item) {
		getHandle().setItem((ItemStack) item.getHandle());
	}
}