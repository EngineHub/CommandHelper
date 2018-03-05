package com.laytonsmith.abstraction.entities;

import com.laytonsmith.abstraction.MCEntity;

public interface MCComplexEntityPart extends MCEntity {

	MCComplexLivingEntity getParent();
}
