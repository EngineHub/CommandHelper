package com.laytonsmith.abstraction.entities;

import com.laytonsmith.abstraction.MCEntity;

public interface MCTNT extends MCEntity {

	MCEntity getSource();

	int getFuseTicks();

	void setFuseTicks(int ticks);
}
