package com.laytonsmith.abstraction;

import com.laytonsmith.abstraction.enums.MCEnchantment;

import java.util.Map;

public interface MCEnchantmentStorageMeta extends MCItemMeta {

	boolean addStoredEnchant(MCEnchantment ench, int level, boolean ignoreRestriction);

	int getStoredEnchantLevel(MCEnchantment ench);

	Map<MCEnchantment, Integer> getStoredEnchants();

	boolean hasStoredEnchant(MCEnchantment ench);

	boolean hasStoredEnchants();

	boolean removeStoredEnchant(MCEnchantment ench);
}
