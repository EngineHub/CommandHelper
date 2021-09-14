package com.laytonsmith.abstraction.entities;

import com.laytonsmith.abstraction.MCEntity;

public interface MCTNT extends MCEntity {

	MCEntity getSource();

	void setSource(MCEntity source);

	int getFuseTicks();

	void setFuseTicks(int ticks);
}
