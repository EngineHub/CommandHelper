package com.laytonsmith.abstraction;

/**
 *
 * @author jb_aero
 */
public interface MCEntityEquipment {

	public void clearEquipment();
	public int getSize();
	
	public MCItemStack[] getAllArmor();
	public void setAllArmor(MCItemStack[] stackarray);
	
	public MCItemStack getWeapon();
	public MCItemStack getHelmet();
	public MCItemStack getChestPlate();
	public MCItemStack getLeggings();
	public MCItemStack getBoots();
	
	public void setWeapon(MCItemStack stack);
	public void setHelmet(MCItemStack stack);
	public void setChestplate(MCItemStack stack);
	public void setLeggings(MCItemStack stack);
	public void setBoots(MCItemStack stack);
	
	public float getWeaponDropChance();
	public float getHelmetDropChance();
	public float getChestPlateDropChance();
	public float getLeggingsDropChance();
	public float getBootsDropChance();
	
	public void setWeaponDropChance(float chance);
	public void setHelmetDropChance(float chance);
	public void setChestPlateDropChance(float chance);
	public void setLeggingsDropChance(float chance);
	public void setBootsDropChance(float chance);
	
}
