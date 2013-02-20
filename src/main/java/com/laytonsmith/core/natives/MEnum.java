package com.laytonsmith.core.natives;

import com.laytonsmith.core.natives.interfaces.Mixed;

/**
 * All native enums must implement MEnum, which allows for POJO enums
 * to be translated to MethodScript enums. An additional requirement, which
 * cannot be programmatically enforced, (but is enforced through unit tests) is
 * that the classes that implement this interface also be enums.
 */
public interface MEnum extends Mixed {
	
}
