package com.laytonsmith.abstraction;

import com.laytonsmith.abstraction.enums.MCFireworkType;

import java.util.List;

public interface MCFireworkEffect extends AbstractionObject {

	boolean hasFlicker();

	boolean hasTrail();

	List<MCColor> getColors();

	List<MCColor> getFadeColors();

	MCFireworkType getType();

}
