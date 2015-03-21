package com.laytonsmith.abstraction;

import com.laytonsmith.abstraction.enums.MCFireworkType;

import java.util.List;

public interface MCFireworkEffect extends AbstractionObject {

	List<MCColor> getColors();

	List<MCColor> getFadeColors();

	MCFireworkType getShape();

	boolean hasFlicker();

	boolean hasTrail();
}
