/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.abstraction.bukkit;

import com.laytonsmith.abstraction.MCInventoryView;
import org.bukkit.inventory.InventoryView;

/**
 *
 * @author Layton
 */
public class BukkitMCInventoryView implements MCInventoryView {
	
	InventoryView iv;

	public BukkitMCInventoryView(InventoryView iv) {
		this.iv = iv;
	}
	
}
