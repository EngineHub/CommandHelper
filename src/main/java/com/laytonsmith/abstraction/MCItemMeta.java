/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.abstraction;

import java.util.List;

/**
 *
 * @author Layton
 */
public interface MCItemMeta extends AbstractionObject {
	    /**
     * Checks for existence of a display name
     *
     * @return true if this has a display name
     */
    boolean hasDisplayName();

    /**
     * Gets the display name that is set
     *
     * @return the display name that is set
     */
    String getDisplayName();

    /**
     * Sets the display name
     *
     * @param name the name to set
     */
    void setDisplayName(String name);

    /**
     * Checks for existence of lore
     *
     * @return true if this has lore
     */
    boolean hasLore();

    /**
     * Gets the lore that is set
     *
     * @return a list of lore that is set
     */
    List<String> getLore();

    /**
     * Sets the lore for this item
     *
     * @param lore the lore that will be set
     */
    void setLore(List<String> lore);
}
