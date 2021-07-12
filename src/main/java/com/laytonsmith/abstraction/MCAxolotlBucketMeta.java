package com.laytonsmith.abstraction;

import com.laytonsmith.abstraction.enums.MCAxolotlType;

public interface MCAxolotlBucketMeta extends MCItemMeta {

	MCAxolotlType getAxolotlType();

	void setAxolotlType(MCAxolotlType type);

}
