/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.abstraction.bukkit;

import com.laytonsmith.abstraction.AbstractionObject;
import com.laytonsmith.abstraction.AbstractionUtils;
import com.laytonsmith.abstraction.MCEnchantment;
import com.laytonsmith.abstraction.MCItemMeta;
import com.laytonsmith.annotations.WrappedItem;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.meta.ItemMeta;

/**
 *
 * @author Layton
 */
public class BukkitMCItemMeta implements MCItemMeta {

	@WrappedItem ItemMeta im;
	
	public boolean hasDisplayName() {
		return im.hasDisplayName();
	}

	public String getDisplayName() {
		return im.getDisplayName();
	}

	public void setDisplayName(String name) {
		im.setDisplayName(name);
	}

	public boolean hasLore() {
		return im.hasLore();
	}

	public List<String> getLore() {
		return im.getLore();
	}

	public void setLore(List<String> lore) {
		im.setLore(lore);
	}
	
	public boolean hasEnchants() {
		return im.hasEnchants();
	}

	public Map<MCEnchantment, Integer> getEnchants() {
		Map<MCEnchantment, Integer> map = new HashMap<MCEnchantment, Integer>();
		for(Entry<Enchantment, Integer> entry : im.getEnchants().entrySet()) {
			map.put((MCEnchantment) AbstractionUtils.wrap(entry.getKey()), entry.getValue());
		}
		return map;
	}
	
	public boolean addEnchant(MCEnchantment ench, int level, boolean ignoreLevelRestriction) {
		return im.addEnchant(((BukkitMCEnchantment) ench).__Enchantment(), level, ignoreLevelRestriction);
	}
	
	public boolean removeEnchant(MCEnchantment ench) {
		return im.removeEnchant(((BukkitMCEnchantment) ench).__Enchantment());
	}

	public Object getHandle() {
		return im;
	}
	
	public ItemMeta asItemMeta() {
		return im;
	}

	@Override
	public boolean equals(Object obj) {
		return im.equals(obj);
	}

	@Override
	public int hashCode() {
		return im.hashCode();
	}

	@Override
	public String toString() {
		return im.toString();
	}
	
	
}
