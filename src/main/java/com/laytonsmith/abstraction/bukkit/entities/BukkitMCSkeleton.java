package com.laytonsmith.abstraction.bukkit.entities;

import com.laytonsmith.abstraction.AbstractionObject;
import com.laytonsmith.abstraction.entities.MCSkeleton;
import com.laytonsmith.abstraction.enums.MCSkeletonType;
import com.laytonsmith.abstraction.enums.bukkit.BukkitMCSkeletonType;
import com.laytonsmith.core.CHLog;
import com.laytonsmith.core.LogLevel;
import com.laytonsmith.core.constructs.Target;
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
		try {
			((Skeleton) getHandle()).setSkeletonType(BukkitMCSkeletonType.getConvertor().getConcreteEnum(type));
		} catch(UnsupportedOperationException ex){
			// 1.11 or later
			CHLog.GetLogger().Log(CHLog.Tags.DEPRECATION, LogLevel.WARNING,
					"Cannot change Skeleton to Stray or WitherSkeleton in Minecraft 1.11+", Target.UNKNOWN);
		}
	}
}