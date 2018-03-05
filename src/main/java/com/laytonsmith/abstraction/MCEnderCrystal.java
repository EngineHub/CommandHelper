package com.laytonsmith.abstraction;

public interface MCEnderCrystal extends MCEntity {

	boolean isShowingBottom();

	void setShowingBottom(boolean showing);

	MCLocation getBeamTarget();

	void setBeamTarget(MCLocation target);
}
