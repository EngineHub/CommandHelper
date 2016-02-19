
package com.laytonsmith.abstraction.bukkit.entities;

import com.laytonsmith.abstraction.MCExperienceOrb;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ExperienceOrb;

/**
 *
 * @author Jim
 */
public class BukkitMCExperienceOrb extends BukkitMCEntity implements MCExperienceOrb {

	ExperienceOrb eo;

	public BukkitMCExperienceOrb(Entity eo) {
		super(eo);
		this.eo = (ExperienceOrb) eo;
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
