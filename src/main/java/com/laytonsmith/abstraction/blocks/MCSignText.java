package com.laytonsmith.abstraction.blocks;

import com.laytonsmith.abstraction.AbstractionObject;
import com.laytonsmith.abstraction.enums.MCDyeColor;

public interface MCSignText extends AbstractionObject {

	String[] getLines();

	void setLine(int i, String line1);

	String getLine(int i);

	boolean isGlowingText();

	void setGlowingText(boolean glowing);

	MCDyeColor getDyeColor();

	void setDyeColor(MCDyeColor color);
}
