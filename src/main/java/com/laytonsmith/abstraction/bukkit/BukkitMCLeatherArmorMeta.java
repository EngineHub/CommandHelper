/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.abstraction.bukkit;

import com.laytonsmith.abstraction.AbstractionObject;
import com.laytonsmith.abstraction.MCColor;
import com.laytonsmith.abstraction.MCLeatherArmorMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;

/**
 *
 * @author Layton
 */
public class BukkitMCLeatherArmorMeta extends BukkitMCItemMeta implements MCLeatherArmorMeta {
	LeatherArmorMeta lam;
	public BukkitMCLeatherArmorMeta(LeatherArmorMeta im) {
		super(im);
		lam = im;
	}

	public BukkitMCLeatherArmorMeta(AbstractionObject o) {
		super(o);
		lam = (LeatherArmorMeta)o;
	}

	public MCColor getColor() {
		return BukkitMCColor.GetMCColor(lam.getColor());
	}

	public void setColor(MCColor color) {
		lam.setColor(BukkitMCColor.GetColor(color));
	}
	
}
