package com.laytonsmith.abstraction.bukkit;

import com.laytonsmith.abstraction.MCEnchantmentStorageMeta;
import java.util.HashMap;
import java.util.Map;

import com.laytonsmith.abstraction.enums.MCEnchantment;
import com.laytonsmith.abstraction.enums.bukkit.BukkitMCEnchantment;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;

public class BukkitMCEnchantmentStorageMeta extends BukkitMCItemMeta implements MCEnchantmentStorageMeta {

	EnchantmentStorageMeta es;

	public BukkitMCEnchantmentStorageMeta(EnchantmentStorageMeta im) {
		super(im);
		this.es = im;
	}

	@Override
	public boolean addStoredEnchant(MCEnchantment ench, int level, boolean ignoreRestriction) {
		return es.addStoredEnchant((Enchantment) ench.getConcrete(), level, ignoreRestriction);
	}

	@Override
	public int getStoredEnchantLevel(MCEnchantment ench) {
		return es.getStoredEnchantLevel((Enchantment) ench.getConcrete());
	}

	@Override
	public Map<MCEnchantment, Integer> getStoredEnchants() {
		Map<MCEnchantment, Integer> ret = new HashMap<>();
		for(Map.Entry<Enchantment, Integer> entry : es.getStoredEnchants().entrySet()) {
			ret.put(BukkitMCEnchantment.valueOfConcrete(entry.getKey()), entry.getValue());
		}
		return ret;
	}

	@Override
	public boolean hasStoredEnchant(MCEnchantment ench) {
		return es.hasStoredEnchant((Enchantment) ench.getConcrete());
	}

	@Override
	public boolean hasStoredEnchants() {
		return es.hasStoredEnchants();
	}

	@Override
	public boolean removeStoredEnchant(MCEnchantment ench) {
		return es.removeStoredEnchant((Enchantment) ench.getConcrete());
	}

}
