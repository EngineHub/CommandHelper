
package com.laytonsmith.abstraction.bukkit.entities;

import com.laytonsmith.abstraction.MCEnderCrystal;
import org.bukkit.entity.EnderCrystal;
import org.bukkit.entity.Entity;

/**
 *
 * 
 */
public class BukkitMCEnderCrystal extends BukkitMCEntity implements MCEnderCrystal{
	
	EnderCrystal ec;

	public BukkitMCEnderCrystal(Entity ec) {
		super(ec);
		this.ec = (EnderCrystal) ec;
	}
	
	
}
