/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.abstraction.bukkit;

import com.laytonsmith.abstraction.MCColor;
import com.laytonsmith.abstraction.MCLeatherArmorMeta;
import com.laytonsmith.annotations.WrappedItem;
import org.bukkit.inventory.meta.LeatherArmorMeta;

/**
 *
 * @author Layton
 */
public class BukkitMCLeatherArmorMeta extends BukkitMCItemMeta implements MCLeatherArmorMeta {
	@WrappedItem LeatherArmorMeta lam;

	public MCColor getColor() {
		return BukkitMCColor.GetMCColor(lam.getColor());
	}

	public void setColor(MCColor color) {
		lam.setColor(BukkitMCColor.GetColor(color));
	}
	
}
