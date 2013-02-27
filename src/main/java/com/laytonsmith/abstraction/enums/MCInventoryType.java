
package com.laytonsmith.abstraction.enums;

import com.laytonsmith.annotations.typename;
import com.laytonsmith.core.constructs.CPrimitive;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.natives.MEnum;

/**
 *
 * @author Layton
 */
@typename("InventoryType")
public enum MCInventoryType implements MEnum {
	BREWING,
	CHEST,
	CRAFTING,
	CREATIVE,
	DISPENSER,
	ENCHANTING,
	ENDER_CHEST,
	FURNACE,
	MERCHANT,
	PLAYER,
	WORKBENCH,
	ANVIL,
	BEACON;

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
