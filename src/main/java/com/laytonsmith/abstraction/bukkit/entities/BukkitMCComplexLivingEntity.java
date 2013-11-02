package com.laytonsmith.abstraction.bukkit.entities;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.entity.ComplexEntityPart;
import org.bukkit.entity.ComplexLivingEntity;

import com.laytonsmith.abstraction.bukkit.BukkitConvertor;
import com.laytonsmith.abstraction.entities.MCComplexEntityPart;
import com.laytonsmith.abstraction.entities.MCComplexLivingEntity;

/**
 *
 * @author Hekta
 */
public abstract class BukkitMCComplexLivingEntity extends BukkitMCLivingEntity implements MCComplexLivingEntity {

	public BukkitMCComplexLivingEntity(ComplexLivingEntity complex) {
		super(complex);
	}

	@Override
	public ComplexLivingEntity getHandle() {
		return (ComplexLivingEntity) metadatable;
	}

	public Set<MCComplexEntityPart> getParts() {
		Set<MCComplexEntityPart> parts = new HashSet<MCComplexEntityPart>();
		for (ComplexEntityPart part : getHandle().getParts()) {
			parts.add((MCComplexEntityPart) BukkitConvertor.BukkitGetCorrectEntity(part));
		}
		return parts;
	}
}