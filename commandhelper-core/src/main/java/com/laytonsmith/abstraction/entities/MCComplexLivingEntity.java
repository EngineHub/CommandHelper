package com.laytonsmith.abstraction.entities;

import com.laytonsmith.abstraction.MCLivingEntity;
import java.util.Set;

/**
 *
 * @author Hekta
 */
public interface MCComplexLivingEntity extends MCLivingEntity {

	public Set<MCComplexEntityPart> getParts();
}