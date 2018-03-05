package com.laytonsmith.abstraction;

import com.laytonsmith.abstraction.enums.MCTone;

public interface MCNote extends AbstractionObject {

	MCTone getTone();

	int getOctave();

	boolean isSharped();
}
