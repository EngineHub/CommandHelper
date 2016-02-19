package com.laytonsmith.abstraction;

import com.laytonsmith.abstraction.enums.MCDyeColor;
import com.laytonsmith.abstraction.enums.MCPatternShape;

public interface MCPattern extends AbstractionObject {

	public MCDyeColor getColor();
	public MCPatternShape getShape();
	
}
