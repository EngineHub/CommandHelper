package com.laytonsmith.abstraction.entities;

import com.laytonsmith.abstraction.MCColor;

public interface MCTextDisplay extends MCDisplay {

	Alignment getAlignment();

	void setAlignment(Alignment alignment);

	MCColor getBackgroundColor();

	void setBackgroundColor(MCColor color);

	int getLineWidth();

	void setLineWidth(int width);

	boolean isVisibleThroughBlocks();

	void setVisibleThroughBlocks(boolean visible);

	boolean hasShadow();

	void setHasShadow(boolean hasShadow);

	String getText();

	void setText(String text);

	byte getOpacity();

	void setOpacity(byte opacity);

	enum Alignment {
		CENTER,
		LEFT,
		RIGHT
	}
}
