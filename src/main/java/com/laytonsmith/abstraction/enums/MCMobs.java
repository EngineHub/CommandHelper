
package com.laytonsmith.abstraction.enums;

import com.laytonsmith.annotations.typename;
import com.laytonsmith.core.constructs.CPrimitive;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.exceptions.NonScriptError;
import com.laytonsmith.core.natives.MEnum;

/**
 * These don't directly map to entity types, because this is mostly used for
 * spawining mobs.
 * @author Layton
 */
@typename("Mobs")
public enum MCMobs implements MEnum {
	CHICKEN, COW, CREEPER, GHAST, PIG, PIGZOMBIE, SHEEP, SKELETON, SLIME, SPIDER, SQUID, WOLF, ZOMBIE, CAVESPIDER, ENDERMAN, SILVERFISH, VILLAGER, BLAZE, ENDERDRAGON, MAGMACUBE, MOOSHROOM, SPIDERJOCKEY, GIANT, SNOWGOLEM, OCELOT, CAT, IRONGOLEM, WITHER, BAT, WITCH, WITHER_SKULL;
    
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
}
