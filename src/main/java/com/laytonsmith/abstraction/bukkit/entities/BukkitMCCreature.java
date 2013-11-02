package com.laytonsmith.abstraction.bukkit.entities;

import org.bukkit.entity.Creature;
import org.bukkit.entity.LivingEntity;

import com.laytonsmith.abstraction.bukkit.BukkitConvertor;
import com.laytonsmith.abstraction.entities.MCCreature;
import com.laytonsmith.abstraction.entities.MCEntity;
import com.laytonsmith.abstraction.entities.MCLivingEntity;

/**
 *
 * @author Hekta
 */
public abstract class BukkitMCCreature extends BukkitMCLivingEntity implements MCCreature {

	public BukkitMCCreature(Creature creature) {
		super(creature);
	}

	@Override
	public Creature getHandle() {
		return (Creature) metadatable;
	}

	public MCLivingEntity getTarget() {
		MCEntity target = BukkitConvertor.BukkitGetCorrectEntity(getHandle().getTarget());
		if (target != null) {
			return (MCLivingEntity) target;
		} else {
			return null;
		}
	}

	public void setTarget(MCLivingEntity target) {
		getHandle().setTarget(target != null ? (LivingEntity) target.getHandle() : null);
	}
}