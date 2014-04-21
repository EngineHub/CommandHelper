
package com.laytonsmith.abstraction.bukkit;

import com.laytonsmith.abstraction.MCEnderCrystal;
import org.bukkit.entity.EnderCrystal;

/**
 *
 * 
 */
public class BukkitMCEnderCrystal extends BukkitMCEntity implements MCEnderCrystal{
	
	EnderCrystal ec;
	public BukkitMCEnderCrystal(EnderCrystal ec){
		super(ec);
		this.ec = ec;
	}
	
	
}
