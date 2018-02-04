package com.laytonsmith.abstraction.bukkit.entities;

import com.laytonsmith.abstraction.MCInventory;
import com.laytonsmith.abstraction.MCItemStack;
import com.laytonsmith.abstraction.bukkit.BukkitMCInventory;
import com.laytonsmith.abstraction.bukkit.BukkitMCItemStack;
import com.laytonsmith.abstraction.entities.MCAbstractHorse;
import org.bukkit.entity.AbstractHorse;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.InventoryHolder;

public class BukkitMCAbstractHorse extends BukkitMCTameable implements MCAbstractHorse {

	AbstractHorse ah;

	public BukkitMCAbstractHorse(Entity t) {
		super(t);
		ah = (AbstractHorse) t;
	}

	@Override
	public MCInventory getInventory() {
		return new BukkitMCInventory(ah.getInventory());
	}

	@Override
	public double getJumpStrength() {
		return ah.getJumpStrength();
	}

	@Override
	public void setJumpStrength(double strength) {
		ah.setJumpStrength(strength);
	}

	@Override
	public int getDomestication() {
		return ah.getDomestication();
	}

	@Override
	public int getMaxDomestication() {
		return ah.getMaxDomestication();
	}

	@Override
	public void setDomestication(int level) {
		ah.setDomestication(level);
	}

	@Override
	public void setMaxDomestication(int level) {
		ah.setMaxDomestication(level);
	}

	@Override
	public void setSaddle(MCItemStack stack) {
		((InventoryHolder) ah).getInventory().setItem(0, ((BukkitMCItemStack)stack).asItemStack());
	}

	@Override
	public MCItemStack getSaddle() {
		return new BukkitMCItemStack(((InventoryHolder) ah).getInventory().getItem(0));
	}
}
