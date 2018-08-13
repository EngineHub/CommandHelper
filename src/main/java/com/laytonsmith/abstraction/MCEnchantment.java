package com.laytonsmith.abstraction;

public interface MCEnchantment extends AbstractionObject {

	boolean canEnchantItem(MCItemStack is);

	int getMaxLevel();

	String getName();

	String getKey();
}
