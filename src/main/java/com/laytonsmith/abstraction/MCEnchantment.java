
package com.laytonsmith.abstraction;

/**
 *
 * @author layton
 */
public interface MCEnchantment extends AbstractionObject{
    public boolean canEnchantItem(MCItemStack is);
    public int getMaxLevel();
    public String getName();
}
