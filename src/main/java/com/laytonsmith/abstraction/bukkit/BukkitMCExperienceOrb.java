
package com.laytonsmith.abstraction.bukkit;

import com.laytonsmith.abstraction.MCExperienceOrb;
import com.laytonsmith.annotations.WrappedItem;
import org.bukkit.entity.ExperienceOrb;

/**
 *
 * @author Jim
 */
public class BukkitMCExperienceOrb extends BukkitMCEntity implements MCExperienceOrb {

	@WrappedItem ExperienceOrb eo;
	
	public BukkitMCExperienceOrb(ExperienceOrb eo){
		super(eo);
		this.eo = eo;
	}
	
	public int getExperience(){
		return eo.getExperience();
	}
	
	public void setExperience(int amount){
		eo.setExperience(amount);
	}
}
