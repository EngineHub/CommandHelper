package com.laytonsmith.abstraction.bukkit.entities;

import com.laytonsmith.abstraction.bukkit.BukkitConvertor;
import com.laytonsmith.abstraction.entities.MCComplexEntityPart;
import com.laytonsmith.abstraction.entities.MCComplexLivingEntity;
import org.bukkit.entity.ComplexEntityPart;
import org.bukkit.entity.ComplexLivingEntity;
import org.bukkit.entity.Entity;

import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author Hekta
 */
public class BukkitMCComplexLivingEntity extends BukkitMCLivingEntity implements MCComplexLivingEntity {

	public BukkitMCComplexLivingEntity(Entity complex) {
		super(complex);
	}

	@Override
	public ComplexLivingEntity getHandle() {
		return (ComplexLivingEntity)super.getHandle();
	}

	@Override
	public Set<MCComplexEntityPart> getParts() {
		Set<MCComplexEntityPart> parts = new HashSet<>();
		for (ComplexEntityPart part : getHandle().getParts()) {
			parts.add((MCComplexEntityPart) BukkitConvertor.BukkitGetCorrectEntity(part));
		}
		return parts;
	}
}