package com.laytonsmith.abstraction.bukkit;

import com.laytonsmith.abstraction.AbstractionObject;
import com.laytonsmith.abstraction.MCItemStack;
import com.laytonsmith.abstraction.MCPlayerInventory;
import org.bukkit.inventory.PlayerInventory;

public class BukkitMCPlayerInventory extends BukkitMCInventory implements MCPlayerInventory {

	private PlayerInventory i;

	public BukkitMCPlayerInventory(PlayerInventory inventory) {
		super(inventory);
		this.i = inventory;
	}

	public BukkitMCPlayerInventory(AbstractionObject a) {
		this((PlayerInventory) null);
		if(a instanceof MCPlayerInventory) {
			this.i = ((PlayerInventory) a.getHandle());
		} else {
			throw new ClassCastException();
		}
	}

	@Override
	public void setHelmet(MCItemStack stack) {
		this.i.setHelmet(((BukkitMCItemStack) stack).__ItemStack());
	}

	@Override
	public void setChestplate(MCItemStack stack) {
		this.i.setChestplate(((BukkitMCItemStack) stack).__ItemStack());
	}

	@Override
	public void setLeggings(MCItemStack stack) {
		this.i.setLeggings(((BukkitMCItemStack) stack).__ItemStack());
	}

	@Override
	public void setBoots(MCItemStack stack) {
		this.i.setBoots(((BukkitMCItemStack) stack).__ItemStack());
	}

	@Override
	public void setItemInMainHand(MCItemStack stack) {
		this.i.setItemInMainHand(((BukkitMCItemStack) stack).__ItemStack());
	}

	@Override
	public void setItemInOffHand(MCItemStack stack) {
		this.i.setItemInOffHand(((BukkitMCItemStack) stack).__ItemStack());
	}

	@Override
	public MCItemStack getHelmet() {
		return new BukkitMCItemStack(this.i.getHelmet());
	}

	@Override
	public MCItemStack getChestplate() {
		return new BukkitMCItemStack(this.i.getChestplate());
	}

	@Override
	public MCItemStack getLeggings() {
		return new BukkitMCItemStack(this.i.getLeggings());
	}

	@Override
	public MCItemStack getBoots() {
		return new BukkitMCItemStack(this.i.getBoots());
	}

	@Override
	public MCItemStack getItemInMainHand() {
		return new BukkitMCItemStack(this.i.getItemInMainHand());
	}

	@Override
	public MCItemStack getItemInOffHand() {
		return new BukkitMCItemStack(this.i.getItemInOffHand());
	}

	@Override
	public int getHeldItemSlot() {
		return i.getHeldItemSlot();
	}

	@Override
	public void setHeldItemSlot(int slot) {
		i.setHeldItemSlot(slot);
	}
}
