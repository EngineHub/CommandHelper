package com.laytonsmith.abstraction.bukkit;

import com.laytonsmith.abstraction.MCEntity;
import com.laytonsmith.abstraction.MCEntityEquipment;
import com.laytonsmith.abstraction.MCItemStack;
import com.laytonsmith.abstraction.enums.MCEquipmentSlot;
import com.laytonsmith.abstraction.enums.MCVersion;
import com.laytonsmith.core.Static;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.EnumMap;
import java.util.Map;

public class BukkitMCEntityEquipment implements MCEntityEquipment {

	private final EntityEquipment ee;

	public BukkitMCEntityEquipment(EntityEquipment equipment) {
		this.ee = equipment;
	}

	@Override
	public void clearEquipment() {
		ee.clear();
	}

	@Override
	public MCEntity getHolder() {
		return BukkitConvertor.BukkitGetCorrectEntity(ee.getHolder());
	}

	@Override
	public Map<MCEquipmentSlot, MCItemStack> getAllEquipment() {
		Map<MCEquipmentSlot, MCItemStack> slots = new EnumMap<>(MCEquipmentSlot.class);
		slots.put(MCEquipmentSlot.WEAPON, getWeapon());
		slots.put(MCEquipmentSlot.OFF_HAND, getItemInOffHand());
		slots.put(MCEquipmentSlot.HELMET, getHelmet());
		slots.put(MCEquipmentSlot.CHESTPLATE, getChestplate());
		slots.put(MCEquipmentSlot.LEGGINGS, getLeggings());
		slots.put(MCEquipmentSlot.BOOTS, getBoots());
		BukkitMCServer server = (BukkitMCServer) Static.getServer();
		if(server.getMinecraftVersion().gte(MCVersion.MC1_20_6)) {
			try {
				slots.put(MCEquipmentSlot.BODY, new BukkitMCItemStack(ee.getItem(EquipmentSlot.BODY)));
			} catch(IllegalArgumentException ignored) {
				// API says it can throw an exception here, but it never seems to do so.
			}
			if(server.getMinecraftVersion().gte(MCVersion.MC1_21_5)) {
				try {
					slots.put(MCEquipmentSlot.SADDLE, new BukkitMCItemStack(ee.getItem(EquipmentSlot.SADDLE)));
				} catch(IllegalArgumentException ignored) {}
			}
		}
		return slots;
	}

	@Override
	public void setAllEquipment(Map<MCEquipmentSlot, MCItemStack> slots) {
		MCItemStack stack;
		for(Map.Entry<MCEquipmentSlot, MCItemStack> entry : slots.entrySet()) {
			stack = entry.getValue();
			switch(entry.getKey()) {
				case WEAPON:
					setWeapon(stack);
					break;
				case OFF_HAND:
					setItemInOffHand(stack);
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
				case BODY:
					if(Static.getServer().getMinecraftVersion().gte(MCVersion.MC1_20_6)) {
						try {
							ee.setItem(EquipmentSlot.BODY, (ItemStack) stack.getHandle());
						} catch(IllegalArgumentException ignored) {
							// API says it can throw an exception here, but it never seems to do so.
						}
					}
					break;
				case SADDLE:
					if(Static.getServer().getMinecraftVersion().gte(MCVersion.MC1_21_5)) {
						try {
							ee.setItem(EquipmentSlot.SADDLE, (ItemStack) stack.getHandle());
						} catch(IllegalArgumentException ignored) {}
					}
					break;
			}
		}
	}

	@Override
	public Map<MCEquipmentSlot, Float> getAllDropChances() {
		Map<MCEquipmentSlot, Float> slots = new EnumMap<>(MCEquipmentSlot.class);
		slots.put(MCEquipmentSlot.WEAPON, getWeaponDropChance());
		slots.put(MCEquipmentSlot.OFF_HAND, getOffHandDropChance());
		slots.put(MCEquipmentSlot.HELMET, getHelmetDropChance());
		slots.put(MCEquipmentSlot.CHESTPLATE, getChestplateDropChance());
		slots.put(MCEquipmentSlot.LEGGINGS, getLeggingsDropChance());
		slots.put(MCEquipmentSlot.BOOTS, getBootsDropChance());
		BukkitMCServer server = (BukkitMCServer) Static.getServer();
		if(server.getMinecraftVersion().gte(MCVersion.MC1_20_6)) {
			slots.put(MCEquipmentSlot.BODY, ee.getDropChance(EquipmentSlot.BODY));
			if(server.getMinecraftVersion().gte(MCVersion.MC1_21_5)) {
				slots.put(MCEquipmentSlot.SADDLE, ee.getDropChance(EquipmentSlot.SADDLE));
			}
		}
		return slots;
	}

	@Override
	public void setAllDropChances(Map<MCEquipmentSlot, Float> slots) {
		float chance;
		for(Map.Entry<MCEquipmentSlot, Float> entry : slots.entrySet()) {
			chance = entry.getValue();
			switch(entry.getKey()) {
				case WEAPON:
					setWeaponDropChance(chance);
					break;
				case OFF_HAND:
					setOffHandDropChance(chance);
					break;
				case HELMET:
					setHelmetDropChance(chance);
					break;
				case CHESTPLATE:
					setChestplateDropChance(chance);
					break;
				case LEGGINGS:
					setLeggingsDropChance(chance);
					break;
				case BOOTS:
					setBootsDropChance(chance);
					break;
				case BODY:
					if(Static.getServer().getMinecraftVersion().gte(MCVersion.MC1_20_6)) {
						ee.setDropChance(EquipmentSlot.BODY, chance);
					}
					break;
				case SADDLE:
					if(Static.getServer().getMinecraftVersion().gte(MCVersion.MC1_21_5)) {
						ee.setDropChance(EquipmentSlot.SADDLE, chance);
					}
					break;
			}
		}
	}

	@Override
	public MCItemStack getWeapon() {
		return new BukkitMCItemStack(ee.getItemInMainHand());
	}

	@Override
	public MCItemStack getItemInOffHand() {
		return new BukkitMCItemStack(ee.getItemInOffHand());
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
		ee.setItemInMainHand(((BukkitMCItemStack) stack).asItemStack());
	}

	@Override
	public void setItemInOffHand(MCItemStack stack) {
		ee.setItemInOffHand(((BukkitMCItemStack) stack).asItemStack());
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
		return ee.getItemInMainHandDropChance();
	}

	@Override
	public float getOffHandDropChance() {
		return ee.getItemInOffHandDropChance();
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
		ee.setItemInMainHandDropChance(chance);
	}

	@Override
	public void setOffHandDropChance(float chance) {
		ee.setItemInOffHandDropChance(chance);
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
