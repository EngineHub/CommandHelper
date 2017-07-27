package com.laytonsmith.abstraction.entities;

import com.laytonsmith.abstraction.MCTameable;
import com.laytonsmith.abstraction.enums.MCParrotType;

public interface MCParrot extends MCTameable {

	boolean isSitting();
	void setSitting(boolean sitting);
	MCParrotType getVariant();
	void setVariant(MCParrotType variant);
}
