
package com.laytonsmith.abstraction.bukkit;

import com.laytonsmith.abstraction.MCTNT;
import com.laytonsmith.annotations.WrappedItem;
import org.bukkit.entity.TNTPrimed;

/**
 *
 * @author Layton
 */
public class BukkitMCTNT extends BukkitMCEntity implements MCTNT{
	@WrappedItem TNTPrimed tnt;
	public BukkitMCTNT(TNTPrimed e) {
		super(e);
		this.tnt = e;
	}	
}
