package com.laytonsmith.abstraction.entities;

/**
 *
 * @author Hekta
 */
public interface MCExplosive extends MCEntity {

	public float getYield();
	public void setYield(float yield);
	public boolean isIncendiary();
	public void setIsIncendiary(boolean isIncendiary);
}