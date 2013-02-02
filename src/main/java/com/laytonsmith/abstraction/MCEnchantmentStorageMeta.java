package com.laytonsmith.abstraction;

import java.util.Map;

/**
 *
 * @author jb_aero
 */
public interface MCEnchantmentStorageMeta extends MCItemMeta {

	public boolean addStoredEnchant(MCEnchantment ench, int level, boolean ignoreRestriction);
	public int getStoredEnchantLevel(MCEnchantment ench);
	public Map<MCEnchantment,Integer> getStoredEnchants();
	public boolean hasStoredEnchant(MCEnchantment ench);
	public boolean hasStoredEnchants();
	public boolean removeStoredEnchant(MCEnchantment ench);
	
}
