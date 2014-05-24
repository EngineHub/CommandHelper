/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.abstraction;


/**
 *
 * 
 */
public interface MCLeatherArmorMeta extends MCItemMeta {
	    /**
     * Gets the color of the armor. If it has not been set otherwise, it will be {@link ItemFactory#getDefaultLeatherColor()}.
     *
     * @return the color of the armor, never null
     */
    MCColor getColor();

    /**
     * Sets the color of the armor.
     *
     * @param color the color to set. Setting it to null is equivalent to setting it to {@link ItemFactory#getDefaultLeatherColor()}.
     */
    void setColor(MCColor color);
}
