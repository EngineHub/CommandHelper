/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.abstraction.bukkit;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import com.laytonsmith.abstraction.AbstractionObject;
import com.laytonsmith.abstraction.MCAttributeModifier;
import com.laytonsmith.abstraction.MCEnchantment;
import com.laytonsmith.abstraction.MCItemMeta;
import com.laytonsmith.abstraction.enums.MCItemFlag;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.laytonsmith.abstraction.enums.bukkit.BukkitMCAttribute;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.Repairable;

/**
 *
 *
 */
public class BukkitMCItemMeta implements MCItemMeta {

	ItemMeta im;

	public BukkitMCItemMeta(ItemMeta im) {
		this.im = im;
	}

	public BukkitMCItemMeta(AbstractionObject o) {
		im = (ItemMeta) o;
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
		Map<MCEnchantment, Integer> map = new HashMap<>();
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
		return obj instanceof MCItemMeta && im.equals(((MCItemMeta) obj).getHandle());
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

	@Override
	public void addItemFlags(MCItemFlag... flags) {
		for(MCItemFlag flag : flags) {
			im.addItemFlags(ItemFlag.valueOf(flag.name()));
		}
	}

	@Override
	public Set<MCItemFlag> getItemFlags() {
		Set<ItemFlag> flags = im.getItemFlags();
		Set<MCItemFlag> ret = new HashSet<>(flags.size());
		for(ItemFlag flag : flags) {
			ret.add(MCItemFlag.valueOf(flag.name()));
		}
		return ret;
	}

	@Override
	public boolean isUnbreakable() {
		return im.isUnbreakable();
	}

	@Override
	public void setUnbreakable(boolean unbreakable) {
		im.setUnbreakable(unbreakable);
	}

	@Override
	public int getDamage() {
		return ((Damageable) im).getDamage();
	}

	@Override
	public void setDamage(int damage) {
		((Damageable) im).setDamage(damage);
	}

	@Override
	public boolean hasCustomModelData() {
		return im.hasCustomModelData();
	}

	@Override
	public int getCustomModelData() {
		return im.getCustomModelData();
	}

	@Override
	public void setCustomModelData(int id) {
		try {
			im.setCustomModelData(id);
		} catch (NoSuchMethodError ex) {
			// probably 1.13
		}
	}

	@Override
	public List<MCAttributeModifier> getAttributeModifiers() {
		Multimap<Attribute, AttributeModifier> modifiers = im.getAttributeModifiers();
		if(modifiers == null) {
			return null;
		}
		List<MCAttributeModifier> ret = new ArrayList<>();
		for(Entry<Attribute, AttributeModifier> modifier : modifiers.entries()) {
			ret.add(new BukkitMCAttributeModifier(modifier.getKey(), modifier.getValue()));
		}
		return ret;
	}

	@Override
	public void setAttributeModifiers(List<MCAttributeModifier> modifiers) {
		Multimap<Attribute, AttributeModifier> map = LinkedHashMultimap.create();
		for(MCAttributeModifier m : modifiers) {
			map.put(BukkitMCAttribute.getConvertor().getConcreteEnum(m.getAttribute()),
					(AttributeModifier) m.getHandle());
		}
		im.setAttributeModifiers(map);
	}
}
