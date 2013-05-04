
package com.laytonsmith.abstraction.bukkit;

import com.laytonsmith.abstraction.MCLightningStrike;
import com.laytonsmith.annotations.WrappedItem;
import org.bukkit.entity.LightningStrike;

/**
 *
 * @author Jim
 */
public class BukkitMCLightningStrike extends BukkitMCEntity implements MCLightningStrike{

	@WrappedItem LightningStrike ls;
	
	public BukkitMCLightningStrike(LightningStrike ls){
		super(ls);
		this.ls = ls;
	}
	
	public boolean isEffect(){
		return ls.isEffect();
	}
}
