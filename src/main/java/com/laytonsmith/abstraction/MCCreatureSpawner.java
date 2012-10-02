/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.abstraction;

import com.laytonsmith.abstraction.blocks.MCBlockState;

/**
 *
 * @author Layton
 */
public interface MCCreatureSpawner extends MCBlockState {
	
	MCEntityType getSpawnedType();
	void setSpawnedType(MCEntityType type);
}
