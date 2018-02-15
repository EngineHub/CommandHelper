package com.laytonsmith.abstraction;

import com.laytonsmith.abstraction.enums.MCWorldEnvironment;
import com.laytonsmith.abstraction.enums.MCWorldType;

public interface MCWorldCreator {
	MCWorld createWorld();
	MCWorldCreator type(MCWorldType type);
	MCWorldCreator environment(MCWorldEnvironment environment);
	MCWorldCreator seed(long seed);
	MCWorldCreator generator(String generator);
	MCWorldCreator copy(MCWorld toCopy);
}
