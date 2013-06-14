package com.laytonsmith.core.natives;

import com.laytonsmith.annotations.typename;
import com.laytonsmith.core.natives.interfaces.MObject;

/**
 *
 */
@typename("Enchantment")
public class MEnchantment extends MObject {
	public String etype;
	public int elevel;
}
