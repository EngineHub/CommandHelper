package com.laytonsmith.abstraction.bukkit;

import java.util.EnumMap;
import java.util.Map;

import com.laytonsmith.abstraction.MCEntityEquipment;
import com.laytonsmith.abstraction.MCItemStack;
import com.laytonsmith.abstraction.enums.MCEquipmentSlot;

import org.bukkit.inventory.EntityEquipment;

/**
 *
 * @author jb_aero
 */
public class BukkitMCEntityEquipment implements MCEntityEquipment {

	private EntityEquipment ee;

	public BukkitMCEntityEquipment(EntityEquipment equipment) {
		this.ee = equipment;
	}
	
	public void clearEquipment() {
		ee.clear();
	}

	public int getSize() {
		return MCEquipmentSlot.values().length;
	}

	public Map<MCEquipmentSlot, MCItemStack> getAllEquipment() {
		Map<MCEquipmentSlot, MCItemStack> slots = new EnumMap<MCEquipmentSlot, MCItemStack>(MCEquipmentSlot.class);
		for (MCEquipmentSlot key : slots.keySet()) {
			switch (key) {
			case WEAPON:
				slots.put(key, (BukkitMCItemStack) getWeapon());
				break;
			case HELMET:
				slots.put(key, (BukkitMCItemStack) getWeapon());
				break;
			case CHESTPLATE:
				slots.put(key, (BukkitMCItemStack) getChestplate());
				break;
			case LEGGINGS:
				slots.put(key, (BukkitMCItemStack) getLeggings());
				break;
			case BOOTS:
				slots.put(key, (BukkitMCItemStack) getBoots());
				break;
			}
		}
		return slots;
	}

	public void setAllEquipment(Map<MCEquipmentSlot, MCItemStack> slots) {
		MCItemStack stack = null;
		for (MCEquipmentSlot key : slots.keySet()) {
			stack = slots.get(key);
			switch (key) {
			case WEAPON:
				setWeapon(stack);
				break;
			case HELMET:
				setHelmet(stack);
				break;
			case CHESTPLATE:
				setChestplate(stack);
				break;
			case LEGGINGS:
				setLeggings(stack);
				break;
			case BOOTS:
				setBoots(stack);
				break;
			}
		}
	}

	// For the purposes of faking a normal inventory, we most likely will not be accessing
	// anything below this line, but they are here for flexibility and completion
	
	public MCItemStack getWeapon() {
		return new BukkitMCItemStack(ee.getItemInHand());
	}

	public MCItemStack getHelmet() {
		return new BukkitMCItemStack(ee.getHelmet());
	}

	public MCItemStack getChestplate() {
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

	public float getChestplateDropChance() {
		return ee.getChestplateDropChance();
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

	public void setChestplateDropChance(float chance) {
		ee.setChestplateDropChance(chance);
	}

	public void setLeggingsDropChance(float chance) {
		ee.setLeggingsDropChance(chance);
	}

	public void setBootsDropChance(float chance) {
		ee.setBootsDropChance(chance);
	}

}
