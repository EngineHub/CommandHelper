/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.abstraction;

import com.laytonsmith.abstraction.enums.MCWorldEnvironment;
import com.laytonsmith.abstraction.enums.MCWorldType;

/**
 *
 * @author Layton
 */
public interface MCWorldCreator {
	MCWorld createWorld();
	MCWorldCreator type(MCWorldType type);
	MCWorldCreator environment(MCWorldEnvironment environment);
	MCWorldCreator seed(long seed);
}
