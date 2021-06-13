package com.laytonsmith.abstraction.blocks;

public interface MCSign extends MCBlockState {

	void setLine(int i, String line1);

	String getLine(int i);

	boolean isGlowingText();

	void setGlowingText(boolean glowing);
}
