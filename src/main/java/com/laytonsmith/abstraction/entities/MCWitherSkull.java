package com.laytonsmith.abstraction.entities;

import com.laytonsmith.abstraction.MCFireball;

/**
 *
 * @author Veyyn
 */
public interface MCWitherSkull extends MCFireball {

	public boolean isCharged();
	public void setCharged(boolean charged);
}