package com.laytonsmith.abstraction.bukkit.entities;

import com.laytonsmith.abstraction.AbstractionObject;
import com.laytonsmith.abstraction.entities.MCExperienceOrb;

import org.bukkit.entity.ExperienceOrb;

/**
 *
 * @author Jim
 */
public class BukkitMCExperienceOrb extends BukkitMCEntity implements MCExperienceOrb {

	public BukkitMCExperienceOrb(ExperienceOrb orb) {
		super(orb);
	}

	public BukkitMCExperienceOrb(AbstractionObject ao) {
		this((ExperienceOrb) ao.getHandle());
	}

	@Override
	public ExperienceOrb getHandle() {
		return (ExperienceOrb) metadatable;
	}

	public int getExperience(){
		return getHandle().getExperience();
	}

	public void setExperience(int amount){
		getHandle().setExperience(amount);
	}
}