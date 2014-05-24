
package com.laytonsmith.abstraction.bukkit;

import com.laytonsmith.abstraction.MCLightningStrike;
import org.bukkit.entity.LightningStrike;

/**
 *
 * @author Jim
 */
public class BukkitMCLightningStrike extends BukkitMCEntity implements MCLightningStrike{

	LightningStrike ls;
	
	public BukkitMCLightningStrike(LightningStrike ls){
		super(ls);
		this.ls = ls;
	}
	
	@Override
	public boolean isEffect(){
		return ls.isEffect();
	}
}
