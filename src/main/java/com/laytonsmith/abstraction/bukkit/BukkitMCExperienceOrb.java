
package com.laytonsmith.abstraction.bukkit;

import com.laytonsmith.abstraction.MCExperienceOrb;
import org.bukkit.entity.ExperienceOrb;

/**
 *
 * @author Jim
 */
public class BukkitMCExperienceOrb extends BukkitMCEntity implements MCExperienceOrb {

	ExperienceOrb eo;
	
	public BukkitMCExperienceOrb(ExperienceOrb eo){
		super(eo);
		this.eo = eo;
	}
	
	@Override
	public int getExperience(){
		return eo.getExperience();
	}
	
	@Override
	public void setExperience(int amount){
		eo.setExperience(amount);
	}
}
