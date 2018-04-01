package com.laytonsmith.abstraction;

import com.laytonsmith.abstraction.enums.MCEntityType;

public interface MCSpawnEggMeta extends MCItemMeta {

	MCEntityType getSpawnedType();

	void setSpawnedType(MCEntityType type);

}
