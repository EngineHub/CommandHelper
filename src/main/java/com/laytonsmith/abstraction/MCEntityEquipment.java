package com.laytonsmith.abstraction;

import com.laytonsmith.abstraction.enums.MCEquipmentSlot;
import java.util.Map;

public interface MCEntityEquipment {

	void clearEquipment();

	int getSize();

	MCEntity getHolder();

	Map<MCEquipmentSlot, MCItemStack> getAllEquipment();

	void setAllEquipment(Map<MCEquipmentSlot, MCItemStack> stackmap);

	Map<MCEquipmentSlot, Float> getAllDropChances();

	void setAllDropChances(Map<MCEquipmentSlot, Float> slots);

	MCItemStack getWeapon();

	MCItemStack getItemInOffHand();

	MCItemStack getHelmet();

	MCItemStack getChestplate();

	MCItemStack getLeggings();

	MCItemStack getBoots();

	void setWeapon(MCItemStack stack);

	void setItemInOffHand(MCItemStack stack);

	void setHelmet(MCItemStack stack);

	void setChestplate(MCItemStack stack);

	void setLeggings(MCItemStack stack);

	void setBoots(MCItemStack stack);

	float getWeaponDropChance();

	float getOffHandDropChance();

	float getHelmetDropChance();

	float getChestplateDropChance();

	float getLeggingsDropChance();

	float getBootsDropChance();

	void setWeaponDropChance(float chance);

	void setOffHandDropChance(float chance);

	void setHelmetDropChance(float chance);

	void setChestplateDropChance(float chance);

	void setLeggingsDropChance(float chance);

	void setBootsDropChance(float chance);
}
