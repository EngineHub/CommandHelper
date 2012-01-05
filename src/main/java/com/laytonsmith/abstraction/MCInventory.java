/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.abstraction;

/**
 *
 * @author layton
 */
public interface MCInventory {
    public MCItemStack getItem(int slot);
    public void setItem(int slot, MCItemStack stack);
    public void setHelmet(MCItemStack stack);
    public void setChestplate(MCItemStack stack);
    public void setLeggings(MCItemStack stack);
    public void setBoots(MCItemStack stack);
    public MCItemStack getHelmet();
    public MCItemStack getChestplate();
    public MCItemStack getLeggings();
    public MCItemStack getBoots();
    
}
