package com.laytonsmith.abstraction.entities;

import com.laytonsmith.abstraction.MCProjectile;

public interface MCFishHook extends MCProjectile {
	/**
	 * This only refers to with the hook is being reeled in.
	 * @return chance from 0.0 to 1.0
	 */
	public double getBiteChance();
	/**
	 * Setting this only has an effect when the hook is reeled in.
	 * 0.0 represents no chance, 1.0 guarantees that a fish will be caught 
	 * when the hook is pulled in.
	 * @param chance, must be from 0.0 to 1.0 (inclusive)
	 */
	public void setBiteChance(double chance);
}
