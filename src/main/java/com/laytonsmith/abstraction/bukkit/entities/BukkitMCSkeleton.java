package com.laytonsmith.abstraction.bukkit.entities;

import org.bukkit.entity.Skeleton;

import com.laytonsmith.abstraction.AbstractionObject;
import com.laytonsmith.abstraction.entities.MCSkeleton;
import com.laytonsmith.abstraction.enums.MCSkeletonType;
import com.laytonsmith.abstraction.enums.bukkit.BukkitMCSkeletonType;

/**
 *
 * @author Hekta
 */
public class BukkitMCSkeleton extends BukkitMCCreature implements MCSkeleton {

	public BukkitMCSkeleton(Skeleton skeleton) {
		super(skeleton);
	}

	public BukkitMCSkeleton(AbstractionObject ao) {
		this((Skeleton) ao.getHandle());
	}

	@Override
	public Skeleton getHandle() {
		return (Skeleton) metadatable;
	}

	public MCSkeletonType getSkeletonType() {
		return BukkitMCSkeletonType.getConvertor().getAbstractedEnum(getHandle().getSkeletonType());
	}

	public void setSkeletonType(MCSkeletonType type) {
		getHandle().setSkeletonType(BukkitMCSkeletonType.getConvertor().getConcreteEnum(type));
	}
}