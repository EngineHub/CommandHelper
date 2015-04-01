package com.laytonsmith.abstraction.bukkit.entities;

import com.laytonsmith.abstraction.AbstractionObject;
import com.laytonsmith.abstraction.entities.MCSkeleton;
import com.laytonsmith.abstraction.enums.MCSkeletonType;
import com.laytonsmith.abstraction.enums.bukkit.BukkitMCSkeletonType;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Skeleton;

/**
 *
 * @author Hekta
 */
public class BukkitMCSkeleton extends BukkitMCLivingEntity implements MCSkeleton {

	public BukkitMCSkeleton(Entity skeleton) {
		super(skeleton);
	}

	public BukkitMCSkeleton(AbstractionObject ao) {
		this((Skeleton) ao.getHandle());
	}

	@Override
	public MCSkeletonType getSkeletonType() {
		return BukkitMCSkeletonType.getConvertor().getAbstractedEnum(((Skeleton)getHandle()).getSkeletonType());
	}

	@Override
	public void setSkeletonType(MCSkeletonType type) {
		((Skeleton)getHandle()).setSkeletonType(BukkitMCSkeletonType.getConvertor().getConcreteEnum(type));
	}
}