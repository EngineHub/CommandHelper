package com.laytonsmith.abstraction.bukkit;

import com.laytonsmith.abstraction.MCArmorMeta;
import com.laytonsmith.abstraction.enums.MCTrimMaterial;
import com.laytonsmith.abstraction.enums.MCTrimPattern;
import com.laytonsmith.abstraction.enums.bukkit.BukkitMCTrimMaterial;
import com.laytonsmith.abstraction.enums.bukkit.BukkitMCTrimPattern;
import org.bukkit.inventory.meta.ArmorMeta;
import org.bukkit.inventory.meta.trim.ArmorTrim;
import org.bukkit.inventory.meta.trim.TrimMaterial;
import org.bukkit.inventory.meta.trim.TrimPattern;

public class BukkitMCArmorMeta extends BukkitMCItemMeta implements MCArmorMeta {

	ArmorMeta am;

	public BukkitMCArmorMeta(ArmorMeta im) {
		super(im);
		this.am = im;
	}

	@Override
	public boolean hasTrim() {
		return am.hasTrim();
	}

	@Override
	public void setTrim(MCTrimPattern pattern, MCTrimMaterial material) {
		am.setTrim(new ArmorTrim((TrimMaterial) material.getConcrete(), (TrimPattern) pattern.getConcrete()));
	}

	@Override
	public MCTrimPattern getTrimPattern() {
		return BukkitMCTrimPattern.valueOfConcrete(am.getTrim().getPattern());
	}

	@Override
	public MCTrimMaterial getTrimMaterial() {
		return BukkitMCTrimMaterial.valueOfConcrete(am.getTrim().getMaterial());
	}
}
