package com.laytonsmith.abstraction.entities;

public interface MCTextDisplay extends MCDisplay {

	Alignment getAlignment();

	void setAlignment(Alignment alignment);

	boolean usesDefaultBackground();

	void setUsesDefaultBackground(boolean defaultBackground);

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
