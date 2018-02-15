package com.laytonsmith.abstraction.entities;

import com.laytonsmith.abstraction.MCFireball;

public interface MCWitherSkull extends MCFireball {
	boolean isCharged();
	void setCharged(boolean charged);
}