package com.laytonsmith.abstraction.entities;

import com.laytonsmith.abstraction.MCLivingEntity;
import com.laytonsmith.abstraction.enums.MCSkeletonType;

public interface MCSkeleton extends MCLivingEntity {

	MCSkeletonType getSkeletonType();

	void setSkeletonType(MCSkeletonType type);
}
