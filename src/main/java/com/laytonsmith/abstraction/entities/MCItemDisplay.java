package com.laytonsmith.abstraction.entities;

import com.laytonsmith.abstraction.MCItemStack;

public interface MCItemDisplay extends MCDisplay {

	MCItemStack getItem();

	void setItem(MCItemStack item);

	ModelTransform getItemModelTransform();

	void setItemModelTransform(ModelTransform transform);

	enum ModelTransform {
		FIRSTPERSON_LEFTHAND,
		FIRSTPERSON_RIGHTHAND,
		FIXED,
		GROUND,
		GUI,
		HEAD,
		NONE,
		THIRDPERSON_LEFTHAND,
		THIRDPERSON_RIGHTHAND
	}
}
