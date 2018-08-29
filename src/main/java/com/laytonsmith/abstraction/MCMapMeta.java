package com.laytonsmith.abstraction;

public interface MCMapMeta extends MCItemMeta {

	boolean hasMapId();

	int getMapId();

	void setMapId(int id);

	MCColor getColor();

	void setColor(MCColor color);

}
