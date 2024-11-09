package com.laytonsmith.abstraction.bukkit;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import com.laytonsmith.abstraction.AbstractionObject;
import com.laytonsmith.abstraction.MCAttributeModifier;
import com.laytonsmith.abstraction.MCItemMeta;
import com.laytonsmith.abstraction.MCTagContainer;
import com.laytonsmith.abstraction.blocks.MCBlockData;
import com.laytonsmith.abstraction.blocks.MCMaterial;
import com.laytonsmith.abstraction.bukkit.blocks.BukkitMCBlockData;
import com.laytonsmith.abstraction.enums.MCEnchantment;
import com.laytonsmith.abstraction.enums.MCItemFlag;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.laytonsmith.abstraction.enums.bukkit.BukkitMCEnchantment;
import com.laytonsmith.abstraction.enums.bukkit.BukkitMCItemFlag;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.block.data.BlockData;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.meta.BlockDataMeta;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.Repairable;

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
			map.put(BukkitMCEnchantment.valueOfConcrete(entry.getKey()), entry.getValue());
		}
		return map;
	}

	@Override
	public boolean addEnchant(MCEnchantment ench, int level, boolean ignoreLevelRestriction) {
		return im.addEnchant((Enchantment) ench.getConcrete(), level, ignoreLevelRestriction);
	}

	@Override
	public boolean removeEnchant(MCEnchantment ench) {
		return im.removeEnchant((Enchantment) ench.getConcrete());
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
			im.addItemFlags(BukkitMCItemFlag.getConvertor().getConcreteEnum(flag));
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
	public boolean hasMaxDamage() {
		return ((Damageable) im).hasMaxDamage();
	}

	@Override
	public int getMaxDamage() {
		return ((Damageable) im).getMaxDamage();
	}

	@Override
	public void setMaxDamage(int damage) {
		((Damageable) im).setMaxDamage(damage);
	}

	@Override
	public MCBlockData getBlockData(MCMaterial material) {
		return new BukkitMCBlockData(((BlockDataMeta) this.im).getBlockData((Material) material.getHandle()));
	}

	@Override
	public boolean hasBlockData() {
		return ((BlockDataMeta) this.im).hasBlockData();
	}

	@Override
	public void setBlockData(MCBlockData blockData) {
		((BlockDataMeta) this.im).setBlockData((BlockData) blockData.getHandle());
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
		im.setCustomModelData(id);
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
			map.put((Attribute) m.getAttribute().getConcrete(), (AttributeModifier) m.getHandle());
		}
		im.setAttributeModifiers(map);
	}

	@Override
	public boolean hasCustomTags() {
		return !im.getPersistentDataContainer().isEmpty();
	}

	public MCTagContainer getCustomTags() {
		return new BukkitMCTagContainer(im.getPersistentDataContainer());
	}

	@Override
	public boolean hasItemName() {
		return im.hasItemName();
	}

	@Override
	public String getItemName() {
		return im.getItemName();
	}

	@Override
	public void setItemName(String name) {
		im.setItemName(name);
	}

	@Override
	public boolean isHideTooltip() {
		return im.isHideTooltip();
	}

	@Override
	public void setHideTooltip(boolean hide) {
		im.setHideTooltip(hide);
	}

	@Override
	public boolean hasEnchantmentGlintOverride() {
		return im.hasEnchantmentGlintOverride();
	}

	@Override
	public boolean getEnchantmentGlintOverride() {
		return im.getEnchantmentGlintOverride();
	}

	@Override
	public void setEnchantmentGlintOverride(boolean glint) {
		im.setEnchantmentGlintOverride(glint);
	}

	@Override
	public boolean isFireResistant() {
		return im.isFireResistant();
	}

	@Override
	public void setFireResistant(boolean fireResistant) {
		im.setFireResistant(fireResistant);
	}

	@Override
	public boolean hasMaxStackSize() {
		return im.hasMaxStackSize();
	}

	@Override
	public int getMaxStackSize() {
		return im.getMaxStackSize();
	}

	@Override
	public void setMaxStackSize(int size) {
		im.setMaxStackSize(size);
	}
}
