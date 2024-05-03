package com.laytonsmith.abstraction.bukkit;

import com.laytonsmith.abstraction.AbstractionObject;
import com.laytonsmith.abstraction.MCItemMeta;
import com.laytonsmith.abstraction.MCItemStack;
import com.laytonsmith.abstraction.blocks.MCMaterial;
import com.laytonsmith.abstraction.bukkit.blocks.BukkitMCMaterial;
import com.laytonsmith.abstraction.enums.MCEnchantment;
import com.laytonsmith.abstraction.enums.bukkit.BukkitMCEnchantment;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.Map;

public class BukkitMCItemStack implements MCItemStack {

	ItemStack is;

	public BukkitMCItemStack(ItemStack is) {
		this.is = is;
	}

	public BukkitMCItemStack(AbstractionObject a) {
		this((ItemStack) null);
		if(a instanceof MCItemStack) {
			this.is = ((ItemStack) a.getHandle());
		} else {
			throw new ClassCastException();
		}
	}

	@Override
	public Object getHandle() {
		return is;
	}

	@Override
	public void addEnchantment(MCEnchantment e, int level) {
		if(is == null) {
			return;
		}
		is.addEnchantment((Enchantment) e.getConcrete(), level);
	}

	@Override
	public void addUnsafeEnchantment(MCEnchantment e, int level) {
		if(is == null) {
			return;
		}
		is.addUnsafeEnchantment((Enchantment) e.getConcrete(), level);
	}

	@Override
	public Map<MCEnchantment, Integer> getEnchantments() {
		Map<MCEnchantment, Integer> map = new HashMap<>();
		try {
			for(Map.Entry<Enchantment, Integer> entry : is.getEnchantments().entrySet()) {
				map.put(BukkitMCEnchantment.valueOfConcrete(entry.getKey()), entry.getValue());
			}
		} catch (NullPointerException npe) {
			// Probably invalid enchantment, always return map
		}
		return map;
	}

	@Override
	public void removeEnchantment(MCEnchantment e) {
		if(is == null) {
			return;
		}
		is.removeEnchantment((Enchantment) e.getConcrete());
	}

	@Override
	public MCMaterial getType() {
		if(is == null) {
			return BukkitMCMaterial.valueOfConcrete(Material.AIR);
		}
		return BukkitMCMaterial.valueOfConcrete(is.getType());
	}

	@Override
	public void setType(MCMaterial type) {
		if(is == null) {
			return;
		}
		is.setType((Material) type.getHandle());
	}

	@Override
	public int getAmount() {
		if(is == null) {
			return 0;
		}
		return is.getAmount();
	}

	@Override
	public void setAmount(int amt) {
		if(is == null) {
			return;
		}
		is.setAmount(amt);
	}

	public ItemStack __ItemStack() {
		return is;
	}

	@Override
	public int maxStackSize() {
		if(is == null) {
			return 0;
		}
		return is.getMaxStackSize();
	}

	public ItemStack asItemStack() {
		return is;
	}

	@Override
	public String toString() {
		return is.toString();
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof BukkitMCItemStack && is.equals(((BukkitMCItemStack) obj).asItemStack());
	}

	@Override
	public int hashCode() {
		return is.hashCode();
	}

	@Override
	public boolean hasItemMeta() {
		return is != null && is.hasItemMeta();
	}

	@Override
	public MCItemMeta getItemMeta() {
		ItemMeta im = is.getItemMeta();
		if(im instanceof BlockStateMeta) {
			return new BukkitMCBlockStateMeta((BlockStateMeta) im, is.getType());
		}
		return BukkitConvertor.BukkitGetCorrectMeta(im);
	}

	@Override
	public void setItemMeta(MCItemMeta im) {
		if(is == null) {
			return;
		}
		if(im == null) {
			is.setItemMeta(null);
			return;
		}
		is.setItemMeta(((BukkitMCItemMeta) im).asItemMeta());
	}

	@Override
	public boolean isEmpty() {
		return is == null || is.getAmount() == 0 || is.getType() == Material.AIR;
	}
}
