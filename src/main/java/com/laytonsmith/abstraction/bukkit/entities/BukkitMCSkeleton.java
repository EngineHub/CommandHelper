package com.laytonsmith.abstraction.bukkit.entities;

import com.laytonsmith.abstraction.AbstractionObject;
import com.laytonsmith.abstraction.entities.MCSkeleton;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Skeleton;

public class BukkitMCSkeleton extends BukkitMCLivingEntity implements MCSkeleton {

	public BukkitMCSkeleton(Entity skeleton) {
		super(skeleton);
	}

	public BukkitMCSkeleton(AbstractionObject ao) {
		this((Skeleton) ao.getHandle());
	}
}
