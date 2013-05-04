
package com.laytonsmith.abstraction.bukkit;

import com.laytonsmith.abstraction.MCEnderCrystal;
import com.laytonsmith.annotations.WrappedItem;
import org.bukkit.entity.EnderCrystal;

/**
 *
 * @author Layton
 */
public class BukkitMCEnderCrystal extends BukkitMCEntity implements MCEnderCrystal{
	
	@WrappedItem EnderCrystal ec;
	public BukkitMCEnderCrystal(EnderCrystal ec){
		super(ec);
		this.ec = ec;
	}
	
	
}
