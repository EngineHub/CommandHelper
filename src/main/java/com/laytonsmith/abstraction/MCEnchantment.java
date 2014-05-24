
package com.laytonsmith.abstraction;

/**
 *
 * 
 */
public interface MCEnchantment extends AbstractionObject{
    public boolean canEnchantItem(MCItemStack is);
    public int getMaxLevel();
    public String getName();
}
