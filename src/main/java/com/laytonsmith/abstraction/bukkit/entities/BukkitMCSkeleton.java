package com.laytonsmith.abstraction.bukkit.entities;

import com.laytonsmith.abstraction.AbstractionObject;
import com.laytonsmith.abstraction.entities.MCSkeleton;
import com.laytonsmith.abstraction.enums.MCSkeletonType;
import com.laytonsmith.core.CHLog;
import com.laytonsmith.core.LogLevel;
import com.laytonsmith.core.constructs.Target;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Skeleton;

public class BukkitMCSkeleton extends BukkitMCLivingEntity implements MCSkeleton {

	public BukkitMCSkeleton(Entity skeleton) {
		super(skeleton);
	}

	public BukkitMCSkeleton(AbstractionObject ao) {
		this((Skeleton) ao.getHandle());
	}

	@Override
	public MCSkeletonType getSkeletonType() {
		return MCSkeletonType.valueOf(((Skeleton) getHandle()).getSkeletonType().name());
	}

	@Override
	public void setSkeletonType(MCSkeletonType type) {
		try {
			((Skeleton) getHandle()).setSkeletonType(Skeleton.SkeletonType.valueOf(type.name()));
		} catch (UnsupportedOperationException ex) {
			// 1.11 or later
			CHLog.GetLogger().Log(CHLog.Tags.DEPRECATION, LogLevel.ERROR,
					"Cannot change Skeleton to Stray or WitherSkeleton in Minecraft 1.11+", Target.UNKNOWN);
		}
	}
}
