package com.laytonsmith.abstraction.bukkit;

import com.laytonsmith.abstraction.MCEntityEquipment;
import com.laytonsmith.abstraction.MCItemStack;

import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;

/**
 *
 * @author jb_aero
 */
public class BukkitMCEntityEquipment implements MCEntityEquipment {

	private EntityEquipment ee;
	
	// Slot positions
	private int WEAP = 0;
	/*
	private int HELM = 4;
	private int CHEST = 3;
	private int LEG = 2;
	private int BOOT = 1;
	*/
	
	// Total number of slots
	private int SLOTS = 5;
	
	public BukkitMCEntityEquipment(EntityEquipment equipment) {
		this.ee = equipment;
	}
	
	public void clearEquipment() {
		ee.clear();
	}

	public int getSize() {
		return 5;
	}

	public MCItemStack[] getAllArmor() {
		BukkitMCItemStack[] stacks = new BukkitMCItemStack[SLOTS - 1];
		for (int i=WEAP+1; i < (SLOTS - 1); i++) {
			stacks[i] = new BukkitMCItemStack(ee.getArmorContents()[i]);
		}
		return stacks;
	}

	public void setAllArmor(MCItemStack[] stackarray) {
		ItemStack[] stacks = new ItemStack[SLOTS - 1];
		for (int i=WEAP+1; i < (SLOTS - 1); i++) {
			stacks[i] = ((BukkitMCItemStack) stackarray[i]).asItemStack();
		}
		ee.setArmorContents(stacks);
	}

	// For the purposes of faking a normal inventory, we most likely will not be accessing
	// anything below this line, but they are here for flexibility and completion
	
	public MCItemStack getWeapon() {
		return new BukkitMCItemStack(ee.getItemInHand());
	}

	public MCItemStack getHelmet() {
		return new BukkitMCItemStack(ee.getHelmet());
	}

	public MCItemStack getChestPlate() {
		return new BukkitMCItemStack(ee.getChestplate());
	}

	public MCItemStack getLeggings() {
		return new BukkitMCItemStack(ee.getLeggings());
	}

	public MCItemStack getBoots() {
		return new BukkitMCItemStack(ee.getBoots());
	}

	public void setWeapon(MCItemStack stack) {
		ee.setItemInHand(((BukkitMCItemStack) stack).asItemStack());
	}

	public void setHelmet(MCItemStack stack) {
		ee.setHelmet(((BukkitMCItemStack) stack).asItemStack());
	}

	public void setChestplate(MCItemStack stack) {
		ee.setChestplate(((BukkitMCItemStack) stack).asItemStack());
	}

	public void setLeggings(MCItemStack stack) {
		ee.setLeggings(((BukkitMCItemStack) stack).asItemStack());
	}

	public void setBoots(MCItemStack stack) {
		ee.setBoots(((BukkitMCItemStack) stack).asItemStack());
	}

	public float getWeaponDropChance() {
		return ee.getItemInHandDropChance();
	}

	public float getHelmetDropChance() {
		return ee.getHelmetDropChance();
	}

	public float getChestPlateDropChance() {
		return ee.getChestPlateDropChance();
	}

	public float getLeggingsDropChance() {
		return ee.getLeggingsDropChance();
	}

	public float getBootsDropChance() {
		return ee.getBootsDropChance();
	}

	public void setWeaponDropChance(float chance) {
		ee.setItemInHandDropChance(chance);
	}

	public void setHelmetDropChance(float chance) {
		ee.setHelmetDropChance(chance);
	}

	public void setChestPlateDropChance(float chance) {
		ee.setChestPlateDropChance(chance);
	}

	public void setLeggingsDropChance(float chance) {
		ee.setLeggingsDropChance(chance);
	}

	public void setBootsDropChance(float chance) {
		ee.setBootsDropChance(chance);
	}

}
