package com.laytonsmith.abstraction.bukkit;

import com.laytonsmith.abstraction.MCEnchantment;
import com.laytonsmith.abstraction.MCEnchantmentStorageMeta;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;

/**
 *
 * @author jb_aero
 */
public class BukkitMCEnchantmentStorageMeta extends BukkitMCItemMeta implements
		MCEnchantmentStorageMeta {

	EnchantmentStorageMeta es;
	public BukkitMCEnchantmentStorageMeta(EnchantmentStorageMeta im) {
		super(im);
		this.es = im;
	}

	public boolean addStoredEnchant(MCEnchantment ench, int level,
			boolean ignoreRestriction) {
		return es.addStoredEnchant(((BukkitMCEnchantment) ench).__Enchantment(), level, ignoreRestriction);
	}

	public int getStoredEnchantLevel(MCEnchantment ench) {
		return es.getStoredEnchantLevel(((BukkitMCEnchantment) ench).__Enchantment());
	}

	public Map<MCEnchantment, Integer> getStoredEnchants() {
		Map<MCEnchantment,Integer> ret = new HashMap<MCEnchantment,Integer>();
		for (Map.Entry<Enchantment,Integer> entry : es.getStoredEnchants().entrySet()) {
			ret.put(new BukkitMCEnchantment(entry.getKey()), entry.getValue());
		}
		return ret;
	}

	public boolean hasStoredEnchant(MCEnchantment ench) {
		return es.hasStoredEnchant(((BukkitMCEnchantment) ench).__Enchantment());
	}

	public boolean hasStoredEnchants() {
		return es.hasStoredEnchants();
	}

	public boolean removeStoredEnchant(MCEnchantment ench) {
		return es.removeStoredEnchant(((BukkitMCEnchantment) ench).__Enchantment());
	}

}
