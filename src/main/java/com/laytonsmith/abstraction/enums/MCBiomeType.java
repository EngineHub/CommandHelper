package com.laytonsmith.abstraction.enums;

import com.laytonsmith.annotations.typename;
import com.laytonsmith.core.constructs.CPrimitive;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.natives.MEnum;
import com.laytonsmith.core.natives.interfaces.Mixed;

/**
 *
 * @author layton
 */
@typename("BiomeType")
public enum MCBiomeType implements MEnum {

        OCEAN,
        PLAINS,
        DESERT,
        EXTREME_HILLS,
        FOREST,
        TAIGA,
        SWAMPLAND,
        RIVER,
        HELL,
        SKY,
        FROZEN_OCEAN,
        FROZEN_RIVER,
        ICE_PLAINS,
        ICE_MOUNTAINS,
        MUSHROOM_ISLAND,
        MUSHROOM_SHORE,
        BEACH,
        DESERT_HILLS,
        FOREST_HILLS,
        TAIGA_HILLS,
        SMALL_MOUNTAINS,
        JUNGLE,
        JUNGLE_HILLS;

	public Object value() {
		return this;
	}

	public String val() {
		return name();
	}

	public boolean isNull() {
		return false;
	}

	public String typeName() {
		return this.getClass().getAnnotation(typename.class).value();
	}

	public CPrimitive primitive(Target t) throws ConfigRuntimeException {
		throw new Error();
	}

	public boolean isImmutable() {
		return true;
	}

	public boolean isDynamic() {
		return false;
	}

	public void destructor() {
		
	}

	public Mixed doClone() {
		return this;
	}

	public Target getTarget() {
		return Target.UNKNOWN;
	}

}
