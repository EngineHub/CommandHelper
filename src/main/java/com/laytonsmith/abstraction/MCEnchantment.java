/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.abstraction;

/**
 *
 * @author layton
 */
public interface MCEnchantment {
    public boolean canEnchantItem(MCItemStack is);
    public int getMaxLevel();
    public String getName();
}
