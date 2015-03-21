package com.laytonsmith.abstraction.bukkit.entities;

import com.laytonsmith.abstraction.AbstractionObject;
import com.laytonsmith.abstraction.MCItemStack;
import com.laytonsmith.abstraction.bukkit.BukkitMCItemStack;
import com.laytonsmith.abstraction.entities.MCThrownPotion;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.inventory.ItemStack;

/**
 *
 * @author Hekta
 */
public class BukkitMCThrownPotion extends BukkitMCProjectile implements MCThrownPotion {

	public BukkitMCThrownPotion(Entity potion) {
		super(potion);
	}

	public BukkitMCThrownPotion(AbstractionObject ao) {
		this((ThrownPotion) ao.getHandle());
	}

	@Override
	public ThrownPotion getHandle() {
		return (ThrownPotion)super.getHandle();
	}

	@Override
	public MCItemStack getItem() {
		return new BukkitMCItemStack(getHandle().getItem());
	}

	@Override
	public void setItem(MCItemStack item) {
		getHandle().setItem((ItemStack) item.getHandle());
	}
}