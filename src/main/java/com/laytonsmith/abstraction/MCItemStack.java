/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.abstraction;

import com.laytonsmith.abstraction.blocks.MCMaterial;
import java.util.Map;

/**
 *
 * @author layton
 */
public interface MCItemStack extends AbstractionObject{
    public void addEnchantment(MCEnchantment e, int level);
    public void addUnsafeEnchantment(MCEnchantment e, int level);
    public int getAmount();
    public MCMaterialData getData();
    public short getDurability();
    public Map<MCEnchantment, Integer> getEnchantments();
    public MCMaterial getType();
    public int getTypeId();
    public int maxStackSize();
    public void removeEnchantment(MCEnchantment e);
    
    public void setData(int data);
    
    public void setDurability(short data);

    public void setTypeId(int type);
}
