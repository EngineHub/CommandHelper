/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.abstraction;

import com.laytonsmith.abstraction.enums.MCItemFlag;

import java.util.List;
import java.util.Map;

/**
 *
 * 
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
    
    /**
     * Checks if this item has any enchantments
     * 
     * @return true if there are enchantments
     */
    boolean hasEnchants();
    
    /**
     * Gets the enchantments on this items
     * 
     * @return a map of MCEnchantment keys and enchantLevel values
     */
    Map<MCEnchantment, Integer> getEnchants();
    
    /**
     * Adds a given enchantment to this meta
     * 
     * @param ench The type of enchantment to add
     * @param level The level of enchantment
     * @param ignoreLevelRestriction Should adding an outrageous level be allowed?
     * @return whether the enchantment was added successfully
     */
    boolean addEnchant(MCEnchantment ench, int level, boolean ignoreLevelRestriction);
    
    /**
     * Removes a given enchantment from this meta
     * 
     * @param ench The type of enchantment to remove
     * @return whether the enchantment was removed successfully
     */
    boolean removeEnchant(MCEnchantment ench);
    
    boolean hasRepairCost();
    
    int getRepairCost();
    
    void setRepairCost(int cost);

    /**
     * Set itemflags which should be ignored when rendering a ItemStack in the Client. This Method does silently ignore double set itemFlags.
     *
     * @param itemFlags The hideflags which shouldn't be rendered
     */
    void addItemFlags(MCItemFlag... itemFlags);

    /**
     * Gets the itemflags that are set
     *
     * @return a list of itemflags that are set
     */
    List<MCItemFlag> getItemFlags();

    /**
     * Check if the specified flag is present on this item.
     *
     * @param itemFlag The flag to check
     * @return if it is present
     */
    boolean hasItemFlag(MCItemFlag itemFlag);

    /**
     * Remove specific set of itemFlags. This tells the Client it should render it again. This Method does silently ignore double removed itemFlags.
     *
     * @param itemFlags Hideflags which should be removed
     */
    void removeItemFlags(MCItemFlag... itemFlags);
}
