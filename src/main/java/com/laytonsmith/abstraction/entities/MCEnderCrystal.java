package com.laytonsmith.abstraction.entities;

import com.laytonsmith.abstraction.MCEntity;
import com.laytonsmith.abstraction.MCLocation;

public interface MCEnderCrystal extends MCEntity {

	boolean isShowingBottom();

	void setShowingBottom(boolean showing);

	MCLocation getBeamTarget();

	void setBeamTarget(MCLocation target);
}
