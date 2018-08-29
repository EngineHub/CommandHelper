package com.laytonsmith.abstraction.bukkit;

import com.laytonsmith.abstraction.AbstractionObject;
import com.laytonsmith.abstraction.MCEnchantment;
import com.laytonsmith.abstraction.MCItemStack;
import org.bukkit.enchantments.Enchantment;

public class BukkitMCEnchantment implements MCEnchantment {

	Enchantment e;

	public BukkitMCEnchantment(Enchantment e) {
		if(e == null) {
			throw new NullPointerException();
		}
		this.e = e;
	}

	public BukkitMCEnchantment(AbstractionObject a) {
		if(a instanceof MCEnchantment) {
			this.e = ((Enchantment) a.getHandle());
		} else {
			throw new ClassCastException();
		}
	}

	@Override
	public Object getHandle() {
		return e;
	}

	Enchantment __Enchantment() {
		return e;
	}

	public Enchantment asEnchantment() {
		return e;
	}

	@Override
	public boolean canEnchantItem(MCItemStack is) {
		return e.canEnchantItem(((BukkitMCItemStack) is).is);
	}

	@Override
	public int getMaxLevel() {
		return e.getMaxLevel();
	}

	@Override
	public String getName() {
		return e.getName();
	}

	@Override
	public String getKey() {
		return e.getKey().getKey();
	}

	@Override
	public String toString() {
		return e.toString();
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof MCEnchantment && e.equals(((MCEnchantment) obj).getHandle());
	}

	@Override
	public int hashCode() {
		return e.hashCode();
	}
}
