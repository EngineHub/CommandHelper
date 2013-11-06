package com.laytonsmith.abstraction.entities;

import com.laytonsmith.abstraction.enums.MCSkeletonType;

/**
 *
 * @author Hekta
 */
public interface MCSkeleton extends MCCreature {

	public MCSkeletonType getSkeletonType();
	public void setSkeletonType(MCSkeletonType type);
}