/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.abstraction;

/**
 *
 * @author layton
 */
public interface MCInventory extends AbstractionObject{
    public MCItemStack getBoots();
    public MCItemStack getChestplate();
    public MCItemStack getHelmet();
    public MCItemStack getItem(int slot);
    public MCItemStack getLeggings();
    public void setBoots(MCItemStack stack);
    public void setChestplate(MCItemStack stack);
    public void setHelmet(MCItemStack stack);
    public void setItem(int slot, MCItemStack stack);
    public void setLeggings(MCItemStack stack);
    
}
