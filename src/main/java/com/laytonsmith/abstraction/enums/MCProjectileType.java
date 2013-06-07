package com.laytonsmith.abstraction.enums;

import com.laytonsmith.annotations.typename;
import com.laytonsmith.core.constructs.CPrimitive;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.natives.MEnum;
import com.laytonsmith.core.natives.interfaces.Mixed;

/**
 * 
 * @author jb_aero
 */
@typename("ProjectileType")
public enum MCProjectileType implements MEnum {
	ARROW,
	EGG,
	ENDER_PEARL,
	FIREBALL,
	//FISHING_HOOK,
	SMALL_FIREBALL,
	SNOWBALL,
	SPLASH_POTION,
	//THROWN_EXP_BOTTLE,
	WITHER_SKULL;

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
