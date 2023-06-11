package com.laytonsmith.abstraction;

import com.laytonsmith.abstraction.enums.MCTrimMaterial;
import com.laytonsmith.abstraction.enums.MCTrimPattern;

public interface MCArmorMeta extends MCItemMeta {
	boolean hasTrim();
	void setTrim(MCTrimPattern pattern, MCTrimMaterial material);
	MCTrimPattern getTrimPattern();
	MCTrimMaterial getTrimMaterial();
}
