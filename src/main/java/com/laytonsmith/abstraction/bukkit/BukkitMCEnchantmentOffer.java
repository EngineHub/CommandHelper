package com.laytonsmith.abstraction.bukkit;

import com.laytonsmith.abstraction.MCEnchantmentOffer;
import com.laytonsmith.abstraction.enums.MCEnchantment;
import com.laytonsmith.abstraction.enums.bukkit.BukkitMCEnchantment;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentOffer;

public class BukkitMCEnchantmentOffer implements MCEnchantmentOffer {
	private final EnchantmentOffer handle;

	public BukkitMCEnchantmentOffer(EnchantmentOffer handle) {
		this.handle = handle;
	}

	@Override
	public MCEnchantment getEnchantment() {
		return BukkitMCEnchantment.valueOfConcrete(handle.getEnchantment());
	}

	@Override
	public void setEnchantment(MCEnchantment enchant) {
		handle.setEnchantment((Enchantment) enchant.getConcrete());
	}

	@Override
	public int getEnchantmentLevel() {
		return handle.getEnchantmentLevel();
	}

	@Override
	public void setEnchantmentLevel(int level) {
		handle.setEnchantmentLevel(level);
	}

	@Override
	public int getCost() {
		return handle.getCost();
	}

	@Override
	public void setCost(int cost) {
		handle.setCost(cost);
	}
}
