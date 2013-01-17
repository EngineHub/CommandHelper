package com.laytonsmith.abstraction.entities;

import com.laytonsmith.abstraction.MCTameable;
import com.laytonsmith.abstraction.enums.MCOcelotType;

/**
 * 
 * @author jb_aero
 */
public interface MCOcelot extends MCTameable {

	MCOcelotType getCatType();
	boolean isSitting();
	void setCatType(MCOcelotType type);
	void setSitting(boolean sitting);
}
