package com.laytonsmith.core.natives.interfaces;

import com.laytonsmith.core.objects.ObjectType;

/**
 * Provides a basic implementation for Mixed. This assumes the object is a class, along with the additional assumptions
 * provided by AbstractMixed.
 */
public abstract class AbstractMixedClass extends AbstractMixed {

	@Override
	public ObjectType getObjectType() {
		return ObjectType.CLASS;
	}

}
