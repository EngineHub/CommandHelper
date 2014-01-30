package com.laytonsmith.abstraction.bukkit.entities;

import com.laytonsmith.abstraction.bukkit.BukkitConvertor;
import com.laytonsmith.abstraction.bukkit.BukkitMCEntity;
import com.laytonsmith.abstraction.entities.MCComplexEntityPart;
import com.laytonsmith.abstraction.entities.MCComplexLivingEntity;
import org.bukkit.entity.ComplexEntityPart;

/**
 *
 * @author Hekta
 */
public class BukkitMCComplexEntityPart extends BukkitMCEntity implements MCComplexEntityPart {

	public BukkitMCComplexEntityPart(ComplexEntityPart part) {
		super(part);
	}

	@Override
	public ComplexEntityPart getHandle() {
		return (ComplexEntityPart)super.getHandle();
	}

	@Override
	public MCComplexLivingEntity getParent() {
		return (MCComplexLivingEntity) BukkitConvertor.BukkitGetCorrectEntity(getHandle().getParent());
	}
}