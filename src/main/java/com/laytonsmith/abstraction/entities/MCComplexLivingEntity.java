package com.laytonsmith.abstraction.entities;

import com.laytonsmith.abstraction.MCLivingEntity;
import java.util.Set;

public interface MCComplexLivingEntity extends MCLivingEntity {

	Set<MCComplexEntityPart> getParts();
}
