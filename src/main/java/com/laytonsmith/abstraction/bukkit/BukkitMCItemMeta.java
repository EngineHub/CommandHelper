package com.laytonsmith.abstraction.bukkit;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import com.laytonsmith.PureUtilities.Common.ReflectionUtils;
import com.laytonsmith.PureUtilities.Common.ReflectionUtils.ReflectionException;
import com.laytonsmith.abstraction.AbstractionObject;
import com.laytonsmith.abstraction.MCAttributeModifier;
import com.laytonsmith.abstraction.MCCooldownComponent;
import com.laytonsmith.abstraction.MCFoodComponent;
import com.laytonsmith.abstraction.MCItemMeta;
import com.laytonsmith.abstraction.MCItemStack;
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

import com.laytonsmith.abstraction.enums.MCItemRarity;
import com.laytonsmith.abstraction.enums.MCVersion;
import com.laytonsmith.abstraction.enums.bukkit.BukkitMCEnchantment;
import com.laytonsmith.abstraction.enums.bukkit.BukkitMCItemFlag;
import com.laytonsmith.core.MSLog;
import com.laytonsmith.core.MSLog.Tags;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.constructs.Target;
import org.bukkit.Bukkit;
import org.bukkit.JukeboxSong;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.block.data.BlockData;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemRarity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockDataMeta;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.Repairable;
import org.bukkit.inventory.meta.components.FoodComponent;
import org.bukkit.inventory.meta.components.JukeboxPlayableComponent;
import org.bukkit.inventory.meta.components.UseCooldownComponent;

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
			try {
				ret.add(MCItemFlag.valueOf(flag.name()));
			} catch(IllegalArgumentException ignore) {}
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
	public boolean hasDamage() {
		if(((BukkitMCServer) Static.getServer()).isPaper()
				&& Static.getServer().getMinecraftVersion().gte(MCVersion.MC1_21)) {
			return ((Damageable) im).hasDamageValue();
		}
		return ((Damageable) im).hasDamage();
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
	public void setMaxDamage(Integer damage) {
		((Damageable) im).setMaxDamage(damage);
	}

	@Override
	public MCBlockData getBlockData(MCMaterial material) {
		return new BukkitMCBlockData(((BlockDataMeta) this.im).getBlockData((Material) material.getHandle()));
	}

	@Override
	public Map<String, String> getExistingBlockData() {
		try {
			Class clz = Class.forName(Bukkit.getServer().getClass().getPackage().getName() + ".inventory.CraftMetaItem");
			return (Map<String, String>) ReflectionUtils.get(clz, this.im, "blockData");
		} catch (ClassNotFoundException e) {
			MSLog.GetLogger().e(Tags.GENERAL, "Failed to get CraftMetaItem class.", Target.UNKNOWN);
			return null;
		} catch (ReflectionException ex) {
			MSLog.GetLogger().e(Tags.GENERAL, "Failed to get blockData from CraftMetaItem.", Target.UNKNOWN);
			return null;
		}
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
	public boolean hasMaxStackSize() {
		return im.hasMaxStackSize();
	}

	@Override
	public int getMaxStackSize() {
		return im.getMaxStackSize();
	}

	@Override
	public void setMaxStackSize(Integer size) {
		im.setMaxStackSize(size);
	}

	@Override
	public boolean hasRarity() {
		return im.hasRarity();
	}

	@Override
	public MCItemRarity getRarity() {
		return MCItemRarity.valueOf(im.getRarity().name());
	}

	@Override
	public void setRarity(MCItemRarity rarity) {
		im.setRarity(ItemRarity.valueOf(rarity.name()));
	}

	@Override
	public boolean hasEnchantable() {
		return im.hasEnchantable();
	}

	@Override
	public int getEnchantable() {
		return im.getEnchantable();
	}

	@Override
	public void setEnchantable(Integer enchantability) {
		im.setEnchantable(enchantability);
	}

	@Override
	public boolean hasJukeboxPlayable() {
		return im.hasJukeboxPlayable();
	}

	@Override
	public String getJukeboxPlayable() {
		return im.getJukeboxPlayable().getSongKey().toString();
	}

	@Override
	public void setJukeboxPlayable(String playable) {
		if(playable == null) {
			im.setJukeboxPlayable(null);
		} else {
			JukeboxSong song = Registry.JUKEBOX_SONG.get(NamespacedKey.fromString(playable));
			if(song != null) {
				JukeboxPlayableComponent component = im.getJukeboxPlayable();
				component.setSong(song);
				im.setJukeboxPlayable(component);
			}
		}
	}

	@Override
	public boolean isGlider() {
		return im.isGlider();
	}

	@Override
	public void setGlider(boolean glider) {
		im.setGlider(glider);
	}

	@Override
	public boolean hasUseRemainder() {
		return im.hasUseRemainder();
	}

	@Override
	public MCItemStack getUseRemainder() {
		return new BukkitMCItemStack(im.getUseRemainder());
	}

	@Override
	public void setUseRemainder(MCItemStack remainder) {
		im.setUseRemainder((ItemStack) remainder.getHandle());
	}

	@Override
	public boolean hasFood() {
		return im.hasFood();
	}

	@Override
	public MCFoodComponent getFood() {
		return new BukkitMCFoodComponent(im.getFood());
	}

	@Override
	public void setFood(MCFoodComponent component) {
		im.setFood((FoodComponent) component.getHandle());
	}

	@Override
	public boolean hasItemModel() {
		return im.hasItemModel();
	}

	@Override
	public String getItemModel() {
		return im.getItemModel().toString();
	}

	@Override
	public void setItemModel(String key) {
		im.setItemModel(NamespacedKey.fromString(key));
	}

	@Override
	public boolean hasTooltipStyle() {
		return im.hasTooltipStyle();
	}

	@Override
	public String getTooltipStyle() {
		return im.getTooltipStyle().toString();
	}

	@Override
	public void setTooltipStyle(String key) {
		im.setTooltipStyle(NamespacedKey.fromString(key));
	}

	@Override
	public boolean hasUseCooldown() {
		return im.hasUseCooldown();
	}

	@Override
	public MCCooldownComponent getUseCooldown() {
		return new BukkitMCCooldownComponent(im.getUseCooldown());
	}

	@Override
	public void setUseCooldown(MCCooldownComponent component) {
		im.setUseCooldown((UseCooldownComponent) component.getHandle());
	}

}
