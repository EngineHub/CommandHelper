/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.abstraction.bukkit;

import com.laytonsmith.abstraction.AbstractionObject;
import com.laytonsmith.abstraction.MCEnchantment;
import com.laytonsmith.abstraction.MCItemMeta;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.Repairable;

/**
 *
 * @author Layton
 */
public class BukkitMCItemMeta implements MCItemMeta {

	ItemMeta im;
	public BukkitMCItemMeta(ItemMeta im) {
		this.im = im;
	}

	public BukkitMCItemMeta(AbstractionObject o) {
		im = (ItemMeta)o;
	}
	
	@Override
	public boolean hasDisplayName() {
		return im.hasDisplayName();
	}

	@Override
	public String getDisplayName() {
		return im.getDisplayName();
	}

	@Override
	public void setDisplayName(String name) {
		im.setDisplayName(name);
	}

	@Override
	public boolean hasLore() {
		return im.hasLore();
	}

	@Override
	public List<String> getLore() {
		return im.getLore();
	}

	@Override
	public void setLore(List<String> lore) {
		im.setLore(lore);
	}
	
	@Override
	public boolean hasEnchants() {
		return im.hasEnchants();
	}

	@Override
	public Map<MCEnchantment, Integer> getEnchants() {
		Map<MCEnchantment, Integer> map = new HashMap<MCEnchantment, Integer>();
		for(Entry<Enchantment, Integer> entry : im.getEnchants().entrySet()) {
			map.put(new BukkitMCEnchantment(entry.getKey()), entry.getValue());
		}
		return map;
	}
	
	@Override
	public boolean addEnchant(MCEnchantment ench, int level, boolean ignoreLevelRestriction) {
		return im.addEnchant(((BukkitMCEnchantment) ench).__Enchantment(), level, ignoreLevelRestriction);
	}
	
	@Override
	public boolean removeEnchant(MCEnchantment ench) {
		return im.removeEnchant(((BukkitMCEnchantment) ench).__Enchantment());
	}

	@Override
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
	
	@Override
	public boolean hasRepairCost() {
		return ((Repairable) im).hasRepairCost();
	}
	
	@Override
	public int getRepairCost() {
		return ((Repairable) im).getRepairCost();
	}
	
	@Override
	public void setRepairCost(int cost) {
		((Repairable) im).setRepairCost(cost);
	}
}
