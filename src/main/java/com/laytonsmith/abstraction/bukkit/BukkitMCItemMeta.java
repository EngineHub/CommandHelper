/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.abstraction.bukkit;

import com.laytonsmith.abstraction.AbstractionObject;
import com.laytonsmith.abstraction.MCItemMeta;
import java.util.List;
import org.bukkit.inventory.meta.ItemMeta;

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
	
	public boolean hasDisplayName() {
		return im.hasDisplayName();
	}

	public String getDisplayName() {
		return im.getDisplayName();
	}

	public void setDisplayName(String name) {
		im.setDisplayName(name);
	}

	public boolean hasLore() {
		return im.hasLore();
	}

	public List<String> getLore() {
		return im.getLore();
	}

	public void setLore(List<String> lore) {
		im.setLore(lore);
	}

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
	
	
}
