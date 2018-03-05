package com.laytonsmith.abstraction.entities;

import com.laytonsmith.abstraction.enums.MCEnderDragonPhase;

public interface MCEnderDragon extends MCComplexLivingEntity {

	MCEnderDragonPhase getPhase();

	void setPhase(MCEnderDragonPhase phase);

}
