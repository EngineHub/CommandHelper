package com.laytonsmith.abstraction.bukkit.entities;

import org.bukkit.entity.ComplexEntityPart;

import com.laytonsmith.abstraction.bukkit.BukkitConvertor;
import com.laytonsmith.abstraction.entities.MCComplexEntityPart;
import com.laytonsmith.abstraction.entities.MCComplexLivingEntity;

/**
 *
 * @author Hekta
 */
public abstract class BukkitMCComplexEntityPart extends BukkitMCEntity implements MCComplexEntityPart {

	public BukkitMCComplexEntityPart(ComplexEntityPart part) {
		super(part);
	}

	@Override
	public ComplexEntityPart getHandle() {
		return (ComplexEntityPart) metadatable;
	}

	public MCComplexLivingEntity getParent() {
		return (MCComplexLivingEntity) BukkitConvertor.BukkitGetCorrectEntity(getHandle().getParent());
	}
}