package com.laytonsmith.core.natives;

import com.laytonsmith.annotations.typename;
import com.laytonsmith.core.natives.interfaces.MObject;

/**
 *
 */
@typename("Equipment")
public class MEquipment extends MObject {
	
	public MItemStack weapon;
	
	public MItemStack boots;
	
	public MItemStack leggings;
	
	public MItemStack chestplate;
	
	public MItemStack helmet;
}
