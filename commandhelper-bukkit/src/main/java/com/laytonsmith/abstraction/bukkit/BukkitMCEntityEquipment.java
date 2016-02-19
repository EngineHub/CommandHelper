package com.laytonsmith.abstraction.bukkit;

import com.laytonsmith.abstraction.MCEntity;
import com.laytonsmith.abstraction.MCEntityEquipment;
import com.laytonsmith.abstraction.MCItemStack;
import com.laytonsmith.abstraction.enums.MCEquipmentSlot;
import java.util.EnumMap;
import java.util.Map;
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
	
	@Override
	public void clearEquipment() {
		ee.clear();
	}

	@Override
	public int getSize() {
		return MCEquipmentSlot.values().length;
	}
	
	@Override
	public MCEntity getHolder() {
		return BukkitConvertor.BukkitGetCorrectEntity(ee.getHolder());
	}

	@Override
	public Map<MCEquipmentSlot, MCItemStack> getAllEquipment() {
		Map<MCEquipmentSlot, MCItemStack> slots = new EnumMap<MCEquipmentSlot, MCItemStack>(MCEquipmentSlot.class);
		for (MCEquipmentSlot key : MCEquipmentSlot.values()) {
			switch (key) {
			case MCEquipmentSlot.WEAPON:
				slots.put(key, getWeapon());
				break;
			case MCEquipmentSlot.HELMET:
				slots.put(key, getHelmet());
				break;
			case MCEquipmentSlot.CHESTPLATE:
				slots.put(key, getChestplate());
				break;
			case MCEquipmentSlot.LEGGINGS:
				slots.put(key, getLeggings());
				break;
			case MCEquipmentSlot.BOOTS:
				slots.put(key, getBoots());
				break;
			}
		}
		return slots;
	}

	@Override
	public void setAllEquipment(Map<MCEquipmentSlot, MCItemStack> slots) {
		MCItemStack stack = null;
		for (MCEquipmentSlot key : slots.keySet()) {
			stack = slots.get(key);
			switch (key) {
			case MCEquipmentSlot.WEAPON:
				setWeapon(stack);
				break;
			case MCEquipmentSlot.HELMET:
				setHelmet(stack);
				break;
			case MCEquipmentSlot.CHESTPLATE:
				setChestplate(stack);
				break;
			case MCEquipmentSlot.LEGGINGS:
				setLeggings(stack);
				break;
			case MCEquipmentSlot.BOOTS:
				setBoots(stack);
				break;
			}
		}
	}
	
	@Override
	public Map<MCEquipmentSlot, Float> getAllDropChances() {
		Map<MCEquipmentSlot, Float> slots = new EnumMap<MCEquipmentSlot, Float>(MCEquipmentSlot.class);
		for (MCEquipmentSlot key : MCEquipmentSlot.values()) {
			switch (key) {
				case MCEquipmentSlot.WEAPON:
					slots.put(key, getWeaponDropChance());
					break;
				case MCEquipmentSlot.HELMET:
					slots.put(key, getHelmetDropChance());
					break;
				case MCEquipmentSlot.CHESTPLATE:
					slots.put(key, getChestplateDropChance());
					break;
				case MCEquipmentSlot.LEGGINGS:
					slots.put(key, getLeggingsDropChance());
					break;
				case MCEquipmentSlot.BOOTS:
					slots.put(key, getBootsDropChance());
					break;
			}
		}
		return slots;
	}
	
	@Override
	public void setAllDropChances(Map<MCEquipmentSlot, Float> slots) {
		float chance;
		for (MCEquipmentSlot key : slots.keySet()) {
			chance = slots.get(key);
			switch (key) {
				case MCEquipmentSlot.WEAPON:
					setWeaponDropChance(chance);
					break;
				case MCEquipmentSlot.HELMET:
					setHelmetDropChance(chance);
					break;
				case MCEquipmentSlot.CHESTPLATE:
					setChestplateDropChance(chance);
					break;
				case MCEquipmentSlot.LEGGINGS:
					setLeggingsDropChance(chance);
					break;
				case MCEquipmentSlot.BOOTS:
					setBootsDropChance(chance);
					break;
			}
		}
	}

	// For the purposes of faking a normal inventory, we most likely will not be accessing
	// anything below this line, but they are here for flexibility and completion
	
	@Override
	public MCItemStack getWeapon() {
		return new BukkitMCItemStack(ee.getItemInHand());
	}

	@Override
	public MCItemStack getHelmet() {
		return new BukkitMCItemStack(ee.getHelmet());
	}

	@Override
	public MCItemStack getChestplate() {
		return new BukkitMCItemStack(ee.getChestplate());
	}

	@Override
	public MCItemStack getLeggings() {
		return new BukkitMCItemStack(ee.getLeggings());
	}

	@Override
	public MCItemStack getBoots() {
		return new BukkitMCItemStack(ee.getBoots());
	}

	@Override
	public void setWeapon(MCItemStack stack) {
		ee.setItemInHand(((BukkitMCItemStack) stack).asItemStack());
	}

	@Override
	public void setHelmet(MCItemStack stack) {
		ee.setHelmet(((BukkitMCItemStack) stack).asItemStack());
	}

	@Override
	public void setChestplate(MCItemStack stack) {
		ee.setChestplate(((BukkitMCItemStack) stack).asItemStack());
	}

	@Override
	public void setLeggings(MCItemStack stack) {
		ee.setLeggings(((BukkitMCItemStack) stack).asItemStack());
	}

	@Override
	public void setBoots(MCItemStack stack) {
		ee.setBoots(((BukkitMCItemStack) stack).asItemStack());
	}

	@Override
	public float getWeaponDropChance() {
		return ee.getItemInHandDropChance();
	}

	@Override
	public float getHelmetDropChance() {
		return ee.getHelmetDropChance();
	}

	@Override
	public float getChestplateDropChance() {
		return ee.getChestplateDropChance();
	}

	@Override
	public float getLeggingsDropChance() {
		return ee.getLeggingsDropChance();
	}

	@Override
	public float getBootsDropChance() {
		return ee.getBootsDropChance();
	}

	@Override
	public void setWeaponDropChance(float chance) {
		ee.setItemInHandDropChance(chance);
	}

	@Override
	public void setHelmetDropChance(float chance) {
		ee.setHelmetDropChance(chance);
	}

	@Override
	public void setChestplateDropChance(float chance) {
		ee.setChestplateDropChance(chance);
	}

	@Override
	public void setLeggingsDropChance(float chance) {
		ee.setLeggingsDropChance(chance);
	}

	@Override
	public void setBootsDropChance(float chance) {
		ee.setBootsDropChance(chance);
	}

}
