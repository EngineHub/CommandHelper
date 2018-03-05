package com.laytonsmith.abstraction;

import com.laytonsmith.abstraction.enums.MCFireworkType;

public interface MCFireworkBuilder {

	/**
	 * Sets whether or not this firework has a flicker
	 *
	 * @param flicker
	 * @return
	 */
	MCFireworkBuilder setFlicker(boolean flicker);

	/**
	 * Sets whether or not this firework has a trail
	 */
	MCFireworkBuilder setTrail(boolean trail);

	/**
	 * Adds a primary color
	 *
	 * @param color
	 * @return
	 */
	MCFireworkBuilder addColor(MCColor color);

	/**
	 * Adds a secondary color
	 *
	 * @param color
	 * @return
	 */
	MCFireworkBuilder addFadeColor(MCColor color);

	/**
	 * Sets the firework type of the firework.
	 *
	 * @param type
	 * @return
	 */
	MCFireworkBuilder setType(MCFireworkType type);

	/**
	 * Returns the firework effect object created by this builder
	 *
	 * @return MCFireworkEffect
	 */
	MCFireworkEffect build();

}
