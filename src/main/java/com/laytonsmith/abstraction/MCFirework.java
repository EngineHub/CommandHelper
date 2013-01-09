/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.abstraction;

import com.laytonsmith.abstraction.enums.MCFireworkType;

/**
 *
 * @author Layton
 */
public interface MCFirework {
	
	/**
	 * Sets whether or not this firework has a flicker
	 * @param flicker
	 * @return 
	 */
	MCFirework setFlicker(boolean flicker);
	/**
	 * Sets whether or not this firework has a trail
	 */
	MCFirework setTrail(boolean trail);
	/**
	 * Adds a primary color
	 * @param color
	 * @return 
	 */
	MCFirework addColor(MCColor color);
	/**
	 * Adds a secondary color
	 * @param color
	 * @return 
	 */
	MCFirework addFadeColor(MCColor color);
	/**
	 * Sets the launch strength of the firework
	 * @param i
	 * @return 
	 */
	MCFirework setStrength(int i);
	
	/**
	 * Sets the firework type of the firework.
	 * @param type
	 * @return 
	 */
	MCFirework setType(MCFireworkType type);
	/**
	 * Launches the firework from the specified location, upward.
	 * @param l 
	 */
	void launch(MCLocation l);
	
}
