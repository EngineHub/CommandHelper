package com.laytonsmith.abstraction.entities;

import com.laytonsmith.abstraction.MCEntity;

/**
 *
 * @author Hekta
 */
public interface MCComplexEntityPart extends MCEntity {

	public MCComplexLivingEntity getParent();
}