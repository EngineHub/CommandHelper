package com.laytonsmith.abstraction.entities;

import java.util.Set;

/**
 *
 * @author Hekta
 */
public interface MCComplexLivingEntity extends MCLivingEntity {

	public Set<MCComplexEntityPart> getParts();
}