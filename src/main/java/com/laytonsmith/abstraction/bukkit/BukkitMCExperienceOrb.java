/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
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
	
	public int getExperience(){
		return eo.getExperience();
	}
	
	public void setExperience(int amount){
		eo.setExperience(amount);
	}
}
