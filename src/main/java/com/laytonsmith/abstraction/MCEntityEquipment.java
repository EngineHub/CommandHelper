package com.laytonsmith.abstraction;

import com.laytonsmith.abstraction.enums.MCEquipmentSlot;
import java.util.Map;

/**
 *
 * @author jb_aero
 */
public interface MCEntityEquipment {

	public void clearEquipment();
	public int getSize();
	
	public Map<MCEquipmentSlot, MCItemStack> getAllEquipment();
	public void setAllEquipment(Map<MCEquipmentSlot, MCItemStack> stackmap);
	
	public MCItemStack getWeapon();
	public MCItemStack getHelmet();
	public MCItemStack getChestplate();
	public MCItemStack getLeggings();
	public MCItemStack getBoots();
	
	public void setWeapon(MCItemStack stack);
	public void setHelmet(MCItemStack stack);
	public void setChestplate(MCItemStack stack);
	public void setLeggings(MCItemStack stack);
	public void setBoots(MCItemStack stack);
	
	public float getWeaponDropChance();
	public float getHelmetDropChance();
	public float getChestplateDropChance();
	public float getLeggingsDropChance();
	public float getBootsDropChance();
	
	public void setWeaponDropChance(float chance);
	public void setHelmetDropChance(float chance);
	public void setChestplateDropChance(float chance);
	public void setLeggingsDropChance(float chance);
	public void setBootsDropChance(float chance);
	
}
