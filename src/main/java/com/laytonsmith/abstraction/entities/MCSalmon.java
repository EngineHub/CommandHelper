package com.laytonsmith.abstraction.entities;

import com.laytonsmith.abstraction.MCLivingEntity;

public interface MCSalmon extends MCLivingEntity {

	Variant getVariant();
	void setVariant(Variant size);

	enum Variant {
		SMALL,
		MEDIUM,
		LARGE
	}

}
