/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.abstraction;

import com.laytonsmith.abstraction.entities.MCFirework;
import com.laytonsmith.abstraction.enums.MCFireworkType;

/**
 *
 *
 */
public interface MCFireworkBuilder {

	/**
	 * Sets whether or not this firework has a flicker
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
	 * @param color
	 * @return
	 */
	MCFireworkBuilder addColor(MCColor color);
	/**
	 * Adds a secondary color
	 * @param color
	 * @return
	 */
	MCFireworkBuilder addFadeColor(MCColor color);
	/**
	 * Sets the launch strength of the firework
	 * @param i
	 * @return
	 */
	MCFireworkBuilder setStrength(int i);

	/**
	 * Sets the firework type of the firework.
	 * @param type
	 * @return
	 */
	MCFireworkBuilder setType(MCFireworkType type);
	/**
	 * Launches the firework from the specified location, upward.
	 * @param l
	 * @return entityID of the firework
	 */
	MCFirework launch(MCLocation l);

	/**
	 * Given the entered builder data, create a firework meta with these attributes
	 * for a given MCFireworkMeta
	 * @return
	 */
	void createFireworkMeta(MCFireworkMeta meta);

}
