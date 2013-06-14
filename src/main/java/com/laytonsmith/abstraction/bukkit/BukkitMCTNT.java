
package com.laytonsmith.abstraction.bukkit;

import com.laytonsmith.abstraction.MCTNT;
import org.bukkit.entity.TNTPrimed;

/**
 *
 */
public class BukkitMCTNT extends BukkitMCEntity implements MCTNT{
	TNTPrimed tnt;
	public BukkitMCTNT(TNTPrimed e) {
		super(e);
		this.tnt = e;
	}	
}
