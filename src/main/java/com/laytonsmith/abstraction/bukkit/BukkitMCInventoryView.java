
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
	
	@Override
	public String toString() {
		return iv.toString();
	}

	@Override
	public boolean equals(Object obj) {
		return (obj instanceof BukkitMCInventoryView?iv.equals(((BukkitMCInventoryView)obj).iv):false);
	}

	@Override
	public int hashCode() {
		return iv.hashCode();
	}
	
}
