package com.laytonsmith.abstraction.entities;

/**
 *
 * @author Layton
 */
public interface MCTNTPrimed extends MCEntity {

	public int getFuseTicks();
	public void setFuseTicks(int fuseTicks);

	public MCEntity getSource();
}