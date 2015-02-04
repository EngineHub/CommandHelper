package com.laytonsmith.abstraction;

import com.laytonsmith.abstraction.enums.MCFireworkType;

import java.util.List;

public interface MCFireworkEffect {

	List<MCColor> getColors();

	List<MCColor> getFadeColors();

	MCFireworkType getShape();

	boolean hasFlicker();

	boolean hasTrail();
}
